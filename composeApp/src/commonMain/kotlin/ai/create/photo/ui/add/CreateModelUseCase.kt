package ai.create.photo.ui.add

import ai.create.photo.supabase.SupabaseDatabase
import ai.create.photo.supabase.SupabaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class CreateModelStatus {
    Idle,
    GettingPhotos,
    Zipping,
    TrainingModel,
}

class CreateModelUseCase(
    private val storage: SupabaseStorage,
    private val database: SupabaseDatabase
) {

    suspend fun invoke(photos: List<AddUiState.Photo>): Flow<CreateModelStatus> = flow {

        emit(CreateModelStatus.GettingPhotos)
    }
}

