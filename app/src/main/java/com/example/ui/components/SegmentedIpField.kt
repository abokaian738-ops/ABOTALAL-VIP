package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SegmentedIpField(
    ipAddress: String,
    onIpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Parse current IP into 4 octets
    val parsedParts = remember(ipAddress) {
        val parts = ipAddress.split(".")
        List(4) { idx -> parts.getOrNull(idx) ?: "" }
    }

    var octet0 by remember(parsedParts) { mutableStateOf(parsedParts[0]) }
    var octet1 by remember(parsedParts) { mutableStateOf(parsedParts[1]) }
    var octet2 by remember(parsedParts) { mutableStateOf(parsedParts[2]) }
    var octet3 by remember(parsedParts) { mutableStateOf(parsedParts[3]) }

    val focusManager = LocalFocusManager.current
    val focusRequester0 = remember { FocusRequester() }
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }

    fun updateAndNotify(o0: String, o1: String, o2: String, o3: String) {
        octet0 = o0
        octet1 = o1
        octet2 = o2
        octet3 = o3
        onIpChange("$o0.$o1.$o2.$o3")
    }

    fun handlePasteOrInput(text: String, currentIndex: Int) {
        // If user pasted a full IP address like "192.168.88.1"
        if (text.contains(".")) {
            val parts = text.split(".")
            val o0 = parts.getOrNull(0)?.filter { it.isDigit() }?.take(3) ?: octet0
            val o1 = parts.getOrNull(1)?.filter { it.isDigit() }?.take(3) ?: octet1
            val o2 = parts.getOrNull(2)?.filter { it.isDigit() }?.take(3) ?: octet2
            val o3 = parts.getOrNull(3)?.filter { it.isDigit() }?.take(3) ?: octet3
            updateAndNotify(o0, o1, o2, o3)
            focusRequester3.requestFocus()
            return
        }

        // Filter digits only up to 3 chars, max 255
        val filtered = text.filter { it.isDigit() }.take(3)
        val num = filtered.toIntOrNull()
        val validText = if (num != null && num > 255) "255" else filtered

        when (currentIndex) {
            0 -> {
                updateAndNotify(validText, octet1, octet2, octet3)
                if (validText.length == 3 || text.endsWith(".")) {
                    focusRequester1.requestFocus()
                }
            }
            1 -> {
                updateAndNotify(octet0, validText, octet2, octet3)
                if (validText.length == 3 || text.endsWith(".")) {
                    focusRequester2.requestFocus()
                }
            }
            2 -> {
                updateAndNotify(octet0, octet1, validText, octet3)
                if (validText.length == 3 || text.endsWith(".")) {
                    focusRequester3.requestFocus()
                }
            }
            3 -> {
                updateAndNotify(octet0, octet1, octet2, validText)
                if (validText.length == 3) {
                    focusManager.clearFocus()
                }
            }
        }
    }

    // Force LTR for IP address numbers regardless of system RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF0C5A60).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Octet 0
                OctetBox(
                    value = octet0,
                    onValueChange = { handlePasteOrInput(it, 0) },
                    placeholder = "192",
                    focusRequester = focusRequester0,
                    enabled = enabled,
                    onBackspaceOnEmpty = {},
                    imeAction = ImeAction.Next,
                    onNext = { focusRequester1.requestFocus() },
                    modifier = Modifier.weight(1f)
                )

                IpSeparatorDot()

                // Octet 1
                OctetBox(
                    value = octet1,
                    onValueChange = { handlePasteOrInput(it, 1) },
                    placeholder = "168",
                    focusRequester = focusRequester1,
                    enabled = enabled,
                    onBackspaceOnEmpty = { focusRequester0.requestFocus() },
                    imeAction = ImeAction.Next,
                    onNext = { focusRequester2.requestFocus() },
                    modifier = Modifier.weight(1f)
                )

                IpSeparatorDot()

                // Octet 2
                OctetBox(
                    value = octet2,
                    onValueChange = { handlePasteOrInput(it, 2) },
                    placeholder = "88",
                    focusRequester = focusRequester2,
                    enabled = enabled,
                    onBackspaceOnEmpty = { focusRequester1.requestFocus() },
                    imeAction = ImeAction.Next,
                    onNext = { focusRequester3.requestFocus() },
                    modifier = Modifier.weight(1f)
                )

                IpSeparatorDot()

                // Octet 3
                OctetBox(
                    value = octet3,
                    onValueChange = { handlePasteOrInput(it, 3) },
                    placeholder = "1",
                    focusRequester = focusRequester3,
                    enabled = enabled,
                    onBackspaceOnEmpty = { focusRequester2.requestFocus() },
                    imeAction = ImeAction.Done,
                    onNext = { focusManager.clearFocus() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OctetBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester,
    enabled: Boolean,
    onBackspaceOnEmpty: () -> Unit,
    imeAction: ImeAction,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (value.isNotEmpty()) Color(0xFF0C5A60) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color(0xFF0C5A60)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = { onNext() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace && value.isEmpty()) {
                        onBackspaceOnEmpty()
                        true
                    } else {
                        false
                    }
                }
        )
    }
}

@Composable
private fun IpSeparatorDot() {
    Text(
        text = "•",
        color = Color(0xFF0C5A60),
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
