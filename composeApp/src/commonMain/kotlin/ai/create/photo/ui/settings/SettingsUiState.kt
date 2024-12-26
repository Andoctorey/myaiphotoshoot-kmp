package ai.create.photo.ui.settings

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.account
import photocreateai.composeapp.generated.resources.privacy_policy
import photocreateai.composeapp.generated.resources.report_a_problem
import photocreateai.composeapp.generated.resources.top_up


@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val currentDestination: Item? = null,

    val items: List<Item> = listOf(
        AccountItem(),
        PlaceholderItem(Res.string.top_up),
        PlaceholderItem(Res.string.privacy_policy),
        PlaceholderItem(Res.string.report_a_problem),
    )
) {

    @Immutable
    sealed class Item()

    sealed class DetailedItem(val nameRes: StringResource) : Item()

    class AccountItem() : DetailedItem(Res.string.account)
    class PlaceholderItem(nameRes: StringResource) : DetailedItem(nameRes)
    class SpacerItem : Item()
}