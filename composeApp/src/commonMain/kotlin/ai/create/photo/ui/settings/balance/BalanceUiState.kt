package ai.create.photo.ui.settings.balance

import ai.create.photo.data.supabase.Supabase
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable


@Immutable
data class BalanceUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val scrollState: ScrollState = ScrollState(0),
    val balance: String = "0",

    val showEnterPromoCode: Boolean = false,
    val promoCode: String = "",
    val isApplyingPromoCode: Boolean = false,
    val isIncorrectPromoCode: Boolean = false,
    val showPromoCodeAppliedPopup: Boolean = false,
    val showBalanceUpdatedPopup: Boolean = false,
)

enum class Pricing(
    val price: String,
    val paymentLink: String,
    val productId: String,
) {
    STARTER(
        price = "$4.99",
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_3cs5mCfvQ1fA88w5kn"
        else "https://buy.stripe.com/aEU3gf2hvce14fK4gg",
        productId = "top_up_5",
    ),
    CREATIVE(
        price = "$9.99",
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_6oE4iybfA5vQ88w7sw"
        else "https://buy.stripe.com/5kA3gf6xLa5Th2w9AB",
        productId = "top_up_10",
    ),
    FAMILY(
        price = "$19.99",
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_5kA16m3N85vQcoM5kp"
        else "https://buy.stripe.com/eVa4kj1drdi5bIcbIK",
        productId = "top_up_20",
    ),
}