package ai.create.photo.platform

actual suspend fun resizeToWidth(
    input: ByteArray,
    targetWidth: Int,
): Result<ByteArray> {
    return Result.success(ByteArray(0))
}