package ai.create.photo.ui.add

import ai.create.photo.supabase.SupabaseDatabase
import ai.create.photo.supabase.SupabaseStorage
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadPhotoUseCase(
    private val storage: SupabaseStorage,
    private val database: SupabaseDatabase
) {

    operator fun invoke(userId: String, file: PlatformFile): Flow<UploadStatus> = flow {
        var successfulResponse: UploadStatus? = null
        storage.uploadPhoto(userId, file)
            .collect { response ->
                if (response is UploadStatus.Success) {
                    successfulResponse = response
                } else {
                    emit(response)
                }
            }

        val filePath = (successfulResponse as? UploadStatus.Success)?.response?.path
            ?: throw Exception("File path is null after upload")
        database.saveFile(userId, filePath).onFailure {
            throw it
        }

        emit(successfulResponse)
    }
}
