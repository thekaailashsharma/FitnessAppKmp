import * as admin from "firebase-admin";
import {onSchedule} from "firebase-functions/v2/scheduler";
import {onDocumentWritten, onDocumentCreated} from "firebase-functions/v2/firestore";
import {sendPush} from "./fcm";
import {generateMessage} from "./gemini";
import {getLocalHour, getLocalDayOfWeek, jsToAppDay} from "./timezone";

const GMAIL_USER = "info.fitnessbysivv@gmail.com";
const GMAIL_APP_PASSWORD = "dikc yrvr vufl zzjy";

admin.initializeApp();
const db = admin.firestore();

// ─── Types ───────────────────────────────────────────────────────

interface ClientDoc {
  firstName?: string;
  lastName?: string;
  email?: string;
  goal?: string;
  fcmToken?: string;
  timezone?: string;
  lastActiveAt?: string;
  language?: string;
  status?: string;
}

interface MealPlanDoc {
  clientEmail?: string;
  mealsData?: string;
  isActive?: boolean;
}

interface MealFromJson {
  name?: string;
  mealSlot?: string;
  dayOfWeek?: number;
  calories?: number;
}

// ─── Helpers ─────────────────────────────────────────────────────

async function getClientsInTimeWindow(targetHour: number): Promise<
  Array<{id: string; data: ClientDoc}>
> {
  const snapshot = await db.collection("clients").get();
  const results: Array<{id: string; data: ClientDoc}> = [];

  for (const doc of snapshot.docs) {
    const data = doc.data() as ClientDoc;
    if (!data.fcmToken || !data.timezone) continue;
    if (data.status && data.status !== "Active") continue;

    const localHour = getLocalHour(data.timezone);
    if (localHour === targetHour) {
      results.push({id: doc.id, data});
    }
  }
  return results;
}

function parseMealsJson(json: string | undefined): MealFromJson[] {
  if (!json) return [];
  try {
    return JSON.parse(json) as MealFromJson[];
  } catch {
    return [];
  }
}

function t(lang: string | undefined, en: string, nl: string): string {
  return lang === "nl" ? nl : en;
}

// ─── 1. Morning Meal Reminder (target: 7 AM local) ──────────────

export const morningMealReminder = onSchedule(
  {schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const clients = await getClientsInTimeWindow(7);
    if (clients.length === 0) return;

    for (const {data} of clients) {
      const email = data.email;
      if (!email || !data.fcmToken) continue;

      const plansSnap = await db.collection("user_meal_plans")
        .where("clientEmail", "==", email)
        .where("isActive", "==", true)
        .limit(1)
        .get();

      if (plansSnap.empty) continue;

      const planDoc = plansSnap.docs[0].data() as MealPlanDoc;
      const meals = parseMealsJson(planDoc.mealsData);
      const dayOfWeek = jsToAppDay(
        getLocalDayOfWeek(data.timezone || "UTC"),
      );

      const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);
      const breakfast = todayMeals.find(
        (m) => m.mealSlot === "BREAKFAST",
      );
      const totalCal = todayMeals.reduce(
        (sum, m) => sum + (m.calories || 0), 0,
      );

      const title = t(
        data.language,
        `Good morning ${data.firstName || ""}! 🌅`,
        `Goedemorgen ${data.firstName || ""}! 🌅`,
      );

      let body: string;
      if (breakfast) {
        body = t(
          data.language,
          `Today's breakfast: ${breakfast.name} (${breakfast.calories} kcal). ` +
          `${todayMeals.length} meals planned — ${totalCal} kcal total.`,
          `Ontbijt vandaag: ${breakfast.name} (${breakfast.calories} kcal). ` +
          `${todayMeals.length} maaltijden gepland — ${totalCal} kcal totaal.`,
        );
      } else {
        body = t(
          data.language,
          `You have ${todayMeals.length} meals planned today — ${totalCal} kcal total.`,
          `Je hebt ${todayMeals.length} maaltijden gepland vandaag — ${totalCal} kcal totaal.`,
        );
      }

      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 2. Workout Reminder (target: 9 AM local) ───────────────────

export const workoutReminder = onSchedule(
  {schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const clients = await getClientsInTimeWindow(9);
    if (clients.length === 0) return;

    for (const {data} of clients) {
      const email = data.email;
      if (!email || !data.fcmToken) continue;

      const plansSnap = await db.collection("workout_plans")
        .where("clientEmail", "==", email)
        .limit(1)
        .get();

      if (plansSnap.empty) continue;
      const planId = plansSnap.docs[0].id;

      const dayOfWeek = jsToAppDay(
        getLocalDayOfWeek(data.timezone || "UTC"),
      );

      const exercisesSnap = await db.collection("exercises")
        .where("planId", "==", planId)
        .where("dayOfWeek", "==", dayOfWeek)
        .get();

      if (exercisesSnap.empty) continue;

      const exerciseNames = exercisesSnap.docs
        .map((d) => d.data().name as string || "Exercise")
        .slice(0, 3);

      const count = exercisesSnap.size;
      const preview = exerciseNames.join(", ");

      const title = t(
        data.language,
        "Workout time! 💪",
        "Tijd om te trainen! 💪",
      );
      const body = t(
        data.language,
        `${count} exercises today including ${preview}. Let's go!`,
        `${count} oefeningen vandaag waaronder ${preview}. Laten we gaan!`,
      );

      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 3. Evening Reflection (target: 20:00 / 8 PM local) ─────────

export const eveningReflection = onSchedule(
  {schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const clients = await getClientsInTimeWindow(20);
    if (clients.length === 0) return;

    for (const {data} of clients) {
      const email = data.email;
      if (!email || !data.fcmToken) continue;

      const plansSnap = await db.collection("user_meal_plans")
        .where("clientEmail", "==", email)
        .where("isActive", "==", true)
        .limit(1)
        .get();

      if (plansSnap.empty) continue;

      const planDoc = plansSnap.docs[0].data() as MealPlanDoc;
      const meals = parseMealsJson(planDoc.mealsData);
      const dayOfWeek = jsToAppDay(
        getLocalDayOfWeek(data.timezone || "UTC"),
      );
      const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);

      if (todayMeals.length === 0) continue;

      const dinner = todayMeals.find((m) => m.mealSlot === "DINNER");

      const title = t(
        data.language,
        `Evening check-in 🌙`,
        `Avond check-in 🌙`,
      );

      let body: string;
      if (dinner) {
        body = t(
          data.language,
          `Don't forget dinner — ${dinner.name} (${dinner.calories} kcal). ` +
          `Mark your meals as completed!`,
          `Vergeet het avondeten niet — ${dinner.name} (${dinner.calories} kcal). ` +
          `Markeer je maaltijden als voltooid!`,
        );
      } else {
        body = t(
          data.language,
          `How was your nutrition today? Mark your meals as completed!`,
          `Hoe was je voeding vandaag? Markeer je maaltijden als voltooid!`,
        );
      }

      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 4. Weigh-in Reminder (target: 8 AM on Mondays) ─────────────

export const weighInReminder = onSchedule(
  {schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const clients = await getClientsInTimeWindow(8);
    if (clients.length === 0) return;

    for (const {data} of clients) {
      if (!data.fcmToken || !data.timezone) continue;

      const jsDay = getLocalDayOfWeek(data.timezone);
      if (jsDay !== 1) continue; // Monday only

      const title = t(
        data.language,
        "Weigh-in day! ⚖️",
        "Weegdag! ⚖️",
      );
      const body = t(
        data.language,
        `${data.firstName || "Hey"}, track your weight and see your progress this week.`,
        `${data.firstName || "Hé"}, weeg jezelf en bekijk je voortgang deze week.`,
      );

      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 5. Inactivity Nudge (runs daily at 12:00 UTC, Gemini-powered) ─

export const inactivityNudge = onSchedule(
  {schedule: "every day 12:00", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const snapshot = await db.collection("clients").get();
    const now = Date.now();
    const THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000;

    for (const doc of snapshot.docs) {
      const data = doc.data() as ClientDoc;
      if (!data.fcmToken || !data.lastActiveAt) continue;
      if (data.status && data.status !== "Active") continue;

      const lastActive = new Date(data.lastActiveAt).getTime();
      if (isNaN(lastActive)) continue;

      const daysInactive = Math.floor((now - lastActive) / (24 * 60 * 60 * 1000));
      if (now - lastActive < THREE_DAYS_MS) continue;

      const lang = data.language === "nl" ? "Dutch" : "English";
      const prompt =
        `Generate a short, warm push notification message (max 100 characters) in ${lang} ` +
        `for ${data.firstName || "a user"} who wants "${data.goal || "to stay fit"}" ` +
        `and hasn't used the fitness app in ${daysInactive} days. ` +
        `Be encouraging, not pushy. No quotes around the message.`;

      let body = await generateMessage(prompt);
      if (!body) {
        body = t(
          data.language,
          `${data.firstName || "Hey"}, we miss you! Your fitness plan is waiting.`,
          `${data.firstName || "Hé"}, we missen je! Je fitnessplan wacht op je.`,
        );
      }

      const title = t(data.language, "We miss you! 👋", "We missen je! 👋");
      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 6. AI Weekly Motivation (Sundays at 18:00 local, Gemini) ───

export const aiWeeklyMotivation = onSchedule(
  {schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const clients = await getClientsInTimeWindow(18);
    if (clients.length === 0) return;

    for (const {data} of clients) {
      if (!data.fcmToken || !data.timezone) continue;

      const jsDay = getLocalDayOfWeek(data.timezone);
      if (jsDay !== 0) continue; // Sunday only

      const lang = data.language === "nl" ? "Dutch" : "English";
      const prompt =
        `Generate a warm, personalized weekly motivational push notification ` +
        `(max 120 characters) in ${lang} for ${data.firstName || "a fitness enthusiast"} ` +
        `whose goal is "${data.goal || "general fitness"}". ` +
        `Reference the new week ahead. Be inspiring. No quotes.`;

      let body = await generateMessage(prompt);
      if (!body) {
        body = t(
          data.language,
          `New week ahead, ${data.firstName || "champ"}! Stay consistent and crush your goals. 🔥`,
          `Nieuwe week, ${data.firstName || "kampioen"}! Blijf consistent en bereik je doelen. 🔥`,
        );
      }

      const title = t(
        data.language,
        "New week, new energy! 🚀",
        "Nieuwe week, nieuwe energie! 🚀",
      );
      await sendPush(data.fcmToken, title, body);
    }
  },
);

// ─── 7. New Meal Plan Congratulation (Firestore trigger) ─────────

export const onMealPlanCreated = onDocumentWritten(
  {document: "user_meal_plans/{planId}", region: "europe-west1"},
  async (event) => {
    const after = event.data?.after?.data() as MealPlanDoc | undefined;
    const before = event.data?.before?.data() as MealPlanDoc | undefined;

    // Only fire on creation (before doesn't exist) or activation
    if (before && before.isActive === after?.isActive) return;
    if (!after?.isActive || !after.clientEmail) return;

    const meals = parseMealsJson(after.mealsData);
    const totalMeals = meals.length;
    const days = new Set(meals.map((m) => m.dayOfWeek)).size;

    // Find the client by email
    const clientSnap = await db.collection("clients")
      .where("email", "==", after.clientEmail)
      .limit(1)
      .get();

    if (clientSnap.empty) return;
    const client = clientSnap.docs[0].data() as ClientDoc;
    if (!client.fcmToken) return;

    const title = t(
      client.language,
      "Meal plan ready! 🎉",
      "Maaltijdplan klaar! 🎉",
    );
    const body = t(
      client.language,
      `Your ${days}-day plan with ${totalMeals} meals is set. Check out today's menu!`,
      `Je ${days}-dagen plan met ${totalMeals} maaltijden staat klaar. Bekijk het menu!`,
    );

    await sendPush(client.fcmToken, title, body);
  },
);

// ─── 8. Test Notification (bypasses all time/day checks) ─────────

export const testNotification = onSchedule(
  {schedule: "every 24 hours", timeZone: "UTC", region: "europe-west1"},
  async () => {
    const snapshot = await db.collection("clients").get();

    for (const doc of snapshot.docs) {
      const data = doc.data() as ClientDoc;
      if (!data.fcmToken) continue;
      if (data.status && data.status !== "Active") continue;

      const email = data.email;
      if (!email) continue;

      let body = `Hi ${data.firstName || "there"}! This is a test notification.`;

      // Try to include real meal data
      const plansSnap = await db.collection("user_meal_plans")
        .where("clientEmail", "==", email)
        .where("isActive", "==", true)
        .limit(1)
        .get();

      if (!plansSnap.empty) {
        const planDoc = plansSnap.docs[0].data() as MealPlanDoc;
        const meals = parseMealsJson(planDoc.mealsData);
        const dayOfWeek = jsToAppDay(
          getLocalDayOfWeek(data.timezone || "UTC"),
        );
        const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);
        const totalCal = todayMeals.reduce(
          (sum, m) => sum + (m.calories || 0), 0,
        );
        if (todayMeals.length > 0) {
          body = t(
            data.language,
            `${data.firstName}, you have ${todayMeals.length} meals today (${totalCal} kcal). ` +
            `Timezone: ${data.timezone || "not set"}. All systems working!`,
            `${data.firstName}, je hebt ${todayMeals.length} maaltijden vandaag (${totalCal} kcal). ` +
            `Tijdzone: ${data.timezone || "niet ingesteld"}. Alles werkt!`,
          );
        }
      }

      // Try to include workout data
      const workoutSnap = await db.collection("workout_plans")
        .where("clientEmail", "==", email)
        .limit(1)
        .get();

      if (!workoutSnap.empty && plansSnap.empty) {
        body = t(
          data.language,
          `${data.firstName}, your workout plan is ready. Timezone: ${data.timezone || "not set"}. All systems working!`,
          `${data.firstName}, je trainingsplan staat klaar. Tijdzone: ${data.timezone || "niet ingesteld"}. Alles werkt!`,
        );
      }

      await sendPush(data.fcmToken, "System Check ✅", body);
    }
  },
);

// ─── 9. Welcome Email on Client Created (Firestore trigger + Nodemailer) ──

const INVITE_BASE_URL = "https://fitness-website-ten-gamma.vercel.app/invite";

export const onClientCreated = onDocumentCreated(
  {document: "clients/{clientId}", region: "europe-west1"},
  async (event) => {
    const data = event.data?.data() as ClientDoc | undefined;
    if (!data?.email) return;

    const nodemailer = await import("nodemailer");
    const transporter = nodemailer.createTransport({
      host: "smtp.gmail.com",
      port: 465,
      secure: true,
      auth: {
        user: GMAIL_USER,
        pass: GMAIL_APP_PASSWORD,
      },
    });

    const inviteLink =
      `${INVITE_BASE_URL}?email=${encodeURIComponent(data.email)}`;
    const firstName = data.firstName || "there";

    await transporter.sendMail({
      from: `TAJLY <${GMAIL_USER}>`,
      to: data.email,
      subject: `Welcome to TAJLY, ${firstName}! 🎉`,
      html: `
        <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; background-color: #09090D; color: #F5F0E6;">
          <div style="text-align: center; margin-bottom: 32px;">
            <h1 style="color: #D4AF37; font-size: 32px; margin: 0;">TAJLY</h1>
            <p style="color: #9E9A90; margin-top: 8px;">Your Personal Fitness Companion</p>
          </div>

          <div style="background-color: #131316; border-radius: 16px; padding: 32px; border: 1px solid #2A2A32;">
            <h2 style="color: #F5F0E6; margin-top: 0;">Welcome, ${firstName}!</h2>
            <p style="color: #9E9A90; line-height: 1.6;">
              Your trainer has set up your TAJLY account. You can now access personalized meal plans,
              workout schedules, and track your fitness progress.
            </p>

            <div style="text-align: center; margin: 32px 0;">
              <a href="${inviteLink}"
                 style="display: inline-block; background-color: #D4AF37; color: #09090D; padding: 14px 32px;
                        border-radius: 12px; text-decoration: none; font-weight: 600; font-size: 16px;">
                Open TAJLY App
              </a>
            </div>

            <p style="color: #9E9A90; font-size: 14px; line-height: 1.5;">
              <strong style="color: #F5F0E6;">Getting started:</strong><br/>
              1. Download the TAJLY app from the App Store or Google Play<br/>
              2. Tap the button above or use this email (${data.email}) to log in<br/>
              3. Create a password and you're ready to go!
            </p>
          </div>

          <p style="color: #555; font-size: 12px; text-align: center; margin-top: 32px;">
            This email was sent by TAJLY. If you didn't expect this, you can ignore it.
          </p>
        </div>
      `,
    });
  },
);
