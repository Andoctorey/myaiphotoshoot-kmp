import co.touchlab.kermit.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual class GeneratedPhotoSharing {
    actual fun sharePhoto(url: String) {
        val clipBoard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(url)
        clipBoard.setContents(selection, null)
        Logger.i("Link copied : $url")
    }
}