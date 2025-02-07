package ai.create.photo.ui.main

import ai.create.photo.platform.BackHandler
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import ai.create.photo.ui.settings.SettingsScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        if (it == AppNavigationRoutes.TAB_1_GALLERY && state.generationsInProgress != 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(bottom = 1.dp),
                                    text = state.generationsInProgress.toString(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    fontSize = 11.sp,
                                    style = TextStyle(
                                        lineHeight = 11.sp,
                                    ),
                                )
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.label)
                            )
                        }
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
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
                    if (prompt.isNotEmpty()) {
                        viewModel.putPrompt(prompt)
                    }
                },
                openUploads = state.openUploads,
            )

            AppNavigationRoutes.TAB_2_GENERATE -> {
                GenerateScreen(
                    trainAiModel = trainAiModel,
                    onGenerate = { trainingId, prompt, photosToGenerate ->
                        viewModel.generatePhoto(trainingId, prompt, photosToGenerate)
                    },
                    prompt = state.putPrompt
                )
            }


            AppNavigationRoutes.TAB_3_SETTINGS -> SettingsScreen(
                trainAiModel = trainAiModel,
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

    LaunchedEffect(state.putPrompt) {
        if (state.putPrompt.isNotEmpty()) {
            viewModel.putPrompt("")
        }
    }

}