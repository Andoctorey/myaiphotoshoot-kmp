package ai.create.photo.ui.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val errorPopup: Throwable? = null,

    val generationsInProgress: Int = 0,
    val generateScreenOpened: Boolean = false,
)