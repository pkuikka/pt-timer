package com.example.pt_timer.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A general-purpose, self-contained text field for settings screens.
 * It manages its own text state while typing and reports the final
 * value only when editing is finished (focus is lost or "Done" is pressed).
 *
 * @param value The initial value to display in the text field.
 * @param onDoneAction A callback that provides the final, confirmed value.
 * @param modifier A modifier to be applied to the OutlinedTextField. Default is an empty Modifier.
 * @param keyboardType The type of keyboard to display (e.g., Number, Text). Defaults to Number.
 */
@Composable
fun CommonField(
    value: String,
    onDoneAction: (String) -> Unit,
    label: String = "",
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    width: (Dp) = 68.dp,
    height: (Dp) = 48.dp,
    keyboardType: KeyboardType = KeyboardType.Number,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(value) }

    // This effect ensures that if the 'value' from the parent state changes
    // (e.g., due to a Bluetooth read), the local 'text' is updated to match.
    LaunchedEffect(value) {
        text = value
    }

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        label = if (label.isNotEmpty()) {
            { Text(label) }
        } else {
            null
        },
        value = text,
        onValueChange = { newText ->
            // Only update the local state while the user is typing
            text = newText
        },
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDoneAction(text)
                focusManager.clearFocus()
            }
        ),
        // 2. APPLY the modifier, chaining it with your specific modifiers
        modifier = modifier
            .padding(1.dp)
            .height(height)
            .width(width)
            .onFocusChanged { focusState ->
                // When focus is lost, report the final value.
                if (!focusState.isFocused) {
                    onDoneAction(text)
                }
            },
    )
}