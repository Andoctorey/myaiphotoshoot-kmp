package ai.create.photo.ui.main

import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.GenerationIcon
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import ai.create.photo.ui.settings.SettingsScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController,
) {
    val state = viewModel.uiState
    val tabLabels = AppNavigationRoutes.entries.associate { it to stringResource(it.label) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    Logger.i(navBackStackEntry?.destination?.route?.toString() ?: "")
    val currentDestination = tabLabels
        .filterValues { it == navBackStackEntry?.destination?.route }.keys
        .firstOrNull()
        ?: AppNavigationRoutes.TAB_1_GALLERY

    LaunchedEffect(currentDestination) {
        Logger.i("change tab: $currentDestination")
    }
    NavigationSuiteScaffold(
        modifier = Modifier.widthIn(min = 200.dp),
        navigationSuiteItems = {
            AppNavigationRoutes.entries.forEach { tab ->
                item(
                    icon = {
                        if (tab == AppNavigationRoutes.TAB_1_GALLERY) {
                            GenerationIcon(
                                generationsInProgress = state.generationsInProgress,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Icon(
                                tab.icon,
                                contentDescription = stringResource(tab.label)
                            )
                        }
                    },
                    label = { Text(stringResource(tab.label)) },
                    selected = tab == currentDestination,
                    onClick = {
                        if (currentDestination != tab) {
                            navController.navigate(tabLabels[tab]!!) {
                                popUpTo(tabLabels[AppNavigationRoutes.TAB_1_GALLERY]!!) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else if (tab == AppNavigationRoutes.TAB_3_SETTINGS) {
                            viewModel.toggleResetSettingTab(true)
                        }
                    }
                )
            }
        }
    ) {
        val trainAiModel = {
            navController.navigate(tabLabels[AppNavigationRoutes.TAB_2_GENERATE]!!)
            viewModel.toggleOpenUploads(true)
        }

        NavHost(
            navController = navController,
            startDestination = tabLabels[AppNavigationRoutes.TAB_1_GALLERY]!!,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(tabLabels[AppNavigationRoutes.TAB_1_GALLERY]!!) {
                GalleryScreen(
                    generationsInProgress = state.generationsInProgress,
                    openGenerateTab = { prompt ->
                        navController.navigate(tabLabels[AppNavigationRoutes.TAB_2_GENERATE]!!)
                        if (prompt != null) viewModel.putPrompt(prompt)
                    },
                    openTopUpTab = {
                        navController.navigate(tabLabels[AppNavigationRoutes.TAB_3_SETTINGS]!!)
                    },
                    openUploads = state.openUploads,
                    openCreations = state.openCreations,
                )
            }
            composable(tabLabels[AppNavigationRoutes.TAB_2_GENERATE]!!) {
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
                        navController.navigate(tabLabels[AppNavigationRoutes.TAB_1_GALLERY]!!)
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt
                )
            }
            composable(tabLabels[AppNavigationRoutes.TAB_3_SETTINGS]!!) {
                SettingsScreen(
                    trainAiModel = trainAiModel,
                    openGenerateTab = {
                        navController.navigate(tabLabels[AppNavigationRoutes.TAB_2_GENERATE]!!)
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