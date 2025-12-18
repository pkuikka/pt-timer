package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pt_timer.R
import com.example.pt_timer.data.MAX_DATA_SETS
import com.example.pt_timer.data.TimerData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServoSetupScreen(
    uiState: UiState,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    onCopyClick: (sourceSet: Int, destinationSet: Int) -> Unit,
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
                onUpdateTimerData {
                    copy(
                        servoMidPosition = this.servoMidPosition.toMutableList()
                            .apply { set(i, intValue) })
                }
            }
            val range = uiState.timerData.servoRange.getOrNull(i).toString()
            val onDoneActionRange = { newValue: String ->
                val intValue = newValue.toIntOrNull() ?: 0
                onUpdateTimerData {
                    copy(
                        servoRange = this.servoRange.toMutableList().apply { set(i, intValue) })
                }
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isSetExpanded by remember { mutableStateOf(false) }
            var destinationSet by remember { mutableStateOf<Int?>(null) }

            Text(
                modifier = Modifier.width(200.dp),
                text = "Current set ${uiState.timerData.modelSet} ${uiState.timerData.setNames[uiState.timerData.modelSet]} -->",
                style = typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = isSetExpanded,
                onExpandedChange = { isSetExpanded = it },
                modifier = Modifier.width(80.dp) // Give it a specific width
            ) {
                // This is the TextField part of the dropdown
                OutlinedTextField(
                    value = destinationSet?.toString() ?: "", // Show selected value or empty
                    onValueChange = {}, // onValueChange is not needed for a read-only dropdown
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSetExpanded)
                    },
                    singleLine = true,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                // This is the actual dropdown menu with the list of items
                ExposedDropdownMenu(
                    expanded = isSetExpanded,
                    onDismissRequest = { isSetExpanded = false }
                ) {
                    (0 until MAX_DATA_SETS).forEach { selectionIndex ->
                        if (selectionIndex == uiState.timerData.modelSet) {
                            // skip current
                        } else {
                            DropdownMenuItem(
                                text = { Text("$selectionIndex ${uiState.timerData.setNames[selectionIndex]}") },
                                onClick = {
                                    destinationSet = selectionIndex
                                    isSetExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Copy Button
            Button(
                onClick = {
                    val currentSet = uiState.timerData.modelSet
                    val dest = destinationSet
                    // 4. Pass both source and destination to the onCopyClick lambda
                    if (dest != null) {
                        onCopyClick(currentSet, dest)
                    }
                },
                // Enable the button only when a destination has been selected
                enabled = destinationSet != null
            ) {
                Text(text = stringResource(R.string.button_copy), fontSize = 16.sp)
            }
        }

        // --- Apply set names
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isSetExpanded by remember { mutableStateOf(false) }
            var nameDestinationSet by remember { mutableStateOf(0) }

            Text(
                text = "Set",
                style = typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = isSetExpanded,
                onExpandedChange = { isSetExpanded = it },
                modifier = Modifier.width(80.dp) // Give it a specific width
            ) {
                // This is the TextField part of the dropdown
                OutlinedTextField(
                    value = nameDestinationSet.toString(),
                    onValueChange = {}, // onValueChange is not needed for a read-only dropdown
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSetExpanded)
                    },
                    singleLine = true,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                // This is the actual dropdown menu with the list of items
                ExposedDropdownMenu(
                    expanded = isSetExpanded,
                    onDismissRequest = { isSetExpanded = false }
                ) {
                    (0 until MAX_DATA_SETS).forEach { selectionIndex ->
                        DropdownMenuItem(
                            { Text("$selectionIndex ${uiState.timerData.setNames[selectionIndex]}") },
                            onClick = {
                                nameDestinationSet = selectionIndex
                                isSetExpanded = false
                            }
                        )
                    }
                }
            }
            Text(
                text = "name to",
                style = typography.titleMedium
            )
            CommonField(
                value = uiState.timerData.setNames[nameDestinationSet],
                onDoneAction = { newValue ->
                    onUpdateTimerData {
                        // Create a mutable copy of the list to update the specific index
                        val updatedList = setNames.toMutableList()
                        if (nameDestinationSet in updatedList.indices) {
                            updatedList[nameDestinationSet] = newValue
                        }
                        copy(setNames = updatedList)
                    }
                },
                keyboardType = KeyboardType.Text,
                width = 150.dp
            )
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
        onUpdateTimerData = { _ -> },
        onCopyClick = { _, _ -> }
    )
}

