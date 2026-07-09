import SwiftUI
import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import ComposeApp

/// Firebase + push wiring, fully instrumented with [PUSH] logs so a fresh install shows
/// exactly where (if anywhere) the token flow stops. Requires: Push Notifications capability
/// + Background Modes → Remote notifications on the target, and GoogleService-Info.plist bundled.
class AppDelegate: NSObject, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        print("[PUSH] didFinishLaunching — configuring Firebase")
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self

        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            print("[PUSH] requestAuthorization granted=\(granted) error=\(String(describing: error))")
            guard granted else {
                print("[PUSH] ❌ notification permission NOT granted — iOS will never issue a token. Enable it in Settings.")
                return
            }
            DispatchQueue.main.async {
                print("[PUSH] registering for remote notifications (asking APNs)…")
                application.registerForRemoteNotifications()
            }
        }
        return true
    }

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let hex = deviceToken.map { String(format: "%02x", $0) }.joined()
        print("[PUSH] ✅ APNs device token received (\(deviceToken.count) bytes): \(hex.prefix(24))…")
        Messaging.messaging().apnsToken = deviceToken
        print("[PUSH] set Messaging.apnsToken; waiting for FCM registration token…")
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // THIS is the usual silent killer: no Push Notifications capability / no APNs entitlement.
        print("[PUSH] ❌ APNs registration FAILED: \(error.localizedDescription)")
        print("[PUSH]    → check: target has 'Push Notifications' capability + provisioning profile with APNs.")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken {
            print("[PUSH] ✅ didReceiveRegistrationToken (FCM): \(token.prefix(24))… len=\(token.count)")
            print("[PUSH] handing FCM token to Kotlin bridge (TajlyPush)")
            TajlyPush.shared.updateFcmToken(token: token)
        } else {
            print("[PUSH] ❌ didReceiveRegistrationToken but token is nil")
        }
    }

    // Show notifications even while the app is in the foreground.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        print("[PUSH] willPresent notification in foreground")
        completionHandler([.banner, .sound, .badge])
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
