package ai.create.photo.platform

expect suspend fun saveGeneratedPhoto(
    bytes: ByteArray,
    baseName: String,
    extension: String,
)
