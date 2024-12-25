package ai.create.photo.platform

import kotlinx.browser.window

package com.example.platform

import kotlinx.browser.window

class WebPlatform : Platform {
    override val name: String = "web " + window.navigator.userAgent
}

actual fun platform(): Platform = WebPlatform()