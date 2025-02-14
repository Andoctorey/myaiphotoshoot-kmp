package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.SupabaseStorage.UPLOADS
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import ai.create.photo.ui.auth.AuthViewModel
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
import kotlinx.datetime.Instant
import kotlin.math.max

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
        loadPhotos()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(isLoadingPhotos = false, loadingError = error)
    }

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
            currentCoroutineContext().ensureActive()
            Logger.e("loadPhotos failed", e)
            uiState = uiState.copy(isLoadingPhotos = false, loadingError = e)
        }
    }


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
            SupabaseStorage.deleteFile("${user?.id}/$UPLOADS/${photo.name}")
            UserFilesRepository.deleteFile(photo.id)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e("deletePhoto failed, $photo", e)
            uiState = uiState.copy(photos = photos, errorPopup = e)
        }
    }

    fun analyzePhotos() = viewModelScope.launch {
        if (uiState.analyzingPhotos != 0) return@launch
        val notAnalyzedPhotos = uiState.photos?.filter { it.analysis == null }
        if (notAnalyzedPhotos.isNullOrEmpty()) return@launch

        Logger.i("analyzePhotos: ${notAnalyzedPhotos.size}")

        uiState =
            uiState.copy(analyzingPhotos = (uiState.photos?.size ?: 0) - notAnalyzedPhotos.size + 1)
        try {
            if (Supabase.LOCAL) {
                notAnalyzedPhotos.forEach { photo ->
                    SupabaseFunction.analyzePhoto(photo.id)
                    uiState = uiState.copy(analyzingPhotos = uiState.analyzingPhotos + 1)
                }
            } else {
                val analysisJobs = notAnalyzedPhotos.map { photo ->
                    async {
                        SupabaseFunction.analyzePhoto(photo.id)
                        uiState = uiState.copy(analyzingPhotos = uiState.analyzingPhotos + 1)
                    }
                }
                analysisJobs.awaitAll()
            }
            uiState = uiState.copy(analyzingPhotos = 0, showAnalysisForAll = true)
            loadPhotos()
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e("analyzePhotos failed", e)
            uiState = uiState.copy(analyzingPhotos = 0, errorPopup = e)
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
            currentCoroutineContext().ensureActive()
            Logger.e("Delete unsuitable photos failed", e)
            uiState = uiState.copy(errorPopup = e, photos = photos)
        }
    }

    fun trainAiModel(steps: Int) = viewModelScope.launch {
        val photos = uiState.photos
        if (photos.isNullOrEmpty() || photos.size < 10) {
            uiState = uiState.copy(showUploadMorePhotosPopup = true)
            return@launch
        }

        if (photos.size > 20) {
            uiState = uiState.copy(showDeleteSomePhotosPopup = true)
            return@launch
        }

        Logger.i("trainAiModel steps: $steps, photos: ${photos.size}")

        uiState = uiState.copy(trainingStatus = TrainingStatus.PROCESSING)
        try {
            SupabaseFunction.trainAiModel(steps)
            loadTraining()
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e("trainAiModel failed", e)
            uiState = uiState.copy(trainingStatus = null, errorPopup = e)
        }
    }

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
            currentCoroutineContext().ensureActive()
            Logger.e("loadTraining failed", e)
            uiState = uiState.copy(isLoadingTraining = false, errorPopup = e)
        }

        if (uiState.trainingStatus == TrainingStatus.PROCESSING) {
            delay(100 * 1000)
            ensureActive()
            if (uiState.isLoadingPhotos) return@launch
            loadTraining()
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

    fun hideDeleteSomePhotosPopup() {
        uiState = uiState.copy(showDeleteSomePhotosPopup = false)
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun toggleTrainAiModelPopup(show: Boolean) {
        if (show) {
            val photos = uiState.photos ?: return
            val hasBadPhotos = photos.any { it.analysisStatus == AnalysisStatus.DECLINED }
            if (hasBadPhotos && !uiState.deleteSomePhotosPopupShown) {
                toggleDeleteUnsuitablePhotosPopup(true)
                uiState = uiState.copy(deleteSomePhotosPopupShown = true)
                return
            }
        }
        uiState = uiState.copy(showTrainAiModelPopup = show)
    }

    fun toggleDeleteUnsuitablePhotosPopup(show: Boolean) {
        uiState = uiState.copy(deleteUnsuitablePhotosPopup = show)
    }
}
