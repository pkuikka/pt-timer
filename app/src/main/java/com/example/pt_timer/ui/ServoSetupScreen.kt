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
import com.example.pt_timer.data.TimerData

@Composable
fun ServoSetupScreen(
    uiState: UiState,
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPositionChanged: (Int, String) -> Unit,
    onServoRangeChanged: (Int, String) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
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

        // -------- Servo1 to 4 settings --------
        (0 until 4).forEach { i ->
            var name = ""
            var inUse = false
            var inUseBit = 0
            var reversed = false
            var reversedBit = 0
            val midPos = uiState.timerData.servoMidPosition.getOrNull(i).toString()
            val range = uiState.timerData.servoRange.getOrNull(i).toString()
            when (i + 1) {
                1 -> {
                    name = uiState.timerData.servo1Label
                    inUse = uiState.timerData.isServo1NotInUse
                    inUseBit = 16
                    reversed = uiState.timerData.isServo1Reversed
                    reversedBit = 1
                }

                2 -> {
                    name = uiState.timerData.servo2Label
                    inUse = uiState.timerData.isServo2NotInUse
                    inUseBit = 32
                    reversed = uiState.timerData.isServo2Reversed
                    reversedBit = 2
                }

                3 -> {
                    name = uiState.timerData.servo3Label
                    inUse = uiState.timerData.isServo3NotInUse
                    inUseBit = 64
                    reversed = uiState.timerData.isServo3Reversed
                    reversedBit = 4
                }

                4 -> {
                    name = uiState.timerData.servo4Label
                    inUse = uiState.timerData.isServo4NotInUse
                    inUseBit = 128
                    reversed = uiState.timerData.isServo4Reversed
                    reversedBit = 8
                }

                else -> {}
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServoDataRow(
                    index = i,
                    name = name,
                    midPos = midPos,
                    range = range,
                    onServoLabelNameChanged,
                    onServoMidPositionChanged,
                    onServoRangeChanged,
                    inUseValue = inUse,
                    inUseValueChange = { isChecked ->
                        onUpdateServoSettingsByte(isChecked, inUseBit)
                    },
                    reversedValue = reversed,
                    reversedValueChange = { isChecked ->
                        onUpdateServoSettingsByte(isChecked, reversedBit)
                    }
                )
            }
        }
    }
}

@Composable
fun ServoDataRow(
    index: (Int),
    name: (String),
    midPos: (String),
    range: (String),
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPositionChanged: (Int, String) -> Unit,
    onServoRangeChanged: (Int, String) -> Unit,
    inUseValue: Boolean,
    inUseValueChange: (Boolean) -> Unit,
    reversedValue: Boolean,
    reversedValueChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServoDataField(
            value = name,
            onValueChange = { newName -> onServoLabelNameChanged(index, newName) },
            onDoneAction = { onServoLabelNameChanged(index, name) },
            keyboardType = KeyboardType.Text,
        )

        ServoDataField(
            value = midPos,
            onValueChange = { newValue -> onServoMidPositionChanged(index, newValue) },
            onDoneAction = { onServoMidPositionChanged(index, midPos) },
            keyboardType = KeyboardType.Number,
        )

        ServoDataField(
            value = range,
            onValueChange = { newValue -> onServoRangeChanged(index, newValue) },
            onDoneAction = { onServoRangeChanged(index, range) },
            keyboardType = KeyboardType.Number,
        )

        Checkbox(
            checked = !inUseValue, // Invert the value as it is *NotInUse
            onCheckedChange = { isChecked ->
                inUseValueChange(!isChecked)  // Need to invert here too
            }
        )
        Checkbox(
            checked = reversedValue,
            onCheckedChange = {
                reversedValueChange(it)
            }
        )
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

@Preview(showBackground = true)
@Composable
fun ServoSetupScreenPreview() {
    // Call the stateless composable with fake data and empty lambdas
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
        )
    )
    ServoSetupScreen(
        uiState = uiState,
        onUpdateServoSettingsByte = { newSettings, position ->
            println("Servo settings byte updated: $newSettings at position $position")
        },
        onServoLabelNameChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") },
        onServoMidPositionChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") },
        onServoRangeChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") }
    )
}

