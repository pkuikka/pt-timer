package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.data.TimerData


fun modelTypeAsString(modelType: Int): String {
    when (modelType) {
        1 -> return "F1B"
        2 -> return "F1A/H"
        3 -> return "P-30"
        4 -> return "E-36"
        5 -> return "F1Q"
    }
    return "Unknown"
}

@Composable
fun SettingsScreen(
    uiState: UiState,
    onUpdateConfigByte: (Boolean, Int) -> Unit,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
) {
    val modelType = uiState.timerData.modelType

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        RowWithText("Model type ${modelTypeAsString(uiState.timerData.modelType)}")
        RowWithField(
            "Number of data lines",
            "${uiState.timerData.numberOfDataRows}",
            onDoneAction = { newValue ->
                onUpdateTimerData { copy(numberOfDataRows = newValue.toIntOrNull() ?: 0) }
            }
        )
        RowWithField(
            "Critical voltage",
            "${uiState.timerData.batteryWarningVoltage}",
            onDoneAction = { newValue ->
                onUpdateTimerData { copy(batteryWarningVoltage = newValue.toDoubleOrNull() ?: 0.0) }
            }
        )
        RowWithCheckBox(
            "RDT enabled", value = uiState.timerData.isRdtEnabled,
            onValueChange = { isChecked ->
                onUpdateConfigByte(isChecked, 4)
            })
        if ((modelType == 2) || (modelType == 1) || (modelType == 5)) {
            RowWithCheckBox(
                "Power-off delay after DT enabled",
                value = uiState.timerData.isDtPowerDownDelayEnabled,
                onValueChange = { isChecked ->
                    onUpdateConfigByte(isChecked, 64)
                })
            RowWithField(
                "Power-off delay after DT (seconds)",
                "${uiState.timerData.dtPowerDownDelay}",
                onDoneAction = { newValue ->
                    onUpdateTimerData { copy(dtPowerDownDelay = newValue.toIntOrNull() ?: 0) }
                }
            )
        }

        // --- Hook / model type settings ---
        if (modelType == 2) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Text(
                text = "Hook settings",
                style = typography.titleSmall
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        }
        if ((modelType == 2) || (modelType == 3) || (modelType == 4)) {
            RowWithCheckBox(
                if (modelType == 2) {
                    "Beep on tow"
                } else {
                    "Beep after DT"
                },
                value = uiState.timerData.isBeepOnTowEnabled,
                onValueChange = { isChecked ->
                    onUpdateConfigByte(isChecked, 32)
                })
        }
        if ((modelType == 2) || (modelType == 1) || (modelType == 5)) {
            RowWithText("Reverse switches")
            RowWithCheckBox(
                if (modelType == 2) {
                    "Tow"
                } else {
                    "Start button"
                },
                value = uiState.timerData.isSwitch1Enabled,
                onValueChange = { isChecked ->
                    onUpdateConfigByte(isChecked, 1)
                })
        }
        if (modelType == 2) {
            RowWithCheckBox(
                "Latch", value = uiState.timerData.isSwitch2Enabled,
                onValueChange = { isChecked ->
                    onUpdateConfigByte(isChecked, 2)
                })
            RowWithText("Hook type")
            val hookType = uiState.timerData.isReLatchEnabled
            RowWithCheckBox(
                "Re-latch (not checked is conventional)",
                value = uiState.timerData.isReLatchEnabled,
                onValueChange = { isChecked ->
                    onUpdateConfigByte(isChecked, 128)
                })
            RowWithField(
                text = if (hookType) {
                    "Re-latch critical time (seconds)"
                } else {
                    "No bunt critical time (seconds)"
                },
                value = "${uiState.timerData.maxTimeForSkippingBunt.toDouble() / 10.0}",
                onDoneAction = { newValue ->
                    onUpdateTimerData {
                        copy(
                            maxTimeForSkippingBunt = ((newValue.toDoubleOrNull()
                                ?: 0.0) * 10).toInt()
                        )
                    }
                }
            )
            if (!hookType) {
                RowWithField(
                    "No bunt jump to line #",
                    "${uiState.timerData.skipBuntGoToRow}",
                    onDoneAction = { newValue ->
                        onUpdateTimerData { copy(skipBuntGoToRow = newValue.toIntOrNull() ?: 0) }
                    }
                )
                val buntStatus = when (uiState.timerData.buntStatus) {
                    1 -> "bunt skipped, longer than max time"
                    2 -> "bunt skipped, shorter than min time"
                    else -> "bunt executed"
                }
                RowWithText("Last bunt status: $buntStatus")
            }
        }
        if (modelType == 5) {
            RowWithField(
                "If limiter cut, jump to line #",
                "${uiState.timerData.skipBuntGoToRow}",
                onDoneAction = { newValue ->
                    onUpdateTimerData { copy(skipBuntGoToRow = newValue.toIntOrNull() ?: 0) }
                }
            )
            val motorTime =
                (uiState.timerData.motorRunTime1 * 256 + uiState.timerData.motorRunTime2) / 10.0
            RowWithText("Last motor run time ${motorTime}s ")
        }

        // --- Advanced settings ---
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Advanced timer settings",
            style = typography.titleMedium
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        RowWithText("Version ${uiState.timerData.timerVersion}")
        RowWithText("Maximum data lines ${uiState.timerData.maxDataRows}")
        RowWithText("Name label index ${uiState.timerData.firstIndexForDataSetName}")
        RowWithField(
            "Wake up count",
            "${uiState.timerData.startUpCycleCount}",
            onDoneAction = { newValue ->
                onUpdateTimerData { copy(startUpCycleCount = newValue.toIntOrNull() ?: 0) }
            }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clock speed",
                style = typography.bodyMedium
            )
            CommonField(
                value = "${uiState.timerData.timerCalibrationInMilliseconds}",
                onDoneAction = { newValue ->
                    onUpdateTimerData {
                        copy(
                            timerCalibrationInMilliseconds = newValue.toIntOrNull() ?: 0
                        )
                    }
                }
            )
            CommonField(
                value = "${uiState.timerData.timerCalibrationInMicroseconds1}",
                onDoneAction = { newValue ->
                    onUpdateTimerData {
                        copy(
                            timerCalibrationInMicroseconds1 = newValue.toIntOrNull() ?: 0
                        )
                    }
                }
            )
            CommonField(
                value = "${uiState.timerData.timerCalibrationInMicroseconds2}",
                onDoneAction = { newValue ->
                    onUpdateTimerData {
                        copy(
                            timerCalibrationInMicroseconds2 = newValue.toIntOrNull() ?: 0
                        )
                    }
                }
            )
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
    value: String,
    onDoneAction: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typography.bodyMedium
        )
        CommonField(value = value, onDoneAction = onDoneAction)
    }
}

@Composable
fun RowWithCheckBox(
    text: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = {
                onValueChange(it)
            }
        )
        Text(
            text = text,
            style = typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    // Call the stateless composable with fake data and empty lambdas
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
        )
    )
    SettingsScreen(
        uiState = uiState,
        onUpdateConfigByte = { _, _ -> },
        onUpdateTimerData = { _ -> }
    )
}
