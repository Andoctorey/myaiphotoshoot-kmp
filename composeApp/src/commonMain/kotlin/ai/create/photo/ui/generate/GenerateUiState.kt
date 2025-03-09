package ai.create.photo.ui.generate

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val showSettings: Boolean = false,

    val trainings: List<Training>? = null,
    val selectedTrainingId: String? = null,

    val isLoadingPersonDescription: Boolean = false,
    val originalPersonDescription: String = "",
    val personDescription: String = "",

    val promptsHistory: List<String> = emptyList(),
    val userPrompt: String = "",
    val promptBgUrl: String? = null,
    val parentGenerationId: String? = null,
    val promptBeforeEnhancing: String = "",
    val surpriseMePrompt: Boolean = false,

    val isLoadingSurpriseMe: Boolean = false,
    val isEnhancingPrompt: Boolean = false,
    val isLoadingPictureToPrompt: Boolean = false,

    val showTranslateButton: Boolean = false,
    val isTranslating: Boolean = false,

    val photosToGenerateX100: Int = 300,

    val showOpenCreations: Boolean = false,
) {
    @Immutable
    data class Training(
        val id: String,
        val personDescription: String?
    )

}