/**
 * Tajly Cloud Functions.
 *
 *  1) tajlyAutoPost — community feed: posts as "TAJLY" 3x/day into fitness_testing_posts.
 *  2) Retention push — THREE distinct daily push notifications (morning / midday / evening),
 *     each a different theme, personalized from the user's real stats via Gemini, delivered
 *     over FCM (→ APNs on iOS once configured).
 *
 * Deploy:  firebase deploy --only functions --force
 */
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

const POSTS = "fitness_testing_posts";
const USERS = "fitness_testing_users";
const TZ = "Europe/Amsterdam";
const TAJLY_ID = "tajly@tajly.app";
const TAJLY_NAME = "TAJLY";
const MODEL = "gemini-2.5-flash";

// ────────────────────────────────────────────────────────────────────────────
// Gemini
// ────────────────────────────────────────────────────────────────────────────
// Key source: functions env (GEMINI_API_KEY in functions/.env) first, then the
// Firestore config/app.geminiApiKey doc. If neither is present, callers fall back
// to their hand-written lines so notifications still go out.
async function getGeminiKey() {
  if (process.env.GEMINI_API_KEY) return process.env.GEMINI_API_KEY;
  try {
    const doc = await db.doc("config/app").get();
    const k = doc.exists ? doc.get("geminiApiKey") : null;
    if (k) return k;
  } catch (_) {}
  return null;
}

async function geminiText(prompt, maxChars) {
  const key = await getGeminiKey();
  if (!key) return null;
  try {
    const res = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + key,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] }),
      }
    );
    const json = await res.json();
    const text = json?.candidates?.[0]?.content?.parts?.[0]?.text?.trim();
    if (!text) return null;
    return text.replace(/^["']|["']$/g, "").slice(0, maxChars);
  } catch (_) {
    return null;
  }
}

// ────────────────────────────────────────────────────────────────────────────
// 1) Community auto-poster
// ────────────────────────────────────────────────────────────────────────────
const THEMES = [
  "a short, energizing morning nudge to move today",
  "a midday reminder to take a movement break, stretch and hydrate",
  "an evening note celebrating small fitness wins and rest",
];
const POST_FALLBACK =
  "Small moves add up. Do one thing for your body today — a walk, a stretch, five squats. Future you says thanks. 💪";

async function postAsTajly(theme) {
  await db.collection(USERS).doc(TAJLY_ID).set(
    {
      email: TAJLY_ID,
      displayName: TAJLY_NAME,
      username: "@tajly",
      bio: "Your fitness home. Move daily, celebrate every win.",
      streakDays: 0,
      level: 1,
      createdAt: Date.now(),
    },
    { merge: true }
  );
  const prompt =
    "You are TAJLY, a warm, upbeat fitness community app. Write ONE short community post " +
    "(max 240 characters, 1–2 sentences, friendly and encouraging, at most one emoji, no hashtags, " +
    "no surrounding quotation marks) about: " + theme + ".";
  const content = (await geminiText(prompt, 280)) || POST_FALLBACK;
  await db.collection(POSTS).add({
    userId: TAJLY_ID,
    userName: TAJLY_NAME,
    content,
    timestamp: Date.now(),
    likes: 0,
    comments: 0,
    isPersonalBest: false,
  });
}

exports.tajlyAutoPost = onSchedule(
  { schedule: "0 9,14,20 * * *", timeZone: TZ, region: "europe-west1" },
  async () => {
    const h = parseInt(
      new Date().toLocaleString("en-US", { timeZone: TZ, hour: "2-digit", hour12: false }),
      10
    );
    const theme = h < 12 ? THEMES[0] : h < 18 ? THEMES[1] : THEMES[2];
    await postAsTajly(theme);
  }
);

// ────────────────────────────────────────────────────────────────────────────
// 2) Retention push — three daily slots, each a STATE-AWARE, emotionally-tuned message
// ────────────────────────────────────────────────────────────────────────────
// Not a fixed template. For each user at each slot we read their REAL situation
// (streak record within reach, level-up close, a round-number workout milestone,
// weight progress, at-risk streak, comeback after a lapse, first-workout) and pick
// the single most resonant "moment". The slot sets the domain (train / fuel / reflect)
// and rotates the voice daily so the same person never hears the same tune twice.
const CHANNEL_ID = "tajly_reminders"; // must match the channel the Android app creates
const DAY = 86400000;
const XP_PER_LEVEL = 500;
const WORKOUT_XP = 50;
const WORKOUT_MILESTONES = [1, 5, 10, 25, 50, 75, 100, 150, 200, 250, 365, 500, 750, 1000];

// yyyy-mm-dd for a given epoch-ms in the app's timezone (used for "today" checks + dedupe).
function localDate(ms) {
  return new Date(ms).toLocaleDateString("en-CA", { timeZone: TZ });
}

// Five distinct emotional registers. Rotated by day so messages stay fresh over time.
const VOICES = [
  "like a close friend texting them — casual, real, no corporate cheer",
  "warm and empathetic, meeting them where they are",
  "identity-affirming — remind them they're the kind of person who shows up",
  "lightly curious and teasing, sparking a bit of intrigue",
  "genuinely, specifically celebratory about their progress",
];

// Turn the raw Firestore doc into a rich, derived profile for message crafting.
function profileOf(d, now, today) {
  const lastWorkout = Number(d.lastWorkoutMillis || 0);
  const streak = Number(d.streakDays || 0);
  const longest = Number(d.longestStreak || streak || 0);
  const totalWorkouts = Number(d.totalWorkouts || 0);
  const level = Number(d.level || 1);
  const xp = Number(d.totalXp || 0);
  const goalRaw = (d.goal && String(d.goal)) || "";
  const gainGoal = /MUSCLE|GAIN|STRENGTH/i.test(goalRaw);
  const start = Number(d.weightStartKg || 0);
  const cur = Number(d.weightCurrentKg || 0);
  const rawDelta = start && cur ? start - cur : 0; // +ve = lost weight
  const progressKg = start && cur ? (gainGoal ? cur - start : start - cur) : 0; // +ve = toward goal
  const workedOutToday = lastWorkout > 0 && localDate(lastWorkout) === today;
  const daysSince = lastWorkout > 0 ? Math.floor((now - lastWorkout) / DAY) : null;
  const xpToNext = XP_PER_LEVEL - (xp % XP_PER_LEVEL);
  const nextMilestone = WORKOUT_MILESTONES.find((m) => m > totalWorkouts) || null;
  return {
    name: (d.displayName && String(d.displayName).trim()) || "there",
    goal: goalToWords(goalRaw),
    streak, longest, totalWorkouts, level, xpToNext, nextMilestone,
    workedOutToday, daysSince, progressKg: Math.round(progressKg), gainGoal,
  };
}

function goalToWords(g) {
  const m = {
    WEIGHT_LOSS: "losing weight", MUSCLE_GAIN: "building muscle",
    ENDURANCE: "building endurance", FLEXIBILITY: "moving more freely",
    GENERAL_FITNESS: "getting fitter",
  };
  return m[g] || "getting fitter";
}

// Pick the single most resonant moment for this user + slot. Order = priority.
// Returns { id, brief (emotional instruction for the LLM), title, fallback (hand-written body) }.
function pickMoment(u, slot) {
  const n = u.name;
  const streakAtRecord = u.streak > 0 && u.longest > 0 && u.streak >= u.longest;
  const oneFromRecord = u.streak > 0 && u.longest > 1 && u.streak === u.longest - 1;
  const oneSessionToLevel = u.xpToNext <= WORKOUT_XP;
  const atMilestoneDoor = u.nextMilestone && u.totalWorkouts === u.nextMilestone - 1 && u.totalWorkouts > 0;
  const hasProgress = u.progressKg >= 1;

  // Morning & evening are moment-rich; midday stays in the fuel/recovery lane but adapts tone.
  if (slot !== "midday") {
    if (!u.workedOutToday && oneFromRecord)
      return { id: "record_reach",
        brief: `They are ONE workout away from beating their best-ever streak of ${u.longest} days (currently ${u.streak}). Make them feel how close the record is and that today is the day.`,
        title: "Your record's right there",
        fallback: `${n}, one session today ties your best streak ever — ${u.longest} days. Go make it ${u.longest} 🔥` };
    if (!u.workedOutToday && streakAtRecord && u.streak >= 3)
      return { id: "record_extend",
        brief: `They are AT their all-time best streak (${u.streak} days). Every workout now is a new personal record. Make it feel historic but light.`,
        title: `Day ${u.streak + 1}?`,
        fallback: `${n}, every workout now is a new personal best — you're on your longest streak ever. Day ${u.streak + 1}? 🔥` };
    if (!u.workedOutToday && oneSessionToLevel && u.totalWorkouts > 0)
      return { id: "level_reach",
        brief: `One workout (about ${WORKOUT_XP} XP) tips them into Level ${u.level + 1}. Make the level-up feel earned and within arm's reach.`,
        title: `Level ${u.level + 1} is one session away`,
        fallback: `${n}, one workout and Level ${u.level + 1} is yours. It's literally one session away ✨` };
    if (atMilestoneDoor)
      return { id: "milestone",
        brief: `Their next workout is their ${u.nextMilestone}th ever. Mark the milestone — that number is a big deal.`,
        title: `Your ${u.nextMilestone}th workout`,
        fallback: `${n}, your next session is workout number ${u.nextMilestone}. That's a milestone — let's log it 💪` };
    if (u.daysSince !== null && u.daysSince >= 3)
      return { id: "comeback",
        brief: `It's been ${u.daysSince} days since they trained. NO guilt, NO lecture. Be the friend who's just glad they're back. Offer one tiny 5-10 minute way in.`,
        title: "No lecture, promise",
        fallback: `${u.daysSince} days quiet, ${n} — no lecture. Just 10 minutes and you're back. That's all it takes.` };
    if (u.daysSince !== null && u.daysSince >= 1 && u.streak === 0 && u.longest >= 3)
      return { id: "rebuild",
        brief: `Their streak broke (best was ${u.longest} days). Reframe it: streaks are meant to be rebuilt, and they've done it before. Encouraging, not sad.`,
        title: "Round two",
        fallback: `${n}, streaks break — you've built one before (${u.longest} days!). Day 1 of the next one starts now 💪` };
  }

  if (slot === "evening" && u.workedOutToday)
    return { id: "celebrate",
      brief: `They already trained today. Celebrate it specifically and warmly${u.streak >= 2 ? `, and honor their ${u.streak}-day streak` : ""}. Make them feel seen, then let them rest.`,
      title: u.streak >= 2 ? `Day ${u.streak} done 🔥` : "Done today ✅",
      fallback: u.streak >= 2
        ? `That's day ${u.streak}, ${n}. You showed up again today — proud of you. Rest well 🔥`
        : `Done and dusted, ${n}. You showed up today — that's the whole game. Rest up ✅` };

  if (slot === "midday") {
    if (hasProgress)
      return { id: "fuel_progress",
        brief: `They've made real body-composition progress (${u.progressKg}kg toward ${u.goal}). Tie a light, well-timed lunch/hydration nudge to protecting that progress. Not preachy.`,
        title: "Fuel the progress",
        fallback: `${u.progressKg}kg of progress didn't happen by accident, ${n}. Keep lunch light and hydrate 🥗` };
    return { id: "fuel",
      brief: `A midday reset: fuel well, hydrate, and take a 2-minute movement break. Make it feel caring and doable, not like a chore. Do NOT talk about hard training.`,
      title: "Midday reset",
      fallback: `${n}, midday reset: drink some water, eat something real, and stretch for two minutes 🥗` };
  }

  // Streak at risk (evening default when not trained) or general steady nudge.
  if (u.streak > 0 && !u.workedOutToday)
    return { id: "protect_streak",
      brief: `Their ${u.streak}-day streak is alive but today isn't logged yet. Gently protect it — one small action keeps it going. Warm, low-pressure.`,
      title: `${u.streak}-day streak`,
      fallback: `${n}, your ${u.streak}-day streak is still alive tonight — a short session keeps it going 🔥` };
  if (u.totalWorkouts === 0)
    return { id: "first_win",
      brief: `They haven't logged a single workout yet. Make the FIRST one feel tiny and safe — the hardest one to start, the best one to finish.`,
      title: "The first one",
      fallback: `${n}, the first workout is the hard one — so make it tiny. Five minutes counts. Start there 💪` };
  return { id: "steady",
    brief: `A warm, ${slot === "morning" ? "morning" : "day"} nudge toward their goal of ${u.goal}. Personal and specific, never generic.`,
    title: "Today counts",
    fallback: `${n}, a little today goes a long way toward ${u.goal}. One small move — that's the win 💪` };
}

const SLOT_DOMAIN = {
  morning: "It's morning — orient it toward training/moving today.",
  midday: "It's midday — keep it about fueling, hydration and a movement break.",
  evening: "It's evening — reflective, about closing the day and their streak.",
};
const SLOT_ROUTE = { morning: "workout", midday: "meals", evening: "community" };

async function craftBody(u, slot, moment, voice) {
  const prompt =
    "You write push notifications for Tajly, a fitness app people actually love. " +
    "Write ONE notification body. Voice: " + voice + ". " + SLOT_DOMAIN[slot] + "\n\n" +
    "The specific moment to convey: " + moment.brief + "\n\n" +
    "Hard rules:\n" +
    "- Max 90 characters. One sentence, maybe two very short.\n" +
    "- Use their first name naturally: " + u.name + ".\n" +
    "- Be concrete and specific to THEM — reference the real detail in the moment.\n" +
    "- At most ONE emoji, and only if it earns its place. No hashtags. No quotation marks.\n" +
    "- Sound like a human who cares, not a brand. NEVER use clichés: 'crush it', 'beast mode', " +
    "'no pain no gain', 'rise and grind', 'you got this', 'let's go', 'smash', 'grind', 'get after it', " +
    "or a generic 'Ready to...'.\n\n" +
    "Quality bar (examples of the tone, do not copy):\n" +
    "- \"One session today ties your best streak ever, Sam. Day 13 is right there.\"\n" +
    "- \"3 days quiet, Mara — no lecture. Ten minutes and you're back.\"\n" +
    "- \"4kg down since you started, Leo. Keep lunch light today.\"\n\n" +
    "Write only the notification text, nothing else.";
  return (await geminiText(prompt, 150)) || moment.fallback;
}

async function runSlot(slot) {
  const snap = await db.collection(USERS).get();
  const now = Date.now();
  const today = localDate(now);
  // Rotate voice by day-of-year + slot so the same user hears a different register each day.
  const dayOfYear = Math.floor((now - Date.UTC(new Date(now).getUTCFullYear(), 0, 0)) / DAY);
  const slotIdx = ["morning", "midday", "evening"].indexOf(slot);
  const voice = VOICES[(dayOfYear + slotIdx) % VOICES.length];

  const jobs = [];
  snap.forEach((doc) => {
    const d = doc.data() || {};
    const token = d.fcmToken;
    if (!token) return;
    if (d[`lastPush_${slot}`] === today) return; // one per slot per day (retry/redeploy safe)

    const u = profileOf(d, now, today);
    const moment = pickMoment(u, slot);

    jobs.push((async () => {
      const body = await craftBody(u, slot, moment, voice);
      try {
        await getMessaging().send({
          token,
          notification: { title: moment.title, body },
          data: { route: SLOT_ROUTE[slot], slot, moment: moment.id },
          android: { priority: "high", notification: { channelId: CHANNEL_ID } },
          apns: { payload: { aps: { sound: "default" } } },
        });
        await doc.ref.set({ [`lastPush_${slot}`]: today, [`lastMoment_${slot}`]: moment.id }, { merge: true });
      } catch (e) {
        const code = String(e?.errorInfo?.code || e?.code || "");
        if (code.includes("registration-token-not-registered")) {
          try { await doc.ref.update({ fcmToken: null }); } catch (_) {}
        }
      }
    })());
  });
  await Promise.all(jobs);
}

// One-shot HTTP endpoint to prove push delivery end-to-end: sends a test notification to
// every user that has an fcmToken. Publicly invokable (Firebase onRequest default). Safe to
// keep or delete later.
exports.sendTestPush = onRequest({ region: "europe-west1" }, async (req, res) => {
  const snap = await db.collection(USERS).get();
  let sent = 0, failed = 0;
  const errors = [];
  const jobs = [];
  snap.forEach((doc) => {
    const token = (doc.data() || {}).fcmToken;
    if (!token) return;
    jobs.push(
      getMessaging().send({
        token,
        notification: { title: "Tajly 🔔", body: "Your notifications are live — tap to open Tajly." },
        data: { route: "home" },
        android: { priority: "high", notification: { channelId: CHANNEL_ID } },
        apns: { payload: { aps: { sound: "default" } } },
      }).then(() => { sent++; })
        .catch((e) => { failed++; errors.push(String(e?.errorInfo?.code || e?.message || e)); })
    );
  });
  await Promise.all(jobs);
  res.json({ sent, failed, errors });
});

// Firestore doc id from an email — must match the app's toFirestoreDocId (@→_AT_, .→_DOT_).
function emailToDocId(email) {
  return String(email).replace(/@/g, "_AT_").replace(/\./g, "_DOT_");
}

// PUSH notifications for social interactions: when a community activity doc is created
// (someone liked/commented/followed), push it to the recipient's device (if they have a token).
exports.onCommunityActivity = onDocumentCreated(
  { document: "fitness_testing_activity/{id}", region: "europe-west1" },
  async (event) => {
    const d = event.data && event.data.data();
    if (!d) return;
    const targetEmail = d.targetUserId;
    const actor = (d.userName && String(d.userName).trim()) || "Someone";
    const type = String(d.type || "").toUpperCase();
    if (!targetEmail) return;

    const userRef = db.collection(USERS).doc(emailToDocId(targetEmail));
    const snap = await userRef.get();
    const token = snap.exists ? snap.get("fcmToken") : null;
    if (!token) return;

    let body;
    if (type === "LIKE") body = `${actor} liked your post ❤️`;
    else if (type === "COMMENT") body = `${actor} commented: ${String(d.targetContent || "").slice(0, 80)}`;
    else if (type === "FOLLOW") body = `${actor} started following you`;
    else body = `${actor} interacted with your post`;

    try {
      await getMessaging().send({
        token,
        notification: { title: "Tajly", body },
        data: { route: "community" },
        android: { priority: "high", notification: { channelId: CHANNEL_ID } },
        apns: { payload: { aps: { sound: "default" } } },
      });
    } catch (e) {
      if (String(e && e.errorInfo && e.errorInfo.code || "").includes("registration-token-not-registered")) {
        try { await userRef.update({ fcmToken: null }); } catch (_) {}
      }
    }
  }
);

exports.tajlyPushMorning = onSchedule(
  { schedule: "0 8 * * *", timeZone: TZ, region: "europe-west1" },
  () => runSlot("morning")
);
exports.tajlyPushMidday = onSchedule(
  { schedule: "0 13 * * *", timeZone: TZ, region: "europe-west1" },
  () => runSlot("midday")
);
exports.tajlyPushEvening = onSchedule(
  { schedule: "30 19 * * *", timeZone: TZ, region: "europe-west1" },
  () => runSlot("evening")
);

// ─────────────────────────────────────────────────────────────────────────────
// RevenueCat webhook — server-authoritative subscription mirror into Firestore.
// Configure in RevenueCat dashboard → Integrations → Webhooks:
//   URL:    the revenueCatWebhook URL printed after deploy
//   Header: Authorization: Bearer <REVENUECAT_WEBHOOK_SECRET>   (same value as functions/.env)
// Writes to fitness_testing_subscriptions/{emailDocId} with merge — NEVER touches premiumOverride.
// ─────────────────────────────────────────────────────────────────────────────
const SUBSCRIPTIONS = "fitness_testing_subscriptions";

function mapStore(store) {
  const s = String(store || "").toUpperCase();
  if (s === "APP_STORE") return "App Store";
  if (s === "PLAY_STORE") return "Play Store";
  if (s === "STRIPE") return "Stripe";
  return store || "RevenueCat";
}

exports.revenueCatWebhook = onRequest(
  { region: "europe-west1", cors: false },
  async (req, res) => {
    // Verify the shared secret so subscription state can't be spoofed.
    const secret = process.env.REVENUECAT_WEBHOOK_SECRET;
    const auth = req.get("Authorization") || "";
    if (!secret || auth !== `Bearer ${secret}`) {
      res.status(401).send("unauthorized");
      return;
    }

    try {
      const event = (req.body && req.body.event) || {};
      const type = String(event.type || "").toUpperCase();
      if (type === "TEST") {
        res.status(200).send("ok (test)");
        return;
      }

      // We key billing docs by email; anonymous RC ids can't be mapped — ack and skip.
      const appUserId = event.app_user_id || event.original_app_user_id;
      if (!appUserId || !String(appUserId).includes("@")) {
        res.status(200).send("ok (no email)");
        return;
      }

      const expMs = Number(event.expiration_at_ms || 0);
      const eventMs = Number(event.event_timestamp_ms || event.purchased_at_ms || 0);
      const isExpiringType = type === "EXPIRATION" || type === "SUBSCRIPTION_PAUSED";
      // Active when it isn't an expiring event and the expiration is still in the future
      //    (or there's no expiration, e.g. a non-renewing/lifetime purchase).
      const isActive = !isExpiringType && (expMs === 0 || eventMs === 0 || expMs > eventMs);

      const doc = {
        status: isActive ? "active" : "expired",
        plan: event.product_id || null,
        store: mapStore(event.store),
        willRenew: type !== "CANCELLATION" && type !== "EXPIRATION",
        isTrial: String(event.period_type || "").toUpperCase() === "TRIAL",
        expiry: expMs > 0 ? new Date(expMs).toISOString() : null,
        source: "webhook",
        updatedAt: eventMs || expMs || 0,
      };

      await db.collection(SUBSCRIPTIONS).doc(emailToDocId(appUserId)).set(doc, { merge: true });
      res.status(200).send("ok");
    } catch (e) {
      console.error("revenueCatWebhook error:", e && e.message);
      res.status(500).send("error");
    }
  }
);
