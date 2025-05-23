package ai.create.photo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        setContent {
            enableEdgeToEdge()
            UpdateSystemBarsColor()
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
fun UpdateSystemBarsColor() {
    val view = LocalView.current
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
//    val color = if (isDarkTheme) {
//        // Define your dark theme navigation bar color
//        Color.Black
//    } else {
//        // Define your light theme navigation bar color
//        Color.White
//    }

    SideEffect {
        val window = (context as Activity).window
//        window.navigationBarColor = color.toArgb() deprecated, use navigationBarsPadding()
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
            !isDarkTheme
    }
}