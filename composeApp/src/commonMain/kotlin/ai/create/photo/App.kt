package ai.create.photo

import ai.create.photo.data.logger.LoggerInitializer
import ai.create.photo.ui.generate.Prompt
import ai.create.photo.ui.main.MainScreen
import ai.create.photo.ui.main.PromptState
import ai.create.photo.ui.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController(),
    initialPrompt: Prompt? = null,
    pendingPromptState: PromptState = PromptState(),
    currentGenerationId: String? = null,
    onPromptCleared: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        LoggerInitializer.initLogger()
    }

    AppTheme {
        MainScreen(
            navController = navController,
            initialPrompt = initialPrompt,
            pendingPromptState = pendingPromptState,
            currentGenerationId = currentGenerationId,
            onPromptCleared = onPromptCleared
        )
    }
}