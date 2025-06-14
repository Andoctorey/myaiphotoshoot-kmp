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

            var successfulStatus: UploadStatus? = null
            storage.uploadPhoto(userId, file.name, resized)
                .collect { status ->
                    if (status is UploadStatus.Success) {
                        successfulStatus = status
                    } else {
                        emit(UploadResponse(uploadStatus = status))
                    }
                }

            val fileName = (successfulStatus as? UploadStatus.Success)?.response?.path
                ?: throw Exception("File path is null after upload")
            val file = database.saveFile(userId, fileName).onFailure {
                throw it
            }.getOrThrow()

            emit(UploadResponse(file, successfulStatus))
        }
}

data class UploadResponse(
    val file: UserFile? = null,
    val uploadStatus: UploadStatus
)
