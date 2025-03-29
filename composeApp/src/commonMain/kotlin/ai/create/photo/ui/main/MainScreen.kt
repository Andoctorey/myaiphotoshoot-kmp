package ai.create.photo.ui.main

import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.GenerationIcon
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import ai.create.photo.ui.settings.SettingsScreen
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import co.touchlab.kermit.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.tab_gallery
import photocreateai.composeapp.generated.resources.tab_generate
import photocreateai.composeapp.generated.resources.tab_settings

object WebRoutes {
    const val GALLERY = "gallery"
    const val GENERATE = "generate"
    const val SETTINGS = "settings"
}

interface TabScreen {
    val route: String
    val label: StringResource
    val icon: ImageVector
}

@Serializable
@SerialName(WebRoutes.GALLERY)
data object GalleryTab : TabScreen {
    override val route = WebRoutes.GALLERY
    override val label = Res.string.tab_gallery
    override val icon = Icons.Default.PhotoLibrary
}

@Serializable
@SerialName(WebRoutes.GENERATE)
data object GenerateTab : TabScreen {
    override val route = WebRoutes.GENERATE
    override val label = Res.string.tab_generate
    override val icon = Icons.Default.Brush
}

@Serializable
@SerialName(WebRoutes.SETTINGS)
data object SettingsTab : TabScreen {
    override val route = WebRoutes.SETTINGS
    override val label = Res.string.tab_settings
    override val icon = Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController,
) {
    val state = viewModel.uiState
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    Logger.i("currentDestination: $currentDestination")

    val tabs = listOf(GalleryTab, GenerateTab, SettingsTab)

    NavigationSuiteScaffold(
        modifier = Modifier.widthIn(min = 200.dp),
        navigationSuiteItems = {
            tabs.forEach { tab ->
                item(
                    icon = {
                        if (tab == GalleryTab) {
                            GenerationIcon(
                                generationsInProgress = state.generationsInProgress,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.label)
                            )
                        }
                    },
                    label = { Text(stringResource(tab.label)) },
                    selected = tab.route == currentDestination,
                    onClick = {
                        if (tab.route != currentDestination) {
                            navController.navigate(tab) {
                                popUpTo(GalleryTab) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else if (tab == SettingsTab) {
                            viewModel.toggleResetSettingTab(true)
                        }
                    }
                )
            }
        }
    ) {
        val trainAiModel = {
            navController.navigate(GenerateTab)
            viewModel.toggleOpenUploads(true)
        }

        NavHost(
            navController = navController,
            startDestination = GalleryTab,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            },
        ) {
            composable<GalleryTab> {
                GalleryScreen(
                    generationsInProgress = state.generationsInProgress,
                    openGenerateTab = { prompt ->
                        navController.navigate(GenerateTab)
                        if (prompt != null) viewModel.putPrompt(prompt)
                    },
                    openTopUpTab = {
                        navController.navigate(SettingsTab)
                    },
                    openUploads = state.openUploads,
                    openCreations = state.openCreations,
                )
            }
            composable<GenerateTab> {
                GenerateScreen(
                    trainAiModel = trainAiModel,
                    generationsInProgress = state.generationsInProgress,
                    onGenerate = { trainingId, prompt, parentGenerationId, photosToGenerate ->
                        viewModel.generatePhoto(
                            trainingId,
                            prompt,
                            parentGenerationId,
                            photosToGenerate
                        )
                    },
                    openCreations = {
                        navController.navigate(GalleryTab)
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt
                )
            }
            composable<SettingsTab> {
                SettingsScreen(
                    trainAiModel = trainAiModel,
                    openGenerateTab = {
                        navController.navigate(GenerateTab)
                    },
                    goToRootScreen = state.resetSettingTab
                )
            }
        }
    }

    if (state.errorPopup != null) {
        ErrorPopup(state.errorPopup) {
            viewModel.hideErrorPopup()
        }
    }

    LaunchedEffect(state.openUploads) {
        if (state.openUploads) {
            viewModel.toggleOpenUploads(false)
        }
    }

    LaunchedEffect(state.openCreations) {
        if (state.openCreations) {
            viewModel.toggleOpenCreations(false)
        }
    }

    LaunchedEffect(state.putPrompt) {
        if (state.putPrompt != null) {
            viewModel.putPrompt(null)
        }
    }

    LaunchedEffect(state.resetSettingTab) {
        if (state.resetSettingTab) {
            viewModel.toggleResetSettingTab(false)
        }
    }
}