package ai.create.photo.platform

actual fun getLocale(): String? =
    kotlinx.browser.window.navigator.language?.substringBefore("-")