package com.example.pt_timer.ui

import com.example.pt_timer.data.TimerData

data class UiState(
    // Timer data related properties
    val timerData: TimerData = TimerData(),

    // BT communication related properties
    val btDevices: List<String> = emptyList(), // List of BT device names
    val selectedBtDevice: String = "", // The currently selected BT device
    val writeCommunicationDelay: Float = 300F, // Timer writing communication delay in milliseconds

    // File handling
    val savedFiles: List<String> = emptyList(), // List of saved files

    // UI helpers
    val selectedRow: Int = -1 // Add this line. -1 indicates no row is selected.
)
