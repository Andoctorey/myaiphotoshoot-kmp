package ai.create.photo.ui.settings

import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.platform.openUrl
import ai.create.photo.ui.auth.AuthViewModel
import ai.create.photo.ui.settings.SettingsUiState.Item
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : AuthViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            ProfilesRepository.profileFlow.collect { profile ->
                uiState = uiState.copy(
                    balance = profile?.formattedBalance ?: "0"
                )
            }
        }
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        uiState = uiState.copy(
            isLoading = false,
            email = user?.email,
        )
        loadProfile()
    }

    fun loadProfile() = viewModelScope.launch {
        ProfilesRepository.reload(user?.id)
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun saveDestination(currentDestination: Item? = null) {
        uiState = uiState.copy(currentDestination = currentDestination)
    }

    fun contact() = viewModelScope.launch {
        openUrl("https://x.com/andoctorey")
    }
}
