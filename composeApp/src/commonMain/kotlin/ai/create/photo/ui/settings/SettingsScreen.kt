package ai.create.photo.ui.settings

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.settings.SettingsUiState.Item
import ai.create.photo.ui.settings.balance.BalanceScreen
import ai.create.photo.ui.settings.login.LoginScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.balance


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
                Screen(
                    email = state.email,
                    balance = state.balance,
                    items = state.items,
                    savedDestination = state.currentDestination,
                    onSaveDestination = viewModel::saveDestination
                )
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
    email: String?,
    balance: String,
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
                    email = email,
                    balance = balance,
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
    email: String?,
    balance: String,
    expanded: Boolean,
    items: List<Item>,
    onItemClick: (Item) -> Unit,
) {
    LazyColumn(
        modifier = (if (expanded) Modifier else Modifier.wrapContentHeight())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            item {
                when (item) {
                    is SettingsUiState.SpacerItem -> {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    is SettingsUiState.DetailedItem -> {
                        OutlinedButton(
                            onClick = { onItemClick(item) },
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.icon.name,
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = when (item) {
                                    is SettingsUiState.LoginItem -> email
                                        ?.takeIf { it.isNotBlank() }
                                        ?: stringResource(item.nameRes)

                                    is SettingsUiState.BalanceItem ->
                                        stringResource(Res.string.balance, balance)
                                            .takeIf { balance != "0" }
                                            ?: stringResource(item.nameRes)
                                    is SettingsUiState.PlaceholderItem -> stringResource(item.nameRes)
                                },
                                fontSize = 16.sp,
                            )
                        }
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
                    is SettingsUiState.BalanceItem -> BalanceScreen()
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
