package ai.create.photo.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenerationIcon(
    generationsInProgress: Int,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    if (generationsInProgress != 0) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 1.dp),
                text = generationsInProgress.toString(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontSize = 11.sp,
                style = TextStyle(
                    lineHeight = 11.sp,
                ),
            )
            CircularProgressIndicator(
                color = color,
                strokeWidth = 2.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        val icon = Icons.Default.PhotoLibrary
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = icon.name,
        )
    }
}