package com.example.pt_timer.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartupScreen(
) {
    Text(com.example.pt_timer.data.TimerData().modelName)

}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    StartupScreen()
}
