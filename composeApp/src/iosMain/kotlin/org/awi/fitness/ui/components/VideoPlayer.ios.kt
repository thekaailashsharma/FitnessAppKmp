package org.awi.fitness.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVPlayerViewController
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGRect
import platform.Foundation.NSURL
import platform.UIKit.UIView
import platform.AVFoundation.rate
import platform.AVFoundation.play
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onReady: () -> Unit
) {
    val player = remember { AVPlayer(uRL = NSURL.URLWithString(url)!!) }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true

    playerLayer.player = player
    
    DisposableEffect(player) {
        onReady()
        onDispose { }
    }
    
    // Use a UIKitView to integrate with your existing UIKit views
    UIKitView(
        factory = {
            // Create a UIView to hold the AVPlayerLayer
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            // Return the playerContainer as the root UIView
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            player.play()
            avPlayerViewController.player!!.play()
        },
        modifier = modifier
    )
}

//@OptIn(ExperimentalForeignApi::class)
//@Composable
//actual fun VideoPlayers(
//    modifier: Modifier, url: String, autoPlay: Boolean,
//    showControls: Boolean
//) {
//    val player = remember { AVPlayer(uRL = NSURL.URLWithString(url)!!) }
//    val playerLayer = remember { AVPlayerLayer() }
//    val avPlayerViewController = remember { AVPlayerViewController() }
//    avPlayerViewController.player = player
//    avPlayerViewController.showsPlaybackControls = true
//
//    playerLayer.player = player
//    // Use a UIKitView to integrate with your existing UIKit views
//    UIKitView(
//        factory = {
//            // Create a UIView to hold the AVPlayerLayer
//            val playerContainer = UIView()
//            playerContainer.addSubview(avPlayerViewController.view)
//            // Return the playerContainer as the root UIView
//            playerContainer
//        },
//        onResize = { view: UIView, rect: CValue<CGRect> ->
//            CATransaction.begin()
//            CATransaction.setValue(true, kCATransactionDisableActions)
//            view.layer.setFrame(rect)
//            playerLayer.setFrame(rect)
//            avPlayerViewController.view.layer.frame = rect
//            CATransaction.commit()
//        },
//        update = { view ->
//            player.play()
//            avPlayerViewController.player!!.play()
//        },
//        modifier = modifier
//    )
//}