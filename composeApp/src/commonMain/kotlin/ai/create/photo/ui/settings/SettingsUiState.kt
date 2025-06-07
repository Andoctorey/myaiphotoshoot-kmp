package ai.create.photo.ui.settings

import ai.create.photo.platform.IgnoredOnParcel
import ai.create.photo.platform.Parcelable
import ai.create.photo.platform.Parcelize
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.account
import photocreateai.composeapp.generated.resources.app_store
import photocreateai.composeapp.generated.resources.google_play
import photocreateai.composeapp.generated.resources.social
import photocreateai.composeapp.generated.resources.support
import photocreateai.composeapp.generated.resources.top_up_pricing


@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val currentDestination: Item? = null,

    val email: String? = null,
    val balance: String = "0",
    val isBalanceLoading: Boolean = false,

    val items: List<Item> = buildList {
        add(LoginItem())
        add(BalanceItem())
        add(SocialItem())
        add(SupportItem())
        if (platform().platform !in listOf(Platforms.ANDROID, Platforms.IOS)) {
            add(GooglePlayItem())
            add(AppStoreItem())
        }
    },

    ) {

    @Immutable
    @Parcelize
    sealed class Item() : Parcelable

    @Immutable
    @Parcelize
    sealed class DetailedItem(
        @IgnoredOnParcel val nameRes: StringResource,
        @IgnoredOnParcel val icon: ImageVector,
    ) : Item(), Parcelable

    @Immutable
    @Parcelize
    class SpacerItem : Item(), Parcelable

    @Immutable
    @Parcelize
    class LoginItem() : DetailedItem(Res.string.account, Icons.Default.AccountCircle), Parcelable

    @Immutable
    @Parcelize
    class BalanceItem() : DetailedItem(Res.string.top_up_pricing, Icons.Default.CreditCard),
        Parcelable

    @Immutable
    @Parcelize
    class SocialItem() : DetailedItem(Res.string.social, Icons.Default.Close),
        Parcelable

    @Immutable
    @Parcelize
    class SupportItem() : DetailedItem(Res.string.support, Icons.AutoMirrored.Filled.Help),
        Parcelable

    @Immutable
    @Parcelize
    class GooglePlayItem() : DetailedItem(Res.string.google_play, Icons.Default.InstallMobile),
        Parcelable

    @Immutable
    @Parcelize
    class AppStoreItem() : DetailedItem(Res.string.app_store, Icons.Default.InstallMobile),
        Parcelable
}