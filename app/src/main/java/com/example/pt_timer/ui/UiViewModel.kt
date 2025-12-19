package com.example.pt_timer.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

    private val Context.dataStore by preferencesDataStore(name = "settings")
    private fun getCommentKey(modelName: String) =
        stringPreferencesKey("comment_$modelName")


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

        // Update write timestamp before writing it to timer
        val newTimeStamp = SimpleDateFormat("yyMMddHHmm", Locale.US).format(Date())
        updateTimerData { copy(writeTimeStamp = newTimeStamp) }

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

    fun loadCommentForModel(modelName: String) {
        viewModelScope.launch {
            applicationContext.dataStore.data
                .map { preferences ->
                    // Return the comment or empty string if none exists
                    preferences[getCommentKey(modelName)] ?: ""
                }
                .collect { storedComment ->
                    // Update the UI state without triggering a full file load
                    _uiState.update { currentState ->
                        currentState.copy(
                            timerData = currentState.timerData.copy(comments = storedComment)
                        )
                    }
                }
        }
    }

    // 3. Function to save the comment whenever it changes
    fun saveCommentForModel(modelName: String, comment: String) {
        viewModelScope.launch {
            applicationContext.dataStore.edit { preferences ->
            preferences[getCommentKey(modelName)] = comment
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

        val modelSet = _uiState.value.timerData.modelSet


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
                        // Create a mutable copy of the outer list
                        val updatedTimeValues = currentState.timerData.timeValues.toMutableList().apply {
                            // Get the specific inner list that needs updating
                            val innerList = this[modelSet].toMutableList().apply {
                                // Update the value at the correct index
                                this[row - 1] = newDoubleValue
                            }
                            // Replace the old inner list with the updated one
                            this[modelSet] = innerList
                        }
                        currentState.timerData.copy(timeValues = updatedTimeValues)
                    }

                    2 -> { // Servo 1 values
                        val updatedServoValues = currentState.timerData.servo1Values.toMutableList().apply {
                            val innerList = this[modelSet].toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                            this[modelSet] = innerList
                        }
                        currentState.timerData.copy(servo1Values = updatedServoValues)
                    }

                    3 -> { // Servo 2 values
                        val updatedServoValues = currentState.timerData.servo2Values.toMutableList().apply {
                            val innerList = this[modelSet].toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                            this[modelSet] = innerList
                        }
                        currentState.timerData.copy(servo2Values = updatedServoValues)
                    }

                    4 -> { // Servo 3 values
                        val updatedServoValues = currentState.timerData.servo3Values.toMutableList().apply {
                            val innerList = this[modelSet].toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                            this[modelSet] = innerList
                        }

                        currentState.timerData.copy(servo3Values = updatedServoValues)
                    }
                    5 -> { // Servo 4 values
                        val updatedServoValues = currentState.timerData.servo4Values.toMutableList().apply {
                            val innerList = this[modelSet].toMutableList().apply {
                                this[row - 1] = newIntValue
                            }
                            this[modelSet] = innerList
                        }
                        currentState.timerData.copy(servo4Values = updatedServoValues)
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

    private val _showOldDataWarningDialog = MutableStateFlow(false)
    val showOldDataWarningDialog: StateFlow<Boolean> = _showOldDataWarningDialog.asStateFlow()

    fun dismissOldDataWarning() {
        _showOldDataWarningDialog.value = false
    }

    private fun processIncomingPacket(packetBytes: ByteArray) {
        try {
            // Use the factory function to create the TimerData object
            val (newTimerData, needsWarning)  = TimerData.fromPacket(packetBytes)

            Log.i("DataParsing", "Updated data: $newTimerData")

            // Update the UI state with the newly parsed data
            _uiState.update { currentState ->
                currentState.copy(timerData = newTimerData)
            }

            loadCommentForModel(newTimerData.modelName)

            // If a warning is needed, update the dialog state
            if (needsWarning) {
                Log.w("DataParsing", "Old data format detected, flagging warning dialog.")
                _showOldDataWarningDialog.value = true
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
                loadCommentForModel(loadedTimerData.modelName)
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

            saveCommentForModel(currentTimerData.modelName, currentTimerData.comments)
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
        }
    }

    fun deleteFile(filename: String) {
        viewModelScope.launch {
            try {
                val file = File(applicationContext.filesDir, filename)
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        saveCommentForModel(filename, "")
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

    fun exportAllFilesToDirectory(treeUri: Uri) {
        viewModelScope.launch {
            // Get the directory the user picked
            val parentDirectory = androidx.documentfile.provider.DocumentFile.fromTreeUri(applicationContext, treeUri)
            if (parentDirectory == null || !parentDirectory.canWrite()) {
                Log.e("UiViewModel", "Cannot write to the selected directory.")
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Error: Cannot write to selected folder.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Get the list of all internal files
            val internalFiles = applicationContext.filesDir.listFiles { _, name -> name.endsWith(".json") }
            if (internalFiles.isNullOrEmpty()) {
                Log.i("UiViewModel", "No files to export.")
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "No saved files to export.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            var successCount = 0
            var failureCount = 0

            // Loop through each internal file and export it
            for (file in internalFiles) {
                try {
                    // Create a new file in the selected external directory
                    val newFile = parentDirectory.createFile("application/json", file.name)
                    if (newFile == null) {
                        failureCount++
                        continue // Skip to the next file
                    }

                    // Read the content of the internal file
                    val content = file.readBytes()

                    // Write the content to the new external file
                    applicationContext.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                        outputStream.write(content)
                    }
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    Log.e("UiViewModel", "Failed to export file: ${file.name}", e)
                }
            }

            // Report the result to the user
            val message = if (failureCount > 0) {
                "Export complete. Success: $successCount, Failed: $failureCount"
            } else {
                "Exported $successCount files successfully."
            }
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importTimerDataFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                // Use a content resolver to open an input stream from the URI
                applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val loadedTimerData = Json.decodeFromString<TimerData>(jsonString)

                    _uiState.update { it.copy(timerData = loadedTimerData) }
                    loadCommentForModel(loadedTimerData.modelName)

                    Log.i("UiViewModel", "File imported successfully from URI: $uri")
                    // Show a confirmation toast
                    android.os.Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, "Imported successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "Failed to import file from URI: $uri", e)
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Error: Failed to import file.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun exportTimerDataToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                // Get the current data to save
                val currentTimerData = _uiState.value.timerData
                val json = Json { prettyPrint = true; encodeDefaults = true }
                val jsonString = json.encodeToString(currentTimerData)

                // Use a content resolver to open an output stream to the URI
                applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }

                Log.i("UiViewModel", "File exported successfully to URI: $uri")
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Exported successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UiViewModel", "Failed to export file to URI: $uri", e)
                android.os.Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Error: Failed to export file.", Toast.LENGTH_SHORT).show()
                }
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

    fun copyDataSet(sourceSet: Int, destinationSet: Int) {
        _uiState.update { currentState ->
            val currentTimerData = currentState.timerData

            // Make mutable copies of all the data tables
            val timeValuesTable = currentTimerData.timeValues.toMutableList()
            val servo1ValuesTable = currentTimerData.servo1Values.toMutableList()
            val servo2ValuesTable = currentTimerData.servo2Values.toMutableList()
            val servo3ValuesTable = currentTimerData.servo3Values.toMutableList()
            val servo4ValuesTable = currentTimerData.servo4Values.toMutableList()
            val stepValuesTable = currentTimerData.stepValues.toMutableList()

            // Copy the data from the source index to the destination index
            timeValuesTable[destinationSet] = timeValuesTable[sourceSet]
            servo1ValuesTable[destinationSet] = servo1ValuesTable[sourceSet]
            servo2ValuesTable[destinationSet] = servo2ValuesTable[sourceSet]
            servo3ValuesTable[destinationSet] = servo3ValuesTable[sourceSet]
            servo4ValuesTable[destinationSet] = servo4ValuesTable[sourceSet]
            stepValuesTable[destinationSet] = stepValuesTable[sourceSet]

            // Create a new TimerData object with the updated tables
            val updatedTimerData = currentTimerData.copy(
                timeValues = timeValuesTable,
                servo1Values = servo1ValuesTable,
                servo2Values = servo2ValuesTable,
                servo3Values = servo3ValuesTable,
                servo4Values = servo4ValuesTable,
                stepValues = stepValuesTable
            )

            // Update the state
            currentState.copy(timerData = updatedTimerData)
        }
        // Optionally, show a toast message to confirm the copy
        android.os.Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, "Copied data from set $sourceSet to $destinationSet", Toast.LENGTH_SHORT).show()
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

