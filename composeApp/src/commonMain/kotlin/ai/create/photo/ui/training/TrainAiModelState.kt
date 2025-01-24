package ai.create.photo.ui.training

import androidx.compose.runtime.Immutable

@Immutable
data class TrainAiModelState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val trainingSteps: Int = 1000,
    val photosRequired: Int = 0,
    val photosTaken: Int = 0,


    val showPhotosRequiredPopup: Boolean = false,
)