package ai.create.photo.ui.settings

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

    val items: List<Item> = listOf(
        Item(Res.string.settings_login),
        Item(Res.string.settings_top_up),
        Item(Res.string.settings_privacy_policy),
        Item(Res.string.settings_report_a_problem),
        Item(Res.string.settings_delete_all_data),
        Item(Res.string.settings_logout),
    )
) {

    @Immutable
    data class Item(
        val nameRes: StringResource,
    )
}