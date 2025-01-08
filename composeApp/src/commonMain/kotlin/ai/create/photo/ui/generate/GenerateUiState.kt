package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,

    val errorPopup: Throwable? = null,

    val isLoadingAiVisionPrompt: Boolean = false,
    val originalAiVisionPrompt: String = "",
    val aiVisionPrompt: String = "",
    val expanded: Boolean = false,

    val userPrompt: String = "",

    val isLoadingSurpriseMe: Boolean = false,
)