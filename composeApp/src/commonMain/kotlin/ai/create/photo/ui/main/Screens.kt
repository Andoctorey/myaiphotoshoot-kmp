package ai.create.photo.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.*

enum class AppNavigationRoutes(
    val label: StringResource,
    val icon: ImageVector,
) {
    TAB1(Res.string.tab_1, Icons.Default.PersonAdd),
    TAB2(Res.string.tab_2, Icons.Default.Brush),
    TAB3(Res.string.tab_3, Icons.Default.PersonSearch),
    TAB4(Res.string.tab_4, Icons.Default.Settings),
}
