package com.example.pt_timer.ui

// ... other imports
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextOverflow
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
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onRefreshDevices()
        }
        else{
            onRefreshDevices()
            Toast.makeText(context, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
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
                .padding(innerPadding) // Apply the inner padding to account for scaffold's insets
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Make the column scrollable
            //verticalArrangement = Arrangement.Center,
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabLayout(uiState,onModelNameChanged, onModelIdChanged, onModelSetChanged, onGridItemChanged)
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
        //onOpenClick = { /* TODO: Call mainScreenViewModel.openFile() */ },
        //onSaveClick = { /* TODO: Call mainScreenViewModel.saveFile() */ },
        //onDeleteClick = { /* TODO: Call mainScreenViewModel.saveFile() */ },
        onModelNameChanged = { newName -> mainScreenViewModel.onModelNameChanged(newName) },
        onModelIdChanged = { newIdString -> mainScreenViewModel.onModelIdChanged(newIdString) },
        onModelSetChanged = { newSetString -> mainScreenViewModel.onModelSetChanged(newSetString) },
        onGridItemChanged = { index, newValue -> mainScreenViewModel.onGridItemChanged(index, newValue) },
    )
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
    var state by rememberSaveable { mutableStateOf(0) }
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
            0 -> TimerSetupTabContent(uiState,onModelNameChanged, onModelIdChanged, onModelSetChanged, onGridItemChanged)
            1  -> ServoSetupTabContent(uiState, onModelNameChanged, onModelIdChanged, onModelSetChanged)
        // Add other tabs as necessary
        }
    }
}

// Tab 0: Start Tab content
@Composable
fun StartTabContent(uiState: UiState) {
    val timerData = uiState.timerData
    Text(timerData.modelName) // Display the model name from the timer data
}

// Tab 1: Timer Setup content
@Composable
fun TimerSetupTabContent(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
    onGridItemChanged: (Int, String) -> Unit,
) {
    TimerLayoutRefresh(uiState,onModelNameChanged, onModelIdChanged, onModelSetChanged) // Possibly refresh the timer setup
    TimerDataGridLayout(uiState, onGridItemChanged)
}
@Composable
fun ServoSetupTabContent(
    uiState: UiState,
    onModelNameChanged: (String) -> Unit,
    onModelIdChanged: (String) -> Unit,
    onModelSetChanged: (String) -> Unit,
) {
    TimerLayoutRefresh(uiState,onModelNameChanged, onModelIdChanged, onModelSetChanged) // Possibly refresh the timer setup
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
                    value =   if (btDeviceList.isNotEmpty()) selectedDevice else "Select BT Device",
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
        onModelNameChanged = {},
        onModelIdChanged = {},
        onModelSetChanged = {},
        onGridItemChanged = { index, value -> println("Grid item changed: Index = $index, Value = $value")}
    )
}

