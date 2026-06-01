import UIKit
import SwiftUI
import ComposeApp

struct ContentView: View {
    var body: some View {
        ComposeNavigationView()
            .ignoresSafeArea(.keyboard) // Compose handles keyboard via imePadding
            .ignoresSafeArea(.container) // Compose handles safe areas
    }
}

/// Wraps the Compose UIViewController in a UINavigationController with the
/// navigation bar hidden. This gives iOS the UINavigationController hierarchy
/// it needs so the interactive edge-swipe pop gesture works.
/// Voyager intercepts the gesture and handles the actual back-navigation.
struct ComposeNavigationView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UINavigationController {
        let composeVC = MainViewControllerKt.MainViewController()
        let nav = UINavigationController(rootViewController: composeVC)
        nav.setNavigationBarHidden(true, animated: false)
        nav.interactivePopGestureRecognizer?.isEnabled = true
        nav.interactivePopGestureRecognizer?.delegate = context.coordinator
        return nav
    }

    func updateUIViewController(_ uiViewController: UINavigationController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator() }

    /// Coordinator acts as the gesture recognizer delegate so that the
    /// interactive pop gesture is active even when there's only one view
    /// controller in the UINavigationController stack (Compose draws its own
    /// navigation stack internally via Voyager).
    class Coordinator: NSObject, UIGestureRecognizerDelegate {
        func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
            return true
        }

        func gestureRecognizer(
            _ gestureRecognizer: UIGestureRecognizer,
            shouldRecognizeSimultaneouslyWith other: UIGestureRecognizer
        ) -> Bool {
            return true
        }
    }
}
