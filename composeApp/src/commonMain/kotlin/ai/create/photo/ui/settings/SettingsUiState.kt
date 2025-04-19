package ai.create.photo.ui.settings

import ai.create.photo.platform.IgnoredOnParcel
import ai.create.photo.platform.Parcelable
import ai.create.photo.platform.Parcelize
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.account
import photocreateai.composeapp.generated.resources.contact
import photocreateai.composeapp.generated.resources.download_android_app
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
        if (platform().platform != Platforms.ANDROID) {
            add(AndroidAppItem())
        }
        add(ContactItem())
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
    class ContactItem() : DetailedItem(Res.string.contact, Icons.AutoMirrored.Filled.Help),
        Parcelable

    @Immutable
    @Parcelize
    class AndroidAppItem() : DetailedItem(Res.string.download_android_app, Icons.Default.Android),
        Parcelable
}