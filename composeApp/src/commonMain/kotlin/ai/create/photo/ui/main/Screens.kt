package ai.create.photo.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.*

enum class AppNavigationRoutes(
    val label: StringResource,
    val icon: ImageVector,
) {
    TAB1(Res.string.tab_1, Icons.Default.Face),
    TAB2(Res.string.tab_2, Icons.Default.FavoriteBorder),
    TAB3(Res.string.tab_3, Icons.Default.Settings),
}
