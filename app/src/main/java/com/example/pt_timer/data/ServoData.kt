package com.example.pt_timer.data

import kotlinx.serialization.Serializable
import com.example.pt_timer.data.TimerData
import com.example.pt_timer.ui.UiState

/**
 * Represents the complete set of timer configuration data.
 * The @Serializable annotation allows this class to be converted to/from JSON.
 */
@Serializable
data class ServoData1(
    val name: String = "",
    val midPos: Int = 0,
    val range: Int = 0,
    val reverse: Boolean = false,
    val inUse: Boolean = false
) {
    fun UpdateServoData(uiState: UiState): ServoData1 {
        //val bByte: byte = uiState.timerData.servoSettingsByte.read().toByte()
        //val bByte: Byte = input.read().toByte()
        //val value: ByteArray = uiState.timerData.servoSettingsByte.value
        val servoData1 = ServoData1 (
            name = uiState.timerData.servo1Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(1) ?:0,
            range = uiState.timerData.servoRange.getOrNull(1) ?:0,
            reverse = false,
            inUse = false
        )
        return servoData1
    }
    fun GetServoName(): ServoData1 {
        return ServoData1(
            name
        )
    }
    fun GetServoMidPos(): ServoData1 {
        return ServoData1(
            midPos.toString()
        )
    }
    fun GetServoRange(): ServoData1 {
        return ServoData1(
            range.toString()
        )
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
    fun UpdateServoData(uiState: UiState): ServoData2 {
        //val bByte: byte = uiState.timerData.servoSettingsByte.read().toByte()
        //val bByte: Byte = input.read().toByte()
        //val value: ByteArray = uiState.timerData.servoSettingsByte.value
        val servoData2 = ServoData2 (
            name = uiState.timerData.servo2Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(2) ?:0,
            range = uiState.timerData.servoRange.getOrNull(2) ?:0,
            reverse = false,
            inUse = false
        )
        return servoData2
    }
    fun GetServoName(): ServoData2 {
        return ServoData2(
            name
        )
    }
    fun GetServoMidPos(): ServoData2 {
        return ServoData2(
            midPos.toString()
        )
    }
    fun GetServoRange(): ServoData2 {
        return ServoData2(
            range.toString()
        )
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
    fun UpdateServoData(uiState: UiState): ServoData3 {
        //val bByte: byte = uiState.timerData.servoSettingsByte.read().toByte()
        //val bByte: Byte = input.read().toByte()
        //val value: ByteArray = uiState.timerData.servoSettingsByte.value
        val servoData3 = ServoData3 (
            name = uiState.timerData.servo3Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(3) ?:0,
            range = uiState.timerData.servoRange.getOrNull(3) ?:0,
            reverse = false,
            inUse = false
        )
        return servoData3
    }
    fun GetServoName(): ServoData3 {
        return ServoData3(
            name
        )
    }
    fun GetServoMidPos(): ServoData3 {
        return ServoData3(
            midPos.toString()
        )
    }
    fun GetServoRange(): ServoData3 {
        return ServoData3(
            range.toString()
        )
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
    fun UpdateServoData(uiState: UiState): ServoData4 {
        //val bByte: byte = uiState.timerData.servoSettingsByte.read().toByte()
        //val bByte: Byte = input.read().toByte()
        //val value: ByteArray = uiState.timerData.servoSettingsByte.value
        val ServoData4 = ServoData4 (
            name = uiState.timerData.servo4Label,
            midPos =  uiState.timerData.servoMidPosition.getOrNull(4) ?:0,
            range = uiState.timerData.servoRange.getOrNull(4) ?:0,
            reverse = false,
            inUse = false
        )
        return ServoData4
    }
    fun GetServoName(): ServoData4 {
        return ServoData4(
            name
        )
    }
    fun GetServoMidPos(): ServoData4 {
        return ServoData4(
            midPos.toString()
        )
    }
    fun GetServoRange(): ServoData4 {
        return ServoData4(
            range.toString()
        )
    }
}