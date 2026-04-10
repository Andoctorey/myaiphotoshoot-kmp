package ai.create.photo.data.logger

import co.touchlab.kermit.Logger
import coil3.network.HttpException

private const val SIGNED_OBJECT_PATH = "/storage/v1/object/sign/"

private fun Throwable?.isExpectedSupabaseSignedUrlImage4xx(): Boolean {
    if (this !is HttpException) return false
    val message = this.message.orEmpty().lowercase()
    return "http 400" in message || "http 404" in message
}

fun logImageLoadError(url: String, throwable: Throwable?) {
    val lowerUrl = url.lowercase()
    val expectedSignedUrl4xx =
        ".supabase.co" in lowerUrl &&
            SIGNED_OBJECT_PATH in lowerUrl &&
            throwable.isExpectedSupabaseSignedUrlImage4xx()

    if (expectedSignedUrl4xx) {
        Logger.w("ignored signed image url 4xx: $url")
        return
    }

    Logger.e("error loading image $url", throwable)
}
