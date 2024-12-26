package ai.create.photo.ui.settings

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.settings.login.LoginScreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() },
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val state = viewModel.uiState

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
                Screen(state.items)
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun Screen(items: List<SettingsUiState.Item>) {
    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsUiState.Item>()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                SettingsItems(
                    items = items,
                    onItemClick = { item ->
                        Logger.i("Navigate to: $item")
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                    },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let {
                    when (it) {
                        is SettingsUiState.SpacerItem -> {}
                        is SettingsUiState.DetailedItem -> {
                            SettingsDetails(
                                expanded = navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded,
                                item = it
                            ) {
                                navigator.navigateBack()
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun SettingsItems(
    items: List<SettingsUiState.Item>,
    onItemClick: (SettingsUiState.Item) -> Unit,
) {
    LazyColumn {
        items.forEach { item ->
            item {
                when (item) {
                    is SettingsUiState.SpacerItem -> {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is SettingsUiState.DetailedItem -> {
                        ListItem(
                            modifier = Modifier.clickable {
                                onItemClick(item)
                            },
                            headlineContent = {
                                Text(text = stringResource(item.nameRes))
                            },
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetails(
    expanded: Boolean,
    item: SettingsUiState.DetailedItem,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            if (!expanded) {
                TopAppBar(
                    title = {
                        Text(text = stringResource(item.nameRes))
                    },
                    windowInsets = WindowInsets(0.dp),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
//                    scrollBehavior =  TODO
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                when (item) {
                    is SettingsUiState.LoginItem -> LoginScreen()
                    is SettingsUiState.PlaceholderItem -> {
                        Text(
                            text = "TODO: ${stringResource(item.nameRes)}",
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
}
