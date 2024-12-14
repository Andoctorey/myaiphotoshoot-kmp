package ai.create.photo.ui.create

import androidx.compose.runtime.Immutable

@Immutable
data class CreateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
)