package ai.create.photo.ui.create

import ai.create.photo.supabase.Supabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CreateViewModel : ViewModel() {

    val supabase = Supabase

    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        uiState = uiState.copy(loading = true)
        viewModelScope.launch {
            supabase.authStatus.catch { throwable ->
                uiState = uiState.copy(uploadError = throwable.message ?: "Unknown error")
            }.collect {
                uiState = uiState.copy(loading = false)
            }
            supabase.authenticate()
        }
    }
}
