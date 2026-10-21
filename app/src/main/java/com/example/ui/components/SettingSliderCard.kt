package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary

@Composable
fun SettingSliderCard(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unitLabel: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    step: Float = 1f,
    presets: List<Pair<String, Float>> = emptyList(),
    onValueChange: (Float) -> Unit,
    testTagInput: String? = null,
    testTagSlider: String? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember(value) {
        mutableStateOf(if (step >= 1f) value.toInt().toString() else "%.1f".format(value))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title & Value pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive value pill / direct edit
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyanPrimary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                onClick = { isEditing = !isEditing }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (step >= 1f) "${value.toInt()}" else "%.1f".format(value),
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = unitLabel,
                        color = CyanPrimary.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать значение",
                        tint = CyanPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Direct input field (if editing enabled)
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { str ->
                        textInput = str.filter { it.isDigit() || it == '.' }
                        val n = textInput.toFloatOrNull()
                        if (n != null && n in valueRange) {
                            onValueChange(n)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .then(if (testTagInput != null) Modifier.testTag(testTagInput) else Modifier),
                    label = { Text("Точное значение ($unitLabel)") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilledTonalButton(
                    onClick = { isEditing = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Готово", fontSize = 12.sp)
                }
            }
        }

        // Stepper: [-] [=====O=====] [+]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    val next = (value - step).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(next)
                },
                enabled = value > valueRange.start,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Уменьшить",
                    modifier = Modifier.size(18.dp),
                    tint = if (value > valueRange.start) CyanPrimary else MaterialTheme.colorScheme.outline
                )
            }

            Slider(
                value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = CyanPrimary,
                    activeTrackColor = CyanPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .then(if (testTagSlider != null) Modifier.testTag(testTagSlider) else Modifier)
            )

            IconButton(
                onClick = {
                    val next = (value + step).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(next)
                },
                enabled = value < valueRange.endInclusive,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Увеличить",
                    modifier = Modifier.size(18.dp),
                    tint = if (value < valueRange.endInclusive) CyanPrimary else MaterialTheme.colorScheme.outline
                )
            }
        }

        // Quick Preset Chips
        if (presets.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { (label, presetVal) ->
                    val isSelected = (value == presetVal) || (step >= 1f && value.toInt() == presetVal.toInt())
                    SuggestionChip(
                        onClick = { onValueChange(presetVal) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isSelected) CyanPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}
