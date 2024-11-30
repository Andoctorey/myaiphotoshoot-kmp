package ai.create.photo.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.back_button

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

    @Composable
    fun TopAppBar(
        currentScreen: AppNavigationRoutes,
        canNavigateBack: Boolean,
        navigateUp: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        TopAppBar(
            title = { Text(stringResource(currentScreen.label)) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = modifier,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_button)
                        )
                    }
                }
            }
        )
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppNavigationRoutes.TAB1) }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppNavigationRoutes.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.label)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppNavigationRoutes.TAB1 -> {
                Box(Modifier.fillMaxSize()) {
                    FloatingActionButton(
                        onClick = {
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            AppNavigationRoutes.TAB2 -> {}
            AppNavigationRoutes.TAB3 -> {}
        }
    }

}