package ai.create.photo.ui.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val generationsInProgress: Int = 0,
)