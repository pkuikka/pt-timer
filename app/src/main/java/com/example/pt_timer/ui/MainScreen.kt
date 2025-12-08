package com.example.pt_timer.ui

// ... other imports
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pt_timer.R

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
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
        onSettingsClick = onSettingsClick,
        onModelNameChanged = { newName -> mainScreenViewModel.onModelNameChanged(newName) },
        onModelIdChanged = { newIdString -> mainScreenViewModel.onModelIdChanged(newIdString) },
        onModelSetChanged = { newSetString -> mainScreenViewModel.onModelSetChanged(newSetString) },
        onDeleteRowClick = { mainScreenViewModel.deleteRow() },
        onGridItemChanged = { index, newValue ->
            mainScreenViewModel.onGridItemChanged(
                index,
                newValue
            )
        },
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
    onSettingsClick: () -> Unit,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    onDeleteRowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = true) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onRefreshDevices()
        } else {
            onRefreshDevices()
            Toast.makeText(context, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
        }
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

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { onCloseMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                onCloseMenu()
                                onSettingsClick()
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
                onModelNameChanged,
                onModelIdChanged,
                onModelSetChanged,
                onGridItemChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabLayout(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Persisting the selected tab index
    var state by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Timer setup", "Servo setup", "information")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // Tab Row for navigating between different sections
        PrimaryTabRow(
            modifier = Modifier.height(35.dp),
            selectedTabIndex = state
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.height(25.dp),
                    selected = state == index,
                    onClick = { state = index },
                    selectedContentColor = Color.DarkGray,
                    unselectedContentColor = Color.Gray,
                    text = {
                        Text(
                            modifier = Modifier.fillMaxSize(),
                            text = title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 15.sp
                        )
                    }
                )
            }
        }

        // Tab content based on the selected index
        when (state) {
            //0 -> StartTabContent(uiState)
            0 -> TimerSetupTabContent(
                uiState,
                onModelNameChanged,
                onModelIdChanged,
                onModelSetChanged,
                onGridItemChanged
            )

            1 -> ServoSetupTabContent(
                uiState,
                onModelNameChanged,
                onModelIdChanged,
                onModelSetChanged
            )
            // Add other tabs as necessary
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

// Tab 1: Timer Setup content
@Composable
fun TimerSetupTabContent(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
) {
    TimerLayoutRefresh(
        uiState,
        onModelNameChanged,
        onModelIdChanged,
        onModelSetChanged
    ) // Possibly refresh the timer setup
    TimerDataGridLayout(uiState, onGridItemChanged)
}

@Composable
fun ServoSetupTabContent(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
) {
    ServoSetupScreen(
        uiState,
        onModelNameChanged,
        onModelIdChanged,
        onModelSetChanged
    ) // Possibly refresh the timer setup
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
                    value = if (btDeviceList.isNotEmpty()) selectedDevice else "Select BT Device",
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
        selectedBtDevice = "Petri-PT-Timer"
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
        onSettingsClick = {},
        onModelNameChanged = {},
        onModelIdChanged = {},
        onModelSetChanged = {},
        onDeleteRowClick = {},
        onGridItemChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value") }
    )
}

