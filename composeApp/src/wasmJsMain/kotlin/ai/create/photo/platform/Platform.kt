package ai.create.photo.platform

import kotlinx.browser.window

class WebPlatform : Platform {
    override val platform = Platforms.WEB
    override val name: String = "web " + window.navigator.userAgent
}

actual fun platform(): Platform = WebPlatform()