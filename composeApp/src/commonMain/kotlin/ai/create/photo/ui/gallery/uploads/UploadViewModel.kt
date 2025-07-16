package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.SupabaseStorage.UPLOADS
import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import ai.create.photo.platform.topUpPlatform
import ai.create.photo.ui.auth.AuthViewModel
import ai.create.photo.ui.settings.balance.Pricing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UploadViewModel : AuthViewModel() {

    private val uploadPhotoUseCase = UploadPhotoUseCase(
        storage = SupabaseStorage,
        database = UserFilesRepository,
    )

    var uiState by mutableStateOf(UploadUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoadingPhotos = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (userChanged) {
            uiState = UploadUiState()
        }
        loadPhotos()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(isLoadingPhotos = false, loadingError = error)
    }

    @OptIn(ExperimentalTime::class)
    private fun loadPhotos() = viewModelScope.launch {
        Logger.i("loadPhotos")
        val userId = user?.id ?: return@launch
        uiState = uiState.copy(isLoadingPhotos = uiState.photos == null)
        try {
            val files = UserFilesRepository.getInputPhotos(userId).getOrThrow()
            uiState = uiState.copy(
                isLoadingPhotos = false,
                photos = files.map { file ->
                    UploadUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        name = file.fileName,
                        url = file.signedUrl,
                        analysis = file.analysis,
                        analysisStatus = file.analysisStatus,
                    )
                },
                scrollToTop = true,
            )
            loadTraining()
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingPhotos = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("loadPhotos failed", e)
            uiState = uiState.copy(loadingError = e)
        }
    }


    @OptIn(ExperimentalTime::class)
    fun uploadPhotos(files: PlatformFiles) = viewModelScope.launch {
        Logger.i("uploadPhotos: ${files.joinToString { it.name }}")
        val userId = user?.id ?: return@launch
        if (files.isEmpty()) return@launch

        uiState = uiState.copy(uploadProgress = 1, errorPopup = null)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase.invoke(userId, file).catch {
                Logger.e("uploadPhotos failed", it)
                uiState = uiState.copy(uploadProgress = 0, errorPopup = it)
            }.collect {
                val (file, status) = it
                if (file != null) {
                    val currentPhotos = uiState.photos ?: emptyList()
                    val photoExists = currentPhotos.any { it.id == file.id }
                    if (!photoExists) {
                        uiState = uiState.copy(
                            photos = (uiState.photos ?: emptyList()) + UploadUiState.Photo(
                                id = file.id,
                                name = file.fileName,
                                url = file.signedUrl,
                                createdAt = file.createdAt,
                                analysis = file.analysis,
                                analysisStatus = file.analysisStatus,
                            )
                        )
                        viewModelScope.launch { analyzePhoto(file.id) }
                    }
                }

                when (status) {
                    is UploadStatus.Progress -> {
                        val currentFileProgress =
                            status.totalBytesSend.toFloat() / status.contentLength * 100
                        val overallProgress =
                            (((completedFiles) + currentFileProgress / 100) / totalFiles) * 100
                        uiState = uiState.copy(uploadProgress = max(overallProgress.toInt() - 5, 1))
                    }

                    is UploadStatus.Success -> {
                        completedFiles++
                        if (completedFiles == totalFiles) {
                            uiState = uiState.copy(uploadProgress = 100)
                        }
                    }
                }
            }
        }
    }

    fun deletePhoto(photo: UploadUiState.Photo) = viewModelScope.launch {
        val photos = uiState.photos ?: return@launch
        val updatedPhotos = photos.filter { it.id != photo.id }
        uiState = uiState.copy(photos = updatedPhotos)
        try {
            SupabaseStorage.deleteFile("${user?.id}/$UPLOADS/${photo.name}")
            UserFilesRepository.deleteFile(photo.id)
        } catch (e: Exception) {
            uiState = uiState.copy(photos = photos)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("deletePhoto failed, $photo", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun analyzePhotos(): Job = viewModelScope.launch {
        updateAnalysisStatus()
        val notAnalyzedPhotos = uiState.photos?.filter { it.analysisStatus == null }
        if (notAnalyzedPhotos.isNullOrEmpty()) return@launch
        try {
            if (Supabase.LOCAL) {
                notAnalyzedPhotos.forEach { photo ->
                    analyzePhoto(photo.id)
                    updateAnalysisStatus()
                }
            } else {
                val analysisJobs = notAnalyzedPhotos.map { photo ->
                    async {
                        try {
                            analyzePhoto(photo.id)
                        } finally {
                            updateAnalysisStatus()
                        }
                    }
                }
                analysisJobs.awaitAll()
            }
            loadPhotos()
        } catch (e: Exception) {
            ensureActive()
            if (!isAuthenticated) return@launch
            loadPhotos()
            Logger.e("analyzePhotos failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    private fun updateAnalysisStatus() {
        val photos = uiState.photos ?: return
        val totalPhotos = photos.size
        val analyzedPhotos =
            photos.count { it.analysisStatus != null && it.analysisStatus != AnalysisStatus.PROCESSING }
        val analyzingPhotos = photos.count { it.analysisStatus == AnalysisStatus.PROCESSING }
        uiState = if (analyzedPhotos == totalPhotos) {
            uiState.copy(analyzingPhotos = 0)
        } else {
            uiState.copy(analyzingPhotos = analyzingPhotos)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun analyzePhoto(photoId: String, maxRetries: Int = 2) {
        uiState = uiState.copy(
            photos = uiState.photos?.map { photo ->
                if (photo.id == photoId) {
                    photo.copy(
                        analysisStatus = AnalysisStatus.PROCESSING,
                    )
                } else {
                    photo
                }
            }
        )
        Logger.i(uiState.toString())
        updateAnalysisStatus()

        var attempt = 0
        var lastException: Exception? = null

        while (attempt <= maxRetries) {
            try {
                val file = SupabaseFunction.analyzePhoto(photoId)
                uiState = uiState.copy(
                    photos = uiState.photos?.map { photo ->
                        if (photo.id == file.id) {
                            photo.copy(
                                analysis = file.analysis,
                                analysisStatus = file.analysisStatus,
                            )
                        } else {
                            photo
                        }
                    }
                )
                uiState = uiState.copy(
                    scrollToPosition = uiState.photos?.indexOfFirst { it.id == photoId },
                    scrollToTop = false
                )
                updateAnalysisStatus()
                if (attempt > 0) {
                    Logger.i("Photo analysis succeeded on attempt ${attempt + 1} for: $photoId")
                }
                return
            } catch (e: Exception) {
                attempt++

                if (attempt <= maxRetries) {
                    val delayMs = (1000 * 2.0.pow(attempt - 1)).toLong()
                    Logger.w("Photo analysis failed (attempt $attempt/${maxRetries + 1}) for: $photoId. Retrying in ${delayMs}ms. Error: ${e.message}")
                    delay(delayMs)
                    currentCoroutineContext().ensureActive()
                } else {
                    lastException = e
                    Logger.e(
                        "Photo analysis failed after ${maxRetries + 1} attempts for: $photoId. Final error: ${e.message}",
                        e
                    )
                }
            }
        }
        lastException?.let {
            Logger.e("All attempts to analyze photo failed for: $photoId", it)
            throw lastException
        }

    }

    fun deleteUnsuitablePhotos() = viewModelScope.launch {
        val photos = uiState.photos ?: return@launch
        Logger.i("deleteUnsuitablePhotos: ${photos.size}")
        uiState = uiState.copy(
            deleteUnsuitablePhotosPopup = false,
            photos = photos.filter { it.analysisStatus != AnalysisStatus.DECLINED })
        val badPhotos = photos.filter { it.analysisStatus == AnalysisStatus.DECLINED }
        try {
            UserFilesRepository.deleteFiles(badPhotos.map { it.id })
            SupabaseStorage.deleteFiles(badPhotos.map { "${user?.id}/$UPLOADS/${it.name}" })
            loadPhotos()
        } catch (e: Exception) {
            uiState = uiState.copy(photos = photos)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("Delete unsuitable photos failed", e)
            uiState = uiState.copy(photos = photos)
        }
    }

    fun trainAiModel(): Job = viewModelScope.launch {
        val photos = uiState.photos
        if (photos.isNullOrEmpty() || photos.size < 10) {
            uiState = uiState.copy(showUploadMorePhotosPopup = true)
            return@launch
        }

        if (photos.size > 20) {
            uiState = uiState.copy(showDeleteSomePhotosPopup = true)
            return@launch
        }

        Logger.i("trainAiModel photos: ${photos.size}")

        uiState = uiState.copy(
            trainingStatus = TrainingStatus.PROCESSING,
        )

        val userId = user?.id ?: return@launch
        ProfilesRepository.loadProfile(userId)
        val profile = ProfilesRepository.profile
        if (profile != null && profile.balance < 3) {
            uiState = uiState.copy(trainingStatus = null)
            topUp()
            return@launch
        }

        uiState = uiState.copy(
            trainingStatus = TrainingStatus.PROCESSING,
            trainingTimeLeft = 150 * 1000L, // 2.5 minutes)
        )

        try {
            runTimer()
            SupabaseFunction.trainAiModel()
            loadTraining()
        } catch (e: Exception) {
            uiState = uiState.copy(trainingStatus = null)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("trainAiModel failed", e)
            uiState = if (e.message?.contains("Insufficient funds") == true) {
                uiState.copy(topUpErrorPopup = e)
            } else {
                uiState.copy(errorPopup = e)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun loadTraining(): Job = viewModelScope.launch {
        Logger.i("loadTraining, trainingStatus: ${uiState.trainingStatus}")
        val userId = user?.id ?: return@launch
        if (uiState.trainingStatus != TrainingStatus.PROCESSING) {
            uiState = uiState.copy(isLoadingTraining = true)
        }

        try {
            val userTraining =
                UserTrainingsRepository.getLatestTraining(userId).getOrThrow()
            var trainingStatus = userTraining?.status
            if (userTraining != null) {
                val lastPhotoUploadDate =
                    uiState.photos?.firstOrNull()?.createdAt ?: Instant.DISTANT_FUTURE
                Logger.i("last photo upload date: $lastPhotoUploadDate")
                val newPhotoSet = userTraining.createdAt
                if (lastPhotoUploadDate > newPhotoSet) {
                    Logger.i("New photos uploaded after the last training")
                    trainingStatus = null
                }
            }
            Logger.i("latest training: $trainingStatus")
            uiState = uiState.copy(
                isLoadingTraining = false,
                trainingStatus = trainingStatus,
                loadingError = null,
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingTraining = false)
            ensureActive()
            if (isAuthenticated) {
                Logger.e("loadTraining failed", e)
                uiState = uiState.copy(errorPopup = e)
            }
        }

        val timeToStartApiRequests = 30 * 1000L // 30 seconds
        if (uiState.trainingStatus == TrainingStatus.PROCESSING) {
            if (uiState.trainingTimeLeft > timeToStartApiRequests) {
                delay(uiState.trainingTimeLeft - timeToStartApiRequests)
            } else {
                delay(5 * 1000)
            }
            ensureActive()
            if (!isAuthenticated) return@launch
            if (uiState.isLoadingPhotos) return@launch
            loadTraining()
        }
    }

    fun runTimer(): Job = viewModelScope.launch {
        if (uiState.trainingStatus != TrainingStatus.PROCESSING) return@launch
        if (uiState.trainingTimeLeft <= 0L) {
            uiState = uiState.copy(trainingTimeLeft = 0L)
            return@launch
        }
        delay(1000L)
        uiState = uiState.copy(trainingTimeLeft = uiState.trainingTimeLeft - 1000L)
        runTimer()
    }

    fun onCreatingModelClick() {
        uiState = uiState.copy(showTrainingAiModelPopup = true)
    }

    fun hideCreatingModelClick() {
        uiState = uiState.copy(showTrainingAiModelPopup = false)
    }

    fun hideUploadMorePhotosPopup() {
        uiState = uiState.copy(showUploadMorePhotosPopup = false)
    }

    fun hideDeleteSomePhotosPopup() {
        uiState = uiState.copy(showDeleteSomePhotosPopup = false)
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun resetScrollToPosition() {
        uiState = uiState.copy(scrollToPosition = null)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null, topUpErrorPopup = null)
    }

    fun checkBadPhotosAndToggleTrainAiModelPopup(show: Boolean) {
        if (show) {
            val photos = uiState.photos ?: return
            val hasBadPhotos = photos.any { it.analysisStatus == AnalysisStatus.DECLINED }
            if (hasBadPhotos) {
                toggleDeleteUnsuitablePhotosPopup(true)
                return
            }
        }
        trainAiModel()
    }


    fun toggleDeleteUnsuitablePhotosPopup(show: Boolean) {
        uiState = uiState.copy(deleteUnsuitablePhotosPopup = show)
    }

    fun topUp() = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        uiState = uiState.copy(toppingUp = true)
        topUpPlatform(
            userId = userId,
            pricing = Pricing.MAIN,
            onFailure = {
                uiState = uiState.copy(toppingUp = false, errorPopup = it)
            },
            onSuccess = {
                viewModelScope.launch {
                    repeat((1..10).count()) {
                        ProfilesRepository.loadProfile(userId)
                        if ((ProfilesRepository.profile?.balance ?: 0f) >= 3f) {
                            trainAiModel()
                            uiState =
                                uiState.copy(toppingUp = false, showBalanceUpdatedPopup = true)
                            return@launch
                        }
                        delay(5000L) // Wait for profile to update
                    }
                }
            },
        )
    }

    fun hideBalanceUpdatedPopup() {
        uiState = uiState.copy(showBalanceUpdatedPopup = false)
    }
}
