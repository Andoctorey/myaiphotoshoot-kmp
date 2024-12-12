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
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
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
        viewModelScope.launch {
            try {
                SupabaseAuth.sessionStatus.collect {
                    when (it) {
                        is SessionStatus.Authenticated -> loadPhotos()
                        is SessionStatus.NotAuthenticated -> SupabaseAuth.signInAnonymously()
                        is SessionStatus.Initializing -> uiState = uiState.copy(isLoading = true)
                        is SessionStatus.RefreshFailure -> {
                            val cause = it.cause
                            when (cause) {
                                is RefreshFailureCause.NetworkError -> throw cause.exception
                                is RefreshFailureCause.InternalServerError -> throw cause.exception
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Sign in failed", e)
                uiState = uiState.copy(
                    isLoading = false,
                    loadingError = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun loadPhotos() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = uiState.photos == null)
        try {
            val files = SupabaseDatabase.getFiles().getOrThrow()
            val filePaths = files.map { it.filePath }
            val urls = SupabaseStorage.getFileUrls(filePaths).getOrThrow()
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
            uiState = uiState.copy(
                isLoading = false,
                loadingError = e.message ?: "Unknown error"
            )
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
