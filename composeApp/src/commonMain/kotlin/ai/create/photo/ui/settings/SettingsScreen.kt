package ai.create.photo.ui.settings

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.settings.SettingsUiState.Item
import ai.create.photo.ui.settings.login.LoginScreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
                Screen(state.items, state.currentDestination, viewModel::saveDestination)
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
private fun Screen(
    items: List<Item>,
    savedDestination: Item?,
    onSaveDestination: (Item?) -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Item>()
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(savedDestination, hasNavigated) {
        if (savedDestination != null && !hasNavigated) {
            Logger.i("Navigate to savedDestination: $savedDestination")
            when (savedDestination) {
                is SettingsUiState.DetailedItem ->
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, savedDestination)

                is SettingsUiState.SpacerItem -> {}
            }
            hasNavigated = true
        }
    }
    val expanded = navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
            && navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded
    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(modifier = Modifier) {
                SettingsItems(
                    expanded = expanded,
                    items = items,
                    onItemClick = { item ->
                        Logger.i("Navigate to: $item")
                        onSaveDestination(item)
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
                                onSaveDestination(null)
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
    expanded: Boolean,
    items: List<Item>,
    onItemClick: (Item) -> Unit,
) {
    LazyColumn(
        modifier = if (expanded) Modifier else Modifier.wrapContentHeight(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items.forEach { item ->
            item {
                when (item) {
                    is SettingsUiState.SpacerItem -> {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    is SettingsUiState.DetailedItem -> {
                        ListItem(
                            modifier = Modifier.clickable { onItemClick(item) },
                            headlineContent = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(item.nameRes),
                                    textAlign = TextAlign.Center
                                )
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
                    .padding(24.dp),
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
