package ai.create.photo.ui.compose

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ErrorPopup(e: Throwable, onDismiss: () -> Unit) {
    AlertDialog(
        icon = { Icon(Icons.Default.Error, contentDescription = "error") },
        onDismissRequest = onDismiss,
        text = {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                text = e.getFriendlyError()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}