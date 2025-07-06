package ai.create.photo.platform

interface UrlHashManager {
    fun setHash(hash: String)
    fun getHash(): String
    fun addHashChangeListener(listener: (String) -> Unit)
}

expect fun getUrlHashManager(): UrlHashManager 