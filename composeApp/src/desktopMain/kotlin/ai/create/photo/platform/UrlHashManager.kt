package ai.create.photo.platform

actual fun getUrlHashManager(): UrlHashManager = object : UrlHashManager {
    override fun setHash(hash: String) {}
    override fun getHash(): String = ""
    override fun addHashChangeListener(listener: (String) -> Unit) {}
}