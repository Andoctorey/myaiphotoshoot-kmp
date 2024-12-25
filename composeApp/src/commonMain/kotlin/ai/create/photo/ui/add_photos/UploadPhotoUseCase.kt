package ai.create.photo.ui.add_photos

import ai.create.photo.supabase.SupabaseStorage
import ai.create.photo.supabase.database.UserFilesRepository
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadPhotoUseCase(
    private val storage: SupabaseStorage,
    private val database: UserFilesRepository
) {

    fun invoke(userId: String, photoSet: Int, file: PlatformFile): Flow<UploadStatus> =
        flow {
            var successfulResponse: UploadStatus? = null
            storage.uploadPhoto(userId, photoSet, file)
                .collect { response ->
                    if (response is UploadStatus.Success) {
                        successfulResponse = response
                    } else {
                        emit(response)
                    }
                }

            val fileName = (successfulResponse as? UploadStatus.Success)?.response?.path
                ?: throw Exception("File path is null after upload")
            database.saveFile(userId, photoSet, fileName).onFailure {
                throw it
            }

            emit(successfulResponse)
        }
}
