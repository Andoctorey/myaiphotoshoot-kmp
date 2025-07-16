package ai.create.photo.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.tab_blog
import photocreateai.composeapp.generated.resources.tab_gallery
import photocreateai.composeapp.generated.resources.tab_generate
import photocreateai.composeapp.generated.resources.tab_settings

object MainRoutes {
    const val GALLERY = "gallery"
    const val GENERATE = "generate"
    const val BLOG = "blog"
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
    override val label = Res.string.tab_gallery
    override val icon = Icons.Default.PhotoLibrary
}

@Serializable
@SerialName(MainRoutes.GENERATE)
data object GenerateTab : TabScreen() {
    override val route = MainRoutes.GENERATE
    override val label = Res.string.tab_generate
    override val icon = Icons.Default.Brush
}

@Serializable
@SerialName(MainRoutes.BLOG)
data object BlogTab : TabScreen() {
    override val route = MainRoutes.BLOG
    override val label = Res.string.tab_blog
    override val icon = Icons.AutoMirrored.Filled.Article
}

@Serializable
@SerialName(MainRoutes.SETTINGS)
data object SettingsTab : TabScreen() {
    override val route = MainRoutes.SETTINGS
    override val label = Res.string.tab_settings
    override val icon = Icons.Default.Settings
}