package ai.create.photo.ui.main

import ai.create.photo.platform.getUrlHashManager
import ai.create.photo.ui.blog.BlogScreen
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController,
    initialPrompt: Prompt? = null,
    pendingPromptState: PromptState = PromptState(),
    currentGenerationId: String? = null,
    onPromptCleared: () -> Unit = {},
) {
    val state = viewModel.uiState
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Set initial prompt if provided
    LaunchedEffect(initialPrompt) {
        if (initialPrompt != null) {
            viewModel.putPrompt(initialPrompt)
        }
    }

    // Handle pending prompts from URL navigation
    LaunchedEffect(pendingPromptState.version) {
        if (pendingPromptState.prompt != null) {
            viewModel.putPrompt(pendingPromptState.prompt)
        }
    }

    val tabs = listOf(GalleryTab, GenerateTab, BlogTab, SettingsTab)

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
                        when (tab) {
                            is GenerateTab -> {
                                // Use stored generation ID if available, otherwise preserve existing query parameters
                                val newHash = if (currentGenerationId != null) {
                                    "#${MainRoutes.GENERATE}?id=$currentGenerationId"
                                } else {
                                    val currentHash = getUrlHashManager().getHash()
                                    val currentParams = if (currentHash.contains("?")) {
                                        currentHash.substringAfter("?")
                                    } else ""
                                    if (currentParams.isNotEmpty()) {
                                        "#${MainRoutes.GENERATE}?$currentParams"
                                    } else {
                                        "#${MainRoutes.GENERATE}"
                                    }
                                }
                                getUrlHashManager().setHash(newHash)
                                navController.navigateSingleTopTo(MainRoutes.GENERATE)
                            }

                            is GalleryTab -> {
                                if (currentDestination?.startsWith(tab.route) != true) {
                                    getUrlHashManager().setHash("#${MainRoutes.GALLERY}")
                                    navController.navigateSingleTopTo(tab.route)
                                }
                            }

                            is BlogTab -> {
                                if (currentDestination?.startsWith(tab.route) != true) {
                                    getUrlHashManager().setHash("#${MainRoutes.BLOG}")
                                    navController.navigateSingleTopTo(tab.route)
                                }
                            }

                            is SettingsTab -> {
                                if (currentDestination?.startsWith(tab.route) != true) {
                                    getUrlHashManager().setHash("#${MainRoutes.SETTINGS}")
                                    navController.navigateSingleTopTo(tab.route)
                                } else {
                                    viewModel.toggleResetSettingTab(true)
                                }
                            }
                        }
                    }
                )
            }
        }
    ) {
        val trainAiModel = {
            getUrlHashManager().setHash("#${MainRoutes.GALLERY}")
            viewModel.toggleOpenUploads(true)
            navController.navigateSingleTopTo(MainRoutes.GALLERY)
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
                        getUrlHashManager().setHash("#${MainRoutes.GENERATE}")
                        navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        if (prompt != null) viewModel.putPrompt(prompt)
                    },
                    openUploads = state.openUploads,
                    openCreations = state.openCreations,
                )

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
                        getUrlHashManager().setHash("#${MainRoutes.GALLERY}")
                        navController.navigateSingleTopTo(MainRoutes.GALLERY)
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt,
                    onPromptCleared = onPromptCleared,
                )
                LaunchedEffect(state.putPrompt) {
                    if (state.putPrompt != null) {
                        viewModel.putPrompt(null)
                    }
                }
            }
            composable<BlogTab> {
                BlogScreen()
            }
            
            composable<SettingsTab> {
                SettingsScreen(
                    trainAiModel = trainAiModel,
                    openGenerateTab = {
                        getUrlHashManager().setHash("#${MainRoutes.GENERATE}")
                        navController.navigateSingleTopTo(MainRoutes.GENERATE)
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

    LaunchedEffect(state.resetSettingTab) {
        if (state.resetSettingTab) {
            viewModel.toggleResetSettingTab(false)
        }
    }
}