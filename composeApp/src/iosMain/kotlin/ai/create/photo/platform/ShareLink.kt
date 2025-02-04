package ai.create.photo.platform

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareLink(url: String) {
    val controller = UIActivityViewController(
        activityItems = listOf(url),
        applicationActivities = null
    )

    val window = UIApplication.sharedApplication.keyWindow
    val rootViewController = window?.rootViewController

    rootViewController?.presentViewController(controller, animated = true, completion = null)
}
