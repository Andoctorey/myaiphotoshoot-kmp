import androidx.compose.animation.core.animate
import platform.UIKit.UIActivityViewController

actual class GeneratedPhotoSharing {
    actual fun sharePhoto(url: String) {
        val controller = UIActivityViewController(
            activityItems = NSArray(url),
            applicationActivities = null
        )

        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController

        rootViewController?.presentViewController(controller, animateed = true, complition = null)
    }
}