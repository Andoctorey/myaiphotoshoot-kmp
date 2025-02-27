package ai.create.photo.ui.settings.balance

import ai.create.photo.data.supabase.Supabase
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.creative
import photocreateai.composeapp.generated.resources.creative_description_1
import photocreateai.composeapp.generated.resources.creative_description_2
import photocreateai.composeapp.generated.resources.family
import photocreateai.composeapp.generated.resources.family_description_1
import photocreateai.composeapp.generated.resources.family_description_2
import photocreateai.composeapp.generated.resources.starter
import photocreateai.composeapp.generated.resources.starter_description_1
import photocreateai.composeapp.generated.resources.starter_description_2

@Immutable
data class BalanceUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val scrollState: ScrollState = ScrollState(0),
    val balance: String = "0",

    val enterPromoCode: Boolean = false,
    val promoCode: String = "",
    val isApplyingPromoCode: Boolean = false,
    val isIncorrectPromoCode: Boolean = false,
    val showPromoCodeAppliedPopup: Boolean = false,
)

enum class Pricing(
    val title: StringResource,
    val price: String,
    val descriptions: List<StringResource>,
    val paymentLink: String,
) {
    STARTER(
        title = Res.string.starter,
        price = "$4.99",
        descriptions = listOf(Res.string.starter_description_1, Res.string.starter_description_2),
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_3cs5mCfvQ1fA88w5kn"
        else "https://buy.stripe.com/aEU3gf2hvce14fK4gg",
    ),
    CREATIVE(
        title = Res.string.creative,
        price = "$9.99",
        descriptions = listOf(Res.string.creative_description_1, Res.string.creative_description_2),
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_6oE4iybfA5vQ88w7sw"
        else "https://buy.stripe.com/5kA3gf6xLa5Th2w9AB",
    ),
    FAMILY(
        title = Res.string.family,
        price = "$19.99",
        descriptions = listOf(Res.string.family_description_1, Res.string.family_description_2),
        paymentLink = if (Supabase.LOCAL) "https://buy.stripe.com/test_5kA16m3N85vQcoM5kp"
        else "https://buy.stripe.com/eVa4kj1drdi5bIcbIK",
    ),
}