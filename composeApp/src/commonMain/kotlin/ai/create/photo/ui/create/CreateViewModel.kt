package ai.create.photo.ui.create

import ai.create.photo.supabase.SupabaseAuth
import ai.create.photo.supabase.SupabaseDatabase
import ai.create.photo.supabase.SupabaseStorage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.UploadStatus
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CreateViewModel : ViewModel() {

    private val uploadPhotoUseCase = UploadPhotoUseCase(
        storage = SupabaseStorage,
        database = SupabaseDatabase,
    )

    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            try {
                SupabaseAuth.signInAnonymously()
            } catch (e: Exception) {
                Logger.e("Sign in failed", e)
                uiState = uiState.copy(
                    isLoading = false,
                    loadingError = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun uploadPhotos(files: PlatformFiles) = viewModelScope.launch {
        Logger.i { "Selected files: ${files.joinToString { it.name }}" }
        if (files.isEmpty()) return@launch

        uiState = uiState.copy(uploadProgress = 1, uploadError = null)

        val totalFiles = files.size
        var completedFiles = 0
        for (file in files) {
            uploadPhotoUseCase(file).catch {
                Logger.e("upload failed", it)
                uiState = uiState.copy(
                    uploadProgress = 0,
                    uploadError = it.message ?: "Unknown error"
                )
            }.collect { status ->
                when (status) {
                    is UploadStatus.Progress -> {
                        val currentFileProgress =
                            status.totalBytesSend.toFloat() / status.contentLength * 100
                        val overallProgress =
                            (((completedFiles) + currentFileProgress / 100) / totalFiles) * 100
                        uiState = uiState.copy(uploadProgress = overallProgress.toInt())
                    }

                    is UploadStatus.Success -> {
                        completedFiles++
                        val overallProgress =
                            ((completedFiles.toFloat() / totalFiles) * 100).toInt()
                        uiState = uiState.copy(uploadProgress = overallProgress)
                    }
                }
            }
        }
    }
}
