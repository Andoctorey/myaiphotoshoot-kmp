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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.balance


@Preview
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() },
    trainAiModel: () -> Unit,
    openGenerateTab: () -> Unit,
    goToRootScreen: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val state = viewModel.uiState

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.loadProfile()
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

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
                    isBalanceLoading = state.isBalanceLoading,
                    items = state.items,
                    savedDestination = state.currentDestination,
                    onSaveDestination = viewModel::saveDestination,
                    goToRootScreen = goToRootScreen,
                    social = viewModel::social,
                    support = viewModel::support,
                    googlePlay = viewModel::googlePlay,
                    appStore = viewModel::appStore,
                    trainAiModel = trainAiModel,
                    openGenerateTab = openGenerateTab,
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
    isBalanceLoading: Boolean,
    items: List<Item>,
    savedDestination: Item?,
    onSaveDestination: (Item?) -> Unit,
    goToRootScreen: Boolean,
    social: () -> Unit,
    support: () -> Unit,
    googlePlay: () -> Unit,
    appStore: () -> Unit,
    trainAiModel: () -> Unit,
    openGenerateTab: () -> Unit,
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

    LaunchedEffect(goToRootScreen) {
        if (goToRootScreen) {
            onSaveDestination(null)
            while (navigator.canNavigateBack()) {
                navigator.navigateBack()
            }
        }
    }

    val scope = rememberCoroutineScope()
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
                    isBalanceLoading = isBalanceLoading,
                    expanded = expanded,
                    items = items,
                    onItemClick = { item ->
                        when (item) {
                            is SettingsUiState.SocialItem -> {
                                social()
                                return@SettingsItems
                            }

                            is SettingsUiState.SupportItem -> {
                                support()
                                return@SettingsItems
                            }

                            is SettingsUiState.GooglePlayItem -> {
                                googlePlay()
                                return@SettingsItems
                            }

                            is SettingsUiState.AppStoreItem -> {
                                appStore()
                                return@SettingsItems
                            }

                            else -> {
                                Logger.i("Navigate to: $item")
                                onSaveDestination(item)
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                                }
                            }
                        }
                    },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.contentKey?.let {
                    when (it) {
                        is SettingsUiState.SpacerItem -> {}
                        is SettingsUiState.DetailedItem -> {
                            SettingsDetails(
                                expanded = navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded,
                                item = it,
                                openGenerateTab = openGenerateTab,
                                trainAiModel = trainAiModel,
                            ) {
                                Logger.i("Back clicked")
                                onSaveDestination(null)
                                scope.launch {
                                    navigator.navigateBack()
                                }
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
    isBalanceLoading: Boolean,
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
                            if (item is SettingsUiState.BalanceItem && isBalanceLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.icon.name,
                                )
                            }

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

                                    else -> stringResource(item.nameRes)
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
    trainAiModel: () -> Unit,
    openGenerateTab: () -> Unit,
    onBackClick: () -> Unit,
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                when (item) {
                    is SettingsUiState.LoginItem -> LoginScreen(onBackClick = onBackClick)

                    is SettingsUiState.BalanceItem -> BalanceScreen(
                        onBackClick = onBackClick,
                        trainAiModel = trainAiModel,
                        openGenerateTab = openGenerateTab,
                    )

                    is SettingsUiState.SocialItem -> {}
                    is SettingsUiState.SupportItem -> {}
                    is SettingsUiState.GooglePlayItem -> {}
                    is SettingsUiState.AppStoreItem -> {}
                }
            }
        }
    )
}
