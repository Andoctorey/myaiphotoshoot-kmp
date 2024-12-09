package ai.create.photo.ui.create

data class CreateUiState(
    val loading: Boolean = false,
    val uploadProgress: Int = 0,
    val uploadError: String? = null,
)