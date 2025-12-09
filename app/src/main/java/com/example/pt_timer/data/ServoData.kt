package com.example.pt_timer.data
import kotlinx.serialization.Serializable
import com.example.pt_timer.ui.UiState

/**
 * Represents the complete set of timer configuration data.
 * The @Serializable annotation allows this class to be converted to/from JSON.
 */

fun isBitSet(byte: Byte, bitPosition: Int): Boolean {
    val mask = (1 shl bitPosition)  // Create a mask to isolate the bit at the given position
    return (byte.toInt() and mask) != 0  // Check if the bit is set
}
@Serializable
object GlobalData {
    val servoDataList = mutableListOf<ServoData>()

    fun createServoDataList(uiState: UiState): List<ServoData> {
        servoDataList.clear()   // Clear old data

        val servoData1 = ServoData1().updateServoData(uiState)
        val servoData2 = ServoData2().updateServoData(uiState)
        val servoData3 = ServoData3().updateServoData(uiState)
        val servoData4 = ServoData4().updateServoData(uiState)

        servoDataList.addAll(
            listOf(
                ServoData(servoData1.name, servoData1.midPos, servoData1.range, servoData1.reverse, servoData1.inUse),
                ServoData(servoData2.name, servoData2.midPos, servoData2.range, servoData2.reverse, servoData2.inUse),
                ServoData(servoData3.name, servoData3.midPos, servoData3.range, servoData3.reverse, servoData3.inUse),
                ServoData(servoData4.name, servoData4.midPos, servoData4.range, servoData4.reverse, servoData4.inUse)
            )
        )

        return servoDataList
    }
    fun getDataRows(): List<ServoData> {
        return servoDataList
    }
}
@Serializable
data class ServoData(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
)
data class ServoData1(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
) {
    fun updateServoData(uiState: UiState): ServoData1 {
        val servoData1 = ServoData1(
            name = uiState.timerData.servo1Label,
            midPos = uiState.timerData.servoMidPosition.getOrNull(0) ?: 0,
            range = uiState.timerData.servoRange.getOrNull(0) ?: 0,
            reverse = isBitSet(uiState.timerData.servoSettingsByte, 0),
            inUse = !isBitSet(uiState.timerData.servoSettingsByte, 4)
        )
        return servoData1
    }
}
@Serializable
data class ServoData2(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
) {
    fun updateServoData(uiState: UiState): ServoData2 {

        val servoData2 = ServoData2 (
            name = uiState.timerData.servo2Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(1) ?:0,
            range = uiState.timerData.servoRange.getOrNull(1) ?:0,
            reverse = isBitSet(uiState.timerData.servoSettingsByte, 1),
            inUse = !isBitSet(uiState.timerData.servoSettingsByte, 5)
        )
        return servoData2
    }
}
@Serializable
data class ServoData3(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
) {
    fun updateServoData(uiState: UiState): ServoData3 {
        val servoData3 = ServoData3 (
            name = uiState.timerData.servo3Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(2) ?:0,
            range = uiState.timerData.servoRange.getOrNull(2) ?:0,
            reverse = isBitSet(uiState.timerData.servoSettingsByte, 2),
            inUse = !isBitSet(uiState.timerData.servoSettingsByte, 6)
        )
        return servoData3
    }
}
@Serializable
data class ServoData4(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
) {
    fun updateServoData(uiState: UiState): ServoData4 {
        val servoData4 = ServoData4 (
            name = uiState.timerData.servo4Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(3) ?:0,
            range = uiState.timerData.servoRange.getOrNull(3) ?:0,
            reverse = isBitSet(uiState.timerData.servoSettingsByte, 3),
            inUse = !isBitSet(uiState.timerData.servoSettingsByte, 7)
        )
        return servoData4
    }
}