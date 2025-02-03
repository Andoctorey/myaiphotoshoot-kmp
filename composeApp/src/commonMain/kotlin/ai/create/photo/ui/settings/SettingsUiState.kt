package ai.create.photo.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Paid
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.about
import photocreateai.composeapp.generated.resources.account


@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val currentDestination: Item? = null,

    val email: String? = null,
    val balance: String = "0",

    val items: List<Item> = listOf(
        LoginItem(),
        BalanceItem(),
        PlaceholderItem(Res.string.about, Icons.AutoMirrored.Default.Help),
    ),

) {

    @Immutable
    sealed class Item()

    sealed class DetailedItem(
        val nameRes: StringResource,
        val icon: ImageVector,
    ) : Item()

    class LoginItem() : DetailedItem(Res.string.account, Icons.Default.AccountCircle)
    class BalanceItem() : DetailedItem(Res.string.account, Icons.Default.Paid)
    class PlaceholderItem(nameRes: StringResource, icon: ImageVector) :
        DetailedItem(nameRes, icon)
    class SpacerItem : Item()
}