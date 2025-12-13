package com.example.pt_timer.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

const val MAX_TIMER_DATA_ROWS = 16
const val NAME_START_POSITION = 160
const val MAX_TIME_TENTHS_LIMIT = 25
const val TIMER_TYPE_F1B = 1
const val TIMER_TYPE_F1A = 2
const val TIMER_TYPE_P30 = 3
const val TIMER_TYPE_E36 = 4
const val TIMER_TYPE_F1Q = 5

/**
 * Represents the complete set of timer configuration data.
 * The @Serializable annotation allows this class to be converted to/from JSON.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class TimerData(

    //  Timer data format (Global G_timer_data(256) as Integer):
    val modelType: Int = 2, // 1 = F1B, 2 = F1A, 3 = P-30, 4 = E-36, 5 = F1Q
    val modelId: Int = 0, // 2 = Model # ID
    val modelSet: Int = 0, // 3 = Data set ID
    val configurationByte: Byte = 1, // 4 = Configuration word:
    // value + 1 = Hook tow switch reverse enabled / F1B switch reverse
    // value + 2 = Hook latch switch reverse enabled / not used in F1B
    // value + 4 = RDT enabled
    // value + 8 = Temperature correction enabled
    // value + 16 = Temperature sensor location (unChecked = S4, Checked = RDT)
    // value + 32 = Beep on tow enabled / not used in F1B
    // value + 64 = Extended Power Enable (1 = enabled, 0 = disabled)
    // value + 128 = Re-latch hook (1 = re-latch, 0 = conventional hook)
    val batteryWarningVoltage: Double = 7.2,  // 5 = Battery voltage warning
    val numberOfDataRows: Int = 8, // 6 = Number of data rows
    val servoSettingsByte: Byte = 1, // 7 = Servo 1 - 4 directions:
    // value + 1 = Servo 1 reversed
    // value + 2 = Servo 2 reversed
    // value + 4 = Servo 3 reversed
    // value + 8 = Servo 4 reversed
    // value + 16  = Servo 1 not in use
    // value + 32  = Servo 2 not in use
    // value + 64  = Servo 3 not in use
    // value + 128 = Servo 4 not in use
    val defaultTemperature: Int = 198, // 8 = Default temperature, which is used if temperature sensor is not giving any readings
    // Stored as Kelvins + 100, so 200 = 300K = 27C (300-273)
    val buntStatus: Int = 0, //  9 = Reason for not executing the bunt:
    // 1 = longer than the maximum limit
    // 2 = shorter than the minimum limit
    val startUpCycleCount: Int = 0, // 10 = Timer startup servo cycle count
    val servoMidPosition: List<Int> = List(4) { 127 }, // 11 - 14 = Servo 1 - 4 mid position
    val servoRange: List<Int> = List(4) { 127 }, // 15 - 18 = Servo 1 - 4 range
    val motorRunTime1: Int = 0,  // Motor run time byte #1
    val motorRunTime2: Int = 0,  // and byte #2
    val servoTemperatureMidPosition: List<Int> = List(4) { 127 }, // 21 - 24 = Servo 1 - 4 temperature correction mid position
    val servoTemperatureRange: List<Int> = List(4) { 127 }, // 25 - 28 = Servo 1 - 4 temperature range
    val empty29: Int = 255,  // 29, 30 =  empty / reserve
    val empty30: Int = 255,
    val timerCalibrationInMilliseconds: Int = 15, // 31 =  Timer calibration in milliseconds
    val timerCalibrationInMicroseconds1: Int = 1, // 32 - 33 = Timer calibration in microseconds
    val timerCalibrationInMicroseconds2: Int = 150, // 32 - 33 = Timer calibration in microseconds
    val maxDataRows: Int = MAX_TIMER_DATA_ROWS, // 34 = Maximum amount of data rows
    val firstIndexForDataSetName: Int = 160, // 35 = First index for data set name
    val maxTimeForSkippingBunt: Int = 80, // 36 =  F1A: max time for skipping bunt
    val minTimeForSkippingBunt: Int = 0, // 37 =  F1A: min time for skipping bunt (if 0, never skip)
    val skipBuntGoToRow: Int = 8, // 38 =  F1A: go to this line when skipping bunt
    val dtPowerDownDelay: Int = 0, // 39 = DT power down delay (to keep logger on) from versions 1.55 onwards.
    val empty40: Int = 255,  // 40 = empty / reserve

    // 41 - 42, 43, 44 - 47  = Row 1 time * 2 bytes, values for servos 1 - 4, step lines (total of 7 bytes)
    // 48 + 7 = Row 2 time, step lines, and values for servos 1 - 4
    //   ...
    // 209 + 7 = Row 25 time, step lines, and values for servos 1 - 4
    // Map them to more easier column values and force step value always to be 0
    val timeValues: List<Double> = List(MAX_TIMER_DATA_ROWS) { it.toDouble() },
    val servo1Values: List<Int> = List(MAX_TIMER_DATA_ROWS) { 125 },
    val servo2Values: List<Int> = List(MAX_TIMER_DATA_ROWS) { 126 },
    val servo3Values: List<Int> = List(MAX_TIMER_DATA_ROWS) { 127 },
    val servo4Values: List<Int> = List(MAX_TIMER_DATA_ROWS) { 128 },
    val stepValues: List<Int> = List(MAX_TIMER_DATA_ROWS) { 0 },

    val modelName: String = "Default Name", // 202 - 220 = Data set name (extended to bigger by reducing rows, default 23 rows gives 202)
    val servo1Label: String = "Servo 1", // 221 - 222 = Servo 1 label
    val servo2Label: String = "Servo 2", // 223 - 224 = Servo 2 label
    val servo3Label: String = "Servo 3", // 225 - 226 = Servo 3 label
    val servo4Label: String = "Servo 4", // 227 - 228 = Servo 4 label
    val empty229: Int = 255,  // 229, 230, 231 = empty / reserve
    val empty230: Int = 255,
    val empty231: Int = 255,
    val row1Label: String = "Row 1", // 232 - 236 = Row 1 label
    val row2Label: String = "Row 2", // 237 + 5 = Row 2 label
    val row3Label: String = "Row 3", // 242 + 5 = Row 3 label
    val row4Label: String = "Row 4", // 247 + 5 = Row 4 label
    val timerVersion: String = "v2.10", // 252 - 256 = timer software version

    // These values are sent after the 256 byte timer data
    val batteryVoltage: Float = 0f,
    val batteryLowestVoltage: Float = 0f,
    val currentTemperature: Float = 0f,
    val usedDt: Int = 0, // Counts together configured DT time + DIP switches
) {

    // --- Configuration byte Booleans ---
    val isSwitch1Enabled: Boolean get() = (configurationByte.toInt() and 1) != 0
    val isSwitch2Enabled: Boolean get() = (configurationByte.toInt() and 2) != 0
    val isRdtEnabled: Boolean get() = (configurationByte.toInt() and 4) != 0
    val isBeepOnTowEnabled: Boolean get() = (configurationByte.toInt() and 32) != 0
    val isDtPowerDownDelayEnabled: Boolean get() = (configurationByte.toInt() and 64) != 0
    val isReLatchEnabled: Boolean get() = (configurationByte.toInt() and 128) != 0

    // --- Servo settings byte Booleans ---
    val isServo1Reversed: Boolean get() = (servoSettingsByte.toInt() and 1) != 0
    val isServo2Reversed: Boolean get() = (servoSettingsByte.toInt() and 2) != 0
    val isServo3Reversed: Boolean get() = (servoSettingsByte.toInt() and 4) != 0
    val isServo4Reversed: Boolean get() = (servoSettingsByte.toInt() and 8) != 0
    val isServo1NotInUse: Boolean get() = (servoSettingsByte.toInt() and 16) != 0
    val isServo2NotInUse: Boolean get() = (servoSettingsByte.toInt() and 32) != 0
    val isServo3NotInUse: Boolean get() = (servoSettingsByte.toInt() and 64) != 0
    val isServo4NotInUse: Boolean get() = (servoSettingsByte.toInt() and 128) != 0


    companion object {
        /**
         * Factory function to create a TimerData object from the raw 256-byte packet
         * received from the timer device.
         *
         * @param packetBytes The 256-byte ByteArray from the device.
         * @return A fully populated TimerData object.
         * @throws IllegalArgumentException if the packet is not 256 bytes long.
         */
        fun fromPacket(packetBytes: ByteArray): Pair<TimerData, Boolean> {
            // Validate the packet size to prevent errors
            require(packetBytes.size == 262) { "Packet must be 262 bytes long." }

            // Helper function to read a string from a specific range in the byte array.
            // It stops at the first null character (0x00).
            fun readString(range: IntRange): String {
                val subArray = packetBytes.slice(range)
                val firstNull = subArray.indexOf(0.toByte())
                val actualEnd = if (firstNull == -1) subArray.size else firstNull
                return String(subArray.take(actualEnd).toByteArray(), Charsets.UTF_8)
            }

            // Helper to get an unsigned byte value as an Int
            fun getUnsignedByte(index: Int): Int = packetBytes[index].toInt() and 0xFF

            // Map main timer values
            // Row 1 time * 2 bytes, values for servos 1 - 4, step lines (total of 7 bytes)
            val indices = 0 until MAX_TIMER_DATA_ROWS
            val timeValues =
                indices.map { i -> (((getUnsignedByte(41 + (i * 7)) * 256) + getUnsignedByte(42 + (i * 7))) / 10.0) }
            val servo1Values = indices.map { i -> getUnsignedByte(43 + (i * 7)) }
            val servo2Values = indices.map { i -> getUnsignedByte(44 + (i * 7)) }
            val servo3Values = indices.map { i -> getUnsignedByte(45 + (i * 7)) }
            val servo4Values = indices.map { i -> getUnsignedByte(46 + (i * 7)) }
            val stepValues = indices.map { i -> getUnsignedByte(47 + (i * 7)) }

            // Read the old modelName name position to read the model name, but we will store it to new place
            val oldFirstIndexForDataSetName = getUnsignedByte(35)

            // Warn user about too many used rows or step usage
            val stepValuesInUse = stepValues.any { it > 0 }
            val needsWarning = ((getUnsignedByte(34) > MAX_TIMER_DATA_ROWS) ||
                    stepValuesInUse || getUnsignedByte(1) in intArrayOf(3,4,6))

            // --- Map each byte to its corresponding property ---
            val timerData = TimerData(
                modelType = getUnsignedByte(1),
                modelId = getUnsignedByte(2),
                modelSet = getUnsignedByte(3),
                configurationByte = packetBytes[4],
                batteryWarningVoltage = getUnsignedByte(5) / 10.0,
                numberOfDataRows = minOf(getUnsignedByte(6), MAX_TIMER_DATA_ROWS), // reset data rows to new max value if bigger
                servoSettingsByte = packetBytes[7],
                defaultTemperature = getUnsignedByte(8),
                buntStatus = getUnsignedByte(9),
                startUpCycleCount = getUnsignedByte(10),

                // Lists of Ints/Bytes
                servoMidPosition = (11..14).map { getUnsignedByte(it) },
                servoRange = (15..18).map { getUnsignedByte(it) },
                motorRunTime1 = getUnsignedByte(19),
                motorRunTime2 = getUnsignedByte(20),
                servoTemperatureMidPosition = (21..24).map { getUnsignedByte(it) },
                servoTemperatureRange = (25..28).map { getUnsignedByte(it) },
                empty29 = 0,
                empty30 = 0,

                // Multi-byte values
                timerCalibrationInMilliseconds = getUnsignedByte(31),
                timerCalibrationInMicroseconds1 = getUnsignedByte(32),
                timerCalibrationInMicroseconds2 = getUnsignedByte(33),

                // Single byte values
                maxDataRows = minOf(getUnsignedByte(34), MAX_TIMER_DATA_ROWS), // reset max data rows to new max value if bigger
                firstIndexForDataSetName = minOf(getUnsignedByte(35), NAME_START_POSITION),  // reset mode name position to new max value if bigger
                maxTimeForSkippingBunt = getUnsignedByte(36),
                minTimeForSkippingBunt = getUnsignedByte(37),
                skipBuntGoToRow = getUnsignedByte(38),
                dtPowerDownDelay = getUnsignedByte(39),
                empty40 = 0,

                // The main timer grid values from 41 - 131
                timeValues = timeValues,
                // stepValues = stepValues,  // these are never read as not supported
                servo1Values = servo1Values,
                servo2Values = servo2Values,
                servo3Values = servo3Values,
                servo4Values = servo4Values,

                // String values read from specific ranges
                modelName = readString(oldFirstIndexForDataSetName..220).trimEnd(),
                servo1Label = readString(221..222),
                servo2Label = readString(223..224),
                servo3Label = readString(225..226),
                servo4Label = readString(227..228),
                empty229 = 0,
                empty230 = 0,
                empty231 = 0,
                row1Label = readString(232..236),
                row2Label = readString(237..241),
                row3Label = readString(242..246),
                row4Label = readString(247..251),
                timerVersion = readString(252..255), // Last byte is 255 (index 0-255)
                batteryVoltage = getUnsignedByte(257).toFloat(),
                batteryLowestVoltage = getUnsignedByte(258).toFloat(),
                currentTemperature = getUnsignedByte(259) + 100 - 273.toFloat(), // Kelvin to Celsius conversion
                usedDt = ((getUnsignedByte(260) * 256) + getUnsignedByte(261)) / 10, // count 2 bytes to DT seconds,
            )
            return Pair(timerData, needsWarning)
        }

        fun createNew(timerType: Int): TimerData {
            val defaultTimerData = TimerData()

            return when (timerType) {
                TIMER_TYPE_F1A -> {
                    defaultTimerData.copy(
                        modelType = TIMER_TYPE_F1A,
                        modelName = "F1A Timer Example",
                        configurationByte = 37.toByte(),
                        servoSettingsByte = 0.toByte(),
                        numberOfDataRows = 8,
                        servo1Label = "EL",
                        servo2Label = "RD",
                        servo3Label = "WW",
                        servo4Label = "--",
                        row1Label = "DT   ",
                        row2Label = "CIRC ",
                        row3Label = "FWD  ",
                        row4Label = "ACCEL",
                    )
                }

                TIMER_TYPE_F1B -> {
                    defaultTimerData.copy(
                        modelType = TIMER_TYPE_F1B,
                        modelName = "F1B Timer Example",
                        configurationByte = 4.toByte(),
                        servoSettingsByte = 0.toByte(),
                        numberOfDataRows = 6,
                        servo1Label = "EL",
                        servo2Label = "RD",
                        servo3Label = "WW",
                        servo4Label = "--",
                        row1Label = "DT   ",
                        row2Label = "ARM  ",
                        row3Label = "START",
                        row4Label = "     ",
                    )
                }

                TIMER_TYPE_F1Q -> {
                    defaultTimerData.copy(
                        modelType = TIMER_TYPE_F1Q,
                        modelName = "F1Q Timer Examole",
                        configurationByte = 4.toByte(),
                        servoSettingsByte = 0.toByte(),
                        numberOfDataRows = 6,
                        servo1Label = "EL",
                        servo2Label = "RD",
                        servo3Label = "WW",
                        servo4Label = "SC",
                        row1Label = "DT   ",
                        row2Label = "ARM  ",
                        row3Label = "START",
                        row4Label = "     ",
                    )
                }

                TIMER_TYPE_P30 -> {
                    defaultTimerData.copy(
                        modelType = timerType,
                        modelName = "P-30 Timer Example",
                        configurationByte = 4.toByte(),
                        servoSettingsByte = 224.toByte(),
                        numberOfDataRows = 5,
                        maxDataRows = 7,
                        firstIndexForDataSetName = 85,
                        servo1Label = "S1",
                        servo2Label = "--",
                        servo3Label = "--",
                        servo4Label = "--",
                        row1Label = "DT   ",
                        row2Label = "ARM  ",
                        row3Label = "START",
                        row4Label = "     ",
                    )
                }

                TIMER_TYPE_E36 -> {
                    defaultTimerData.copy(
                        modelType = timerType,
                        modelName = "E-36 Timer Example",
                        configurationByte = 4.toByte(),
                        servoSettingsByte = 192.toByte(),
                        numberOfDataRows = 5,
                        maxDataRows = 7,
                        firstIndexForDataSetName = 85,
                        servo1Label = "SC",
                        servo2Label = "S1",
                        servo3Label = "--",
                        servo4Label = "--",
                        row1Label = "DT   ",
                        row2Label = "ARM  ",
                        row3Label = "START",
                        row4Label = "     ",
                    )
                }

                else -> {
                    // It's good practice to handle the unknown case
                    defaultTimerData
                }
            }
        }
    }

    /**
     * Serializes the TimerData object back into a 262-byte ByteArray
     * to be written to the timer device.
     *
     * @return A 262-byte ByteArray representing the current state of this object.
     */
    fun toPacket(): ByteArray {
        val packet = ByteArray(253)

        // Writes a single Int as a byte.
        fun setByte(index: Int, value: Int) {
            if (index < packet.size) {
                packet[index] = value.toByte()
            }
        }

        // Writes a Double that needs to be multiplied by 10.
        fun setDoubleAsByte(index: Int, value: Double) {
            setByte(index, (value * 10).toInt())
        }

        // Writes a String into a specific range, padding with nulls.
        fun setString(range: IntRange, value: String) {
            val stringBytes = value.toByteArray(Charsets.UTF_8)
            stringBytes.copyInto(packet, range.first, 0, minOf(stringBytes.size, range.count()))
        }

        // Writes a 16-bit unsigned Int as two bytes (Little Endian).
        fun setUnsignedInt(startIndex: Int, value: Int) {
            val lowByte: Int = value / 256
            val highByte: Int = value % 256
            if (startIndex + 1 < packet.size) {
                packet[startIndex] = lowByte.toByte()
                packet[startIndex + 1] = highByte.toByte()
            }
        }

        // 3. Write each property from the data class into the packet at the correct index.

        // Single value properties
        setByte(1, modelType)
        setByte(2, modelId)
        setByte(3, modelSet)
        packet[4] = configurationByte // Direct byte assignment
        setDoubleAsByte(5, batteryWarningVoltage)
        setByte(6, numberOfDataRows)
        packet[7] = servoSettingsByte // Direct byte assignment
        setByte(8, defaultTemperature)
        setByte(9, buntStatus)
        setByte(10, startUpCycleCount)

        // List properties
        servoMidPosition.forEachIndexed { i, value -> setByte(11 + i, value) }
        servoRange.forEachIndexed { i, value -> setByte(15 + i, value) }

        setByte(19, motorRunTime1)
        setByte(20, motorRunTime2)

        servoTemperatureMidPosition.forEachIndexed { i, value -> setByte(21 + i, value) }
        servoTemperatureRange.forEachIndexed { i, value -> setByte(25 + i, value) }

        // empty29, empty30 are already 0

        // More single value properties
        setByte(31, timerCalibrationInMilliseconds)
        setByte(32, timerCalibrationInMicroseconds1)
        setByte(33, timerCalibrationInMicroseconds2)
        setByte(34, maxDataRows)
        setByte(35, firstIndexForDataSetName)
        setByte(36, maxTimeForSkippingBunt)
        setByte(37, minTimeForSkippingBunt)
        setByte(38, skipBuntGoToRow)
        setByte(39, dtPowerDownDelay)

        // empty40 is already 0

        // The main timer grid values (7 bytes per row)
        for (i in 0 until MAX_TIMER_DATA_ROWS) {
            val baseIndex = 41 + (i * 7)
            val timeAsInt = (timeValues[i] * 10).toInt()
            setUnsignedInt(baseIndex, timeAsInt)  // Time (2 bytes)
            setByte(baseIndex + 2, servo1Values[i])  // Servo 1 (1 byte)
            setByte(baseIndex + 3, servo2Values[i])  // Servo 2 (1 byte)
            setByte(baseIndex + 4, servo3Values[i])  // Servo 3 (1 byte)
            setByte(baseIndex + 5, servo4Values[i])  // Servo 4 (1 byte)
            setByte(baseIndex + 6, stepValues[i])    // Step value (1 byte)
        }

        // String properties
        setString(firstIndexForDataSetName..220, modelName)
        setString(221..222, servo1Label)
        setString(223..224, servo2Label)
        setString(225..226, servo3Label)
        setString(227..228, servo4Label)

        // empty229-231 are already 0

        setString(232..236, row1Label)
        setString(237..241, row2Label)
        setString(242..246, row3Label)
        setString(247..251, row4Label)
        return packet
    }
}
