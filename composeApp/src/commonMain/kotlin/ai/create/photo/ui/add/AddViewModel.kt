package ai.create.photo.ui.add

import ai.create.photo.supabase.SessionViewModel
import ai.create.photo.supabase.SupabaseFunction
import ai.create.photo.supabase.SupabaseStorage
import ai.create.photo.supabase.database.UserFilesRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFiles
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
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        loadPhotos()
    }

    override fun onError(error: Throwable) {
        uiState.copy(loadingError = error)
    }

    private fun loadPhotos() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = uiState.photosByPhotoSet == null)
        try {
            val files = UserFilesRepository.getFiles(userId).getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                photosByPhotoSet = files.map { file ->
                    AddUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        path = file.filePath,
                        photoSet = file.photoSet,
                        url = file.signedUrl,
                    )
                }.groupBy { it.photoSet },
                photoSet = uiState.photoSet,
                scrollToTop = true,
            )
        } catch (e: Exception) {
            Logger.e("Loading photos failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
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
            SupabaseStorage.deleteFile("$userId/${photo.photoSet}/${photo.path}")
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

        uiState = uiState.copy(creatingModel = true)
        try {
            SupabaseFunction.createAiModel(uiState.photoSet)
        } catch (e: Exception) {
            Logger.e("Create model failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
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
            UserFilesRepository.deletePhotoSet(userId, photoSet)
            SupabaseStorage.deletePhotoSet(userId, photoSet)
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
    }

    fun createPhotoSet() {
        uiState = uiState.copy(showMenu = false, photoSet = (uiState.photoSets?.max() ?: 0) + 1)
    }
}
