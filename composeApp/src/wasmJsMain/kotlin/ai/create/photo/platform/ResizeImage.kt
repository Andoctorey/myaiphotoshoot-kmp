package ai.create.photo.platform

import ai.create.photo.platform.resize.JsUint8Array
import ai.create.photo.platform.resize.resizeImageToWidthJs
import kotlinx.coroutines.await
import kotlin.Result

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int
): Result<ByteArray> = runCatching {
    val inputU8 = JsUint8Array(input.size)
    for (i in input.indices) {
        inputU8[i] = input[i].toInt()
    }
    val resizedU8: JsUint8Array = resizeImageToWidthJs(inputU8, targetWidth).await()
    val output = ByteArray(resizedU8.length)
    for (i in output.indices) {
        output[i] = resizedU8[i].toByte()
    }
    output
}
