package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
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
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // -------- Model / ID / Set Row --------
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommonField(
                label = "Model",
                value = uiState.timerData.modelName,
                onDoneAction = { newValue: String ->
                    onUpdateTimerData { copy(modelName = newValue) }
                },
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardType = KeyboardType.Text,
                modifier = Modifier.weight(1f),
                height = 68.dp

            )

            CommonField(
                label = "ID",
                value = uiState.timerData.modelId.toString(),
                onDoneAction = { newValue ->
                    onUpdateTimerData { copy(modelId = newValue.toIntOrNull() ?: 0) }
                },
                textStyle = MaterialTheme.typography.titleMedium,
                width = 68.dp,
                height = 68.dp
            )

            CommonField(
                label = "Set",
                value = uiState.timerData.modelSet.toString(),
                onDoneAction = { newValue ->
                    onUpdateTimerData { copy(modelSet = newValue.toIntOrNull() ?: 0) }
                },
                textStyle = MaterialTheme.typography.titleMedium,
                width = 68.dp,
                height = 68.dp
            )
        }
    }
}

@Preview
@Composable
fun TimerLayoutPreview() {
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
            modelId = 1,
            modelSet = 2
        )
    )
    TimerLayout(
        uiState = uiState,
        onUpdateTimerData = { _ -> }
    )
}