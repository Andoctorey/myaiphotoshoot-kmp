package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,

    val aiVisionPrompt: String = "",
    val userPrompt: String = "",

    val errorPopup: Throwable? = null,
)