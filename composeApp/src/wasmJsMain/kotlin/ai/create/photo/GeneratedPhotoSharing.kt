import co.touchlab.kermit.consoleInfo
import kotlinx.coroutines.MainScope

actual class GeneratedPhotoSharing {
    actual fun sharePhoto(url: String) {
        try {
            js("navigatior.clipboard.writeText(url)")
            consoleInfo("Link copied - $url")
        } catch (e: Exception) {
            consoleInfo("Error : $e")
        }
    }
}