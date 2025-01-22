package ai.create.photo.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.options_description
import photocreateai.composeapp.generated.resources.search

@Composable
fun <DeleteItem>DropMenu(
    modifier: Modifier = Modifier,
    deletedItem: DeleteItem,
    onDeleteClicked: (DeleteItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd)) {
        IconButton(
            modifier = modifier
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape),
            onClick = {
                expanded = !expanded
            }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.options_description))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.search)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(Res.string.search)
                    )
                },
                onClick = {
                // create sealed class for all events or pass it as lamdas?
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.delete)) },
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.delete)) },
                onClick = {
                    onDeleteClicked(deletedItem)
                }
            )
        }
    }
}

//@Preview
//@Composable
//fun DropMenuPreview() {
//    DropMenu()
//}