# TAJLY community auto-poster (Cloud Function)

Posts as **TAJLY** into `fitness_testing_posts` **3×/day** (09:00, 14:00, 20:00 Europe/Amsterdam)
using **Gemini**, in the exact shape the app's community feed reads.

- Project: `awi-fitness-app`
- Function: `tajlyAutoPost` (scheduled, region `europe-west1`)
- Gemini key source: reads `config/app.geminiApiKey` in Firestore (same doc the app uses).
  If that field is empty, set an env secret instead (see below) or it falls back to a fixed message.

## Deploy (2 commands — needs your Google login, which is interactive)

```bash
# one-time: install the CLI (Node/npm already present)
npm install -g firebase-tools

cd "tajly-functions"
firebase login          # opens a browser for your Google account (interactive)
cd functions && npm install && cd ..
firebase deploy --only functions
```

That's it. Firebase provisions Cloud Scheduler + Pub/Sub automatically for the schedule.

### Optional: set the Gemini key as a secret instead of Firestore
```bash
firebase functions:secrets:set GEMINI_API_KEY
# then add  secrets: ["GEMINI_API_KEY"]  to the onSchedule options in index.js and redeploy
```

### Test immediately (without waiting for the schedule)
In Google Cloud Console → Cloud Scheduler → find the `tajlyAutoPost` job → **Run now**.
A new TAJLY post should appear in the app's Community feed within seconds.

### Requirements
- The project must be on the **Blaze** (pay-as-you-go) plan — scheduled functions require it.
  (Cost for 3 tiny posts/day is effectively zero.)

---

# Personalized retention push (`tajlyRetentionPush`)

Once/day (18:00 Europe/Amsterdam) it reads every user in `fitness_testing_users`, and for
anyone with an `fcmToken` who **hasn't worked out today**, asks **Gemini** to write a personal
nudge from their real stats (streak, level, goal) and sends it via **FCM** (→ APNs on iOS).
Stale tokens are auto-cleared. Same `firebase deploy --only functions` deploys it.

## Setup needed to actually send (CLIENT side — not yet wired)
The function is ready, but it can only send once the APP registers device tokens. That needs:

1. **Firebase config files in the app** (currently missing):
   - `google-services.json` → `composeApp/` (Android)
   - `GoogleService-Info.plist` → `iosApp/iosApp/` (iOS)
2. **Enable Cloud Messaging** in the Firebase console.
3. **iOS APNs Auth Key (.p8)** uploaded to Firebase (Project Settings → Cloud Messaging).
   (The Push Notifications capability is already on the App ID.)
4. **Client integration (I build once the config files are in):**
   - Android: `firebase-messaging` + the `google-services` Gradle plugin; request notification
     permission; fetch the FCM token.
   - iOS: Firebase Messaging + APNs registration; fetch the token.
   - On login, write the token to `fitness_testing_users/{docId}.fcmToken`, and sync
     `lastWorkoutMillis` alongside the existing stat sync so the "hasn't worked out today"
     filter works.

Until the two config files exist, the Gradle `google-services` plugin can't be added (the
Android build fails without `google-services.json`), so the client push code is deferred.
The Cloud Function itself is complete and will work the moment tokens start flowing.
