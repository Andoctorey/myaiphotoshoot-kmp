package ai.create.photo.platform

import kotlinx.browser.window

actual fun getUrlHashManager(): UrlHashManager = object : UrlHashManager {
    override fun setHash(hash: String) {
        window.location.hash = hash
    }

    override fun getHash(): String {
        return window.location.hash
    }

    override fun addHashChangeListener(listener: (String) -> Unit) {
        window.addEventListener("hashchange") {
            listener(window.location.hash)
        }
    }
} 