package ai.create.photo.platform

import ai.create.photo.data.runCatchingCancellable
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int
): Result<ByteArray> = runCatchingCancellable {
    withContext(Dispatchers.IO) {
        val originalBitmap = BitmapFactory.decodeByteArray(input, 0, input.size)
            ?: throw IllegalArgumentException("Unable to decode image from ByteArray")
        val normalizedBitmap = originalBitmap.applyExifOrientation(input)
        if (normalizedBitmap !== originalBitmap) {
            originalBitmap.recycle()
        }
        val aspectRatio = normalizedBitmap.height.toFloat() / normalizedBitmap.width
        val scaledHeight = (targetWidth * aspectRatio).roundToInt()

        val scaledBitmap = normalizedBitmap.scale(targetWidth, scaledHeight)
        if (scaledBitmap !== normalizedBitmap) {
            normalizedBitmap.recycle()
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        scaledBitmap.recycle()
        outputStream.toByteArray()
    }
}

private fun Bitmap.applyExifOrientation(input: ByteArray): Bitmap {
    val orientation = ExifInterface(ByteArrayInputStream(input))
        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    if (orientation == ExifInterface.ORIENTATION_NORMAL ||
        orientation == ExifInterface.ORIENTATION_UNDEFINED
    ) {
        return this
    }
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                postRotate(90f)
                postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                postRotate(270f)
                postScale(-1f, 1f)
            }

            else -> Unit
        }
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
