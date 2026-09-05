package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.LeafGreenAccent

@Composable
fun FieldNumericInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    unit: String = "",
    step: Double = 1.0,
    minValue: Double = 0.0,
    maxValue: Double = Double.MAX_VALUE,
    hint: String = "",
    presets: List<Pair<String, Double>> = emptyList(),
    isError: Boolean = false,
    errorMessage: String? = null,
    isTextOnly: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label & Optional Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (hint.isNotBlank()) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isTextOnly) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("input_${label.lowercase().replace(' ', '_')}"),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                placeholder = {
                    if (hint.isNotBlank()) {
                        Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LeafGreenAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = EarthAmber,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            )
        } else {
            // Large high-contrast input box with Stepper Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Decrement Stepper
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    IconButton(
                        onClick = {
                            val current = value.toDoubleOrNull() ?: minValue
                            val updated = (current - step).coerceAtLeast(minValue)
                            onValueChange(if (updated % 1.0 == 0.0) updated.toLong().toString() else "%.2f".format(updated))
                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease $label",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Main Text Field with bold characters and unit badge
                OutlinedTextField(
                    value = value,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            onValueChange(filtered)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("input_${label.lowercase().replace(' ', '_')}"),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions.Default,
                    trailingIcon = {
                        if (unit.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LeafGreenAccent.copy(alpha = 0.2f))
                                    .border(1.dp, LeafGreenAccent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreenAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = EarthAmber,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Increment Stepper
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    IconButton(
                        onClick = {
                            val current = value.toDoubleOrNull() ?: minValue
                            val updated = (current + step).coerceAtMost(maxValue)
                            onValueChange(if (updated % 1.0 == 0.0) updated.toLong().toString() else "%.2f".format(updated))
                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase $label",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Quick Preset Chips (if provided)
        if (presets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (name, presetVal) ->
                    val isSelected = value.toDoubleOrNull() == presetVal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) LeafGreenAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) LeafGreenAccent else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onValueChange(
                                    if (presetVal % 1.0 == 0.0) presetVal.toLong().toString() else presetVal.toString()
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("preset_${name.lowercase().replace(' ', '_')}")
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Error message row
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = EarthAmber,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
