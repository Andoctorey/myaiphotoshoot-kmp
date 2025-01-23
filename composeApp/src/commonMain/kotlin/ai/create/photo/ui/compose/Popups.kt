package ai.create.photo.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.cancel

@Composable
fun ErrorPopup(e: Throwable, onDismiss: () -> Unit) {
    AlertDialog(
        icon = { Icon(Icons.Default.Error, contentDescription = "error") },
        onDismissRequest = onDismiss,
        text = { Text(text = e.getFriendlyError()) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// don't add title, it looks ugly
@Composable
fun InfoPopup(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        icon = { Icon(Icons.Default.Info, contentDescription = message) },
        onDismissRequest = onDismiss,
        text = { Text(text = message, fontSize = 16.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun ConfirmationPopup(
    icon: ImageVector,
    message: String,
    confirmButton: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = message) },
        onDismissRequest = onDismiss,
        text = {
            Text(text = message)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButton)
            }
        }
    )
}