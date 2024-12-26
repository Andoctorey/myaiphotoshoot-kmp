package ai.create.photo.ui.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
)