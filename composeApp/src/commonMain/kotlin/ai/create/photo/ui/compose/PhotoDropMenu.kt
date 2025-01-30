package ai.create.photo.ui.compose

import ai.create.photo.ui.gallery.creations.CreationsUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.*
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.delete_photo_confirmation
import photocreateai.composeapp.generated.resources.make_private
import photocreateai.composeapp.generated.resources.make_public
import photocreateai.composeapp.generated.resources.share

@Composable
fun <Item> PhotoDropMenu(
    modifier: Modifier = Modifier,
    item: Item,
    onDownload: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onShare: (Item) -> Unit,
    onTogglePublic: (Item) -> Unit,
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
                text = { Text(text = stringResource(Res.string.download)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = Icons.Default.Download.name
                    )
                },
                onClick = {
                    expanded = false
                    onDownload(item)
                }
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.share)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = Icons.Default.Share.name
                    )
                },
                onClick = {
                    expanded = false
                    onShare.invoke(item)
                }
            )

            val isPublic = (item as? CreationsUiState.Photo)?.isPublic == true
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (isPublic) Res.string.make_private
                            else Res.string.make_public
                        )
                    )
                },
                leadingIcon = {
                    val icon =
                        if (isPublic) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                    )
                },
                onClick = {
                    expanded = false
                    onTogglePublic(item)
                }
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
                onClick = {
                    showConfirmDeletePopup = true
                }
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