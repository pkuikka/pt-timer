package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.data.TimerData



@Composable
fun TimerLayout(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier.fillMaxWidth()) {
        // -------- Model / ID / Set Row --------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier.width(220.dp),
                value = uiState.timerData.modelName,
                onValueChange = onModelNameChanged,
                label = { Text("Model") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.width(70.dp),
                value = uiState.timerData.modelId.toString(),
                onValueChange = onModelIdChanged,
                label = { Text("ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                modifier = Modifier.width(70.dp),
                value = uiState.timerData.modelSet.toString(),
                onValueChange = onModelSetChanged,
                label = { Text("Set") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
        }

        // -------- Status Row --------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val dt = uiState.timerData.usedDt
            val batteryVoltage = uiState.timerData.batteryVoltage
            val batteryLowestVoltage = uiState.timerData.batteryLowestVoltage

            Text(text = "DT $dt", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${batteryVoltage / 10}V", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Min ${batteryLowestVoltage / 10}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TimerLayoutRefresh(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
) {
    // Optionally, you could update the ViewModel with new TimerData
    TimerLayout(uiState, onModelNameChanged, onModelIdChanged, onModelSetChanged)
}

@Preview
@Composable
fun TimerLayoutPreview() {
    val uiState = UiState(timerData = TimerData(
        modelName = "Test Model",
        modelId = 1,
        modelSet = 2,
        usedDt = 10,
        batteryVoltage = 37f,
        batteryLowestVoltage = 33f,
        currentTemperature = 22.5f
    ))
    TimerLayout(
        uiState = uiState,
        onModelNameChanged = {},
        onModelIdChanged = {},
        onModelSetChanged = {}
    )
}