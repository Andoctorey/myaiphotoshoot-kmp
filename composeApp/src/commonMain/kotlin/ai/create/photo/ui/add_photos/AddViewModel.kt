package ai.create.photo.ui.add_photos

import ai.create.photo.data.MemoryStore
import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import ai.create.photo.data.supabase.model.AnalysisStatus
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

class AddViewModel : SessionViewModel() {

    private val uploadPhotoUseCase = UploadPhotoUseCase(
        storage = SupabaseStorage,
        database = UserFilesRepository,
    )

    var uiState by mutableStateOf(AddUiState())
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
        uiState = uiState.copy(isLoadingPhotos = uiState.photosByPhotoSet == null)
        try {
            val files = UserFilesRepository.getInputPhotos(userId).getOrThrow()
            uiState = uiState.copy(
                isLoadingPhotos = false,
                photosByPhotoSet = files.map { file ->
                    AddUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        name = file.fileName,
                        photoSet = file.photoSet,
                        url = file.signedUrl,
                        analysis = file.analysis,
                        analysisStatus = file.analysisStatus,
                    )
                }.groupBy { it.photoSet },
                photoSet = uiState.photoSet,
                scrollToTop = true,
            )
        } catch (e: Exception) {
            Logger.e("Loading photos failed", e)
            uiState = uiState.copy(isLoadingPhotos = false, loadingError = e)
        }
    }

    private fun loadTraining(): Job = viewModelScope.launch {
        Logger.i("loadTraining")
        uiState = uiState.copy(isLoadingTraining = true)
        try {
            val userTraining =
                UserTrainingsRepository.getTraining(userId, uiState.photoSet).getOrThrow()
            uiState = uiState.copy(
                isLoadingTraining = false,
                trainingStatus = userTraining?.status,
                loadingError = null,
            )
            if (userTraining?.status == TrainingStatus.SUCCEEDED) {
                MemoryStore.trainingId = userTraining.id
            }
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
        Logger.i { "Selected files: ${files.joinToString { it.name }}" }
        if (files.isEmpty()) return@launch

        uiState = uiState.copy(uploadProgress = 1, errorPopup = null, showMenu = false)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase.invoke(userId, uiState.photoSet, file).catch {
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

    fun toggleMenu() {
        uiState = uiState.copy(showMenu = !uiState.showMenu)
    }

    fun deletePhoto(photo: AddUiState.Photo) = viewModelScope.launch {
        val photosByPhotoSet = uiState.photosByPhotoSet ?: return@launch
        val photosInPhotoSet = photosByPhotoSet[photo.photoSet] ?: return@launch
        val updatedPhotos = photosInPhotoSet.filter { it.id != photo.id }
        val updatedPhotosByPhotoSet = photosByPhotoSet.toMutableMap().apply {
            put(photo.photoSet, updatedPhotos)
        }
        uiState = uiState.copy(photosByPhotoSet = updatedPhotosByPhotoSet, showMenu = false)
        try {
            UserFilesRepository.deleteFile(photo.id)
            SupabaseStorage.deleteFile("$userId/${photo.photoSet}/${photo.name}")
        } catch (e: Exception) {
            Logger.e("Delete photo failed, $photo", e)
            uiState = uiState.copy(photosByPhotoSet = photosByPhotoSet, errorPopup = e)
        }
    }

    fun createModel() = viewModelScope.launch {
        uiState = uiState.copy(showMenu = false)

        val photos = uiState.displayingPhotos
        if (photos.isNullOrEmpty() || photos.size < 10) {
            uiState = uiState.copy(showUploadMorePhotosPopup = true)
            return@launch
        }

        val notAnalyzedPhotos = photos.filter { it.analysis == null }
        if (notAnalyzedPhotos.isNotEmpty()) {
            uiState = uiState.copy(trainingStatus = TrainingStatus.ANALYZING_PHOTOS)
            try {
                val analysisJobs = notAnalyzedPhotos.map { photo ->
                    async {
                        SupabaseFunction.analyzePhoto(photo.id)
                    }
                }
                analysisJobs.awaitAll()
                uiState = uiState.copy(trainingStatus = null)
                loadPhotos()
            } catch (e: Exception) {
                Logger.e("Analyzing photos failed", e)
                uiState = uiState.copy(trainingStatus = null, errorPopup = e)
            }
            return@launch
        }

        val badPhotos = photos.filter { it.analysisStatus != AnalysisStatus.APPROVED }
        if (badPhotos.isNotEmpty()) {
            uiState = uiState.copy(deleteUnsuitablePhotosPopup = true)
            return@launch
        }

        uiState = uiState.copy(trainingStatus = TrainingStatus.PROCESSING)
        try {
            SupabaseFunction.createAiModel(uiState.photoSet)
            loadTraining()
        } catch (e: Exception) {
            Logger.e("Create model failed", e)
            uiState = uiState.copy(trainingStatus = null, errorPopup = e)
        }
    }

    fun onCreatingModelClick() {
        uiState = uiState.copy(showCreatingModelPopup = true)
    }

    fun hideCreatingModelClick() {
        uiState = uiState.copy(showCreatingModelPopup = false)
    }

    fun hideUploadMorePhotosPopup() {
        uiState = uiState.copy(showUploadMorePhotosPopup = false)
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun deletePhotoSet() = viewModelScope.launch {
        uiState = uiState.copy(showMenu = false)

        val photoSet = uiState.photoSet
        try {
            val photos = uiState.photosByPhotoSet?.get(photoSet) ?: return@launch
            val ids = mutableListOf<String>()
            val paths = mutableListOf<String>()
            photos.forEach {
                ids.add(it.id)
                paths.add("$userId/$photoSet/${it.name}")
            }
            UserFilesRepository.deleteFiles(ids)
            SupabaseStorage.deleteFiles(paths)
            loadPhotos()
        } catch (e: Exception) {
            Logger.e("Delete photoSet failed: $photoSet", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun selectPhotoSet(photoSet: Int) {
        uiState = uiState.copy(showMenu = false, photoSet = photoSet)
        loadTraining()
    }

    fun createPhotoSet() {
        uiState = uiState.copy(showMenu = false, photoSet = (uiState.photoSets?.max() ?: 0) + 1)
    }

    fun openedCreatePhotosScreen() {
        uiState = uiState.copy(openedCreatePhotosScreen = true)
    }

    fun hideDeleteUnsuitablePhotosPopup() {
        uiState = uiState.copy(deleteUnsuitablePhotosPopup = false)
    }

    fun deleteUnsuitablePhotos() = viewModelScope.launch {
        val photos = uiState.displayingPhotos ?: return@launch
        uiState = uiState.copy(isLoadingPhotos = true)
        val badPhotos = photos.filter { it.analysisStatus != AnalysisStatus.APPROVED }
        val ids = badPhotos.map { it.id }
        val paths = badPhotos.map { "$userId/${it.photoSet}/${it.name}" }
        try {
            uiState = uiState.copy(deleteUnsuitablePhotosPopup = false)
            UserFilesRepository.deleteFiles(ids)
            SupabaseStorage.deleteFiles(paths)
            loadPhotos()
        } catch (e: Exception) {
            Logger.e("Delete unsuitable photos failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }
}
