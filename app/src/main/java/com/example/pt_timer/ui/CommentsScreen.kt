package com.example.pt_timer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pt_timer.R
import com.example.pt_timer.data.TimerData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    uiState: UiState,
    onUpdateTimerData: (TimerData.() -> TimerData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.bodyMedium
        )
        CommentsField(
            value = uiState.timerData.comments,
            onDoneAction = { newValue: String ->
                onUpdateTimerData { copy(comments = newValue) }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CommentsField(
    value: String,
    onDoneAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by remember { mutableStateOf(value) }

    // This effect ensures that if the 'value' from the parent state changes
    // (e.g., due to a Bluetooth read), the local 'text' is updated to match.
    LaunchedEffect(value) {
        text = value
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            // Only update the local state while the user is typing
            text = newText
        },
        singleLine = false,
        textStyle = MaterialTheme.typography.bodySmall,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions.Default, // Or just remove this line completely

        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .onFocusChanged { focusState ->
                // When focus is lost, report the final value.
                if (!focusState.isFocused) {
                    onDoneAction(text)
                }
            },
    )
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    val uiState = UiState(
        timerData = TimerData(
            modelName = "Test Model",
        )
    )
    CommentsScreen(
        uiState = uiState,
        onUpdateTimerData = { _ -> }
    )
}
