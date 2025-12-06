package com.example.pt_timer.ui

import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pt_timer.ui.navigation.Screen
import android.Manifest


@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun TimerApp() {
    val navController = rememberNavController()
    val uiViewModel: UiViewModel = viewModel(factory = UiViewModel.Factory)
    val uiState by uiViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.name
    ) {
        // Define the MainScreen
        composable(route = Screen.Main.name) {
            MainScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.name)
                }
            )
        }

        // Define the UserSettingsScreen
        composable(route = Screen.Settings.name) {
            UserSettingsScreen(
                writeCommunicationDelay = uiState.writeCommunicationDelay,
                onDelayChanged = uiViewModel::onDelayChanged,
                // Add a lambda to handle navigating back
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
    }
}
