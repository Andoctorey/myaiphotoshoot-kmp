package ai.create.photo.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.delete_photo_confirmation
import photocreateai.composeapp.generated.resources.save
import photocreateai.composeapp.generated.resources.share

@Composable
fun <Item> PhotoDropMenu(
    modifier: Modifier = Modifier,
    item: Item,
    onDelete: (Item) -> Unit,
    onShare: (Item) -> Unit,
    onSave: (Item) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDeletePopup by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), shape = CircleShape)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = Icons.Default.MoreVert.name)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.save)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = Icons.Default.Save.name
                    )
                },
                onClick = { onSave(item) }
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.share)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = Icons.Default.Share.name
                    )
                },
                onClick = { onShare.invoke(item) }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = Icons.Default.Delete.name,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = { showConfirmDeletePopup = true }
            )

            if (showConfirmDeletePopup) {
                ConfirmationPopup(
                    icon = Icons.Default.Delete,
                    message = stringResource(Res.string.delete_photo_confirmation),
                    confirmButton = stringResource(Res.string.delete),
                    onConfirm = { onDelete(item) },
                    onDismiss = { showConfirmDeletePopup = false }
                )
            }
        }
    }
}