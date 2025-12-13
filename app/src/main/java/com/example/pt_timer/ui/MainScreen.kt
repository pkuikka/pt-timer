package com.example.pt_timer.ui

// ... other imports
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pt_timer.R
import com.example.pt_timer.data.MAX_TIMER_DATA_ROWS
import com.example.pt_timer.data.TIMER_TYPE_E36
import com.example.pt_timer.data.TIMER_TYPE_F1A
import com.example.pt_timer.data.TIMER_TYPE_F1B
import com.example.pt_timer.data.TIMER_TYPE_F1Q
import com.example.pt_timer.data.TIMER_TYPE_P30
import com.example.pt_timer.data.TimerData


@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    onUserSettingsClick: () -> Unit,
    mainScreenViewModel: UiViewModel = viewModel(
        factory = UiViewModel.Factory
    )
) {
    val mainScreenUiState by mainScreenViewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    val onDismissDialog = remember { { showDialog = false } }

    val showMismatchDialog by mainScreenViewModel.showMismatchDialog.collectAsState()
    val mismatchMessage by mainScreenViewModel.mismatchDialogMessage.collectAsState()

    val showOldDataWarningDialog by mainScreenViewModel.showOldDataWarningDialog.collectAsState()

    // Launcher for the EXPORT ALL (ACTION_OPEN_DOCUMENT)
    val exportAllLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { treeUri: Uri? ->
            // The user has selected a directory.
            treeUri?.let {
                mainScreenViewModel.exportAllFilesToDirectory(it)
            }
        }
    )

    // Launcher for the IMPORT file picker (ACTION_OPEN_DOCUMENT)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            // The user has selected a file.
            uri?.let {
                mainScreenViewModel.importTimerDataFromUri(it)
            }
        }
    )

    // Launcher for the EXPORT file picker (ACTION_CREATE_DOCUMENT)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            // The user has chosen a location and filename.
            uri?.let {
                mainScreenViewModel.exportTimerDataToUri(it)
            }
        }
    )

    val onExportAllClick = {
        exportAllLauncher.launch(null)
    }

    val onExportClick = {
        val modelName = mainScreenUiState.timerData.modelName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val suggestedName = "$modelName.json"
        exportLauncher.launch(suggestedName)
    }

    val onImportClick = {
        importLauncher.launch(arrayOf("application/json"))
    }

    if (showOldDataWarningDialog) {
        AlertDialog(
            onDismissRequest = { mainScreenViewModel.dismissOldDataWarning() },
            title = { Text("Data Format Warning") },
            text = { Text("The timer data is from old Palm software and contains following " +
                    "non supported features\n" +
                    "- steps\n" +
                    "- more data rows than supported $MAX_TIMER_DATA_ROWS\n\n" +
                    "Please check your data carefully before using it.") },
            confirmButton = {
                Button(onClick = { mainScreenViewModel.dismissOldDataWarning() }) {
                    Text("OK")
                }
            }
        )
    }

    fun openFileSelectionDialog(title: String, action: (String) -> Unit) {
        dialogTitle = title
        dialogAction = action
        showDialog = true
    }

    fun handleFileSelection(filename: String) {
        dialogAction?.invoke(filename) // Perform the stored action (Open or Delete)
        showDialog = false             // Close the dialog
    }

    LaunchedEffect(Unit) {
        mainScreenViewModel.getSavedFilesList()
    }

    if (showDialog) {
        FileSelectionDialog(
            title = dialogTitle,
            files = mainScreenUiState.savedFiles,
            onDismiss = onDismissDialog,
            onFileSelected = { filename ->
                handleFileSelection(filename)
            }
        )
    }

    if (showMismatchDialog) {
        AlertDialog(
            onDismissRequest = { mainScreenViewModel.onDismissMismatchDialog() },
            title = { Text("Model mismatch") },
            text = { Text(mismatchMessage) },
            confirmButton = {
                Button(
                    onClick = { mainScreenViewModel.onConfirmMismatchDialog() }
                ) {
                    Text("OK (overwrite)")
                }
            },
            dismissButton = {
                Button(
                    onClick = { mainScreenViewModel.onDismissMismatchDialog() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    MainScreenContent(
        uiState = mainScreenUiState,
        onReadClick = { mainScreenViewModel.read() },
        onWriteClick = { mainScreenViewModel.write() },
        onOpenClick = {
            openFileSelectionDialog("Open file", mainScreenViewModel::loadJsonFromFile)
        },
        onSaveClick = { mainScreenViewModel.saveJsonToFile() },
        onDeleteClick = {
            openFileSelectionDialog("Delete file", mainScreenViewModel::deleteFile)
        },
        onDeviceSelected = { deviceName -> mainScreenViewModel.onDeviceSelected(deviceName) },
        onRefreshDevices = { mainScreenViewModel.refreshPairedDevices() },
        onUserSettingsClick = onUserSettingsClick,
        onExportAllClick = onExportAllClick,
        onExportClick = onExportClick,
        onImportClick = onImportClick,
        onAddRowClick = { mainScreenViewModel.addRow() },
        onDeleteRowClick = { mainScreenViewModel.deleteRow() },
        onNewTimerDataClick = { mainScreenViewModel.newTimerData(it) },
        onGridItemChanged = { index, newValue ->
            mainScreenViewModel.onGridItemChanged(
                index,
                newValue
            )
        },
        onUpdateConfigurationByte = { isSet, bitValue ->
            mainScreenViewModel.updateCheckBoxesWithByte(isSet, bitValue)
        },
        onUpdateTimerData = { updateAction ->
            mainScreenViewModel.updateTimerData(updateAction)
        },
        onUpdateServoSettingsByte = { isSet, bitValue ->
            mainScreenViewModel.updateCheckBoxesWithByte(
                isSet,
                bitValue,
                updateServoSettingsByte = true
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: UiState,
    onReadClick: () -> Unit,
    onWriteClick: () -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onUserSettingsClick: () -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    onExportAllClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onAddRowClick: () -> Unit,
    onDeleteRowClick: () -> Unit,
    onNewTimerDataClick: (Int) -> Unit,
    onUpdateConfigurationByte: (Boolean, Int) -> Unit,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = true) {
        onRefreshDevices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PT-Timer Control") },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    val onCloseMenu = remember { { showMenu = false } }

                    // Icon button to open the menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, // Use the standard "more options" icon
                            contentDescription = "More options"
                        )
                    }

                    MenuContent(
                        showMenu,
                        onCloseMenu,
                        focusManager,
                        onUserSettingsClick,
                        onExportAllClick,
                        onExportClick,
                        onImportClick,
                        onAddRowClick,
                        onDeleteRowClick,
                        onNewTimerDataClick
                    )
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
                onOpenClick = onOpenClick,
                onSaveClick = onSaveClick,
                onDeleteClick = onDeleteClick,
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
                .padding(innerPadding) // Apply the inner padding to account for scaffold's insets
                .fillMaxSize()
        ) {
            TabLayout(
                uiState,
                onGridItemChanged,
                onUpdateConfigurationByte,
                onUpdateTimerData,
                onUpdateServoSettingsByte,
            )
        }
    }
}


@Composable
fun ModelStatusBar(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabLayout(
    uiState: UiState,
    onGridItemChanged: (Int, String) -> Unit,
    onUpdateConfigurationByte: (Boolean, Int) -> Unit,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {

    ModelStatusBar(uiState, onUpdateTimerData)

    // Tab navigation
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {

        var state by remember { mutableIntStateOf(0) }
        val distanceThreshold = uiState.displaySwipeDistance
        val velocityThreshold = uiState.displaySwipeVelocity
        val titles = listOf("Timer setup", "Servo setup", "Settings")

        val velocityTracker = remember { VelocityTracker() }
        var dragAmountTotal by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragAmountTotal = 0f
                            velocityTracker.resetTracking()
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            dragAmountTotal += dragAmount
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position
                            )
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity().x

                            // Swipe Right
                            if (
                                dragAmountTotal > distanceThreshold ||
                                velocity > velocityThreshold
                            ) {
                                if (state > 0) state--
                            }

                            // Swipe Left
                            if (
                                dragAmountTotal < -distanceThreshold ||
                                velocity < -velocityThreshold
                            ) {
                                if (state < titles.size - 1) state++
                            }
                        }
                    )
                }
        ) {

            Column(Modifier.fillMaxSize()) {

                // TAB ROW
                PrimaryTabRow(
                    modifier = Modifier.height(30.dp),
                    selectedTabIndex = state
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = state == index,
                            onClick = { state = index },
                            modifier = Modifier
                                .height(28.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (state == index) Color(0xFFE0E0E0)
                                    else Color.Transparent
                                ),
                            text = {
                                Text(
                                    text = title,
                                    color = if (state == index) Color.Black else Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                // SCREEN CONTENT
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (state) {
                        0 -> TimerSetupTabContent(
                            uiState,
                            onGridItemChanged
                        )

                        1 -> ServoSetupTabContent(
                            uiState,
                            onUpdateTimerData,
                            onUpdateServoSettingsByte
                        )

                        2 -> SettingsTabContent(
                            uiState,
                            onUpdateConfigurationByte,
                            onUpdateTimerData
                        )
                    }
                }
            }
        }
    }
}

// Tab 0: Start Tab content
/*
@Composable
fun StartTabContent(uiState: UiState) {
    val timerData = uiState.timerData
    Text(timerData.modelName) // Display the model name from the timer data
}*/

@Composable
fun TimerSetupTabContent(
    uiState: UiState,
    onGridItemChanged: (Int, String) -> Unit,
) {
    TimerScreen(uiState, onGridItemChanged)
}

@Composable
fun ServoSetupTabContent(
    uiState: UiState,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    onUpdateServoSettingsByte: (Boolean, Int) -> Unit,
) {
    ServoSetupScreen(
        uiState,
        onUpdateTimerData,
        onUpdateServoSettingsByte
    )
}

@Composable
fun SettingsTabContent(
    uiState: UiState,
    onUpdateConfigurationByte: (Boolean, Int) -> Unit,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
) {
    SettingsScreen(
        uiState,
        onUpdateConfigurationByte,
        onUpdateTimerData
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomButtonsPanel(
    btDeviceList: List<String>,
    selectedDevice: String,
    onDeviceSelected: (String) -> Unit,
    onReadClick: () -> Unit,
    onWriteClick: () -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isExpanded by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val onShowPermissionDialog = remember { { showPermissionDialog = true } }
    val onDismissPermissionDialog = remember { { showPermissionDialog = false } }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text("Bluetooth Permissions Required") },
            text = {
                Text(
                    "This app requires 'Nearby devices' permission to find and connect " +
                            "to the timer. Please grant the permission in your phone's settings " +
                            "and restart the application."
                )
            },
            confirmButton = {
                Button(onClick = onDismissPermissionDialog) {
                    Text("OK")
                }
            }
        )
    }

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
                if (btDeviceList.isEmpty()) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onShowPermissionDialog),
                        value = "BT Permissions Missing",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("BT Device") },
                        singleLine = true,
                        maxLines = 1,
                        enabled = false // Disable the field to indicate an issue
                    )
                } else {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        value = if (btDeviceList.isNotEmpty()) selectedDevice else "Select BT Device",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("BT Device") },
                        singleLine = true,
                        maxLines = 1,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        }
                    )
                }

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
            val isReadButtonEnabled = true
            Button(
                onClick = onReadClick,
                enabled = isReadButtonEnabled
            ) {
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
            OutlinedButton(
                onClick = {
                    // force focus cleaning, so that last edit on grid is noticed
                    focusManager.clearFocus()
                    onSaveClick()
                })
            {
                Text(text = stringResource(R.string.button_save), fontSize = 14.sp)
            }
            OutlinedButton(onClick = onDeleteClick) {
                Text(text = stringResource(R.string.button_delete), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun MenuContent(
    showMenu: Boolean,
    onCloseMenu: () -> Unit,
    focusManager: FocusManager,
    onUserSettingsClick: () -> Unit,
    onExportAllClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onAddRowClick: () -> Unit,
    onDeleteRowClick: () -> Unit,
    onNewTimerDataClick: (Int) -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onCloseMenu() }
    ) {
        DropdownMenuItem(
            text = { Text("User settings") },
            onClick = {
                onCloseMenu()
                onUserSettingsClick()
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp)
        )
        DropdownMenuItem(
            text = { Text("Export all to files") },
            onClick = {
                focusManager.clearFocus()
                onCloseMenu()
                onExportAllClick()
            }
        )
        DropdownMenuItem(
            text = { Text("Export to file") },
            onClick = {
                focusManager.clearFocus()
                onCloseMenu()
                onExportClick()
            }
        )
        DropdownMenuItem(
            text = { Text("Import from file") },
            onClick = {
                focusManager.clearFocus()
                onCloseMenu()
                onImportClick()
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp)
        )
        DropdownMenuItem(
            text = { Text("Add row below") },
            onClick = {
                focusManager.clearFocus()
                onCloseMenu()
                onAddRowClick()
            }
        )
        DropdownMenuItem(
            text = { Text("Delete row") },
            onClick = {
                focusManager.clearFocus()
                onCloseMenu()
                onDeleteRowClick()
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("New F1A/H timer") },
            onClick = {
                onCloseMenu()
                onNewTimerDataClick(TIMER_TYPE_F1A)
            }
        )
        DropdownMenuItem(
            text = { Text("New F1B timer") },
            onClick = {
                onCloseMenu()
                onNewTimerDataClick(TIMER_TYPE_F1B)
            }
        )
        DropdownMenuItem(
            text = { Text("New F1Q timer") },
            onClick = {
                onCloseMenu()
                onNewTimerDataClick(TIMER_TYPE_F1Q)
            }
        )
        DropdownMenuItem(
            text = { Text("New P-30 timer") },
            onClick = {
                onCloseMenu()
                onNewTimerDataClick(TIMER_TYPE_P30)
            }
        )
        DropdownMenuItem(
            text = { Text("New E-36 timer") },
            onClick = {
                onCloseMenu()
                onNewTimerDataClick(TIMER_TYPE_E36)
            }
        )
    }
}

@Composable
fun FileSelectionDialog(
    title: String,
    files: List<String>,
    onDismiss: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    if (files.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text("There are no saved files to select.") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(files) { filename ->
                        Text(
                            text = filename,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFileSelected(filename) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
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
        selectedBtDevice = "Petri-PT-Timer",
        timerData = TimerData(
            modelName = "Model 15",
            modelId = 1,
            modelType = 2
        )
    )

    // Call the stateless composable with fake data and empty lambdas
    MainScreenContent(
        uiState = fakeUiState,
        onReadClick = {},
        onWriteClick = {},
        onOpenClick = {},
        onSaveClick = {},
        onDeleteClick = {},
        onDeviceSelected = {},
        onRefreshDevices = {},
        onUserSettingsClick = {},
        onExportAllClick = {},
        onExportClick = {},
        onImportClick  = {},
        onAddRowClick = {},
        onDeleteRowClick = {},
        onNewTimerDataClick = {},
        onGridItemChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") },
        onUpdateConfigurationByte = { _, _ -> },
        onUpdateTimerData = { _ -> },
        onUpdateServoSettingsByte = { newSettings, position -> println("Servo settings byte updated: $newSettings at position $position") },
    )
}

