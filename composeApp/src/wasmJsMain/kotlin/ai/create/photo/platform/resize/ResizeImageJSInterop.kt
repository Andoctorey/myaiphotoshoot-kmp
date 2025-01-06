@file:JsModule("./resize.js")

package ai.create.photo.platform.resize

import kotlin.js.JsName
import kotlin.js.Promise

@JsName("resizeImageToWidth")
external fun resizeImageToWidthJs(
    inputBytes: JsUint8Array,
    targetWidth: Int
): Promise<JsUint8Array>
