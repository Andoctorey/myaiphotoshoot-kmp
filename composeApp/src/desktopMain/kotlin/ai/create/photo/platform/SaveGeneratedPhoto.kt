package ai.create.photo.platform

import io.github.vinceglb.filekit.core.FileKit

actual suspend fun saveGeneratedPhoto(
    bytes: ByteArray,
    baseName: String,
    extension: String,
) {
    FileKit.saveFile(bytes = bytes, baseName = baseName, extension = extension)
}
