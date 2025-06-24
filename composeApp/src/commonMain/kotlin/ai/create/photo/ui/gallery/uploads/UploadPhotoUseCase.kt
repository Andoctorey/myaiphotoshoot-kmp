package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.model.UserFile
import ai.create.photo.platform.resizeToWidth
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadPhotoUseCase(
    private val storage: SupabaseStorage,
    private val database: UserFilesRepository
) {

    fun invoke(userId: String, file: PlatformFile): Flow<UploadResponse> =
        flow {
            val resized = resizeToWidth(file.readBytes()).getOrThrow()

            // Convert WebP filename to JPEG if needed
            val finalFileName = if (file.name.lowercase().endsWith(".webp")) {
                file.name.dropLast(5) + ".jpg"
            } else {
                file.name
            }

            var successfulStatus: UploadStatus? = null
            storage.uploadPhoto(userId, finalFileName, resized)
                .collect { status ->
                    if (status is UploadStatus.Success) {
                        successfulStatus = status
                    } else {
                        emit(UploadResponse(uploadStatus = status))
                    }
                }

            val fileName = (successfulStatus as? UploadStatus.Success)?.response?.path
                ?: throw Exception("File path is null after upload")
            val userFile = database.saveFile(userId, fileName).onFailure {
                throw it
            }.getOrThrow()

            emit(UploadResponse(userFile, successfulStatus))
        }
}

data class UploadResponse(
    val file: UserFile? = null,
    val uploadStatus: UploadStatus
)
