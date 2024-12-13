package ai.create.photo.ui.create

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

class CreateViewModel : SessionViewModel() {

    private val uploadPhotoUseCase = UploadPhotoUseCase(
        storage = SupabaseStorage,
        database = SupabaseDatabase,
    )

    var uiState by mutableStateOf(CreateUiState())
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
        uiState = uiState.copy(isLoading = uiState.photos == null)
        try {
            val files = SupabaseDatabase.getFiles().getOrThrow()
            val filePaths = files.map { it.filePath }
            val urls = SupabaseStorage.getFileUrls(userId, filePaths).getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                photos = files.mapIndexed { index, file ->
                    CreateUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        url = urls[index].signedURL
                    )
                }
            )
        } catch (e: Exception) {
            Logger.e("Loading photos failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }

    fun uploadPhotos(files: PlatformFiles) = viewModelScope.launch {
        Logger.i { "Selected files: ${files.joinToString { it.name }}" }
        if (files.isEmpty()) return@launch

        uiState = uiState.copy(uploadProgress = 1, uploadError = null)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase(userId, file).catch {
                Logger.e("upload failed", it)
                uiState = uiState.copy(uploadProgress = 0, uploadError = it)
            }.collect { status ->
                when (status) {
                    is UploadStatus.Progress -> {
                        val currentFileProgress =
                            status.totalBytesSend.toFloat() / status.contentLength * 100
                        val overallProgress =
                            (((completedFiles) + currentFileProgress / 100) / totalFiles) * 100 - 1// waiting for db
                        uiState = uiState.copy(uploadProgress = overallProgress.toInt())
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
}
