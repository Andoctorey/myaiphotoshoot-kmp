package ai.create.photo.platform.resize

import kotlin.js.JsAny
import kotlin.js.JsName

@JsName("Uint8Array")
external class JsUint8Array(length: Int) : JsAny {
    val length: Int
    operator fun get(index: Int): Int
    operator fun set(index: Int, value: Int)
}