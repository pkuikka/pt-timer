package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.data.GlobalData
import com.example.pt_timer.data.TimerData

fun modelTypeAsString(modelType: Int): String {
    when (modelType) {
        1 -> return "F1B"
        2 -> return "F1A/H"
        3 -> return "P-30"
        4 -> return "E-36"
        5 -> return "F1Q"
        6 -> return "E-20"
    }
    return "Unknown"
}

@Composable
fun SettingsScreen(
    uiState: UiState,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPosition: (Int, String) -> Unit,
    onServoRange: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        RowWithText("Model type ${modelTypeAsString(uiState.timerData.modelType)}")
        RowWithField("Number of data lines", "${uiState.timerData.numberOfDataRows}")
        RowWithField("Critical voltage", "${uiState.timerData.batteryWarningVoltage}")
        RowWithField("Power-off delay after DT (seconds)", "${uiState.timerData.dtPowerDownDelay}")
        RowWithCheckBox("RDT enabled", value = true)
        RowWithCheckBox("Beep on tow", value = true)

        // --- Hook settings ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Hook settings",
            style = typography.titleSmall
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        RowWithText("Reverse switches")
        RowWithCheckBox("Tow", value = true)
        RowWithCheckBox("Latch", value = true)
        RowWithText("Hook type")
        RowWithCheckBox("Conventional", value = false)
        RowWithCheckBox("Re-latch", value = true)
        RowWithField("Re-latch critical time (seconds)", "${uiState.timerData.maxTimeForSkippingBunt/10}")

        // --- Advanced settings ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Adcanced timer settings",
            style = typography.titleMedium
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        RowWithText("Version ${uiState.timerData.timerVersion}")
        RowWithText("Maximum data lines ${uiState.timerData.maxDataRows}")
        RowWithText("Name label index ${uiState.timerData.firstIndexForDataSetName}")
        RowWithField("Wake up count", "${uiState.timerData.startUpCycleCount}")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clock speed",
                style = typography.bodyMedium
            )
            SettingsField(value = "${uiState.timerData.timerCalibrationInMilliseconds}", onValueChange = {}, onDoneAction = {})
            SettingsField(value = "${uiState.timerData.timerCalibrationInMicroseconds1}", onValueChange = {}, onDoneAction = {})
            SettingsField(value = "${uiState.timerData.timerCalibrationInMicroseconds2}", onValueChange = {}, onDoneAction = {})
        }
    }
}

@Composable
fun RowWithText(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typography.bodyMedium
        )
    }
}

@Composable
fun RowWithField(
    text: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typography.bodyMedium
        )
        SettingsField(value = value, onValueChange = {}, onDoneAction = {})
    }
}

@Composable
fun RowWithCheckBox(
    text: String,
    value: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val reversed = remember { mutableStateOf(value) }
        Checkbox(
            checked = value,
            onCheckedChange = {
                reversed.value = it
            }
        )
        Text(
            text = text,
            style = typography.bodyMedium
        )
    }
}


@Composable
fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    onDoneAction: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var text by remember { mutableStateOf(value) }

    // Sync local state if parent updates value
    LaunchedEffect(value) {
        text = value
    }

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = Modifier
            .padding(1.dp)
            .height(48.dp)
            .width(68.dp)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    onDoneAction()
                }
            },
        value = text,
        onValueChange = { newText ->
            onValueChange(newText)   // send update up (if needed)
        },
        singleLine = true,
        textStyle = typography.bodySmall,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDoneAction()
                focusManager.clearFocus()
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    // Call the stateless composable with fake data and empty lambdas
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
            modelId = 1,
            modelSet = 2,
            usedDt = 10,
            batteryVoltage = 37f,
            batteryLowestVoltage = 33f,
            currentTemperature = 22.5f,
        )
    )
    SettingsScreen(
        uiState = uiState,
        onUpdateServoSettingsByte = { newSettings, position ->
            // Example logic for updating the servo settings byte
            println("Servo settings byte updated: $newSettings at position $position")
        },
        onServoLabelNameChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") },
        onServoMidPosition = { index, value -> println("Grid item changed: Index = $index, Value = $value") },
        onServoRange = { index, value -> println("Grid item changed: Index = $index, Value = $value") }
    )
}

