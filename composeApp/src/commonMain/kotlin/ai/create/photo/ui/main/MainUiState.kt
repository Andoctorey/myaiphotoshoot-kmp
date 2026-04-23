package ai.create.photo.ui.main

import ai.create.photo.ui.generate.Prompt
import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val errorPopup: Throwable? = null,
    val topUpErrorPopup: Throwable? = null,
    val showBalanceUpdatedPopup: Boolean = false,

    val generationsInProgress: Int = 0,
    val openUploads: Boolean = false,
    val showUploadHint: Boolean = false,
    val openCreations: Boolean = false,
    val putPrompt: Prompt? = null,
    val resetSettingTab: Boolean = false
)
