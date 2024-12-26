package ai.create.photo.ui.settings

import ai.create.photo.ui.settings.SettingsUiState.SpacerItem
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.settings_delete_all_data
import photocreateai.composeapp.generated.resources.settings_login
import photocreateai.composeapp.generated.resources.settings_logout
import photocreateai.composeapp.generated.resources.settings_privacy_policy
import photocreateai.composeapp.generated.resources.settings_report_a_problem
import photocreateai.composeapp.generated.resources.settings_top_up


@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val currentDestination: Item? = null,

    val items: List<Item> = listOf(
        LoginItem(),
        PlaceholderItem(Res.string.settings_top_up),
        SpacerItem(),
        PlaceholderItem(Res.string.settings_privacy_policy),
        PlaceholderItem(Res.string.settings_report_a_problem),
        SpacerItem(),
        PlaceholderItem(Res.string.settings_delete_all_data),
        PlaceholderItem(Res.string.settings_logout),
    )
) {

    @Immutable
    sealed class Item()

    sealed class DetailedItem(val nameRes: StringResource) : Item()

    class LoginItem() : DetailedItem(Res.string.settings_login)
    class PlaceholderItem(nameRes: StringResource) : DetailedItem(nameRes)
    class SpacerItem : Item()
}