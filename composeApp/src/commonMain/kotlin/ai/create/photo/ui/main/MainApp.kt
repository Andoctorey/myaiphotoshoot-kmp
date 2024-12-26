package ai.create.photo.ui.main

import ai.create.photo.ui.add_photos.AddScreen
import ai.create.photo.ui.gallery.GalleryScreen
import ai.create.photo.ui.generate.GenerateScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    AppNavigationRoutes.valueOf(
        backStackEntry?.destination?.route ?: AppNavigationRoutes.TAB1.name
    )

    val state = viewModel.uiState
    var currentDestination by rememberSaveable { mutableStateOf(AppNavigationRoutes.TAB1) }
    NavigationSuiteScaffold(
        modifier = Modifier.widthIn(min = 200.dp),
        navigationSuiteItems = {
            AppNavigationRoutes.entries.forEach {
                item(
                    icon = {
                        if (it == AppNavigationRoutes.TAB3 && state.generationsInProgress != 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(24.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Text has weird padding, waiting for fix style = TextStyle( platformStyle = PlatformTextStyle( includeFontPadding = false, ), ),
//                                Text(
//                                    modifier = Modifier .padding(0.dp) .size(14.dp).background(Color.Red),
//                                    style = TextStyle( platformStyle = PlatformTextStyle( includeFontPadding = false, ), ),
//                                    text = state.generationsInProgress.toString(),
//                                    textAlign = TextAlign.Center,
//                                    maxLines = 1,
//                                    fontSize = 8.sp,
//                                )
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
        when (currentDestination) {
            AppNavigationRoutes.TAB1 -> AddScreen(
                openCreatePhotosScreen = { currentDestination = AppNavigationRoutes.TAB2 }
            )

            AppNavigationRoutes.TAB2 -> GenerateScreen {
                viewModel.onGenerationsInProgressChanged(it)
            }

            AppNavigationRoutes.TAB3 -> {
                val generationInProgress by remember {
                    derivedStateOf { state.generationsInProgress != 0 }
                }
                GalleryScreen(generationInProgress = generationInProgress)
            }

            AppNavigationRoutes.TAB4 -> {}
        }
    }

}