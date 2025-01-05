package ai.create.photo.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int
): Result<ByteArray> = runCatching {
    withContext(Dispatchers.IO) {
        val originalImage = ImageIO.read(ByteArrayInputStream(input))
        val scaledHeight =
            (originalImage.height * targetWidth.toDouble() / originalImage.width).roundToInt()
        val scaledImage = BufferedImage(targetWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = scaledImage.createGraphics()
        graphics.drawImage(originalImage, 0, 0, targetWidth, scaledHeight, null)
        graphics.dispose()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(scaledImage, "jpg", outputStream)
        outputStream.toByteArray()
    }
}
