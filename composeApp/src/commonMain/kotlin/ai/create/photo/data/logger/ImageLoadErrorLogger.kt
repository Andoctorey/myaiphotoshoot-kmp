package ai.create.photo.data.logger

import co.touchlab.kermit.Logger
import coil3.network.HttpException

private const val SIGNED_OBJECT_PATH = "/storage/v1/object/sign/"
private const val BITMAP_FACTORY_NULL_BITMAP_MARKER = "bitmapfactory returned a null bitmap"
private const val IMAGE_DECODER_CREATE_FAILED_MARKER = "failed to create image decoder"
private const val IMAGE_DECODER_INPUT_ERROR_MARKER = "input contained an error"

private fun Throwable?.isExpectedSupabaseSignedUrlImage4xx(): Boolean {
    if (this !is HttpException) return false
    val message = this.message.orEmpty().lowercase()
    return "http 400" in message || "http 404" in message
}

private fun Throwable?.isExpectedInvalidImagePayload(): Boolean {
    var current = this
    var depth = 0
    while (current != null && depth < 8) {
        val message = current.message.orEmpty().lowercase()
        if (
            BITMAP_FACTORY_NULL_BITMAP_MARKER in message ||
            IMAGE_DECODER_CREATE_FAILED_MARKER in message ||
            IMAGE_DECODER_INPUT_ERROR_MARKER in message
        ) {
            return true
        }
        current = current.cause
        depth++
    }
    return false
}

fun logImageLoadError(url: String, throwable: Throwable?) {
    val lowerUrl = url.lowercase()
    val isRemoteUrl = lowerUrl.startsWith("https://") || lowerUrl.startsWith("http://")
    val expectedSignedUrl4xx =
        ".supabase.co" in lowerUrl &&
            SIGNED_OBJECT_PATH in lowerUrl &&
            throwable.isExpectedSupabaseSignedUrlImage4xx()
    val expectedInvalidImagePayload = isRemoteUrl && throwable.isExpectedInvalidImagePayload()

    if (expectedSignedUrl4xx) {
        Logger.w("ignored signed image url 4xx: $url")
        return
    }

    if (expectedInvalidImagePayload) {
        Logger.w("ignored invalid image payload: $url")
        return
    }

    Logger.e("error loading image $url", throwable)
}
