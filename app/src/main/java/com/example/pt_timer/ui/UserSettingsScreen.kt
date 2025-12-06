package com.example.pt_timer.ui

// Add these imports
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Ruler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    writeCommunicationDelay: Float,
    onDelayChanged: (Float) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Settings") },
                navigationIcon = {
                    // Add the back button
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
                .padding(innerPadding) // Use padding from Scaffold
                .padding(mediumPadding),
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PT-Timer controller version: ${BuildConfig.VERSION_NAME}",
                    style = typography.bodyMedium
                )
                HorizontalDivider(color = Color.Blue, thickness = 1.dp)
                Text(
                    text = "Write Delay: ${writeCommunicationDelay.toLong()} ms",
                    style = typography.bodyMedium
                )
                Slider(
                    value = writeCommunicationDelay,
                    onValueChange = onDelayChanged,
                    valueRange = 50F..500F,
                    steps = 8,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UserSettingsScreenPreview() {
    UserSettingsScreen(
        writeCommunicationDelay = 100f,
        onDelayChanged = {},
        onNavigateUp = {} // Add this for the preview
    )
}
