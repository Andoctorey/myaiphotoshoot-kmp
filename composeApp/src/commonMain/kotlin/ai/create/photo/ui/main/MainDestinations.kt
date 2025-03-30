package ai.create.photo.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.tab_gallery
import photocreateai.composeapp.generated.resources.tab_generate
import photocreateai.composeapp.generated.resources.tab_settings

object MainRoutes {
    const val GALLERY = "gallery"
    const val GENERATE = "generate"
    const val SETTINGS = "settings"
}

sealed class TabScreen {
    abstract val route: String
    abstract val label: StringResource
    abstract val icon: ImageVector
}

@Serializable
@SerialName(MainRoutes.GALLERY)
data object GalleryTab : TabScreen() {
    override val route = MainRoutes.GALLERY

    @Contextual
    override val label = Res.string.tab_gallery

    @Contextual
    override val icon = Icons.Default.PhotoLibrary
}

@Serializable
@SerialName(MainRoutes.GENERATE)
data class GenerateTab(
    @SerialName("prompt") val promptBase64: String? = null,
) : TabScreen() {
    override val route = MainRoutes.GENERATE

    @Contextual
    override val label = Res.string.tab_generate

    @Contextual
    override val icon = Icons.Default.Brush
}

@Serializable
@SerialName(MainRoutes.SETTINGS)
data object SettingsTab : TabScreen() {
    override val route = MainRoutes.SETTINGS

    @Contextual
    override val label = Res.string.tab_settings

    @Contextual
    override val icon = Icons.Default.Settings
}