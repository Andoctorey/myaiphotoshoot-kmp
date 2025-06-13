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
    MAIN(
        price = "$3.99",
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_bJe00j2cH1O773U5N8cbC06"
        else "https://buy.stripe.com/6oUdR98B50K3dsi8ZkcbC03",
        productId = "top_up_4",
    ),
}