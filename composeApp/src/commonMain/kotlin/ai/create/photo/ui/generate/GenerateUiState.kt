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

    val isLoadingPersonDescription: Boolean = false,
    val originalPersonDescription: String = "",
    val personDescription: String = "",

    val userPrompt: String = "",
    val promptBeforeEnhancing: String = "",

    val isLoadingSurpriseMe: Boolean = false,
    val isEnhancingPrompt: Boolean = false,
    val isLoadingPictureToPrompt: Boolean = false,

    val photosToGenerateX100: Int = 100,
) {
    @Immutable
    data class Training(
        val id: String,
        val personDescription: String?
    )

}