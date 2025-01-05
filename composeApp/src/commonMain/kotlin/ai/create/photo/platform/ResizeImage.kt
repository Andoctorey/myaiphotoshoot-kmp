package ai.create.photo.platform

expect suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int = 1024
): Result<ByteArray>