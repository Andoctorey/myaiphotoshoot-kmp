package ai.create.photo.ui.compose

import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.ui.gallery.creations.CreationsUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.*

@Composable
fun <Item> PhotoDropMenu(
    modifier: Modifier = Modifier,
    item: Item,
    onDownload: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onShare: (Item) -> Unit,
    onTogglePublic: (Item) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDeletePopup by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    val (shareIcon, shareText) = when(platform().platform) {
        Platforms.ANDROID -> Icons.Default.Share to stringResource(Res.string.share)
        Platforms.IOS -> Icons.Default.Share to stringResource(Res.string.share)
        Platforms.DESKTOP -> Icons.Default.Link to stringResource(Res.string.copy_link)
        Platforms.WEB_DESKTOP -> Icons.Default.Link to stringResource(Res.string.copy_link)
        Platforms.WEB_MOBILE -> Icons.Default.Link to stringResource(Res.string.copy_link)
    }

    val coroutineScope = rememberCoroutineScope()

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
                    if (!isDownloaded) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = Icons.Default.Download.name
                        )
                    } else {
                        Icon(
                            Icons.Default.DownloadDone,
                            contentDescription = Icons.Default.DownloadDone.name
                        )
                    }
                },
                onClick = {
                    expanded = false
                    isDownloaded = true
                    onDownload(item)
                }
            )

            DropdownMenuItem(
                text = { Text(text = shareText) },
                leadingIcon = { Icon(shareIcon, contentDescription = shareIcon.name) },
                onClick = {
                    if (platform().platform == Platforms.DESKTOP) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Link was copied")
                        }
                    }
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