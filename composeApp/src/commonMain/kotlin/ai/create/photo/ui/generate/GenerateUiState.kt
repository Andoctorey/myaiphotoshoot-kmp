package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val trainingId: String? = null,

    val prompt: String = "",
    val isGenerating: Boolean = false,

    val errorPopup: Throwable? = null,
)