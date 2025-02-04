package ai.create.photo.platform

import kotlin.js.js


actual fun shareLink(url: String) {
    js("navigator.clipboard.writeText(url)")
}