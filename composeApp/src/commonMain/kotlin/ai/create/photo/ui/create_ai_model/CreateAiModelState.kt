package ai.create.photo.ui.create_ai_model

import androidx.compose.runtime.Immutable

@Immutable
data class CreateAiModelState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val trainingSteps: Int = 1000,
)