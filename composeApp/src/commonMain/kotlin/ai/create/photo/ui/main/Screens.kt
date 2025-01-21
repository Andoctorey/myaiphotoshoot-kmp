package ai.create.photo.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.*

enum class AppNavigationRoutes(
    val label: StringResource,
    val icon: ImageVector,
) {
    TAB_1_GALLERY(Res.string.tab_gallery, Icons.Default.PersonSearch),
    TAB_2_GENERATE(Res.string.tab_generate, Icons.Default.Brush),
    TAB_3_SETTINGS(Res.string.tab_settings, Icons.Default.Settings),
}
