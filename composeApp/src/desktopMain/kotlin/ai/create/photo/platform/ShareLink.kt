package ai.create.photo.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun shareLink(url: String) {
    val clipBoard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(url)
    clipBoard.setContents(selection, null)
}