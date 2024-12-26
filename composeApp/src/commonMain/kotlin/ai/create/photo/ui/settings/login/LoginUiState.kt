package ai.create.photo.ui.settings.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
)