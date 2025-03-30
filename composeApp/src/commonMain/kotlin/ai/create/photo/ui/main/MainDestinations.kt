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

object WebRoutes {
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
@SerialName(WebRoutes.GALLERY)
data object GalleryTab : TabScreen() {
    override val route = WebRoutes.GALLERY

    @Contextual
    override val label = Res.string.tab_gallery

    @Contextual
    override val icon = Icons.Default.PhotoLibrary
}

@Serializable
@SerialName(WebRoutes.GENERATE)
data class GenerateTab(
    @SerialName("prompt") val prompt: String? = null,
) : TabScreen() {
    override val route = WebRoutes.GENERATE

    @Contextual
    override val label = Res.string.tab_generate

    @Contextual
    override val icon = Icons.Default.Brush
}

@Serializable
@SerialName(WebRoutes.SETTINGS)
data object SettingsTab : TabScreen() {
    override val route = WebRoutes.SETTINGS

    @Contextual
    override val label = Res.string.tab_settings

    @Contextual
    override val icon = Icons.Default.Settings
}