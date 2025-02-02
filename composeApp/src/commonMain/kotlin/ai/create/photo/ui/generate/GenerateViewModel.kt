package ai.create.photo.ui.generate

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.platform.resizeToWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.launch

class GenerateViewModel : SessionViewModel() {

    var uiState by mutableStateOf(GenerateUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        loadTrainings()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadTrainings() = viewModelScope.launch {
        Logger.i("loadTrainings")
        val userId = userId ?: return@launch

        uiState = uiState.copy(isLoading = true, loadingError = null)
        try {
            var trainings = UserTrainingsRepository.getTrainings(userId).getOrThrow().map {
                GenerateUiState.Training(
                    id = it.id,
                    personDescription = it.personDescription,
                )
            }
            uiState = uiState.copy(
                isLoading = false,
                trainings = trainings,
                training = uiState.training ?: trainings.firstOrNull(),
                personDescription = "",
                originalPersonDescription = "",
            )

            uiState.training?.let { selectTraining(it) }
        } catch (e: Exception) {
            Logger.e("Load training failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }


    fun onPersonDescriptionChanged(prompt: String) {
        uiState = uiState.copy(personDescription = prompt)
    }

    fun onUserPromptChanged(prompt: String) {
        uiState = uiState.copy(userPrompt = prompt, promptBeforeEnhancing = "")
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun prepareToGenerate(onGenerate: (String, String, Int) -> Unit, trainAiModel: () -> Unit) =
        viewModelScope.launch {
            val trainingId = uiState.training?.id
            if (trainingId.isNullOrEmpty()) {
                trainAiModel()
                return@launch
            }

            uiState = uiState.copy(showSettings = false)

            val oldDescription = uiState.originalPersonDescription
            val newDescription = uiState.personDescription
            if (oldDescription == newDescription) {
                onGenerate(trainingId, uiState.userPrompt, uiState.photosToGenerateX100 / 100)
                return@launch
            }

            try {
                UserTrainingsRepository.updatePersonDescription(trainingId, newDescription)
                    .getOrThrow()
                uiState = uiState.copy(originalPersonDescription = newDescription)
                onGenerate(trainingId, uiState.userPrompt, uiState.photosToGenerateX100 / 100)
            } catch (e: Exception) {
                Logger.e("Update description failed", e)
                uiState = uiState.copy(errorPopup = e)
            }
        }

    fun onRefreshPersonDescription() = viewModelScope.launch {
        val trainingId = uiState.training?.id ?: return@launch
        Logger.i("onRefreshAiVisionPrompt: $trainingId")
        uiState = uiState.copy(isLoadingPersonDescription = true, personDescription = " ")
        try {
            SupabaseFunction.generatePersonDescription(trainingId)
            val training = UserTrainingsRepository.getTraining(trainingId).getOrThrow()
            uiState =
                uiState.copy(
                    isLoadingPersonDescription = false,
                    originalPersonDescription = training?.personDescription ?: "",
                    personDescription = training?.personDescription ?: ""
                )

        } catch (e: Exception) {
            Logger.e("onRefreshAiVisionPrompt failed", e)
            uiState = uiState.copy(isLoadingPersonDescription = false, errorPopup = e)
        }
    }

    fun surpriseMe() = viewModelScope.launch {
        uiState = uiState.copy(isLoadingSurpriseMe = true)
        try {
            val prompt = SupabaseFunction.surpriseMe()
            uiState = uiState.copy(
                userPrompt = prompt,
                promptBeforeEnhancing = "",
                isLoadingSurpriseMe = false
            )
        } catch (e: Exception) {
            Logger.e("surpriseMe failed", e)
            uiState = uiState.copy(isLoadingSurpriseMe = false, errorPopup = e)
        }
    }

    fun selectTraining(training: GenerateUiState.Training) = viewModelScope.launch {
        if (training.personDescription.isNullOrEmpty()) {
            onRefreshPersonDescription()
        }
        uiState = uiState.copy(
            training = training,
            originalPersonDescription = training.personDescription ?: "",
            personDescription = training.personDescription ?: ""
        )
    }

    fun onPhotosToGenerateChanged(photosToGenerate: Int) {
        uiState = uiState.copy(photosToGenerateX100 = photosToGenerate)
    }

    fun enhancePrompt() = viewModelScope.launch {
        if (uiState.userPrompt.isEmpty()) return@launch
        uiState = uiState.copy(isEnhancingPrompt = true)
        if (uiState.promptBeforeEnhancing.isEmpty()) {
            uiState = uiState.copy(promptBeforeEnhancing = uiState.userPrompt)
        }

        try {
            val prompt = SupabaseFunction.enhancePrompt(uiState.promptBeforeEnhancing)
            uiState =
                uiState.copy(userPrompt = prompt, isEnhancingPrompt = false)
        } catch (e: Exception) {
            Logger.e("surpriseMe failed", e)
            uiState = uiState.copy(isEnhancingPrompt = false, errorPopup = e)
        }
    }

    fun pictureToPrompt(file: PlatformFile) = viewModelScope.launch {
        val userId = userId ?: return@launch
        Logger.i("pictureToPrompt: ${file.name}")
        uiState = uiState.copy(isLoadingPictureToPrompt = true)
        try {
            val resizedImage = resizeToWidth(
                file.readBytes(),
                targetWidth = 768, // openai model input size
            ).getOrThrow()

            val result = SupabaseStorage.uploadPictureToPrompt(userId, resizedImage)
            val url = SupabaseStorage.createTempSignedUrl(userId, result.path)
            val prompt = SupabaseFunction.pictureToPrompt(url)
            uiState = uiState.copy(userPrompt = prompt, isLoadingPictureToPrompt = false)
        } catch (e: Exception) {
            Logger.e("pictureToPrompt failed", e)
            uiState = uiState.copy(isLoadingPictureToPrompt = false, errorPopup = e)
        }
    }

    fun toggleSettings() {
        uiState = uiState.copy(showSettings = !uiState.showSettings)
    }

    fun putPrompt(prompt: String) {
        uiState = uiState.copy(userPrompt = prompt)
    }
}
