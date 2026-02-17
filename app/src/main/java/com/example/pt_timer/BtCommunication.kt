package com.example.pt_timer

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.util.UUID

// Define constants used in the class
private const val TAG = "BtCommunication"

// This is a standard UUID for SPP (Serial Port Profile). You can use it for testing.
private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
const val REQUEST_ENABLE_BT = 1 // Moved here to be accessible

class BtCommunication(private val context: Context) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mmServerSocket: BluetoothSocket? = null
    private var btSerialInputStream: InputStream? = null
    private var btSerialOutputStream: OutputStream? = null
    private var workerThread: Thread? = null

    private val permissionRequestCode = 1001

    // Function to check if all necessary Bluetooth permissions are granted
    fun checkBlePermissions(): Boolean {
        val permissionsToRequest = mutableListOf<String>()

        // Android 12+ (API level 31 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addPermissionIfNeeded(permissionsToRequest, Manifest.permission.BLUETOOTH_CONNECT)
            addPermissionIfNeeded(permissionsToRequest, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // For Android versions below Android 12 (Android 10 and 11)
            addPermissionIfNeeded(permissionsToRequest, Manifest.permission.BLUETOOTH_ADMIN)
            addPermissionIfNeeded(permissionsToRequest, Manifest.permission.BLUETOOTH)
            addPermissionIfNeeded(permissionsToRequest, Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // If no permissions need to be requested, return true
        if (permissionsToRequest.isEmpty()) {
            return true
        }

        // Request the missing permissions
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                permissionsToRequest.toTypedArray(),
                permissionRequestCode
            )
        }
        return false
    }

    private fun addPermissionIfNeeded(
        permissionsToRequest: MutableList<String>,
        permission: String
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(permission)
        }
    }

    // Function to get paired Bluetooth devices
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getPairedDevices(): List<String> {
        // Check permissions before proceeding
        if (!checkBlePermissions()) {
            // If permissions are not granted, return empty list
            return emptyList()
        }

        // Check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null) {
            Log.w("Bluetooth", "Device does not support Bluetooth")
            return emptyList()
        }

        if (!bluetoothAdapter.isEnabled) {
            // Optionally, you can prompt the user to enable Bluetooth if it's disabled
            Log.w("Bluetooth", "Bluetooth is disabled, please enable it.")
            return emptyList()
        }

        // Fetch paired devices
        val pairedDevices = bluetoothAdapter.bondedDevices
        return pairedDevices.map { it.name }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectDevice(selectedDevice: String, onConnectionResult: (Boolean) -> Unit) {
        if (bluetoothAdapter == null) {
            toastAndLog("Device doesn't support Bluetooth", logLevel = "Log.e")
            onConnectionResult(false)
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (context is Activity) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    toastAndLog("BLUETOOTH_CONNECT permission not granted.", logLevel = "Log.w")
                    onConnectionResult(false)
                    return
                }
            }
        }

        scope.launch {
            if (selectedDevice == "") {
                toastAndLog("No BlueTooth device selected.", logLevel = "Log.w")
                onConnectionResult(false)
                return@launch
            }

            // 1. Ensure we aren't discoverying (canceling discovery is often the "fix")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery()
            }

            var deviceToConnect: android.bluetooth.BluetoothDevice? = null

            // 2. Retry lookup twice with a small delay
            repeat(2) { attempt ->
                deviceToConnect = bluetoothAdapter.bondedDevices.find { it.name == selectedDevice }
                if (deviceToConnect == null) {
                    Log.w(TAG, "Warning: Bonded device lookup attempt ${attempt + 1} failed.")
                    kotlinx.coroutines.delay(5000) // Wait 1 second before retry
                }
            }

            if (deviceToConnect == null) {
                toastAndLog("ERROR: Device '$selectedDevice' not found in paired devices.", logLevel = "Log.e")
                onConnectionResult(false)
                return@launch
            }

            toastAndLog(message = "Connecting to device ${deviceToConnect.name} at ${deviceToConnect.address}...")

            try {
                // 2. Timeout for the actual connection attempt (10 seconds)
                val isConnected = withTimeoutOrNull(10000) {
                    try {
                        val socket: BluetoothSocket? =
                            deviceToConnect.createRfcommSocketToServiceRecord(SPP_UUID)
                        mmServerSocket = socket
                        mmServerSocket?.connect()
                        true
                    } catch (e: IOException) {
                        Log.e(TAG, "Socket connection failed", e)
                        false
                    }
                } ?: false

                if (isConnected) {
                    btSerialOutputStream = mmServerSocket?.outputStream
                    btSerialInputStream = mmServerSocket?.inputStream
                    onConnectionResult(true)
                } else {
                    toastAndLog(
                        "ERROR: Connection to ${deviceToConnect.name} timed out or failed.",
                        logLevel = "Log.e"
                    )
                    onConnectionResult(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during connection", e)
                onConnectionResult(false)
            }
        }
    }

    fun timerCommunication(
        packetToSend: ByteArray? = null,
        writeDelay: Long = 100,
        onDataReceived: (ByteArray) -> Unit,
        onConfirmationRequired: ((message: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit)? = null
    ) {
        var stopWorker = false
        var retryCount = 0

        fun getUnsignedByte(index: Int, convertBytes: ByteArray): Int =
            convertBytes[index].toInt() and 0xFF

        if (btSerialInputStream == null || btSerialOutputStream == null) {
            toastAndLog("ERROR: BT serial streams are not connected!")
            return
        }
        workerThread = Thread {
            while (!Thread.currentThread().isInterrupted && !stopWorker) {
                sleep(200)
                try {
                    val bytesAvailable = btSerialInputStream!!.available()

                    when (bytesAvailable) {
                        0 -> {
                            Log.i(TAG, "Bytes available: 0")
                            btSerialOutputStream!!.write("P".toByteArray())
                        }

                        1 -> {
                            Log.i(TAG, "Bytes available: 1")
                            btSerialOutputStream!!.write("s".toByteArray())
                        }

                        263 -> {
                            val packetBytes = ByteArray(bytesAvailable)
                            btSerialInputStream!!.read(packetBytes) // Empty buffer always
                            val buffer = String(packetBytes, charset("utf-8"))
                            Log.i(TAG, "Bytes available: 263, Buffer: $buffer")

                            // We must post it to the main thread to be safe
                            if (packetToSend == null) {  // only send in read mode
                                toastAndLog("Timer read.")
                                Handler(Looper.getMainLooper()).post {
                                    val processedPacket = packetBytes.drop(1).toByteArray()
                                    onDataReceived(processedPacket)
                                }
                                cancel()
                                stopWorker = true
                            }

                            // Write new data if packetToSend is not null
                            if (packetToSend != null) {
                                val currentPacket = packetBytes.drop(1).toByteArray()
                                val currentModelType = getUnsignedByte(1, currentPacket)
                                val writeModelType = getUnsignedByte(1, packetToSend)
                                val currentModelId = getUnsignedByte(2, currentPacket)
                                val writeModelId = getUnsignedByte(2, packetToSend)
                                val currentModelSet = getUnsignedByte(3, currentPacket)
                                val writeModelSet = getUnsignedByte(3, packetToSend)

                                if (currentModelId == writeModelId && currentModelType == writeModelType && currentModelSet == writeModelSet) {
                                    toastAndLog("Timer read and model validation OK. Writing...")
                                    Log.i(
                                        TAG,
                                        "All ok: Model type $writeModelType and model ID $writeModelId and model set $writeModelSet"
                                    )
                                    val writeResult = writeData(
                                        packetBytes = packetToSend,
                                        writeDelay = writeDelay
                                    )

                                    if (writeResult != "") {
                                        Handler(Looper.getMainLooper()).post {
                                            // Pass the current packet (containing battery/temp readings) back to UI
                                            onDataReceived(currentPacket)
                                        }
                                    }

                                    cancel()
                                    stopWorker = true
                                } else {
                                    val warningMessage = "Warning: Model doesn't match: \n" +
                                            "type: $currentModelType <> $writeModelType\n" +
                                            "id: $currentModelId <> $writeModelId\n" +
                                            "set: $currentModelSet <> $writeModelSet"
                                    Log.w(TAG, warningMessage)
                                    Handler(Looper.getMainLooper()).post {
                                        onConfirmationRequired?.invoke(
                                            warningMessage,
                                            {
                                                scope.launch {
                                                    val writeResult = writeData(
                                                        packetBytes = packetToSend,
                                                        writeDelay = writeDelay
                                                    )
                                                    if (writeResult != "") {
                                                        Handler(Looper.getMainLooper()).post {
                                                            // Pass the current packet (containing battery/temp readings) back to UI
                                                            onDataReceived(currentPacket)
                                                        }
                                                    }
                                                    cancel()
                                                }
                                            },
                                            {
                                                cancel()
                                            }
                                        )
                                    }
                                    // Crucially, we return here to stop the function from proceeding
                                    // to the cancel() call below, keeping the connection open.
                                    return@Thread
                                }
                            }
                        }

                        else -> {
                            Log.i(TAG, "Bytes available: $bytesAvailable")
                        }
                    }
                    retryCount += 1
                    if (retryCount > 100) {  // Abort after 20s of trying
                        val packetBytes = ByteArray(bytesAvailable)
                        btSerialInputStream!!.read(packetBytes) // Empty buffer always
                        toastAndLog(message = "ERROR: Timer communication failed! " +
                                              "Bytes available: $bytesAvailable, " +
                                              "Buffer: $packetBytes", logLevel = "Log.e"
                        )
                        cancel()
                        stopWorker = true
                    }
                } catch (_: IOException) {
                    cancel()
                    stopWorker = true
                }
            }
        }
        workerThread?.start()
    }

    fun writeData(packetBytes: ByteArray, writeDelay: Long = 100): String {
        var writeDataString = ""
        val buffer = String(packetBytes, charset("utf-8"))
        val moduloForWriteMessages = if (writeDelay > 200L) 20 else 50

        Log.i(TAG, "Writing data bytes: ${packetBytes.size}, buffer $buffer")

        // Start data dump with "D"
        sleep(1000)
        btSerialOutputStream!!.write("D".toByteArray())
        btSerialOutputStream!!.flush()
        sleep(1000)

        // Note thet we are skipping the 0 position in the array.
        for (i in 1..252) {  //if this is 251 it doesn't complete and crashes?
            val value: Int = packetBytes[i].toInt()

            if ((i == 1) || (i.mod(moduloForWriteMessages) == 0)) {
                  toastAndLog("Writing $i")
            }
            btSerialOutputStream!!.write(value)
            btSerialOutputStream!!.flush()
            sleep(writeDelay)
        }

        // Verifying data (wait max 10s)
        for (i in 1..20) {
            sleep(500)
            val bytesAvailable = btSerialInputStream!!.available()
            Log.i(TAG, "$i, bytes available After write: $bytesAvailable")

            if (bytesAvailable == 256) {
                break
            }
        }
        val bytesAvailableAfterWrite = btSerialInputStream!!.available()
        if (bytesAvailableAfterWrite == 256) {
            val dataAfterWrite = ByteArray(bytesAvailableAfterWrite)
            btSerialInputStream!!.read(dataAfterWrite) // Empty buffer always

            // Convert data to integer string and verify it is the same as sent one
            for (i in 0..250) {
                writeDataString = writeDataString + dataAfterWrite[i].toInt().toString() + ","
                if (packetBytes[i+1] != dataAfterWrite[i]) {
                    writeDataString = ""
                    toastAndLog("Write verification failed!\nByte $i doesn't match: " +
                            "${packetBytes[i+1]} != ${dataAfterWrite[i]}", logLevel = "Log.e"
                    )
                    return writeDataString
                }
            }
            toastAndLog("Write successful!")
        } else {
            writeDataString = ""
            toastAndLog("Write verification failed! Only bytes available: " +
                        "$bytesAvailableAfterWrite", logLevel = "Log.e"
            )
        }
        return writeDataString
    }


    private fun toastAndLog(message: String, logLevel: String = "Log.i") {
        Handler(Looper.getMainLooper()).post {
            when (logLevel) {
                "Log.e" -> {
                    Log.e(TAG, message)
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }

                "Log.w" -> {
                    Log.w(TAG, message)
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }

                else -> {
                    Log.i(TAG, message)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cancel() {
        // Send " " to close thw timer serial communication
        if (btSerialOutputStream != null)
            btSerialOutputStream!!.write(" ".toByteArray())
        try {
            mmServerSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }

}
