package ai.create.photo.ui.settings.balance

import androidx.compose.runtime.Immutable

@Immutable
data class BalanceUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val promoCode: String = "",
    val isApplyingPromoCode: Boolean = false,
    val isIncorrectPromoCode: Boolean = false,
)