package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.data.TimerData

@Composable
fun ServoSetupScreen(
    uiState: UiState,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            Column(modifier = modifier.width(100.dp)) {
                Text(
                    text = " Servo\n name",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(80.dp)) {
                Text(
                    text = "Mid\npos.",
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
                    text = "In\nuse",
                    style = typography.bodyMedium
                )
            }
            Column(modifier = modifier.width(80.dp)) {
                Text(
                    text = "Reversed",
                    style = typography.bodyMedium
                )
            }
        }

        // -------- Servo1 to 4 settings --------
        (0 until 4).forEach { i ->
            var name = ""
            var onDoneActionName: (String) -> Unit = {}
            val midPos = uiState.timerData.servoMidPosition.getOrNull(i).toString()
            val onDoneActionMidPos = { newValue: String ->
                val intValue = newValue.toIntOrNull() ?: 0
                onUpdateTimerData { copy(servoMidPosition = this.servoMidPosition.toMutableList().apply { set(i, intValue) }) }
            }
            val range = uiState.timerData.servoRange.getOrNull(i).toString()
            val onDoneActionRange = { newValue: String ->
                val intValue = newValue.toIntOrNull() ?: 0
                onUpdateTimerData { copy(servoRange = this.servoRange.toMutableList().apply { set(i, intValue) }) }
            }
            var inUse = false
            var inUseBit = 0
            var reversed = false
            var reversedBit = 0
            when (i + 1) {
                1 -> {
                    name = uiState.timerData.servo1Label
                    onDoneActionName = { newValue: String ->
                        onUpdateTimerData { copy(servo1Label = newValue) }
                    }
                    inUse = uiState.timerData.isServo1NotInUse
                    inUseBit = 16
                    reversed = uiState.timerData.isServo1Reversed
                    reversedBit = 1
                }

                2 -> {
                    name = uiState.timerData.servo2Label
                    onDoneActionName = { newValue: String ->
                        onUpdateTimerData { copy(servo2Label = newValue) }
                    }
                    inUse = uiState.timerData.isServo2NotInUse
                    inUseBit = 32
                    reversed = uiState.timerData.isServo2Reversed
                    reversedBit = 2
                }

                3 -> {
                    name = uiState.timerData.servo3Label
                    onDoneActionName = { newValue: String ->
                        onUpdateTimerData { copy(servo3Label = newValue) }
                    }
                    inUse = uiState.timerData.isServo3NotInUse
                    inUseBit = 64
                    reversed = uiState.timerData.isServo3Reversed
                    reversedBit = 4
                }

                4 -> {
                    name = uiState.timerData.servo4Label
                    onDoneActionName = { newValue: String ->
                        onUpdateTimerData { copy(servo4Label = newValue) }
                    }
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
                    name = name,
                    midPos = midPos,
                    range = range,
                    onDoneActionName = onDoneActionName,
                    onDoneActionMidPos = onDoneActionMidPos,
                    onDoneActionRange = onDoneActionRange,
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
    name: String,
    midPos: String,
    range: String,
    onDoneActionName: (String) -> Unit,
    onDoneActionMidPos: (String) -> Unit,
    onDoneActionRange: (String) -> Unit,
    inUseValue: Boolean,
    inUseValueChange: (Boolean) -> Unit,
    reversedValue: Boolean,
    reversedValueChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonField(
            value = name,
            onDoneAction = onDoneActionName,
            width = 85.dp,
            keyboardType = KeyboardType.Text,
        )

        CommonField(
            value = midPos,
            onDoneAction = onDoneActionMidPos
        )

        CommonField(
            value = range,
            onDoneAction = onDoneActionRange
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
        onUpdateServoSettingsByte = { _, _ -> },
        onUpdateTimerData = { _ -> }
    )
}

