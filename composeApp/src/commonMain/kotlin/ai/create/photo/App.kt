package ai.create.photo

import ai.create.photo.data.logger.LoggerInitializer
import ai.create.photo.ui.main.MainScreen
import ai.create.photo.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(Unit) {
        LoggerInitializer.initLogger()
    }

    AppTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (isSystemInDarkTheme()) {
                        // wtf is tabs color in dark mode?
                        MaterialTheme.colorScheme.inverseOnSurface
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    }
                )
                .navigationBarsPadding()
        ) {
            MainScreen(navController = navController)
        }
    }
}