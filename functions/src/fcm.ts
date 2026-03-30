import * as admin from "firebase-admin";

export async function sendPush(
  token: string,
  title: string,
  body: string,
): Promise<boolean> {
  try {
    await admin.messaging().send({
      token,
      notification: {title, body},
      android: {
        priority: "high",
        notification: {channelId: "fitness_notifications"},
      },
      apns: {
        payload: {aps: {alert: {title, body}, sound: "default", badge: 1}},
      },
    });
    return true;
  } catch (error: unknown) {
    const errMsg = error instanceof Error ? error.message : String(error);
    if (
      errMsg.includes("not-registered") ||
      errMsg.includes("invalid-registration-token")
    ) {
      console.warn(`Stale token, should clean up: ${token.substring(0, 10)}…`);
    } else {
      console.error("FCM send error:", errMsg);
    }
    return false;
  }
}
