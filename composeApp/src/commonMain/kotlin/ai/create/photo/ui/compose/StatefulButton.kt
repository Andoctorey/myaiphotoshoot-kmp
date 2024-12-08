import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun StatefulButton(
    text: String,
    isLoading: Boolean,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(targetState = isLoading) { isLoading ->
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onSurface,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        } else {
            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = modifier,
            ) {
                Text(
                    text = text,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}