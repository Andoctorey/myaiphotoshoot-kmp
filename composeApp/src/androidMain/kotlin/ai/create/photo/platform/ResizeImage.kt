package ai.create.photo.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int
): Result<ByteArray> = runCatching {
    withContext(Dispatchers.IO) {
        val originalBitmap = BitmapFactory.decodeByteArray(input, 0, input.size)
            ?: throw IllegalArgumentException("Unable to decode image from ByteArray")
        val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width
        val scaledHeight = (targetWidth * aspectRatio).roundToInt()

        val scaledBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            targetWidth,
            scaledHeight,
            true
        )

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.toByteArray()
    }
}
