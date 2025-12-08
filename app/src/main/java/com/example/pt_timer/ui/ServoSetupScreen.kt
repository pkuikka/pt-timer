package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.data.ServoData1
import com.example.pt_timer.data.ServoData2
import com.example.pt_timer.data.ServoData3
import com.example.pt_timer.data.ServoData4
import com.example.pt_timer.data.TimerData



@Composable
fun ServoSetupScreen(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    // -------- Servo1 settings --------
    val servoData1 = ServoData1().updateServoData(uiState)
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = servoData1.name,//uiState.timerData.servo1Label,
                    onValueChange = onModelNameChanged,
                    label = { Text("Servo1") },
                    singleLine = true
                )
                OutlinedTextField(
                        modifier = Modifier.width(80.dp),
                        value = servoData1.midPos.toString(),//midPos.toString(),
                        onValueChange = onModelIdChanged,
                        //label = { Text("Mid pos.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                OutlinedTextField(
                    modifier = Modifier.width(80.dp),
                    value = servoData1.range.toString(),
                    onValueChange = onModelSetChanged,
                    //label = { Text("Range") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            // Correct: state inside a Composable
            val servo1InUseCheckedState = remember { mutableStateOf(servoData1.inUse) }

            Checkbox(
                checked = servo1InUseCheckedState.value,
                onCheckedChange = { servo1InUseCheckedState.value = it }
            )

            val servo1ReversedCheckedState = remember { mutableStateOf(servoData1.reverse) }
            Checkbox(
                checked = servo1ReversedCheckedState.value,
                onCheckedChange = { servo1ReversedCheckedState.value = it }
            )
        }

        // -------- Servo2 settings --------
        val servoData2 = ServoData2().updateServoData(uiState)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.width(100.dp),
                value = servoData2.name,//uiState.timerData.servo1Label,
                onValueChange = onModelNameChanged,
                label = { Text("Servo2") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData2.midPos.toString(),//midPos.toString(),
                onValueChange = onModelIdChanged,
                //label = { Text("Mid pos.") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData2.range.toString(),
                onValueChange = onModelSetChanged,
                //label = { Text("Range") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            // Correct: state inside a Composable
            val servo2InUseCheckedState = remember { mutableStateOf(servoData2.inUse) }

            Checkbox(
                checked = servo2InUseCheckedState.value,
                onCheckedChange = { servo2InUseCheckedState.value = it }
            )

            val servo2ReversedCheckedState = remember { mutableStateOf(servoData2.reverse) }
            Checkbox(
                checked = servo2ReversedCheckedState.value,
                onCheckedChange = { servo2ReversedCheckedState.value = it }
            )
        }

        // -------- Servo3 settings --------
        val servoData3 = ServoData3().updateServoData(uiState)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.width(100.dp),
                value = servoData3.name,//uiState.timerData.servo1Label,
                onValueChange = onModelNameChanged,
                label = { Text("Servo3") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData3.midPos.toString(),//midPos.toString(),
                onValueChange = onModelIdChanged,
                //label = { Text("Mid pos.") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData3.range.toString(),
                onValueChange = onModelSetChanged,
                //label = { Text("Range") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            // Correct: state inside a Composable
            val servo3InUseCheckedState = remember { mutableStateOf(servoData3.inUse) }

            Checkbox(
                checked = servo3InUseCheckedState.value,
                onCheckedChange = { servo3InUseCheckedState.value = it }
            )

            val servo3ReversedCheckedState = remember { mutableStateOf(servoData3.reverse) }
            Checkbox(
                checked = servo3ReversedCheckedState.value,
                onCheckedChange = { servo3ReversedCheckedState.value = it }
            )
        }

        // -------- Servo4 settings --------
        val servoData4 = ServoData4().updateServoData(uiState)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.width(100.dp),
                value = servoData4.name,//uiState.timerData.servo1Label,
                onValueChange = onModelNameChanged,
                label = { Text("Servo4") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData4.midPos.toString(),//midPos.toString(),
                onValueChange = onModelIdChanged,
                //label = { Text("Mid pos.") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.width(80.dp),
                value = servoData4.range.toString(),
                onValueChange = onModelSetChanged,
                //label = { Text("Range") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            // Correct: state inside a Composable
            val servo4InUseCheckedState = remember { mutableStateOf(servoData4.inUse) }

            Checkbox(
                checked = servo4InUseCheckedState.value,
                onCheckedChange = { servo4InUseCheckedState.value = it }
            )

            val servo4ReversedCheckedState = remember { mutableStateOf(servoData4.reverse) }
            Checkbox(
                checked = servo4ReversedCheckedState.value,
                onCheckedChange = { servo4ReversedCheckedState.value = it }
            )
        }
    }
}

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
            //servoMidPosition = List(4) { 127 },
        )
    )
    ServoSetupScreen(
        uiState = uiState,
        onModelNameChanged = {},
        onModelIdChanged = {},
        onModelSetChanged = {}

    )
}

