package com.example.pt_timer.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pt_timer.BtCommunication
import com.example.pt_timer.PtTimerApplication
import com.example.pt_timer.data.TimerData
import com.example.pt_timer.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

class UiViewModel(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val applicationContext: Context
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val btCommunication = BtCommunication(application.applicationContext)

    private val _showMismatchDialog = MutableStateFlow(false)
    val showMismatchDialog: StateFlow<Boolean> = _showMismatchDialog.asStateFlow()
    private val _mismatchDialogMessage = MutableStateFlow("")
    val mismatchDialogMessage: StateFlow<String> = _mismatchDialogMessage.asStateFlow()

    private var onConfirmWriteAction: (() -> Unit)? = null
    private var onCancelWriteAction: (() -> Unit)? = null

    fun onConfirmMismatchDialog() {
        onConfirmWriteAction?.invoke()
        _showMismatchDialog.value = false
    }

    fun onDismissMismatchDialog() {
        onConfirmWriteAction = null
        onCancelWriteAction?.invoke()
        onCancelWriteAction = null
        _showMismatchDialog.value = false
    }

    fun getSavedFilesList() {
        val internalDir = applicationContext.filesDir
        val files = internalDir.listFiles { _, name -> name.endsWith(".json") }
            ?.map { it.name } ?: emptyList()
        _uiState.update { it.copy(savedFiles = files) }
    }

    init {
        resetMainScreen()

        viewModelScope.launch {
            userPreferencesRepository.timerWriteDelayMillis.collect { timerWriteDelayMillis ->
                // 3. Update the UI state with the value from the repository
                _uiState.update { currentState ->
                    currentState.copy(writeCommunicationDelay = timerWriteDelayMillis)
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.selectedBtDevice.collect { deviceName ->
                _uiState.update { currentState ->
                    currentState.copy(selectedBtDevice = deviceName)
                }
            }
        }
    }

    fun resetMainScreen() {

    }

    // Function to update the list of BT devices in the state
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun refreshPairedDevices() {
        val pairedDevices =
            btCommunication.getPairedDevices() // Assume this function exists in BtCommunication
        _uiState.update { currentState ->
            currentState.copy(btDevices = pairedDevices)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun read() {
        // Read the timer data via bluetooth
        btCommunication.connectDevice(selectedDevice = uiState.value.selectedBtDevice) { isSuccess ->
            if (isSuccess) {
                btCommunication.timerCommunication(
                    onDataReceived = { processIncomingPacket(it) }
                )
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun write() {
        val currentTimerData = _uiState.value.timerData
        val packetToSend = currentTimerData.toPacket()

        btCommunication.connectDevice(selectedDevice = uiState.value.selectedBtDevice) { isSuccess ->
            if (isSuccess) {
                btCommunication.timerCommunication(
                    packetToSend = packetToSend,
                    writeDelay = uiState.value.writeCommunicationDelay.toLong(),
                    onDataReceived = { }, // NO UI updates on write operations
                    onConfirmationRequired = { message, onConfirm, onCancel ->
                        onConfirmWriteAction = onConfirm
                        onCancelWriteAction = onCancel
                        _mismatchDialogMessage.value = message
                        _showMismatchDialog.value = true
                    }
                )
            }
        }
    }

    fun onDelayChanged(delay: Float) {
        viewModelScope.launch {
            userPreferencesRepository.saveLayoutPreference(delay)
        }
    }

    fun onDeviceSelected(deviceName: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedDevice(deviceName)
        }
    }

    fun onModelNameChanged(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                timerData = currentState.timerData.copy(modelName = newName)
            )
        }
    }

    fun onModelIdChanged(newId: String) {
        if (newId.isEmpty()) return
        _uiState.update { currentState ->
            currentState.copy(
                timerData = currentState.timerData.copy(modelId = newId.toIntOrNull() ?: 0)
            )
        }
    }

    fun onModelSetChanged(newSet: String) {
        if (newSet.isEmpty()) return
        _uiState.update { currentState ->
            currentState.copy(
                timerData = currentState.timerData.copy(modelSet = newSet.toIntOrNull() ?: 0)
            )
        }
    }

    fun onGridItemChanged(index: Int, newValue: String) {
        val newIntValue = newValue.toIntOrNull() ?: 0

        val row = index / 6
        val col = index % 6

        Log.i("UiViewModel", "onGridItemChanged: row=$row, col=$col, newValue=$newValue")

        // We only process changes if they are NOT in the header row or column.
        if (row > 0 && col > 0) {
            _uiState.update { currentState ->
                val updatedTimerData = when (col) {
                    1 -> { // Time values (which are Doubles, so we need to handle them differently)
                        val newDoubleValue = newValue.toDoubleOrNull() ?: 0.0
                        val updatedList = currentState.timerData.timeValues.toMutableList().apply {
                            this[row - 1] = newDoubleValue
                        }
                        currentState.timerData.copy(timeValues = updatedList)
                    }
                    2 -> { // Servo 1 values
                        val updatedList = currentState.timerData.servo1Values.toMutableList().apply {
                            this[row - 1] = newIntValue
                        }
                        currentState.timerData.copy(servo1Values = updatedList)
                    }
                    3 -> { // Servo 2 values
                        val updatedList = currentState.timerData.servo2Values.toMutableList().apply {
                            this[row - 1] = newIntValue
                        }
                        currentState.timerData.copy(servo2Values = updatedList)
                    }
                    4 -> { // Servo 3 values
                        val updatedList = currentState.timerData.servo3Values.toMutableList().apply {
                            this[row - 1] = newIntValue
                        }
                        currentState.timerData.copy(servo3Values = updatedList)
                    }
                    5 -> { // Servo 4 values
                        val updatedList = currentState.timerData.servo4Values.toMutableList().apply {
                            this[row - 1] = newIntValue
                        }
                        currentState.timerData.copy(servo4Values = updatedList)
                    }
                    // We assume there are only 5 editable columns.
                    else -> currentState.timerData // No change if the column is out of expected range.
                }

                // Update the UiState with the new TimerData object.
                currentState.copy(timerData = updatedTimerData)
            }
        }
        // If it's a header cell (row or col is 0), do nothing.
    }

    private fun processIncomingPacket(packetBytes: ByteArray) {
        try {
            // Use the factory function to create the TimerData object
            val newTimerData = TimerData.fromPacket(packetBytes)

            Log.i("DataParsing", "Updated data: $newTimerData")
            // Update the UI state with the newly parsed data
            _uiState.update { currentState ->
                currentState.copy(timerData = newTimerData)
            }
        } catch (e: IllegalArgumentException) {
            // Handle the case where the packet is the wrong size
            Log.e("DataParsing", "Received invalid packet: ${e.message}")
        } catch (e: Exception) {
            // Handle any other potential parsing errors
            Log.e("DataParsing", "Failed to parse timer data packet.", e)
        }
    }

    fun loadJsonFromFile(filename: String) {
        viewModelScope.launch {
            try {
                val file = File(applicationContext.filesDir, filename)
                val jsonString = file.bufferedReader().use { it.readText() }
                val loadedTimerData = Json.decodeFromString<TimerData>(jsonString)

                _uiState.update { it.copy(timerData = loadedTimerData) }
                Log.i("UiViewModel", "File loaded: $filename")
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Loaded: $filename", Toast.LENGTH_SHORT).show()
                }
            } catch (e: FileNotFoundException) {
                Log.e("UiViewModel", "File not found: $filename", e)
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Error: File not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "Failed to load file: $filename", e)
            }
        }
    }

    fun saveJsonToFile() {
        // We now generate the filename internally from the modelName
        val currentTimerData = _uiState.value.timerData
        val filename = currentTimerData.modelName.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".json"

        try {
            val json = Json { prettyPrint = true; encodeDefaults = true }
            val jsonString = json.encodeToString(currentTimerData)
            Log.i("UiViewModel", "File content to be saved to $filename: $jsonString")

            applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }

            Log.i("UiViewModel", "Successfully saved file to $filename")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "File saved successfully!", Toast.LENGTH_LONG).show()
            }
            getSavedFilesList()

        } catch (e: Exception) {
            Log.e("UiViewModel", "Failed to save file", e)
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "Error: Failed to save file.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteFile(filename: String) {
        viewModelScope.launch {
            try {
                val file = File(applicationContext.filesDir, filename)
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        Log.i("UiViewModel", "File deleted: $filename")
                        android.os.Handler(Looper.getMainLooper()).post {
                            Toast.makeText(applicationContext, "File deleted.", Toast.LENGTH_SHORT).show()
                        }
                        getSavedFilesList()
                    }
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "File deletion failed.", e)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PtTimerApplication)
                UiViewModel(
                    application = application,
                    userPreferencesRepository = application.userPreferencesRepository,
                    applicationContext = application.applicationContext
                )
            }
        }
    }
}

