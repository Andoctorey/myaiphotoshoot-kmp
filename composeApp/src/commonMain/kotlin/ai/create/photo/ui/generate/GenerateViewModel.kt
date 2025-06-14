package ai.create.photo.ui.generate

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.platform.resizeToWidth
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class GenerateViewModel : AuthViewModel() {

    var uiState by mutableStateOf(GenerateUiState())
        private set

    private var updatePhotosToGenerateJob: Job? = null
    private var updateSelectedTrainingJob: Job? = null

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (userChanged || uiState.trainings == null) {
            loadProfile()
        }
        loadTrainings()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadTrainings() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        Logger.i("loadTrainings")
        uiState = uiState.copy(
            isLoading = uiState.trainings == null && uiState.promptBgUrl == null,
            loadingError = null
        )
        try {
            var trainings = UserTrainingsRepository.getTrainings(userId).getOrThrow().map {
                GenerateUiState.Training(
                    id = it.id,
                    personDescription = it.personDescription,
                )
            }
            val training =
                trainings.find { it.id == uiState.selectedTrainingId } ?: trainings.firstOrNull()
            uiState = uiState.copy(
                isLoading = false,
                trainings = trainings,
                selectedTrainingId = training?.id,
                personDescription = "",
                originalPersonDescription = "",
            )

            selectTraining(training)
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("Load training failed", e)
            uiState = uiState.copy(loadingError = e)
        }
    }


    fun onPersonDescriptionChanged(prompt: String) {
        uiState = uiState.copy(personDescription = prompt)
    }

    fun onUserPromptChanged(prompt: String) {
        uiState = uiState.copy(
            userPrompt = prompt,
            promptBgUrl = null,
            parentGenerationId = if (prompt.isEmpty()) null else uiState.parentGenerationId,
            promptBeforeEnhancing = "",
            surpriseMePrompt = false,
            showTranslateButton = !isLikelyEnglish(prompt),
        )
    }

    fun onHistoryClicked() {
        val history = uiState.promptsHistory
        val currentPrompt = uiState.userPrompt

        val newPrompt = if (currentPrompt.isEmpty() || !history.contains(currentPrompt)) {
            history.lastOrNull() ?: ""
        } else {
            val index = history.indexOf(currentPrompt)
            if (index > 0) history[index - 1] else history.last()
        }

        uiState =
            uiState.copy(userPrompt = newPrompt, promptBgUrl = null, parentGenerationId = null)
    }


    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun prepareToGenerate(
        onGenerate: (String, String, String?, Int) -> Unit,
        trainAiModel: () -> Unit
    ) =
        viewModelScope.launch {
            val trainingId = uiState.selectedTrainingId
            if (trainingId.isNullOrEmpty()) {
                trainAiModel()
                return@launch
            }

            uiState = uiState.copy(
                showSettings = false,
                promptsHistory = if (uiState.userPrompt.isNotBlank() && !uiState.promptsHistory.contains(
                        uiState.userPrompt
                    )
                )
                    uiState.promptsHistory + uiState.userPrompt else uiState.promptsHistory,
            )

            val oldDescription = uiState.originalPersonDescription
            val newDescription = uiState.personDescription
            if (oldDescription == newDescription) {
                onGenerate(
                    trainingId,
                    uiState.userPrompt,
                    uiState.parentGenerationId,
                    uiState.photosToGenerateX100 / 100
                )
                uiState =
                    uiState.copy(
                        showOpenCreations = true, userPrompt = "",
                        promptBgUrl = null, parentGenerationId = null,
                    )
            } else {
                try {
                    UserTrainingsRepository.updatePersonDescription(trainingId, newDescription)
                        .getOrThrow()
                    uiState = uiState.copy(originalPersonDescription = newDescription)
                    onGenerate(
                        trainingId, uiState.userPrompt,
                        uiState.parentGenerationId, uiState.photosToGenerateX100 / 100
                    )
                } catch (e: Exception) {
                    ensureActive()
                    if (isAuthenticated) {
                        Logger.e("Update description failed", e)
                        uiState = uiState.copy(errorPopup = e)
                    }
                }
            }
            uiState = uiState.copy(showOpenCreations = true, userPrompt = "", promptBgUrl = null)
        }

    fun onRefreshPersonDescription() = viewModelScope.launch {
        val trainingId = uiState.selectedTrainingId ?: return@launch
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
            uiState = uiState.copy(isLoadingPersonDescription = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("onRefreshAiVisionPrompt failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }


    fun loadProfile() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        try {
            val profile = ProfilesRepository.loadProfile(userId)
            val preferences = profile?.preferences
            val photosToGenerate = preferences?.photosToGenerate
            val trainingId = preferences?.selectedTrainingId
            uiState = uiState.copy(
                photosToGenerateX100 = if (photosToGenerate != null) photosToGenerate * 100
                else uiState.photosToGenerateX100,
                selectedTrainingId = trainingId,
            )
            val training =
                uiState.trainings?.find { it.id == trainingId } ?: uiState.trainings?.firstOrNull()
            selectTraining(training)
        } catch (e: Exception) {
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("loadProfile failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun surpriseMe() = viewModelScope.launch {
        uiState = uiState.copy(
            isLoadingSurpriseMe = true, userPrompt = "",
            parentGenerationId = null, promptBgUrl = null
        )
        try {
            val prompt = SupabaseFunction.surpriseMe()
            uiState = uiState.copy(
                userPrompt = prompt,
                promptBeforeEnhancing = "",
                isLoadingSurpriseMe = false,
                surpriseMePrompt = true,
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingSurpriseMe = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("surpriseMe failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun selectTraining(training: GenerateUiState.Training?, saveInDb: Boolean = false) =
        viewModelScope.launch {
            if (training == null) return@launch
            if (training.personDescription.isNullOrEmpty()) {
                uiState = uiState.copy(showSettings = true)
                onRefreshPersonDescription()
            }
            uiState = uiState.copy(
                selectedTrainingId = training.id,
                originalPersonDescription = training.personDescription ?: "",
                personDescription = training.personDescription ?: ""
            )
            Logger.i("selectTraining: ${training.id}, saveInDb: $saveInDb")

            if (!saveInDb) return@launch
            updateSelectedTrainingJob?.cancel()
            val userId = user?.id ?: return@launch
            updateSelectedTrainingJob = viewModelScope.launch {
                delay(1000L)
                try {
                    val profile = ProfilesRepository.loadProfile(userId)
                    var preferences = profile?.preferences ?: return@launch
                    preferences = preferences.copy(
                        selectedTrainingId = training.id,
                        firstTrainingCompleted = true,
                    )
                    ProfilesRepository.updateProfilePreference(userId, preferences)
                } catch (e: Exception) {
                    ensureActive()
                    if (!isAuthenticated) return@launch
                    Logger.e("selectTraining failed", e)
                    uiState = uiState.copy(errorPopup = e)
                }
            }
        }

    fun onPhotosToGenerateChanged(photosToGenerate: Int) {
        updatePhotosToGenerateJob?.cancel()
        val userId = user?.id ?: return
        val originalPhotosToGenerate = uiState.photosToGenerateX100
        uiState = uiState.copy(photosToGenerateX100 = photosToGenerate)
        val photosToGenerate = photosToGenerate / 100

        updatePhotosToGenerateJob = viewModelScope.launch {
            delay(2000L)
            try {
                val profile = ProfilesRepository.loadProfile(userId)
                var preferences = profile?.preferences ?: return@launch
                preferences = preferences.copy(
                    photosToGenerate = photosToGenerate,
                    firstTrainingCompleted = true,
                )
                ProfilesRepository.updateProfilePreference(userId, preferences)
            } catch (e: Exception) {
                uiState = uiState.copy(photosToGenerateX100 = originalPhotosToGenerate)
                ensureActive()
                if (!isAuthenticated) return@launch
                Logger.e("onPhotosToGenerateChanged failed", e)
                uiState = uiState.copy(errorPopup = e)
            }
        }
    }

    fun enhancePrompt() = viewModelScope.launch {
        if (uiState.userPrompt.isEmpty()) return@launch
        Logger.i("enhancePrompt")
        uiState = uiState.copy(isEnhancingPrompt = true)
        if (uiState.promptBeforeEnhancing.isEmpty()) {
            uiState = uiState.copy(promptBeforeEnhancing = uiState.userPrompt)
        }

        try {
            val prompt = SupabaseFunction.enhancePrompt(uiState.promptBeforeEnhancing)
            uiState =
                uiState.copy(userPrompt = prompt, promptBgUrl = null, isEnhancingPrompt = false)
        } catch (e: Exception) {
            uiState = uiState.copy(isEnhancingPrompt = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("enhancePrompt failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun pictureToPrompt(file: PlatformFile) = viewModelScope.launch {
        val userId = user?.id ?: return@launch
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
            uiState = uiState.copy(isLoadingPictureToPrompt = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("pictureToPrompt failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun toggleSettings() {
        uiState = uiState.copy(showSettings = !uiState.showSettings)
    }

    fun setPredefinedPrompt(prompt: Prompt) {
        uiState = uiState.copy(
            parentGenerationId = prompt.generationId,
            userPrompt = prompt.text,
            promptBgUrl = prompt.url,
        )
    }

    fun translate() = viewModelScope.launch {
        uiState = uiState.copy(isTranslating = true)
        try {
            val translated = SupabaseFunction.translate(uiState.userPrompt)
            uiState = uiState.copy(userPrompt = translated, isTranslating = false)
        } catch (e: Exception) {
            uiState = uiState.copy(isTranslating = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("translate failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }


    fun hideOpenCreations() {
        uiState = uiState.copy(showOpenCreations = false)
    }

    // This regex allows:
    // - Basic English letters: A-Z, a-z
    // - Accented Latin letters: À-Ö, Ø-ö, ø-ÿ
    // - Digits: 0-9
    // - Whitespace: \s (spaces, tabs, etc.)
    // - Common punctuation and symbols often seen in English text
    private val englishRegex = Regex(
        // The character class includes:
        // A-Za-z           -> basic English letters
        // À-ÖØ-öø-ÿ       -> extended accented characters from Latin-1 Supplement
        // 0-9             -> digits
        // \s              -> whitespace characters
        // \.,!?           -> period, comma, exclamation, question marks
        // '"              -> straight single and double quotes
        // “”, ‘’         -> “smart” quotes
        // ()              -> parentheses
        // \[\]            -> square brackets
        // \{\}            -> curly braces
        // :;              -> colon and semicolon
        // @#$%&*+         -> various symbols
        // \-              -> hyphen (dash)
        // _=              -> underscore and equals
        // \/\\           -> forward and back slashes
        // —–              -> em dash and en dash
        // …               -> ellipsis
        // `               -> backtick
        """^[A-Za-zÀ-ÖØ-öø-ÿ0-9\s.,!?'"“”‘’()\[\]{}:;@#$%&*+\-_=/\\—–…`]+$"""
    )

    private fun isLikelyEnglish(text: String): Boolean {
        if (text.isEmpty()) return true
        return englishRegex.matches(text)
    }
}
