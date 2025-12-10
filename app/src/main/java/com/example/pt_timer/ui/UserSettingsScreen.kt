package com.example.pt_timer.ui

// Add these imports
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.BuildConfig
import com.example.pt_timer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    writeCommunicationDelay: Float,
    onDelayChanged: (Float) -> Unit,
    onNavigateUp: () -> Unit,
    displaySwipeVelocity: Float,
    onSwipeVelocityChanged: (Float) -> Unit,
    displaySwipeDistance: Float,
    onSwipeDistanceChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(mediumPadding)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "PT-Timer controller version: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Communication settings",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Write Delay: ${writeCommunicationDelay.toLong()} ms",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = writeCommunicationDelay,
                onValueChange = onDelayChanged,
                valueRange = 50f..500f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Display settings",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Display Swipe Velocity: ${displaySwipeVelocity.toLong()} units/s",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = displaySwipeVelocity,
                onValueChange = onSwipeVelocityChanged,
                valueRange = 50f..300f,
                steps = 10
            )

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Display Swipe Distance: ${displaySwipeDistance.toLong()} units",
                    style = MaterialTheme.typography.bodyMedium // Correct style for Material 3
                )
            }

            Slider(
                value = displaySwipeDistance,
                onValueChange = onSwipeDistanceChange,
                valueRange = 100f..900f,
                steps = 10
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserSettingsScreenPreview() {
    UserSettingsScreen(
        writeCommunicationDelay = 100f,
        onDelayChanged = {},
        onNavigateUp = {},
        displaySwipeVelocity = 150f,
        displaySwipeDistance = 300f,
        onSwipeVelocityChanged = {},
        onSwipeDistanceChange = {}
    )
}
