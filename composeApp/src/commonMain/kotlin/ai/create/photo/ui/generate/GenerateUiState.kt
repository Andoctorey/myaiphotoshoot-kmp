package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,

    val prompt: String = "",

    val errorPopup: Throwable? = null,
)