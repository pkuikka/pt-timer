package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
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
fun TimerDataGridLayout(
    uiState: UiState,
    onGridItemChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayGridItems = remember(uiState.timerData) {
        // This remember block re-calculates 'displayGridItems'
        // only when 'timerData' changes.
        val columnHeaders = listOf(
            "",
            "Time",
            uiState.timerData.servo1Label,
            uiState.timerData.servo2Label,
            uiState.timerData.servo3Label,
            uiState.timerData.servo4Label
        )

        val rowHeaders = listOf(
            uiState.timerData.row1Label,
            uiState.timerData.row2Label,
            uiState.timerData.row3Label,
            uiState.timerData.row4Label,
            "", "", "", "", "", "", "", "", "", "", "", ""
        )

        // The actual numerical data from the device
        var data = listOf<String>()
        for (i in 0 until (uiState.timerData.numberOfDataRows)) {
            data = if (uiState.timerData.timeValues[i] < com.example.pt_timer.data.MAX_TIME_TENTHS_LIMIT)
                data + uiState.timerData.timeValues[i].toString()
            else
                data + uiState.timerData.timeValues[i].toInt().toString()
            data = data + uiState.timerData.servo1Values[i].toString()
            data = data + uiState.timerData.servo2Values[i].toString()
            data = data + uiState.timerData.servo3Values[i].toString()
            data = data + uiState.timerData.servo4Values[i].toString()
        }

        // We will now build the final display list (90 items)
        val combinedList = mutableListOf<String>()
        combinedList.addAll(columnHeaders) // Add the first row (headers)

        // For the remaining 14 rows...
        for (i in 0 until (uiState.timerData.numberOfDataRows)) {
            // Add the row header for this row
            if (i < rowHeaders.size) {
                if (i+1 == uiState.timerData.skipBuntGoToRow) // header row is the +1
                    combinedList.add("-->")
                else
                    combinedList.add(rowHeaders[i])
            } else {
                combinedList.add("") // Fallback if not enough headers
            }
            // Add the 5 data points for this row
            // The data is organized in rows of 5 in your timerGridValues
            val dataStartIndex = i * 5
            combinedList.addAll(data.subList(dataStartIndex, dataStartIndex + 5))
        }
        combinedList
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6)
            ) {
                //val uiState.displayGridItems
                items(displayGridItems.size) { index ->
                    //    The `remember` key ensures that if the underlying `displayGridItems[index]`
                    //    changes (e.g., from a BT read), the local state `text` is reset.
                    var text by remember(displayGridItems[index]) {
                        mutableStateOf(displayGridItems[index])
                    }
                    val focusManager = LocalFocusManager.current

                    OutlinedTextField(
                        modifier = Modifier
                            .padding(1.dp) // Add a little padding around each field
                            .height(48.dp) // Set a fixed height for alignment
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    onGridItemChanged(index, text)
                                }
                            },
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                        },
                        singleLine = true,
                        textStyle = typography.bodySmall,
                        readOnly = (index < 6) || (index % 6 == 0),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onGridItemChanged(index, text)
                                focusManager.clearFocus()
                            }
                        )
                    )
                }
            }
        }
    }
}

/*
@Composable
fun TimerDataGridLayoutRefresh(
    uiState: UiState,
    onGridItemChanged: (Int, String) -> Unit,
) {
    // Optionally, you could update the ViewModel with new TimerData
    TimerDataGridLayout(uiState, onGridItemChanged)
}*/

@Preview
@Composable
fun TimerDataGridLayoutPreview() {
    // Define the sample UI state with some test data
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
            modelId = 1,
            modelSet = 2,
            usedDt = 10,
            batteryVoltage = 3.7f,
            batteryLowestVoltage = 3.3f,
            currentTemperature = 22.5f
        )
    )

    // Display the layout with a mock onGridItemChanged handler
    TimerDataGridLayout(
        uiState = uiState,
        onGridItemChanged = { index, value ->
            // Implement your logic here, for now just log it
            println("Grid item changed: Index = $index, Value = $value")
        }
    )
}