import android.content.Context
import android.content.Intent

actual class GeneratedPhotoSharing (private val context: Context) {
    actual fun sharePhoto(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpg"
            putExtra(Intent.EXTRA_STREAM, url)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share photo"))
    }
}