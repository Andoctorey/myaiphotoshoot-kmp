package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.RetryConfig
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.model.UserFile
import ai.create.photo.data.supabase.retryWithBackoff
import ai.create.photo.platform.resizeToWidth
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadPhotoUseCase(
    private val storage: SupabaseStorage,
    private val database: UserFilesRepository
) {

    private val uploadRetryConfig = RetryConfig(
        maxRetries = 3,
        initialDelayMs = 1_000,
        maxDelayMs = 8_000,
    )

    fun invoke(userId: String, file: PlatformFile): Flow<UploadResponse> =
        flow {
            val resized = resizeToWidth(file.readBytes()).getOrThrow()

            // Normalize unsupported storage key characters to avoid Supabase InvalidKey errors.
            val finalFileName = toStorageSafeFileName(file.name)

            var successfulStatus: UploadStatus.Success? = null
            retryWithBackoff(config = uploadRetryConfig) {
                successfulStatus = null
                storage.uploadPhoto(userId, finalFileName, resized)
                    .collect { status ->
                        if (status is UploadStatus.Success) {
                            successfulStatus = status
                        } else {
                            emit(UploadResponse(uploadStatus = status))
                        }
                    }
                successfulStatus ?: error("File path is null after upload")
            }

            val fileName = successfulStatus?.response?.path
                ?: error("File path is null after upload")
            val userFile = database.saveFile(userId, fileName).onFailure {
                throw it
            }.getOrThrow()

            emit(UploadResponse(userFile, successfulStatus!!))
        }
}

private val nonAlphanumericUnderscoreDash = Regex("[^A-Za-z0-9_-]")
private val multiUnderscore = Regex("_+")

private fun sanitizeNamePart(value: String): String {
    return value
        .replace(nonAlphanumericUnderscoreDash, "_")
        .replace(multiUnderscore, "_")
        .trim('_')
}

private fun toStorageSafeFileName(originalName: String): String {
    val normalized = originalName.trim().ifBlank { "upload.jpg" }
    val converted = if (normalized.lowercase().endsWith(".webp")) {
        normalized.dropLast(5) + ".jpg"
    } else {
        normalized
    }

    val dotIndex = converted.lastIndexOf('.')
    val rawBase = if (dotIndex > 0) converted.substring(0, dotIndex) else converted
    val rawExt = if (dotIndex > 0 && dotIndex < converted.lastIndex) {
        converted.substring(dotIndex + 1)
    } else {
        "jpg"
    }

    val base = sanitizeNamePart(rawBase).ifBlank { "upload" }
    val ext = sanitizeNamePart(rawExt).ifBlank { "jpg" }
    val sanitized = "$base.$ext"

    if (sanitized == converted) return sanitized

    val suffix = converted.hashCode().toUInt().toString(16).take(8)
    return "${base}_$suffix.$ext"
}

data class UploadResponse(
    val file: UserFile? = null,
    val uploadStatus: UploadStatus
)
