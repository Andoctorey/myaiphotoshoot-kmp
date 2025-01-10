package ai.create.photo.platform

import kotlinx.browser.window

class WebPlatform : Platform {
    override val platform: Platforms = getPlatformType()
    override val name: String = "web " + window.navigator.userAgent
}

actual fun platform(): Platform = WebPlatform()

private fun getPlatformType(): Platforms {
    val userAgent = window.navigator.userAgent
    val mobileRegex =
        Regex("Mobile|Android|iP(hone|od|ad)|IEMobile|BlackBerry|Kindle|Silk-Accelerated|Opera Mini")
    return if (mobileRegex.containsMatchIn(userAgent)) {
        Platforms.WEB_MOBILE
    } else {
        Platforms.WEB_DESKTOP
    }
}
