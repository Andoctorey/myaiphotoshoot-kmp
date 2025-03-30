package ai.create.photo.ui.main

import ai.create.photo.data.model.Base64String
import ai.create.photo.data.model.parse
import ai.create.photo.data.model.toBase64String
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.GenerationIcon
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import ai.create.photo.ui.generate.Prompt
import ai.create.photo.ui.settings.SettingsScreen
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource

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

    val tabs = listOf(GalleryTab, GenerateTab(), SettingsTab)

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
                    selected = currentDestination?.startsWith(tab.route) == true,
                    onClick = {
                        if (tab.route != currentDestination) {
                            navController.navigateSingleTopTo(tab.route)
                        } else if (tab == SettingsTab) {
                            viewModel.toggleResetSettingTab(true)
                        }
                    }
                )
            }
        }
    ) {
        val trainAiModel = {
            viewModel.toggleOpenUploads(true)
            navController.navigateSingleTopTo(MainRoutes.GENERATE)
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
                        navController.navigateSingleTopTo(
                            MainRoutes.GENERATE + "?prompt=${prompt?.toBase64String()?.value}"
                        )
                    },
                    openTopUpTab = { navController.navigateSingleTopTo(MainRoutes.SETTINGS) },
                    openUploads = state.openUploads,
                    openCreations = state.openCreations,
                )
            }
            composable(
                route = "${MainRoutes.GENERATE}?prompt={prompt}",
                arguments = listOf(
                    navArgument("prompt") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry: NavBackStackEntry ->
                val generateTab = backStackEntry.toRoute<GenerateTab>()
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
                        viewModel.toggleOpenCreations(true)
                        navController.navigateSingleTopTo(MainRoutes.GALLERY)
                    },
                    prompt = generateTab.promptBase64?.let { Base64String(it).parse<Prompt>() },
                )
            }
            composable<SettingsTab> {
                SettingsScreen(
                    trainAiModel = trainAiModel,
                    openGenerateTab = { navController.navigateSingleTopTo(MainRoutes.GENERATE) },
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

    LaunchedEffect(state.resetSettingTab) {
        if (state.resetSettingTab) {
            viewModel.toggleResetSettingTab(false)
        }
    }
}


fun NavHostController.navigateSingleTopTo(route: String) = navigate(route) {
    // Pop up to the start destination of the graph to
    // avoid building up a large stack of destinations
    // on the back stack as users select items
    popUpTo(graph.findStartDestination().route!!) {
        saveState = true
    }
    // Avoid multiple copies of the same destination when
    // re-selecting the same item
    launchSingleTop = true
    // Restore state when re-selecting a previously selected item
    restoreState = true
}