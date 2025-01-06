@file:OptIn(ExperimentalForeignApi::class)

package ai.create.photo.platform

import kotlinx.cinterop.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.posix.memcpy

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int
): Result<ByteArray> {
    if (input.isEmpty()) {
        return Result.failure(IllegalArgumentException("Input byte array is empty."))
    }
    if (targetWidth <= 0) {
        return Result.failure(IllegalArgumentException("Target width must be > 0."))
    }

    val data = input.toNSData() ?: return Result.failure(
        IllegalArgumentException("Unable to convert input ByteArray to NSData.")
    )
    val originalImage = UIImage(data = data)

    val originalWidth = originalImage.size.useContents { this.width }
    val originalHeight = originalImage.size.useContents { this.height }
    if (originalWidth <= 0.0 || originalHeight <= 0.0) {
        return Result.failure(IllegalArgumentException("Invalid UIImage dimensions."))
    }
    val scale = targetWidth.toDouble() / originalWidth
    val targetHeight = (originalHeight * scale).toInt()

    val newSize = CGSizeMake(
        width = targetWidth.toDouble(),
        height = targetHeight.toDouble()
    )

    UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
    originalImage.drawInRect(
        CGRectMake(
            x = 0.0,
            y = 0.0,
            width = newSize.useContents { this.width },
            height = newSize.useContents { this.height }
        )
    )
    val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    if (resizedImage == null) {
        return Result.failure(IllegalStateException("Resizing failed, result UIImage is null."))
    }

    val jpeg = UIImageJPEGRepresentation(resizedImage, 1.0)
        ?: return Result.failure(IllegalStateException("Failed to get JPEG data from resized image."))

    return Result.success(jpeg.toByteArray())
}

fun ByteArray.toNSData(): NSData? =
    memScoped {
        this@toNSData.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
        }
    }

fun NSData.toByteArray(): ByteArray =
    ByteArray(this.length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }

