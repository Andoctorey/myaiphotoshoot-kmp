package ai.create.photo.ui.add

import ai.create.photo.supabase.SessionViewModel
import ai.create.photo.supabase.SupabaseDatabase
import ai.create.photo.supabase.SupabaseStorage
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
        database = SupabaseDatabase,
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
        uiState = uiState.copy(isLoading = uiState.photosByFolder == null)
        try {
            val files = SupabaseDatabase.getFiles().getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                photosByFolder = files.map { file ->
                    AddUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        path = file.filePath,
                        folder = file.folder,
                        url = file.signedUrl,
                    )
                }.groupBy { it.folder },
                folder = uiState.folder ?: files.firstOrNull()?.folder,
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
        val folder = uiState.folder!!

        uiState = uiState.copy(uploadProgress = 1, errorPopup = null, showMenu = false)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase.invoke(userId, folder, file).catch {
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
        val photosByFolder = uiState.photosByFolder ?: return@launch
        val photosInFolder = photosByFolder[photo.folder] ?: return@launch
        val updatedPhotos = photosInFolder.filter { it.id != photo.id }
        val updatedPhotosByFolder = photosByFolder.toMutableMap().apply {
            put(photo.folder, updatedPhotos)
        }
        uiState = uiState.copy(photosByFolder = updatedPhotosByFolder, showMenu = false)
        try {
            SupabaseDatabase.deleteFile(photo.id)
            SupabaseStorage.deleteFile("$userId/${photo.folder}/${photo.path}")
        } catch (e: Exception) {
            Logger.e("Delete photo failed, $photo", e)
            uiState = uiState.copy(photosByFolder = photosByFolder, errorPopup = e)
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
    }

    fun hideUploadMorePhotosPopup() {
        uiState = uiState.copy(showUploadMorePhotosPopup = false)
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun setFolderDefaultValue(folderName: String) {
        uiState = uiState.copy(folder = folderName + " " + getNewFolderNumber())
    }

    fun getNewFolderNumber(): Int {
        val folders = uiState.folders ?: return 0
        val folderNumbers = folders.map { it.filter { it.isDigit() }.toInt() }
        return (folderNumbers.maxOrNull() ?: 0) + 1
    }

    fun deleteFolder() = viewModelScope.launch {
        uiState = uiState.copy(showMenu = false)

        val folder = uiState.folder ?: return@launch
        try {
            SupabaseDatabase.deleteFolder(folder)
            SupabaseStorage.deleteFolder("$userId/$folder/")
            loadPhotos()
        } catch (e: Exception) {
            Logger.e("Delete folder failed: $folder", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun selectFolder(folder: String) {
        uiState = uiState.copy(showMenu = false, folder = folder)
    }

    fun createFolder(folderName: String) {
        uiState = uiState.copy(showMenu = false, folder = folderName + " " + getNewFolderNumber())
    }
}
