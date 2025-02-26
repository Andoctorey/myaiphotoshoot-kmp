package ai.create.photo.ui.settings.balance

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.platform.openUrl
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.currentCoroutineContext
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
            currentCoroutineContext().ensureActive()
            Logger.e("applyPromoCode failed", e)
            uiState = uiState.copy(isApplyingPromoCode = false, errorPopup = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun hidePromoCodeAppliedPopup() {
        uiState = uiState.copy(showPromoCodeAppliedPopup = false)
    }

    fun topUp() {
        val userId = user?.id ?: return
        val paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_fZe7uK1F05vQgF228a"
        else "https://buy.stripe.com/aEU3gf2hvce14fK4gg"
        openUrl("$paymentLink?client_reference_id=$userId")
    }
}
