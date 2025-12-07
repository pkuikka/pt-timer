package com.example.pt_timer

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getPairedDevices(): List<String> {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        return bluetoothAdapter?.bondedDevices?.map { it.name } ?: emptyList()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectDevice(selectedDevice: String, onConnectionResult: (Boolean) -> Unit) {
        if (bluetoothAdapter == null) {
            toastAndLog("Device doesn't support Bluetooth", logLevel="Log.e")
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
                    toastAndLog("BLUETOOTH_CONNECT permission not granted.", logLevel="Log.w")
                    onConnectionResult(false)
                    return
                }
            }
        }

        // Check for BLUETOOTH_CONNECT permission before accessing bondedDevices
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            toastAndLog("BLUETOOTH_CONNECT permission not granted.", logLevel="Log.w")
            onConnectionResult(false)
            return
        }

        if (selectedDevice == "") {
            toastAndLog("No BlueTooth device selected.", logLevel="Log.w")
            onConnectionResult(false)
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        val deviceToConnect = pairedDevices?.find { it.name == selectedDevice }

        if (deviceToConnect == null) {
            Log.w(TAG, "Device '$selectedDevice' not found in paired devices.")
            onConnectionResult(false)
            return
        }

        toastAndLog(message = "Connecting to device ${deviceToConnect.name} at ${deviceToConnect.address}...")
        val socket: BluetoothSocket? = deviceToConnect.createRfcommSocketToServiceRecord(SPP_UUID)

        scope.launch {
            try {
                mmServerSocket = socket
                mmServerSocket?.connect()
                btSerialOutputStream = mmServerSocket?.outputStream
                btSerialInputStream = mmServerSocket?.inputStream
                onConnectionResult(true)
            } catch (_: IOException) {
                Handler(Looper.getMainLooper()).post {
                    toastAndLog(
                        message = "ERROR: Could not connect BT device $deviceToConnect socket!",
                        logLevel = "Log.e"
                    )
                }
                onConnectionResult(false)
            }
        }
        return
    }

    fun timerCommunication(packetToSend: ByteArray? = null,
                           writeDelay: Long = 100,
                           onDataReceived: (ByteArray) -> Unit,
                           onConfirmationRequired: ((message: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit)? = null) {
        var stopWorker = false
        var retryCount = 0

        fun getUnsignedByte(index: Int, convertBytes: ByteArray): Int = convertBytes[index].toInt() and 0xFF

        if (btSerialInputStream == null || btSerialOutputStream == null) {
            Handler(Looper.getMainLooper()).post {
                toastAndLog("ERROR: BT serial streams are not connected!")
            }
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

                            Handler(Looper.getMainLooper()).post {
                                toastAndLog("Timer read.")
                            }

                            // We must post it to the main thread to be safe
                            if (packetToSend == null) {  // only send in read mode
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
                                val currentModelType = getUnsignedByte (1, currentPacket)
                                val writeModelType = getUnsignedByte (1, packetToSend)
                                val currentModelId = getUnsignedByte (2, currentPacket)
                                val writeModelId = getUnsignedByte (2, packetToSend)
                                val currentModelSet = getUnsignedByte (3, currentPacket)
                                val writeModelSet = getUnsignedByte (3, packetToSend)

                                if (currentModelId == writeModelId  && currentModelType == writeModelType && currentModelSet == writeModelSet) {
                                    Log.i(
                                        TAG,
                                        "All ok: Model type $writeModelType and model ID $writeModelId and model set $writeModelSet"
                                    )
                                    writeData(packetBytes = packetToSend, writeDelay = writeDelay)
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
                                                    writeData(packetBytes = packetToSend, writeDelay = writeDelay)
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
                        Handler(Looper.getMainLooper()).post {
                            toastAndLog(
                                message = "ERROR: Timer communication failed! " +
                                        "Bytes available: $bytesAvailable, " +
                                        "Buffer: $packetBytes", logLevel = "Log.e"
                            )
                        }
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
            /*if (i < 4) {
                Handler(Looper.getMainLooper()).post {
                    toastAndLog("Writing $i...$value")
                }
            }*/
            if (i.mod(moduloForWriteMessages) == 0) {
                Handler(Looper.getMainLooper()).post {
                    toastAndLog("Writing $i")
                }
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

            // Convert data to integer string
            for (i in 0..250) {
                writeDataString = writeDataString + dataAfterWrite[i].toInt().toString() + ","
            }
            Handler(Looper.getMainLooper()).post {
                toastAndLog("Write successful!")
            }
        } else {
            writeDataString = ""
            Handler(Looper.getMainLooper()).post {
                toastAndLog("Write verification failed! Only bytes available: " +
                        "$bytesAvailableAfterWrite", logLevel = "Log.e"
                )
            }
        }
        return writeDataString
    }


    private fun toastAndLog (message: String, logLevel: String = "Log.i") {
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
