package ai.create.photo.ui.train_ai_model

import androidx.compose.runtime.Immutable

@Immutable
data class TrainAiModelState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val trainingSteps: Int = 1000,
)