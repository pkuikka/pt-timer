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
import com.example.pt_timer.data.MAX_TIMER_DATA_ROWS
import com.example.pt_timer.data.TimerData
import com.example.pt_timer.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
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
            userPreferencesRepository.displaySwipeVelocity.collect { displaySwipeVelocity ->
                // 3. Update the UI state with the value from the repository
                _uiState.update { currentState ->
                    currentState.copy(displaySwipeVelocity = displaySwipeVelocity)
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.displaySwipeDistance.collect { displaySwipeDistance ->
                // 3. Update the UI state with the value from the repository
                _uiState.update { currentState ->
                    currentState.copy(displaySwipeDistance = displaySwipeDistance)
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

    // Generic function to update the TimerData object in the state
    // Can be well used to edit e.g. single value
    fun updateTimerData(updateAction: TimerData.() -> TimerData) {
        _uiState.update { currentState ->
            val updatedTimerData = currentState.timerData.updateAction()
            currentState.copy(timerData = updatedTimerData)
        }
    }

    // Function to update the list of BT devices in the state

    //@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun refreshPairedDevices() {
        if(btCommunication.checkBlePermissions()){
            val pairedDevices =
                btCommunication.getPairedDevices() // Assume this function exists in BtCommunication
            _uiState.update { currentState ->
            currentState.copy(btDevices = pairedDevices)
            }
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

    fun onSwipeVelocityChanged(delay: Float) {
        viewModelScope.launch {
            userPreferencesRepository.saveSwipeVelocity(delay)
        }
    }

    fun onSwipeDistanceChange(delay: Float) {
        viewModelScope.launch {
            userPreferencesRepository.saveSwipeDistance(delay)
        }
    }

    fun onDeviceSelected(deviceName: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedDevice(deviceName)
        }
    }

    fun updateCheckBoxesWithByte(
        isSet: Boolean,
        bitValue: Int,
        updateServoSettingsByte: Boolean = false
    ) {
        Log.i(
            "UiViewModel", "updateCheckBoxesWithByte: isSet=$isSet," +
                    " bitValue=$bitValue, updateServoSettingsByte=$updateServoSettingsByte"
        )
        _uiState.update { currentState ->
            val currentByte = if (updateServoSettingsByte) {
                currentState.timerData.servoSettingsByte.toInt()
            } else {
                currentState.timerData.configurationByte.toInt()
            }

            val newByte = if (isSet) {
                // Set the bit (bitwise OR)
                currentByte or bitValue
            } else {
                // Unset the bit (bitwise AND with the inverted bitValue)
                currentByte and bitValue.inv()
            }

            val updatedTimerData = if (updateServoSettingsByte) {
                currentState.timerData.copy(servoSettingsByte = newByte.toByte())
            } else {
                currentState.timerData.copy(configurationByte = newByte.toByte())
            }

            currentState.copy(timerData = updatedTimerData)
        }
    }

    fun onGridItemChanged(index: Int, newValue: String) {
        val newIntValue = newValue.toIntOrNull() ?: 0

        val row = index / 6
        val col = index % 6

        Log.i("UiViewModel", "onGridItemChanged: row=$row, col=$col, newValue=$newValue")
        if (col == 0)
            _uiState.update { it.copy(selectedRow = row) }
        else
            _uiState.update { it.copy(selectedRow = -1) }

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
                        val updatedList =
                            currentState.timerData.servo1Values.toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                        currentState.timerData.copy(servo1Values = updatedList)
                    }

                    3 -> { // Servo 2 values
                        val updatedList =
                            currentState.timerData.servo2Values.toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                        currentState.timerData.copy(servo2Values = updatedList)
                    }

                    4 -> { // Servo 3 values
                        val updatedList =
                            currentState.timerData.servo3Values.toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                        currentState.timerData.copy(servo3Values = updatedList)
                    }

                    5 -> { // Servo 4 values
                        val updatedList =
                            currentState.timerData.servo4Values.toMutableList().apply {
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
                    Toast.makeText(applicationContext, "Loaded: $filename", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: FileNotFoundException) {
                Log.e("UiViewModel", "File not found: $filename", e)
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Error: File not found.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "Failed to load file: $filename", e)
            }
        }
    }

    fun saveJsonToFile() {
 /*       // We now generate the filename internally from the modelName
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
                Toast.makeText(applicationContext, "File saved successfully!", Toast.LENGTH_LONG)
                    .show()
            }
            getSavedFilesList()

        } catch (e: Exception) {
            Log.e("UiViewModel", "Failed to save file", e)
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "Error: Failed to save file.", Toast.LENGTH_LONG)
                    .show()
            }
        }*/
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
                            Toast.makeText(applicationContext, "File deleted.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        getSavedFilesList()
                    }
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "File deletion failed.", e)
            }
        }
    }

    fun addRow() {
        val rowToAdd = _uiState.value.selectedRow

        // make sure multiple adds don't happen with single row select
        _uiState.update { it.copy(selectedRow = -1) }

        if (rowToAdd == -1) {
            // No row is selected, do nothing or show a Toast
            Log.w("UiViewModel", "Add row action called but no row is selected.")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "Please select a row to add from column 1.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            return
        }

        // Check that 'static' rows are not broken
        if ((rowToAdd <= 3 && _uiState.value.timerData.modelType == 2) ||
            (rowToAdd <= 2 && _uiState.value.timerData.modelType == 1)
        ) {
            Log.i("UiViewModel", "Cannot add row here. Row=$rowToAdd")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "Cannot add row here.", Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }

        // Check that MAX_TIMER_DATA_ROWS is not broken
        if (uiState.value.timerData.numberOfDataRows + 1 > MAX_TIMER_DATA_ROWS) {
            Log.i("UiViewModel", "Cannot add more that $MAX_TIMER_DATA_ROWS rows.")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "Cannot add more that $MAX_TIMER_DATA_ROWS rows.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            return
        }

        Log.i("UiViewModel", "Adding row $rowToAdd...")
        _uiState.update { currentState ->
            val currentTimerData = currentState.timerData
            var newNumberOfRows = currentTimerData.numberOfDataRows + 1
            if (newNumberOfRows < 0) newNumberOfRows = 0

            // Create mutable copies of the lists to modify them
            val newTimeValues = currentTimerData.timeValues.toMutableList()
            val newServo1Values = currentTimerData.servo1Values.toMutableList()
            val newServo2Values = currentTimerData.servo2Values.toMutableList()
            val newServo3Values = currentTimerData.servo3Values.toMutableList()
            val newServo4Values = currentTimerData.servo4Values.toMutableList()

            // Shift all data rows down starting from the biggest row number
            for (i in (newNumberOfRows - 2) downTo (rowToAdd - 1)) {
                newTimeValues[i + 1] = newTimeValues[i]
                newServo1Values[i + 1] = newServo1Values[i]
                newServo2Values[i + 1] = newServo2Values[i]
                newServo3Values[i + 1] = newServo3Values[i]
                newServo4Values[i + 1] = newServo4Values[i]
            }

            // Remember to move the bunt safety line if it's affected
            var newSkipBuntGoToRow = currentTimerData.skipBuntGoToRow
            if (rowToAdd < newSkipBuntGoToRow) {
                newSkipBuntGoToRow += 1
            }

            // Create a new TimerData object with all the updated values
            val updatedTimerData = currentTimerData.copy(
                numberOfDataRows = newNumberOfRows,
                timeValues = newTimeValues,
                servo1Values = newServo1Values,
                servo2Values = newServo2Values,
                servo3Values = newServo3Values,
                servo4Values = newServo4Values,
                skipBuntGoToRow = newSkipBuntGoToRow
            )

            // Update the state with the new TimerData object
            currentState.copy(timerData = updatedTimerData)
        }
    }

    fun deleteRow() {
        val rowToDelete = _uiState.value.selectedRow

        // make sure multiple deletions don't happen with single row select
        _uiState.update { it.copy(selectedRow = -1) }

        if (rowToDelete == -1) {
            // No row is selected, do nothing or show a Toast
            Log.w("UiViewModel", "Delete row action called but no row is selected.")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "Please select a row to delete from column 1.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            return
        }

        // Check that 'static' rows are not deleted
        if ((rowToDelete <= 4 && _uiState.value.timerData.modelType == 2) ||
            (rowToDelete <= 3 && _uiState.value.timerData.modelType == 1)
        ) {
            Log.i("UiViewModel", "Cannot delete this row. Row=$rowToDelete")
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "Cannot delete this row.", Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }

        Log.i("UiViewModel", "Deleting row $rowToDelete...")
        _uiState.update { currentState ->
            val currentTimerData = currentState.timerData
            var newNumberOfRows = currentTimerData.numberOfDataRows - 1
            if (newNumberOfRows < 0) newNumberOfRows = 0

            // Create mutable copies of the lists to modify them
            val newTimeValues = currentTimerData.timeValues.toMutableList()
            val newServo1Values = currentTimerData.servo1Values.toMutableList()
            val newServo2Values = currentTimerData.servo2Values.toMutableList()
            val newServo3Values = currentTimerData.servo3Values.toMutableList()
            val newServo4Values = currentTimerData.servo4Values.toMutableList()

            // Shift all data rows up starting from the deleted row's index
            // Note: The loop should go up to 'newNumberOfRows'
            for (i in (rowToDelete - 1) until newNumberOfRows) {
                newTimeValues[i] = newTimeValues[i + 1]
                newServo1Values[i] = newServo1Values[i + 1]
                newServo2Values[i] = newServo2Values[i + 1]
                newServo3Values[i] = newServo3Values[i + 1]
                newServo4Values[i] = newServo4Values[i + 1]
            }

            // Remember to move the bunt safety line if it's affected
            var newSkipBuntGoToRow = currentTimerData.skipBuntGoToRow
            if (rowToDelete < newSkipBuntGoToRow) {
                newSkipBuntGoToRow -= 1
            }

            // Create a new TimerData object with all the updated values
            val updatedTimerData = currentTimerData.copy(
                numberOfDataRows = newNumberOfRows,
                timeValues = newTimeValues,
                servo1Values = newServo1Values,
                servo2Values = newServo2Values,
                servo3Values = newServo3Values,
                servo4Values = newServo4Values,
                skipBuntGoToRow = newSkipBuntGoToRow
            )

            // Update the state with the new TimerData object
            currentState.copy(timerData = updatedTimerData)
        }
    }

    fun newTimerData(timerType: Int) {
        val newTimerData = TimerData.createNew(timerType)

        if (newTimerData.modelType == 0 && timerType != 0) { // Check if it fell back to default
            Log.w("UiViewModel", "Unknown timer type: $timerType. Using defaults.")
        } else {
            Log.i("UiViewModel", "New TimerData created for type: $timerType")
        }

        _uiState.update { currentState ->
            currentState.copy(
                timerData = newTimerData
            )
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

