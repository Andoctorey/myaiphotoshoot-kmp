package ai.create.photo.ui.main


import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.ui.article.ArticleScreen
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.savedstate.read
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController,
) {
    // Helper function for tab navigation with state preservation
    fun navigateToTab(route: String) {
        navController.navigate(route) {
            // Pop up to the start destination but save state
            popUpTo(navController.graph.findStartDestination().route!!) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = true
        }
    }
    val state = viewModel.uiState
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

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
                    selected = when (tab) {
                        is BlogTab -> currentDestination?.startsWith(tab.route) == true ||
                                currentDestination?.startsWith(MainRoutes.ARTICLE) == true

                        else -> currentDestination?.startsWith(tab.route) == true
                    },
                    onClick = {
                        when (tab) {
                            is GenerateTab -> {
                                navController.navigateSingleTopTo(MainRoutes.GENERATE)
                            }

                            is GalleryTab -> {
                                navigateToTab(tab.route)
                            }

                            is BlogTab -> {
                                navigateToTab(tab.route)
                            }

                            is SettingsTab -> {
                                if (currentDestination?.startsWith(tab.route) == true) {
                                    viewModel.toggleResetSettingTab(true)
                                } else {
                                    navigateToTab(tab.route)
                                }
                            }
                        }
                    }
                )
            }
        }
    ) {
        val trainAiModel = {
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
                        easing = LinearEasing,
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing,
                    )
                )
            },
        ) {
            composable<GalleryTab> {
                GalleryScreen(
                    generationsInProgress = state.generationsInProgress,
                    openGenerateTab = { prompt ->
                        if (prompt?.generationId != null) {
                            navController.navigate("${MainRoutes.GENERATE}/${prompt.generationId}")
                        } else {
                            navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        }
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
                        navController.navigateSingleTopTo(MainRoutes.GALLERY)
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt,
                )
                LaunchedEffect(state.putPrompt) {
                    if (state.putPrompt != null) {
                        viewModel.putPrompt(null)
                    }
                }
            }

            composable(
                route = "${MainRoutes.GENERATE}/{generationId}",
            ) { backStackEntry ->
                val generationId = backStackEntry.arguments!!.read { getString("generationId") }
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
                        navController.navigateSingleTopTo(MainRoutes.GALLERY)
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt,
                )
                LaunchedEffect(generationId) {
                    if (generationId.isNotEmpty() && state.putPrompt?.generationId != generationId) {
                        try {
                            val generation = SupabaseFunction.getGeneration(generationId)
                            if (generation != null) {
                                val prompt = Prompt(
                                    generationId = generation.id,
                                    text = generation.prompt,
                                    url = generation.imageUrl
                                )
                                viewModel.putPrompt(prompt)
                            }
                        } catch (e: Exception) {
                            Logger.e("Failed to load generation data for ID: $generationId", e)
                        }
                    }
                }
                LaunchedEffect(state.putPrompt) {
                    if (state.putPrompt != null) {
                        viewModel.putPrompt(null)
                    }
                }
            }
            composable<BlogTab> {
                BlogScreen(
                    openGenerateTab = { prompt ->
                        if (prompt.generationId.isNotEmpty()) {
                            navController.navigate("${MainRoutes.GENERATE}/${prompt.generationId}")
                        } else {
                            navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        }
                        viewModel.putPrompt(prompt)
                    },
                    onArticleClick = { postId ->
                        navController.navigate("${MainRoutes.ARTICLE}/$postId")
                    }
                )
            }

            composable(
                route = "${MainRoutes.ARTICLE}/{postId}",
            ) { backStackEntry ->
                val postId = backStackEntry.arguments!!.read { getString("postId") }
                ArticleScreen(
                    postId = postId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    openGenerateTab = { prompt ->
                        if (prompt.generationId.isNotEmpty()) {
                            navController.navigate("${MainRoutes.GENERATE}/${prompt.generationId}")
                        } else {
                            navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        }
                        viewModel.putPrompt(prompt)
                    }
                )
            }

            composable<SettingsTab> {
                SettingsScreen(
                    trainAiModel = trainAiModel,
                    openGenerateTab = {
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