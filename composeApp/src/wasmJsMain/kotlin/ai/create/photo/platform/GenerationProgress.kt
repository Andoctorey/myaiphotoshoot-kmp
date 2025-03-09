package ai.create.photo.platform

import kotlinx.browser.document

val originalTitle = document.title

actual fun updateGenerationProgress(count: Int) {
    if (count == 0) {
        document.title = originalTitle
        return
    }
    document.title = "$originalTitle ($count)"
}