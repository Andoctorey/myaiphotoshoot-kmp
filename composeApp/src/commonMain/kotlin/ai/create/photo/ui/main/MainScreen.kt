package ai.create.photo.ui.main

import ai.create.photo.platform.BackHandler
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.GenerationIcon
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import ai.create.photo.ui.settings.SettingsScreen
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController = rememberNavController(),
    onExitApp: () -> Unit = {}
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    AppNavigationRoutes.valueOf(
        backStackEntry?.destination?.route ?: AppNavigationRoutes.TAB_1_GALLERY.name
    )

    val state = viewModel.uiState
    var currentDestination by rememberSaveable { mutableStateOf(AppNavigationRoutes.TAB_1_GALLERY) }

    BackHandler {
        if (currentDestination != AppNavigationRoutes.TAB_1_GALLERY) {
            currentDestination = AppNavigationRoutes.TAB_1_GALLERY
        } else {
            onExitApp()
        }
    }

    LaunchedEffect(currentDestination) {
        Logger.i("change tab: ${currentDestination.name}")
    }
    NavigationSuiteScaffold(
        modifier = Modifier.widthIn(min = 200.dp),
        navigationSuiteItems = {
            AppNavigationRoutes.entries.forEach {
                item(
                    icon = {
                        if (it == AppNavigationRoutes.TAB_1_GALLERY) {
                            GenerationIcon(
                                generationsInProgress = state.generationsInProgress,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.label)
                            )
                        }
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = {
                        if (currentDestination == it) {
                            if (it == AppNavigationRoutes.TAB_3_SETTINGS) {
                                viewModel.toggleResetSettingTab(true)
                            }
                        }
                        currentDestination = it
                    }
                )
            }
        }
    ) {

        val trainAiModel = {
            currentDestination = AppNavigationRoutes.TAB_1_GALLERY
            viewModel.toggleOpenUploads(true)
        }

        when (currentDestination) {
            AppNavigationRoutes.TAB_1_GALLERY -> GalleryScreen(
                generationsInProgress = state.generationsInProgress,
                openGenerateTab = { prompt ->
                    currentDestination = AppNavigationRoutes.TAB_2_GENERATE
                    if (prompt != null) viewModel.putPrompt(prompt)
                },
                openTopUpTab = {
                    currentDestination = AppNavigationRoutes.TAB_3_SETTINGS
                },
                openUploads = state.openUploads,
                openCreations = state.openCreations,
            )

            AppNavigationRoutes.TAB_2_GENERATE -> {
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
                        currentDestination = AppNavigationRoutes.TAB_1_GALLERY
                        viewModel.toggleOpenCreations(true)
                    },
                    prompt = state.putPrompt
                )
            }


            AppNavigationRoutes.TAB_3_SETTINGS -> SettingsScreen(
                trainAiModel = trainAiModel,
                openGenerateTab = {
                    currentDestination = AppNavigationRoutes.TAB_2_GENERATE
                },
                goToRootScreen = state.resetSettingTab
            )
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