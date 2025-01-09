package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val showSettings: Boolean = false,

    val trainings: List<Training>? = null,
    val training: Training? = null,

    val isLoadingAiVisionPrompt: Boolean = false,
    val originalAiVisionPrompt: String = "",
    val aiVisionPrompt: String = "",
    val aiVisionPromptExpanded: Boolean = false,

    val userPrompt: String = "",
    val promptBeforeEnhancing: String = "",

    val isEnhancingPrompt: Boolean = false,
    val isLoadingSurpriseMe: Boolean = false,

    val photosToGenerateX100: Int = 300,
) {
    @Immutable
    data class Training(
        val id: String,
        val personDescription: String?
    )

}