package org.awi.fitness.utils

import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual fun openInAppBrowser(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    val vc = SFSafariViewController(uRL = nsUrl)
    // Walk to the top-most presented controller so this works even over a modal (e.g. the paywall).
    var top: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    top?.presentViewController(vc, animated = true, completion = null)
}
