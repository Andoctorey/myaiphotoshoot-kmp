package ai.create.photo.ui.settings.balance

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.platform.topUpPlatform
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class BalanceViewModel : AuthViewModel() {

    var uiState by mutableStateOf(BalanceUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        uiState = uiState.copy(isLoading = false)
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun onPromoCodeChanged(promoCode: String) {
        uiState = uiState.copy(promoCode = promoCode)
    }

    fun applyPromoCode() = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        val promoCode = uiState.promoCode.trim().takeIf { it.isNotEmpty() } ?: return@launch
        Logger.i("applyPromoCode: $promoCode")
        uiState = uiState.copy(isApplyingPromoCode = true, isIncorrectPromoCode = false)
        try {
            val isApplied = SupabaseFunction.applyPromoCode(promoCode)
            if (isApplied) {
                ProfilesRepository.loadProfile(userId)
            }
            uiState = uiState.copy(
                isApplyingPromoCode = false,
                isIncorrectPromoCode = !isApplied,
                showPromoCodeAppliedPopup = isApplied,
                balance = ProfilesRepository.profile?.formattedBalance ?: "0",
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isApplyingPromoCode = false)
            ensureActive()
            if (isAuthenticated) return@launch
            Logger.e("applyPromoCode failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun hidePromoCodeAppliedPopup() {
        uiState = uiState.copy(showPromoCodeAppliedPopup = false)
    }

    fun enterPromoCode() {
        uiState = uiState.copy(showEnterPromoCode = true)
    }

    fun hideBalanceUpdatedPopup() {
        uiState = uiState.copy(showEnterPromoCode = false)
    }

    fun topUp(pricing: Pricing) = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        topUpPlatform(
            userId = userId,
            pricing = pricing,
            onFailure = {
                uiState = uiState.copy(errorPopup = it)
            },
            onSuccess = {
                uiState = uiState.copy(showBalanceUpdatedPopup = true)
                viewModelScope.launch {
                    (1..10).forEach {
                        ProfilesRepository.loadProfile(userId)
                        delay(5000L) // Wait for profile to update
                    }
                }
            },
        )
    }
}
