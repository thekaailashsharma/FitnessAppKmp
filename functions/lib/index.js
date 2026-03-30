"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.testNotification = exports.onMealPlanCreated = exports.aiWeeklyMotivation = exports.inactivityNudge = exports.weighInReminder = exports.eveningReflection = exports.workoutReminder = exports.morningMealReminder = void 0;
const admin = __importStar(require("firebase-admin"));
const scheduler_1 = require("firebase-functions/v2/scheduler");
const firestore_1 = require("firebase-functions/v2/firestore");
const fcm_1 = require("./fcm");
const gemini_1 = require("./gemini");
const timezone_1 = require("./timezone");
admin.initializeApp();
const db = admin.firestore();
// ─── Helpers ─────────────────────────────────────────────────────
async function getClientsInTimeWindow(targetHour) {
    const snapshot = await db.collection("clients").get();
    const results = [];
    for (const doc of snapshot.docs) {
        const data = doc.data();
        if (!data.fcmToken || !data.timezone)
            continue;
        if (data.status && data.status !== "Active")
            continue;
        const localHour = (0, timezone_1.getLocalHour)(data.timezone);
        if (localHour === targetHour) {
            results.push({ id: doc.id, data });
        }
    }
    return results;
}
function parseMealsJson(json) {
    if (!json)
        return [];
    try {
        return JSON.parse(json);
    }
    catch (_a) {
        return [];
    }
}
function t(lang, en, nl) {
    return lang === "nl" ? nl : en;
}
// ─── 1. Morning Meal Reminder (target: 7 AM local) ──────────────
exports.morningMealReminder = (0, scheduler_1.onSchedule)({ schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const clients = await getClientsInTimeWindow(7);
    if (clients.length === 0)
        return;
    for (const { data } of clients) {
        const email = data.email;
        if (!email || !data.fcmToken)
            continue;
        const plansSnap = await db.collection("user_meal_plans")
            .where("clientEmail", "==", email)
            .where("isActive", "==", true)
            .limit(1)
            .get();
        if (plansSnap.empty)
            continue;
        const planDoc = plansSnap.docs[0].data();
        const meals = parseMealsJson(planDoc.mealsData);
        const dayOfWeek = (0, timezone_1.jsToAppDay)((0, timezone_1.getLocalDayOfWeek)(data.timezone || "UTC"));
        const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);
        const breakfast = todayMeals.find((m) => m.mealSlot === "BREAKFAST");
        const totalCal = todayMeals.reduce((sum, m) => sum + (m.calories || 0), 0);
        const title = t(data.language, `Good morning ${data.firstName || ""}! 🌅`, `Goedemorgen ${data.firstName || ""}! 🌅`);
        let body;
        if (breakfast) {
            body = t(data.language, `Today's breakfast: ${breakfast.name} (${breakfast.calories} kcal). ` +
                `${todayMeals.length} meals planned — ${totalCal} kcal total.`, `Ontbijt vandaag: ${breakfast.name} (${breakfast.calories} kcal). ` +
                `${todayMeals.length} maaltijden gepland — ${totalCal} kcal totaal.`);
        }
        else {
            body = t(data.language, `You have ${todayMeals.length} meals planned today — ${totalCal} kcal total.`, `Je hebt ${todayMeals.length} maaltijden gepland vandaag — ${totalCal} kcal totaal.`);
        }
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 2. Workout Reminder (target: 9 AM local) ───────────────────
exports.workoutReminder = (0, scheduler_1.onSchedule)({ schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const clients = await getClientsInTimeWindow(9);
    if (clients.length === 0)
        return;
    for (const { data } of clients) {
        const email = data.email;
        if (!email || !data.fcmToken)
            continue;
        const plansSnap = await db.collection("workout_plans")
            .where("clientEmail", "==", email)
            .limit(1)
            .get();
        if (plansSnap.empty)
            continue;
        const planId = plansSnap.docs[0].id;
        const dayOfWeek = (0, timezone_1.jsToAppDay)((0, timezone_1.getLocalDayOfWeek)(data.timezone || "UTC"));
        const exercisesSnap = await db.collection("exercises")
            .where("planId", "==", planId)
            .where("dayOfWeek", "==", dayOfWeek)
            .get();
        if (exercisesSnap.empty)
            continue;
        const exerciseNames = exercisesSnap.docs
            .map((d) => d.data().name || "Exercise")
            .slice(0, 3);
        const count = exercisesSnap.size;
        const preview = exerciseNames.join(", ");
        const title = t(data.language, "Workout time! 💪", "Tijd om te trainen! 💪");
        const body = t(data.language, `${count} exercises today including ${preview}. Let's go!`, `${count} oefeningen vandaag waaronder ${preview}. Laten we gaan!`);
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 3. Evening Reflection (target: 20:00 / 8 PM local) ─────────
exports.eveningReflection = (0, scheduler_1.onSchedule)({ schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const clients = await getClientsInTimeWindow(20);
    if (clients.length === 0)
        return;
    for (const { data } of clients) {
        const email = data.email;
        if (!email || !data.fcmToken)
            continue;
        const plansSnap = await db.collection("user_meal_plans")
            .where("clientEmail", "==", email)
            .where("isActive", "==", true)
            .limit(1)
            .get();
        if (plansSnap.empty)
            continue;
        const planDoc = plansSnap.docs[0].data();
        const meals = parseMealsJson(planDoc.mealsData);
        const dayOfWeek = (0, timezone_1.jsToAppDay)((0, timezone_1.getLocalDayOfWeek)(data.timezone || "UTC"));
        const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);
        if (todayMeals.length === 0)
            continue;
        const dinner = todayMeals.find((m) => m.mealSlot === "DINNER");
        const title = t(data.language, `Evening check-in 🌙`, `Avond check-in 🌙`);
        let body;
        if (dinner) {
            body = t(data.language, `Don't forget dinner — ${dinner.name} (${dinner.calories} kcal). ` +
                `Mark your meals as completed!`, `Vergeet het avondeten niet — ${dinner.name} (${dinner.calories} kcal). ` +
                `Markeer je maaltijden als voltooid!`);
        }
        else {
            body = t(data.language, `How was your nutrition today? Mark your meals as completed!`, `Hoe was je voeding vandaag? Markeer je maaltijden als voltooid!`);
        }
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 4. Weigh-in Reminder (target: 8 AM on Mondays) ─────────────
exports.weighInReminder = (0, scheduler_1.onSchedule)({ schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const clients = await getClientsInTimeWindow(8);
    if (clients.length === 0)
        return;
    for (const { data } of clients) {
        if (!data.fcmToken || !data.timezone)
            continue;
        const jsDay = (0, timezone_1.getLocalDayOfWeek)(data.timezone);
        if (jsDay !== 1)
            continue; // Monday only
        const title = t(data.language, "Weigh-in day! ⚖️", "Weegdag! ⚖️");
        const body = t(data.language, `${data.firstName || "Hey"}, track your weight and see your progress this week.`, `${data.firstName || "Hé"}, weeg jezelf en bekijk je voortgang deze week.`);
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 5. Inactivity Nudge (runs daily at 12:00 UTC, Gemini-powered) ─
exports.inactivityNudge = (0, scheduler_1.onSchedule)({ schedule: "every day 12:00", timeZone: "UTC", region: "europe-west1" }, async () => {
    const snapshot = await db.collection("clients").get();
    const now = Date.now();
    const THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000;
    for (const doc of snapshot.docs) {
        const data = doc.data();
        if (!data.fcmToken || !data.lastActiveAt)
            continue;
        if (data.status && data.status !== "Active")
            continue;
        const lastActive = new Date(data.lastActiveAt).getTime();
        if (isNaN(lastActive))
            continue;
        const daysInactive = Math.floor((now - lastActive) / (24 * 60 * 60 * 1000));
        if (now - lastActive < THREE_DAYS_MS)
            continue;
        const lang = data.language === "nl" ? "Dutch" : "English";
        const prompt = `Generate a short, warm push notification message (max 100 characters) in ${lang} ` +
            `for ${data.firstName || "a user"} who wants "${data.goal || "to stay fit"}" ` +
            `and hasn't used the fitness app in ${daysInactive} days. ` +
            `Be encouraging, not pushy. No quotes around the message.`;
        let body = await (0, gemini_1.generateMessage)(prompt);
        if (!body) {
            body = t(data.language, `${data.firstName || "Hey"}, we miss you! Your fitness plan is waiting.`, `${data.firstName || "Hé"}, we missen je! Je fitnessplan wacht op je.`);
        }
        const title = t(data.language, "We miss you! 👋", "We missen je! 👋");
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 6. AI Weekly Motivation (Sundays at 18:00 local, Gemini) ───
exports.aiWeeklyMotivation = (0, scheduler_1.onSchedule)({ schedule: "every 1 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const clients = await getClientsInTimeWindow(18);
    if (clients.length === 0)
        return;
    for (const { data } of clients) {
        if (!data.fcmToken || !data.timezone)
            continue;
        const jsDay = (0, timezone_1.getLocalDayOfWeek)(data.timezone);
        if (jsDay !== 0)
            continue; // Sunday only
        const lang = data.language === "nl" ? "Dutch" : "English";
        const prompt = `Generate a warm, personalized weekly motivational push notification ` +
            `(max 120 characters) in ${lang} for ${data.firstName || "a fitness enthusiast"} ` +
            `whose goal is "${data.goal || "general fitness"}". ` +
            `Reference the new week ahead. Be inspiring. No quotes.`;
        let body = await (0, gemini_1.generateMessage)(prompt);
        if (!body) {
            body = t(data.language, `New week ahead, ${data.firstName || "champ"}! Stay consistent and crush your goals. 🔥`, `Nieuwe week, ${data.firstName || "kampioen"}! Blijf consistent en bereik je doelen. 🔥`);
        }
        const title = t(data.language, "New week, new energy! 🚀", "Nieuwe week, nieuwe energie! 🚀");
        await (0, fcm_1.sendPush)(data.fcmToken, title, body);
    }
});
// ─── 7. New Meal Plan Congratulation (Firestore trigger) ─────────
exports.onMealPlanCreated = (0, firestore_1.onDocumentWritten)({ document: "user_meal_plans/{planId}", region: "europe-west1" }, async (event) => {
    var _a, _b, _c, _d;
    const after = (_b = (_a = event.data) === null || _a === void 0 ? void 0 : _a.after) === null || _b === void 0 ? void 0 : _b.data();
    const before = (_d = (_c = event.data) === null || _c === void 0 ? void 0 : _c.before) === null || _d === void 0 ? void 0 : _d.data();
    // Only fire on creation (before doesn't exist) or activation
    if (before && before.isActive === (after === null || after === void 0 ? void 0 : after.isActive))
        return;
    if (!(after === null || after === void 0 ? void 0 : after.isActive) || !after.clientEmail)
        return;
    const meals = parseMealsJson(after.mealsData);
    const totalMeals = meals.length;
    const days = new Set(meals.map((m) => m.dayOfWeek)).size;
    // Find the client by email
    const clientSnap = await db.collection("clients")
        .where("email", "==", after.clientEmail)
        .limit(1)
        .get();
    if (clientSnap.empty)
        return;
    const client = clientSnap.docs[0].data();
    if (!client.fcmToken)
        return;
    const title = t(client.language, "Meal plan ready! 🎉", "Maaltijdplan klaar! 🎉");
    const body = t(client.language, `Your ${days}-day plan with ${totalMeals} meals is set. Check out today's menu!`, `Je ${days}-dagen plan met ${totalMeals} maaltijden staat klaar. Bekijk het menu!`);
    await (0, fcm_1.sendPush)(client.fcmToken, title, body);
});
// ─── 8. Test Notification (bypasses all time/day checks) ─────────
exports.testNotification = (0, scheduler_1.onSchedule)({ schedule: "every 24 hours", timeZone: "UTC", region: "europe-west1" }, async () => {
    const snapshot = await db.collection("clients").get();
    for (const doc of snapshot.docs) {
        const data = doc.data();
        if (!data.fcmToken)
            continue;
        if (data.status && data.status !== "Active")
            continue;
        const email = data.email;
        if (!email)
            continue;
        let body = `Hi ${data.firstName || "there"}! This is a test notification.`;
        // Try to include real meal data
        const plansSnap = await db.collection("user_meal_plans")
            .where("clientEmail", "==", email)
            .where("isActive", "==", true)
            .limit(1)
            .get();
        if (!plansSnap.empty) {
            const planDoc = plansSnap.docs[0].data();
            const meals = parseMealsJson(planDoc.mealsData);
            const dayOfWeek = (0, timezone_1.jsToAppDay)((0, timezone_1.getLocalDayOfWeek)(data.timezone || "UTC"));
            const todayMeals = meals.filter((m) => m.dayOfWeek === dayOfWeek);
            const totalCal = todayMeals.reduce((sum, m) => sum + (m.calories || 0), 0);
            if (todayMeals.length > 0) {
                body = t(data.language, `${data.firstName}, you have ${todayMeals.length} meals today (${totalCal} kcal). ` +
                    `Timezone: ${data.timezone || "not set"}. All systems working!`, `${data.firstName}, je hebt ${todayMeals.length} maaltijden vandaag (${totalCal} kcal). ` +
                    `Tijdzone: ${data.timezone || "niet ingesteld"}. Alles werkt!`);
            }
        }
        // Try to include workout data
        const workoutSnap = await db.collection("workout_plans")
            .where("clientEmail", "==", email)
            .limit(1)
            .get();
        if (!workoutSnap.empty && plansSnap.empty) {
            body = t(data.language, `${data.firstName}, your workout plan is ready. Timezone: ${data.timezone || "not set"}. All systems working!`, `${data.firstName}, je trainingsplan staat klaar. Tijdzone: ${data.timezone || "niet ingesteld"}. Alles werkt!`);
        }
        await (0, fcm_1.sendPush)(data.fcmToken, "System Check ✅", body);
    }
});
//# sourceMappingURL=index.js.map