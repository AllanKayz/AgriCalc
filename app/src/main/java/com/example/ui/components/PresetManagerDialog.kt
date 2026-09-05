package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CustomPreset
import com.example.model.PresetType
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.ui.theme.SlateDarkBackground

@Composable
fun PresetManagerDialog(
    presets: List<CustomPreset>,
    onDismiss: () -> Unit,
    onSavePreset: (CustomPreset) -> Unit,
    onDeletePreset: (CustomPreset) -> Unit,
    onSelectPreset: ((CustomPreset) -> Unit)? = null,
    filterType: PresetType? = null
) {
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (filterType) {
                null -> 0
                PresetType.FIELD_SHAPE -> 1
                PresetType.CROP_NPK -> 2
                PresetType.FERTILIZER_TYPE -> 3
            }
        )
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<CustomPreset?>(null) }

    val tabs = listOf("All", "Field Shapes", "Crop NPK", "Fertilizer Types")

    val displayedPresets = presets.filter { preset ->
        when (selectedTabIndex) {
            0 -> true
            1 -> preset.type == PresetType.FIELD_SHAPE
            2 -> preset.type == PresetType.CROP_NPK
            3 -> preset.type == PresetType.FERTILIZER_TYPE
            else -> true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, LeafGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("preset_manager_dialog"),
            color = SlateDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForestGreenPrimary)
                                .border(1.5.dp, LeafGreenAccent, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AgriCalc Presets",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "${displayedPresets.size} presets available",
                                style = MaterialTheme.typography.labelSmall,
                                color = LeafGreenAccent
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("close_presets_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    containerColor = ForestGreenPrimary,
                    contentColor = LeafGreenAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, LeafGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add Preset Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CUSTOM & SAVED PRESETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeafGreenAccent,
                            contentColor = SlateDarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("create_new_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Preset",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets List
                if (displayedPresets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No presets found for this category.\nTap '+ New Preset' to create one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedPresets, key = { it.id }) { preset ->
                            PresetItemCard(
                                preset = preset,
                                onSelect = if (onSelectPreset != null) {
                                    { onSelectPreset(preset) }
                                } else null,
                                onDelete = { presetToDelete = preset }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (presetToDelete != null) {
        val p = presetToDelete!!
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${p.name}' from your presets?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePreset(p)
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EarthAmber)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = ForestGreenPrimary,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.85f)
        )
    }

    // Create Custom Preset Dialog
    if (showCreateDialog) {
        CreateCustomPresetDialog(
            initialType = when (selectedTabIndex) {
                1 -> PresetType.FIELD_SHAPE
                2 -> PresetType.CROP_NPK
                3 -> PresetType.FERTILIZER_TYPE
                else -> PresetType.FIELD_SHAPE
            },
            onDismiss = { showCreateDialog = false },
            onSave = { newPreset ->
                onSavePreset(newPreset)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun PresetItemCard(
    preset: CustomPreset,
    onSelect: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, LeafGreenAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .let { if (onSelect != null) it.clickable(onClick = onSelect) else it }
            .testTag("preset_card_${preset.id}"),
        colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (preset.type) {
                                PresetType.FIELD_SHAPE -> LeafGreenAccent
                                PresetType.CROP_NPK -> Color.White
                                PresetType.FERTILIZER_TYPE -> EarthAmber
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (preset.type) {
                            PresetType.FIELD_SHAPE -> Icons.Default.Place
                            PresetType.CROP_NPK -> Icons.Default.Agriculture
                            PresetType.FERTILIZER_TYPE -> Icons.Default.Eco
                        },
                        contentDescription = null,
                        tint = SlateDarkBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (preset.isPredefined) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SYSTEM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = preset.description.ifEmpty {
                            when (preset.type) {
                                PresetType.FIELD_SHAPE -> "${preset.shapeType} • Dim: ${preset.dim1}m × ${preset.dim2}m"
                                PresetType.CROP_NPK -> "Target NPK: ${preset.targetN.toInt()}-${preset.targetP.toInt()}-${preset.targetK.toInt()} kg/ha"
                                PresetType.FERTILIZER_TYPE -> "${preset.fertNPercent.toInt()}-${preset.fertPPercent.toInt()}-${preset.fertKPercent.toInt()} • ${preset.bagWeightKg.toInt()}kg bag"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = LeafGreenBright,
                        maxLines = 2
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSelect != null) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeafGreenAccent,
                            contentColor = SlateDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("apply_preset_${preset.id}")
                    ) {
                        Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_preset_${preset.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (preset.isPredefined) Color.White.copy(alpha = 0.35f) else EarthAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCustomPresetDialog(
    initialType: PresetType = PresetType.FIELD_SHAPE,
    onDismiss: () -> Unit,
    onSave: (CustomPreset) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Field Shape fields
    var shapeType by remember { mutableStateOf("RECTANGLE") }
    var dim1 by remember { mutableStateOf("400.0") } // length or base
    var dim2 by remember { mutableStateOf("250.0") } // width or height
    var dim3 by remember { mutableStateOf("0.0") } // top base or side

    // Crop NPK fields
    var targetN by remember { mutableStateOf("150.0") }
    var targetP by remember { mutableStateOf("60.0") }
    var targetK by remember { mutableStateOf("50.0") }
    var expectedYield by remember { mutableStateOf("8.0") }

    // Fertilizer fields
    var fertN by remember { mutableStateOf("20.0") }
    var fertP by remember { mutableStateOf("10.0") }
    var fertK by remember { mutableStateOf("10.0") }
    var bagWeight by remember { mutableStateOf("50.0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, LeafGreenAccent, RoundedCornerShape(20.dp))
                .testTag("create_preset_dialog"),
            color = SlateDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "New Custom Preset",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LeafGreenAccent else ForestGreenPrimary)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    PresetType.FIELD_SHAPE -> "Field"
                                    PresetType.CROP_NPK -> "Crop NPK"
                                    PresetType.FERTILIZER_TYPE -> "Fertilizer"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) SlateDarkBackground else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preset Name
                FieldNumericInput(
                    label = "Preset Name",
                    value = name,
                    onValueChange = { name = it },
                    hint = "e.g. North Paddock / Custom Blend",
                    isTextOnly = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type-specific inputs
                when (selectedType) {
                    PresetType.FIELD_SHAPE -> {
                        // Shape selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("RECTANGLE", "TRIANGLE", "TRAPEZOID").forEach { st ->
                                val sel = shapeType == st
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) ForestGreenPrimary else Color.Transparent)
                                        .border(1.dp, if (sel) LeafGreenAccent else Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .clickable { shapeType = st }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = st.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (sel) LeafGreenAccent else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (shapeType) {
                            "RECTANGLE" -> {
                                FieldNumericInput(
                                    label = "Length (m)",
                                    value = dim1,
                                    onValueChange = { dim1 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FieldNumericInput(
                                    label = "Width (m)",
                                    value = dim2,
                                    onValueChange = { dim2 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                            }
                            "TRIANGLE" -> {
                                FieldNumericInput(
                                    label = "Base (m)",
                                    value = dim1,
                                    onValueChange = { dim1 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FieldNumericInput(
                                    label = "Height (m)",
                                    value = dim2,
                                    onValueChange = { dim2 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                            }
                            "TRAPEZOID" -> {
                                FieldNumericInput(
                                    label = "Top Base (m)",
                                    value = dim1,
                                    onValueChange = { dim1 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FieldNumericInput(
                                    label = "Bottom Base (m)",
                                    value = dim2,
                                    onValueChange = { dim2 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FieldNumericInput(
                                    label = "Height (m)",
                                    value = dim3,
                                    onValueChange = { dim3 = it },
                                    unit = "m",
                                    step = 10.0
                                )
                            }
                        }
                    }

                    PresetType.CROP_NPK -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FieldNumericInput(
                                label = "Target N",
                                value = targetN,
                                onValueChange = { targetN = it },
                                unit = "kg/ha",
                                step = 10.0,
                                modifier = Modifier.weight(1f)
                            )
                            FieldNumericInput(
                                label = "Target P₂O₅",
                                value = targetP,
                                onValueChange = { targetP = it },
                                unit = "kg/ha",
                                step = 5.0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FieldNumericInput(
                                label = "Target K₂O",
                                value = targetK,
                                onValueChange = { targetK = it },
                                unit = "kg/ha",
                                step = 5.0,
                                modifier = Modifier.weight(1f)
                            )
                            FieldNumericInput(
                                label = "Yield (t/ha)",
                                value = expectedYield,
                                onValueChange = { expectedYield = it },
                                unit = "t/ha",
                                step = 1.0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    PresetType.FERTILIZER_TYPE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FieldNumericInput(
                                label = "N (%)",
                                value = fertN,
                                onValueChange = { fertN = it },
                                unit = "%",
                                step = 1.0,
                                modifier = Modifier.weight(1f)
                            )
                            FieldNumericInput(
                                label = "P₂O₅ (%)",
                                value = fertP,
                                onValueChange = { fertP = it },
                                unit = "%",
                                step = 1.0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FieldNumericInput(
                                label = "K₂O (%)",
                                value = fertK,
                                onValueChange = { fertK = it },
                                unit = "%",
                                step = 1.0,
                                modifier = Modifier.weight(1f)
                            )
                            FieldNumericInput(
                                label = "Bag Size",
                                value = bagWeight,
                                onValueChange = { bagWeight = it },
                                unit = "kg",
                                step = 5.0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val presetName = if (name.isBlank()) "Custom ${selectedType.label}" else name.trim()
                            val desc = when (selectedType) {
                                PresetType.FIELD_SHAPE -> "$shapeType • ${dim1}m × ${dim2}m"
                                PresetType.CROP_NPK -> "Target NPK: ${targetN}-${targetP}-${targetK} kg/ha"
                                PresetType.FERTILIZER_TYPE -> "${fertN}-${fertP}-${fertK} • ${bagWeight}kg bag"
                            }
                            val preset = CustomPreset(
                                name = presetName,
                                type = selectedType,
                                description = desc,
                                shapeType = shapeType,
                                dim1 = dim1.toDoubleOrNull() ?: 0.0,
                                dim2 = dim2.toDoubleOrNull() ?: 0.0,
                                dim3 = dim3.toDoubleOrNull() ?: 0.0,
                                targetN = targetN.toDoubleOrNull() ?: 0.0,
                                targetP = targetP.toDoubleOrNull() ?: 0.0,
                                targetK = targetK.toDoubleOrNull() ?: 0.0,
                                expectedYield = expectedYield.toDoubleOrNull() ?: 0.0,
                                fertNPercent = fertN.toDoubleOrNull() ?: 0.0,
                                fertPPercent = fertP.toDoubleOrNull() ?: 0.0,
                                fertKPercent = fertK.toDoubleOrNull() ?: 0.0,
                                bagWeightKg = bagWeight.toDoubleOrNull() ?: 50.0,
                                isPredefined = false
                            )
                            onSave(preset)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeafGreenAccent,
                            contentColor = SlateDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_custom_preset_confirm")
                    ) {
                        Text("Save Preset", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
