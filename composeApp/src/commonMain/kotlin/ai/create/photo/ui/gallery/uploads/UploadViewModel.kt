package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.SupabaseStorage.UPLOADS
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.data.supabase.model.TrainingStatus
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.max

class UploadViewModel : SessionViewModel() {

    private val uploadPhotoUseCase = UploadPhotoUseCase(
        storage = SupabaseStorage,
        database = UserFilesRepository,
    )

    var uiState by mutableStateOf(UploadUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoadingPhotos = true)
    }

    override fun onAuthenticated() {
        loadPhotos()
        loadTraining()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(isLoadingPhotos = false, loadingError = error)
    }

    private fun loadPhotos() = viewModelScope.launch {
        Logger.i("loadPhotos")
        val userId = userId ?: return@launch
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
        } catch (e: Exception) {
            Logger.e("Loading photos failed", e)
            uiState = uiState.copy(isLoadingPhotos = false, loadingError = e)
        }
    }

    private fun loadTraining(): Job = viewModelScope.launch {
        Logger.i("loadPhotos")
        val userId = userId ?: return@launch
        if (uiState.trainingStatus != TrainingStatus.PROCESSING) {
            uiState = uiState.copy(isLoadingTraining = true)
        }
        try {
            val userTraining =
                UserTrainingsRepository.getLatestTraining(userId).getOrThrow()
            uiState = uiState.copy(
                isLoadingTraining = false,
                trainingStatus = userTraining?.status,
                loadingError = null,
            )
        } catch (e: Exception) {
            Logger.e("Loading training failed", e)
            uiState = uiState.copy(isLoadingTraining = false, errorPopup = e)
        }
        if (uiState.trainingStatus == TrainingStatus.PROCESSING) {
            delay(100 * 1000)
            ensureActive()
            if (uiState.isLoadingPhotos) return@launch
            loadTraining()
        }
    }

    fun uploadPhotos(files: PlatformFiles) = viewModelScope.launch {
        Logger.i("uploadPhotos: ${files.joinToString { it.name }}")
        val userId = userId ?: return@launch
        if (files.isEmpty()) return@launch

        uiState = uiState.copy(uploadProgress = 1, errorPopup = null)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase.invoke(userId, file).catch {
                Logger.e("upload failed", it)
                uiState = uiState.copy(uploadProgress = 0, errorPopup = it)
            }.collect { status ->
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
                        loadPhotos()
                        val overallProgress =
                            ((completedFiles.toFloat() / totalFiles) * 100).toInt()
                        uiState = uiState.copy(uploadProgress = overallProgress)
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
            UserFilesRepository.deleteFile(photo.id)
            SupabaseStorage.deleteFile("$userId/$UPLOADS/${photo.name}")
        } catch (e: Exception) {
            Logger.e("Delete photo failed, $photo", e)
            uiState = uiState.copy(photos = photos, errorPopup = e)
        }
    }

    fun createModel() = viewModelScope.launch {
        val photos = uiState.photos
        if (photos.isNullOrEmpty() || photos.size < 10) {
            uiState = uiState.copy(showUploadMorePhotosPopup = true)
            return@launch
        }

        val notAnalyzedPhotos = photos.filter { it.analysis == null }
        if (notAnalyzedPhotos.isNotEmpty()) {
            uiState = uiState.copy(trainingStatus = TrainingStatus.ANALYZING_PHOTOS)
            try {
                if (Supabase.local) {
                    notAnalyzedPhotos.forEach { photo ->
                        SupabaseFunction.analyzePhoto(photo.id)
                    }
                } else {
                    val analysisJobs = notAnalyzedPhotos.map { photo ->
                        async {
                            SupabaseFunction.analyzePhoto(photo.id)
                        }
                    }
                    analysisJobs.awaitAll()
                }
                uiState = uiState.copy(trainingStatus = TrainingStatus.SELECT_PHOTOS)
                loadPhotos()
            } catch (e: Exception) {
                Logger.e("Analyzing photos failed", e)
                uiState = uiState.copy(trainingStatus = null, errorPopup = e)
            }
            return@launch
        }

        uiState = uiState.copy(trainingStatus = TrainingStatus.PROCESSING)
        try {
            SupabaseFunction.createAiModel(photos.map { it.id })
            loadTraining()
        } catch (e: Exception) {
            Logger.e("Create model failed", e)
            uiState = uiState.copy(trainingStatus = null, errorPopup = e)
        }
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

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun toggleTrainAiModelPopup(show: Boolean) {
        uiState = uiState.copy(showTrainAiModelPopup = show)
    }
}
