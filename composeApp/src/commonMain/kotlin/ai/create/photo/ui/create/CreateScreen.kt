package ai.create.photo.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.*

@Composable
fun CreateScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val launcher = rememberFilePickerLauncher(
            title = stringResource(Res.string.add_your_photos),
            type = PickerType.Image,
            mode = PickerMode.Multiple()
        ) { files ->
            Logger.i { "Selected files: ${files?.joinToString { it.name }}" }
        }

        Button(onClick = { launcher.launch() }) {
            Text(
                text = stringResource(Res.string.add_your_photos),
                textAlign = TextAlign.Center,
            )
        }
    }
}