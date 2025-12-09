package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.data.GlobalData
import com.example.pt_timer.data.TimerData

@Composable
fun ServoSetupScreen(
    uiState: UiState,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPosition: (Int, String) -> Unit,
    onServoRange: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(modifier = modifier.fillMaxWidth()) {
            Column(modifier = modifier.width(108.dp)) {
                Text(
                    text = "Servo name",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(88.dp)) {
                Text(
                    text = "Mid pos.",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(80.dp)) {
                Text(
                    text = "Range",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(50.dp)) {
                Text(
                    text = "In use",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(65.dp)) {
                Text(
                    text = "Reversed",
                    style = typography.bodyMedium
                )
            }
        }
        GlobalData.createServoDataList(uiState)

        // -------- Servo1 to 4 settings --------
        (0 until 4).forEach { i ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServoDataRow(
                    index = i,
                    onServoLabelNameChanged,
                    onServoMidPosition,
                    onServoRange,
                    onUpdateServoSettingsByte
                )
            }
        }
    }
}

@Composable
fun ServoDataField(
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
            .width(80.dp)
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


@Composable
fun ServoDataRow(
    index: (Int),
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPosition: (Int, String) -> Unit,
    onServoRange: (Int, String) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    ) {

    var text by remember { mutableStateOf(GlobalData.getDataRows()[index].name) }
    var textMidPos by remember { mutableStateOf(GlobalData.getDataRows()[index].midPos.toString()) }
    var textRange by remember { mutableStateOf(GlobalData.getDataRows()[index].range.toString()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServoDataField(
            value = text,
            onValueChange = { text = it },
            onDoneAction = { onServoLabelNameChanged(1 + index, text) } ,
            keyboardType = KeyboardType.Text,
        )

        ServoDataField(
            value = textMidPos,
            onValueChange = { textMidPos = it },
            onDoneAction = { onServoMidPosition(0 + index, textMidPos) },
            keyboardType = KeyboardType.Number,
        )

        ServoDataField(
            value = textRange,
            onValueChange = { textRange = it },
            onDoneAction = { onServoRange(0 + index, textRange) },
            keyboardType = KeyboardType.Number,
        )

        val inUse = remember { mutableStateOf(GlobalData.getDataRows()[index].inUse) }
        Checkbox(
            checked = inUse.value,
            onCheckedChange = {
                onUpdateServoSettingsByte(!it, index + 4)
                inUse.value = it
            }
        )

        val reversed = remember { mutableStateOf(GlobalData.getDataRows()[index].reverse) }
        Checkbox(
            checked = reversed.value,
            onCheckedChange = {
                onUpdateServoSettingsByte(it, index)
                reversed.value = it
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ServoSetupScreenPreview() {
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
    ServoSetupScreen(
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

