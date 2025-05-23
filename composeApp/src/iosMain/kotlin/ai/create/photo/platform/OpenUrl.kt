package ai.create.photo.platform

actual fun openUrl(url: String) =
    openUrlProvider?.openUrl(url)
        ?: throw IllegalStateException("OpenUrlProvider not set")

interface OpenUrlProvider {
    fun openUrl(url: String)
}

var openUrlProvider: OpenUrlProvider? = null