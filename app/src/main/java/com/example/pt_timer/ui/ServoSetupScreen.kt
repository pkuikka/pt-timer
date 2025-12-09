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
import com.example.pt_timer.data.ServoData
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
    //val mediumPadding = dimensionResource(R.dimen.padding_medium)
    //val focusManager = LocalFocusManager.current

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
        // -------- Servo1 settings --------

        val servoDataList = ServoData().createServoDataList(uiState)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServoDataRow(
                0,
                servoDataList,
                onServoLabelNameChanged,
                onServoMidPosition,
                onServoRange
            )


            // Correct: state inside a Composable
            val servo1InUseCheckedState = remember { mutableStateOf(servoDataList[0].inUse) }
            Checkbox(
                checked = servo1InUseCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(!it, 4)
                    servo1InUseCheckedState.value = it
                }
            )

            val servo1ReversedCheckedState = remember { mutableStateOf(servoDataList[0].reverse) }
            Checkbox(
                checked = servo1ReversedCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(it, 0)
                    servo1ReversedCheckedState.value = it
                }
            )
        }
        // -------- Servo2 settings --------

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServoDataRow(
                1,
                servoDataList,
                onServoLabelNameChanged,
                onServoMidPosition,
                onServoRange
            )
            // Correct: state inside a Composable

            val servo2InUseCheckedState = remember { mutableStateOf(servoDataList[1].inUse) }
            Checkbox(
                checked = servo2InUseCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(!it, 5)
                    servo2InUseCheckedState.value = it
                }
            )

            val servo2ReversedCheckedState = remember { mutableStateOf(servoDataList[1].reverse) }
            Checkbox(
                checked = servo2ReversedCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(it, 1)
                    servo2ReversedCheckedState.value = it
                }
            )
        }

        // -------- Servo3 settings --------

        Row(

            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServoDataRow(
                2,
                servoDataList,
                onServoLabelNameChanged,
                onServoMidPosition,
                onServoRange
            )

            // Correct: state inside a Composable
            val servo3InUseCheckedState = remember { mutableStateOf(servoDataList[2].inUse) }
            Checkbox(
                checked = servo3InUseCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(!it, 6)
                    servo3InUseCheckedState.value = it
                }
            )

            val servo3ReversedCheckedState = remember { mutableStateOf(servoDataList[2].reverse) }
            Checkbox(
                checked = servo3ReversedCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(it, 2)
                    servo3ReversedCheckedState.value = it
                }
            )
        }

        // -------- Servo4 settings --------

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServoDataRow(
                3,
                servoDataList,
                onServoLabelNameChanged,
                onServoMidPosition,
                onServoRange
            )
            // Correct: state inside a Composable
            val servo4InUseCheckedState = remember { mutableStateOf(servoDataList[3].inUse) }
            Checkbox(
                checked = servo4InUseCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(!it, 7)
                    servo4InUseCheckedState.value = it
                }
            )

            val servo4ReversedCheckedState = remember { mutableStateOf(servoDataList[3].reverse) }
            Checkbox(
                checked = servo4ReversedCheckedState.value,
                onCheckedChange = {
                    onUpdateServoSettingsByte(it, 3)
                    servo4ReversedCheckedState.value = it
                }
            )
        }
    }
}
/*
fun createServoDataList(uiState: UiState): List<ServoData> {
    // Store instances and update them
    val servoData1 = ServoData1().updateServoData(uiState)
    val servoData2 = ServoData2().updateServoData(uiState)
    val servoData3 = ServoData3().updateServoData(uiState)
    val servoData4 = ServoData4().updateServoData(uiState)

    // Create the list using the updated instances
    val servoDataList: List<ServoData> = listOf(
        ServoData(servoData1.name, servoData1.midPos, servoData1.range, servoData1.reverse, servoData1.inUse),
        ServoData(servoData2.name, servoData2.midPos, servoData2.range, servoData2.reverse, servoData2.inUse),
        ServoData(servoData3.name, servoData3.midPos, servoData3.range, servoData3.reverse, servoData3.inUse),
        ServoData(servoData4.name, servoData4.midPos, servoData4.range, servoData4.reverse, servoData4.inUse)
    )

    return servoDataList
}
*/
@Composable
fun ServoDataField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDoneAction: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
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
        value = value,
        onValueChange = { newText -> onValueChange(newText) },
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
    servoDataList: List<ServoData>,
    onServoLabelNameChanged: (Int, String) -> Unit,
    onServoMidPosition: (Int, String) -> Unit,
    onServoRange: (Int, String) -> Unit,
    ) {

    val servoData = servoDataList[index]

    var text by remember { mutableStateOf(servoData.name) }
    var textMidPos by remember { mutableStateOf(servoData.midPos.toString()) }
    var textRange by remember { mutableStateOf(servoData.range.toString()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServoDataField(
            label = "Name",
            value = text,
            onValueChange = { text = it },
            onDoneAction = { onServoLabelNameChanged(1 + index, text) }
        )

        ServoDataField(
            label = "Mid Pos",
            value = textMidPos,
            onValueChange = { textMidPos = it },
            onDoneAction = { onServoMidPosition(0 + index, textMidPos) },
            keyboardType = KeyboardType.Number
        )

        ServoDataField(
            label = "Range",
            value = textRange,
            onValueChange = { textRange = it },
            onDoneAction = { onServoRange(0 + index, textRange) },
            keyboardType = KeyboardType.Number
        )
    }
}

/*fun ServoSetupScreenRefresh(
    uiState: UiState
){

}*/
@Preview(showBackground = true)
@Composable
fun ServoSetupScreenPreview() {
    //servo1 = uiState.timerData.servoMidPosition[11].toString()
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

