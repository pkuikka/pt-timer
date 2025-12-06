package com.example.pt_timer.ui

// ... other imports
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pt_timer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: UiState,
    onReadClick: () -> Unit,
    onWriteClick: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val context = LocalContext.current

    // Effect to refresh devices
    LaunchedEffect(key1 = true) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onRefreshDevices()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PT-Timer Control") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "User Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomButtonsPanel(
                btDeviceList = uiState.btDevices,
                selectedDevice = uiState.selectedBtDevice,
                onDeviceSelected = onDeviceSelected,
                onReadClick = onReadClick,
                onWriteClick = onWriteClick,
                onOpenClick = {},
                onSaveClick = {},
                onDeleteClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    // TO FIX Samsung navigation overlap.
                    // It adds padding to the bottom to avoid the system navigation bar.
                    .navigationBarsPadding()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerLayout(
                timerData = uiState.timerData,
                onModelNameChanged = onModelNameChanged,
                onModelIdChanged = onModelIdChanged,
                onModelSetChanged = onModelSetChanged,
                onGridItemChanged = onGridItemChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(mediumPadding)
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    mainScreenViewModel: UiViewModel = viewModel(
        factory = UiViewModel.Factory
    )
) {
    val mainScreenUiState by mainScreenViewModel.uiState.collectAsState()

    MainScreenContent(
        uiState = mainScreenUiState,
        onReadClick = { mainScreenViewModel.read() },
        onWriteClick = { mainScreenViewModel.write() },
        onDeviceSelected = { deviceName -> mainScreenViewModel.onDeviceSelected(deviceName) },
        onRefreshDevices = { mainScreenViewModel.refreshPairedDevices() },
        onSettingsClick = onSettingsClick,
        onOpenClick = { /* TODO: Call mainScreenViewModel.openFile() */ },
        onSaveClick = { /* TODO: Call mainScreenViewModel.saveFile() */ },
        onDeleteClick = { /* TODO: Call mainScreenViewModel.saveFile() */ },
        onModelNameChanged = { newName -> mainScreenViewModel.onModelNameChanged(newName) },
        onModelIdChanged = { newIdString -> mainScreenViewModel.onModelIdChanged(newIdString) },
        onModelSetChanged = { newSetString -> mainScreenViewModel.onModelSetChanged(newSetString) },
        onGridItemChanged = { index, newValue -> mainScreenViewModel.onGridItemChanged(index, newValue) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerLayout(
    timerData: com.example.pt_timer.data.TimerData,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayGridItems = remember(timerData) {
        // This remember block re-calculates 'displayGridItems'
        // only when 'timerData' changes.
        val columnHeaders = listOf(
            "",
            "Time",
            timerData.servo1Label,
            timerData.servo2Label,
            timerData.servo3Label,
            timerData.servo4Label
        )

        val rowHeaders = listOf(
            timerData.row1Label,
            timerData.row2Label,
            timerData.row3Label,
            timerData.row4Label,
            "", "", "", "-->", "", "", "", "", "", "14", "15", "16"
        )

        // The actual numerical data from the device
        var data = listOf<String>()
        for (i in 0 until (timerData.numberOfDataRows)) {
            data = if (timerData.timeValues[i] < com.example.pt_timer.data.MAX_TIME_TENTHS_LIMIT)
                data + timerData.timeValues[i].toString()
            else
                data + timerData.timeValues[i].toInt().toString()
            data = data + timerData.servo1Values[i].toString()
            data = data + timerData.servo2Values[i].toString()
            data = data + timerData.servo3Values[i].toString()
            data = data + timerData.servo4Values[i].toString()
        }

        // We will now build the final display list (90 items)
        val combinedList = mutableListOf<String>()
        combinedList.addAll(columnHeaders) // Add the first row (headers)

        // For the remaining 14 rows...
        for (i in 0 until (timerData.numberOfDataRows)) {
            // Add the row header for this row
            if (i < rowHeaders.size) {
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.width(220.dp),
                value = timerData.modelName,
                onValueChange = onModelNameChanged,
                label = { Text(stringResource(R.string.label_model )) },
                singleLine = true,
                textStyle = typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
            )
            OutlinedTextField(
                modifier = Modifier.width(70.dp),
                value =  timerData.modelId.toString(),
                onValueChange = onModelIdChanged,
                label = { Text(stringResource(R.string.label_id )) },
                singleLine = true,
                textStyle = typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
            )
            OutlinedTextField(
                modifier = Modifier.width(70.dp),
                value = timerData.modelSet.toString(),
                onValueChange = onModelSetChanged,
                label = { Text(stringResource(R.string.label_set )) },
                singleLine = true,
                textStyle = typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)), // Use smaller spacing
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =  "DT " + timerData.usedDt.toString(),
                textAlign = TextAlign.Center,
                style = typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Text(
                text = timerData.batteryVoltage.toString() + "V",
                textAlign = TextAlign.Center,
                style = typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "Min " + timerData.batteryLowestVoltage.toString() + "V",
                textAlign = TextAlign.Center,
                style = typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "Temp." + timerData.currentTemperature.toString() + "C",
                textAlign = TextAlign.Center,
                style = typography.bodyMedium
            )
        }
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.height(500.dp) // Adjust height as needed
        ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomButtonsPanel(
    btDeviceList: List<String>,
    selectedDevice: String,
    onDeviceSelected: (String) -> Unit,
    onReadClick: () -> Unit,
    onWriteClick: () -> Unit,
    onOpenClick: () -> Unit,  // 1. Add this for the "Open" button
    onSaveClick: () -> Unit,  // 2. Add this for the "Save" butto
    onDeleteClick: () -> Unit,  // 2. Add this for the "Save" butto
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)), // Use smaller spacing
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
            ) {
            // BT device selection
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                    value = selectedDevice,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select BT Device") },
                    singleLine = true,
                    maxLines = 1,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    }
                )

                // The actual dropdown menu with the list of items
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    btDeviceList.forEach { deviceName ->
                        DropdownMenuItem(
                            text = { Text(deviceName) },
                            onClick = {
                                onDeviceSelected(deviceName)
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            // Read Button
            Button(onClick = onReadClick) {
                Text(text = stringResource(R.string.button_read), fontSize = 16.sp)
            }

            // Write Button
            OutlinedButton(onClick = {
                // force focus cleaning, so that last edit on grid is noticed
                focusManager.clearFocus()
                onWriteClick()
            }) {
                Text(text = stringResource(R.string.button_write), fontSize = 16.sp)
            }

        }
        Row(
            //horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)), // Use smaller spacing
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = onOpenClick) {
                Text(text = stringResource(R.string.button_open), fontSize = 14.sp)
            }
            OutlinedButton(onClick = onSaveClick) {
                Text(text = stringResource(R.string.button_save), fontSize = 14.sp)
            }
            OutlinedButton(onClick = onDeleteClick) {
                Text(text = stringResource(R.string.button_delete), fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Create a fake UiState for the preview
    val fakeUiState = UiState(
        btDevices = listOf("Device 1", "Petri-PT-Timer", "Device 3"),
        selectedBtDevice = "Petri-PT-Timer"
    )

    // Call the stateless composable with fake data and empty lambdas
    MainScreenContent(
        uiState = fakeUiState,
        onReadClick = {},
        onWriteClick = {},
        onDeviceSelected = {},
        onRefreshDevices = {},
        onSettingsClick = {},
        onOpenClick = {},
        onSaveClick = {},
        onDeleteClick = {},
        onModelNameChanged = {},
        onModelIdChanged = {},
        onModelSetChanged = {},
        onGridItemChanged = { _, _ -> }
    )
}
