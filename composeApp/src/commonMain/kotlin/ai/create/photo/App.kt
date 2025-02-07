package ai.create.photo

import ai.create.photo.data.logger.LoggerInitializer
import ai.create.photo.ui.main.MainScreen
import ai.create.photo.ui.theme.AppTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(onExitApp: () -> Unit = {}) {
    LaunchedEffect(Unit) {
        LoggerInitializer.initLogger()
    }

    AppTheme {
        MainScreen(onExitApp = onExitApp)
    }
}