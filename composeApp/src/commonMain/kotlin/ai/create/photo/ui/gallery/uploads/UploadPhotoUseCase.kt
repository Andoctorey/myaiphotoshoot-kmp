package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.platform.resizeToWidth
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadPhotoUseCase(
    private val storage: SupabaseStorage,
    private val database: UserFilesRepository
) {

    fun invoke(userId: String, file: PlatformFile): Flow<UploadStatus> =
        flow {
            val resized = resizeToWidth(file.readBytes()).getOrThrow()

            var successfulResponse: UploadStatus? = null
            storage.uploadPhoto(userId, file.name, resized)
                .collect { response ->
                    if (response is UploadStatus.Success) {
                        successfulResponse = response
                    } else {
                        emit(response)
                    }
                }

            val fileName = (successfulResponse as? UploadStatus.Success)?.response?.path
                ?: throw Exception("File path is null after upload")
            database.saveFile(userId, fileName).onFailure {
                throw it
            }

            emit(successfulResponse)
        }
}
