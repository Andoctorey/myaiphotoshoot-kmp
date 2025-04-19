package ai.create.photo.ui.settings

import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.platform.openUrl
import ai.create.photo.ui.auth.AuthViewModel
import ai.create.photo.ui.settings.SettingsUiState.Item
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class SettingsViewModel : AuthViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            ProfilesRepository.profileFlow.collect { profile ->
                uiState = uiState.copy(
                    balance = profile?.formattedBalance ?: "0",
                    isBalanceLoading = false,
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
        val userId = user?.id ?: return@launch
        if (auth.sessionStatus.value !is SessionStatus.Authenticated) return@launch
        val loadingJob = launch {
            delay(2000)
            uiState = uiState.copy(isBalanceLoading = true)
        }

        try {
            val profile = ProfilesRepository.loadProfile(userId)
            loadingJob.cancel()
            uiState = uiState.copy(
                isBalanceLoading = false,
                balance = profile?.formattedBalance ?: "0"
            )
        } catch (e: Exception) {
            loadingJob.cancel()
            uiState = uiState.copy(isBalanceLoading = false)
            currentCoroutineContext().ensureActive()
            Logger.e("loadProfile failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
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

    fun downloadAndroidApp() = viewModelScope.launch {
        openUrl("https://play.google.com/store/apps/details?id=com.myaiphotoshoot")
    }
}
