package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.model.CustomPreset
import com.example.model.PresetType
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.components.FieldNumericInput
import com.example.ui.components.FormulaCard
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.util.ReportExporter
import com.example.viewmodel.AgriCalcViewModel
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun SoilFertilizerScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("NPK Balancer", "Lime Requirement")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("soil_fertilizer_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = LeafGreenAccent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSubTab == index) FontWeight.Black else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            0 -> NpkBalancerView(viewModel, context)
            1 -> LimeRequirementView(viewModel, context)
        }
    }
}

@Composable
private fun NpkBalancerView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var targetN by remember { mutableStateOf("120.0") } // kg N/ha
    var targetP by remember { mutableStateOf("60.0") } // kg P2O5/ha
    var targetK by remember { mutableStateOf("50.0") } // kg K2O/ha
    var fieldArea by remember { mutableStateOf("10.0") } // ha

    val allPresets by viewModel.allPresets.collectAsState()
    val cropNpkPresets = allPresets.filter { it.type == PresetType.CROP_NPK }
    val fertilizerPresets = allPresets.filter { it.type == PresetType.FERTILIZER_TYPE }

    var showSaveCropPresetDialog by remember { mutableStateOf(false) }
    var newCropPresetName by remember { mutableStateOf("") }

    var showSaveFertPresetDialog by remember { mutableStateOf(false) }
    var newFertPresetName by remember { mutableStateOf("") }
    var customFertN by remember { mutableStateOf("20.0") }
    var customFertP by remember { mutableStateOf("10.0") }
    var customFertK by remember { mutableStateOf("10.0") }
    var customFertBag by remember { mutableStateOf("50.0") }

    val nVal = targetN.toDoubleOrNull() ?: 0.0
    val pVal = targetP.toDoubleOrNull() ?: 0.0
    val kVal = targetK.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0

    // Fertilizer source calculations:
    // 1. DAP (18-46-0) supplies P2O5 first:
    // DAP kg/ha = P_target / 0.46
    val dapKgHa = if (pVal > 0) pVal / 0.46 else 0.0
    val nSuppliedByDap = dapKgHa * 0.18

    // 2. Remaining N needed supplied by Urea (46-0-0):
    val nRemaining = max(0.0, nVal - nSuppliedByDap)
    val ureaKgHa = if (nRemaining > 0) nRemaining / 0.46 else 0.0

    // 3. K2O needed supplied by MOP (0-0-60):
    val mopKgHa = if (kVal > 0) kVal / 0.60 else 0.0

    // Field totals and 50kg bag counts
    val ureaTotalKg = ureaKgHa * area
    val dapTotalKg = dapKgHa * area
    val mopTotalKg = mopKgHa * area

    val ureaBags = ceil(ureaTotalKg / 50.0).toInt()
    val dapBags = ceil(dapTotalKg / 50.0).toInt()
    val mopBags = ceil(mopTotalKg / 50.0).toInt()
    val totalBags = ureaBags + dapBags + mopBags

    val primaryBagsStr = "$totalBags Bags"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Crop NPK Presets Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, LeafGreenAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CROP NPK TARGET PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        TextButton(
                            onClick = { showSaveCropPresetDialog = true },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save Target",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LeafGreenAccent
                                )
                            )
                        }
                    }

                    if (cropNpkPresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cropNpkPresets) { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ForestGreenPrimary)
                                        .border(1.dp, LeafGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            targetN = preset.targetN.toString()
                                            targetP = preset.targetP.toString()
                                            targetK = preset.targetK.toString()
                                            Toast.makeText(context, "Loaded target: ${preset.name}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Target: ${preset.targetN.toInt()}-${preset.targetP.toInt()}-${preset.targetK.toInt()} kg/ha",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = LeafGreenAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TARGET NUTRIENT REQUIREMENTS (PER HECTARE)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Nitrogen (N) Target",
                        value = targetN,
                        onValueChange = { targetN = it },
                        unit = "kg/ha",
                        step = 10.0,
                        presets = listOf("80 kg/ha" to 80.0, "120 kg/ha" to 120.0, "160 kg/ha" to 160.0)
                    )

                    FieldNumericInput(
                        label = "Phosphate (P₂O₅) Target",
                        value = targetP,
                        onValueChange = { targetP = it },
                        unit = "kg/ha",
                        step = 10.0,
                        presets = listOf("40 kg/ha" to 40.0, "60 kg/ha" to 60.0, "90 kg/ha" to 90.0)
                    )

                    FieldNumericInput(
                        label = "Potassium (K₂O) Target",
                        value = targetK,
                        onValueChange = { targetK = it },
                        unit = "kg/ha",
                        step = 10.0,
                        presets = listOf("30 kg/ha" to 30.0, "50 kg/ha" to 50.0, "80 kg/ha" to 80.0)
                    )

                    FieldNumericInput(
                        label = "Total Field Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 1.0,
                        presets = listOf("5 ha" to 5.0, "10 ha" to 10.0, "50 ha" to 50.0)
                    )
                }
            }
        }

        // Fertilizer Bag Breakdown Summary Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, LeafGreenAccent, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RECOMMENDED FERTILIZER SOURCES (50 KG BAGS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = LeafGreenBright
                    )

                    // Urea row
                    FertilizerItemRow(
                        name = "Urea (46% N)",
                        bags = ureaBags,
                        ratePerHa = String.format("%.1f kg/ha", ureaKgHa),
                        totalKg = String.format("%,.0f kg", ureaTotalKg)
                    )

                    // DAP row
                    FertilizerItemRow(
                        name = "DAP (18-46-0)",
                        bags = dapBags,
                        ratePerHa = String.format("%.1f kg/ha", dapKgHa),
                        totalKg = String.format("%,.0f kg", dapTotalKg)
                    )

                    // MOP row
                    FertilizerItemRow(
                        name = "MOP / Potash (60% K₂O)",
                        bags = mopBags,
                        ratePerHa = String.format("%.1f kg/ha", mopKgHa),
                        totalKg = String.format("%,.0f kg", mopTotalKg)
                    )
                }
            }
        }

        // Fertilizer Types Presets Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, LeafGreenAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "FERTILIZER TYPE PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        TextButton(
                            onClick = { showSaveFertPresetDialog = true },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Custom Blend",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LeafGreenAccent
                                )
                            )
                        }
                    }

                    if (fertilizerPresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(fertilizerPresets) { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ForestGreenPrimary)
                                        .border(1.dp, LeafGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${preset.fertNPercent.toInt()}-${preset.fertPPercent.toInt()}-${preset.fertKPercent.toInt()} • ${preset.bagWeightKg.toInt()}kg bag",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = LeafGreenAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC SOIL NPK BALANCER REPORT
                • Total Fertilizers: $totalBags bags (50kg each) for $area ha
                • Urea (46% N): $ureaBags bags (${String.format("%,.0f", ureaTotalKg)} kg)
                • DAP (18-46-0): $dapBags bags (${String.format("%,.0f", dapTotalKg)} kg)
                • MOP (60% K₂O): $mopBags bags (${String.format("%,.0f", mopTotalKg)} kg)
                • NPK Target: $nVal N - $pVal P₂O₅ - $kVal K₂O kg/ha
                • Formula: Multi-source stoichiometric nutrient balance
            """.trimIndent()

            FormulaCard(
                title = "Target NPK Fertilizer Schedule",
                primaryResult = primaryBagsStr,
                primaryUnit = "50kg Bags",
                secondaryResults = listOf(
                    "Urea (46% N)" to "$ureaBags bags (${String.format("%,.0f kg", ureaTotalKg)})",
                    "DAP (18-46-0)" to "$dapBags bags (${String.format("%,.0f kg", dapTotalKg)})",
                    "MOP (60% K₂O)" to "$mopBags bags (${String.format("%,.0f kg", mopTotalKg)})",
                    "DAP Nitrogen Credit" to String.format("%.1f kg N/ha", nSuppliedByDap),
                    "Total Product Mass" to String.format("%,.0f kg", ureaTotalKg + dapTotalKg + mopTotalKg)
                ),
                formula = "DAP = P₂O₅ / 0.46 | Urea = (N - 0.18×DAP) / 0.46 | MOP = K₂O / 0.60",
                explanation = "Accounts for the secondary nitrogen delivered by DAP before calculating top-dress Urea requirements.",
                warningMessage = if (nSuppliedByDap > nVal) "Warning: DAP supplies ${String.format("%.1f", nSuppliedByDap)} kg N, which exceeds target N by ${String.format("%.1f", nSuppliedByDap - nVal)} kg/ha." else null,
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Soil & Fertilizer",
                        calculationType = "Target NPK Balancer",
                        title = "NPK Plan ($totalBags bags)",
                        inputsSummary = "N: $nVal, P: $pVal, K: $kVal kg/ha for $area ha",
                        resultsSummary = "Urea: $ureaBags bags, DAP: $dapBags bags, MOP: $mopBags bags ($totalBags total)",
                        formulaUsed = "Multi-source Stoichiometric Balance with DAP N-credit"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "NPK Fertilizer Balancer",
                        category = "Soil & Fertilizer",
                        primaryMetric = "$totalBags Bags (50kg)",
                        formula = "DAP = P / 0.46, Urea = (N - 0.18×DAP) / 0.46, MOP = K / 0.60",
                        details = listOf(
                            "Target Nutrients" to "$nVal N - $pVal P₂O₅ - $kVal K₂O kg/ha",
                            "Field Area" to "$area ha",
                            "Urea 46% N" to "$ureaBags bags (${String.format("%,.0f", ureaTotalKg)} kg)",
                            "DAP 18-46-0" to "$dapBags bags (${String.format("%,.0f", dapTotalKg)} kg)",
                            "MOP 60% K₂O" to "$mopBags bags (${String.format("%,.0f", mopTotalKg)} kg)",
                            "Total Fertilizer Weight" to String.format("%,.0f kg", ureaTotalKg + dapTotalKg + mopTotalKg)
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showSaveCropPresetDialog) {
        AlertDialog(
            onDismissRequest = { showSaveCropPresetDialog = false },
            title = {
                Text(
                    text = "Save Crop NPK Target",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Save current nutrient target ($nVal N - $pVal P₂O₅ - $kVal K₂O kg/ha):",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FieldNumericInput(
                        label = "Target Name / Crop Variety",
                        value = newCropPresetName,
                        onValueChange = { newCropPresetName = it },
                        hint = "e.g. Barley Malting High Yield",
                        isTextOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (newCropPresetName.isBlank()) "Custom Crop Target" else newCropPresetName.trim()
                        val preset = CustomPreset(
                            name = name,
                            type = PresetType.CROP_NPK,
                            targetN = nVal,
                            targetP = pVal,
                            targetK = kVal,
                            description = "N: ${nVal.toInt()}, P: ${pVal.toInt()}, K: ${kVal.toInt()} kg/ha",
                            isPredefined = false
                        )
                        viewModel.savePreset(preset)
                        newCropPresetName = ""
                        showSaveCropPresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeafGreenAccent,
                        contentColor = SlateDarkBackground
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveCropPresetDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = ForestGreenPrimary,
            titleContentColor = Color.White
        )
    }

    if (showSaveFertPresetDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFertPresetDialog = false },
            title = {
                Text(
                    text = "Save Custom Fertilizer Type",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldNumericInput(
                        label = "Fertilizer Blend Name",
                        value = newFertPresetName,
                        onValueChange = { newFertPresetName = it },
                        hint = "e.g. Starter Blend 20-10-10",
                        isTextOnly = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FieldNumericInput(
                                label = "N %",
                                value = customFertN,
                                onValueChange = { customFertN = it },
                                unit = "%"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FieldNumericInput(
                                label = "P %",
                                value = customFertP,
                                onValueChange = { customFertP = it },
                                unit = "%"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FieldNumericInput(
                                label = "K %",
                                value = customFertK,
                                onValueChange = { customFertK = it },
                                unit = "%"
                            )
                        }
                    }
                    FieldNumericInput(
                        label = "Bag Weight",
                        value = customFertBag,
                        onValueChange = { customFertBag = it },
                        unit = "kg",
                        step = 5.0
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (newFertPresetName.isBlank()) "Custom Fertilizer" else newFertPresetName.trim()
                        val nP = customFertN.toDoubleOrNull() ?: 0.0
                        val pP = customFertP.toDoubleOrNull() ?: 0.0
                        val kP = customFertK.toDoubleOrNull() ?: 0.0
                        val bag = customFertBag.toDoubleOrNull() ?: 50.0

                        val preset = CustomPreset(
                            name = name,
                            type = PresetType.FERTILIZER_TYPE,
                            fertNPercent = nP,
                            fertPPercent = pP,
                            fertKPercent = kP,
                            bagWeightKg = bag,
                            description = "${nP.toInt()}-${pP.toInt()}-${kP.toInt()} (${bag.toInt()}kg bag)",
                            isPredefined = false
                        )
                        viewModel.savePreset(preset)
                        newFertPresetName = ""
                        showSaveFertPresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeafGreenAccent,
                        contentColor = SlateDarkBackground
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFertPresetDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = ForestGreenPrimary,
            titleContentColor = Color.White
        )
    }
}

@Composable
private fun FertilizerItemRow(
    name: String,
    bags: Int,
    ratePerHa: String,
    totalKg: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$ratePerHa • $totalKg total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ForestGreenPrimary)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$bags Bags",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black
                ),
                color = LeafGreenBright
            )
        }
    }
}

@Composable
private fun LimeRequirementView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var currentSoilPh by remember { mutableStateOf("5.2") }
    var targetSoilPh by remember { mutableStateOf("6.5") }
    var selectedSoilType by remember { mutableStateOf("Loam") } // Sandy, Loam, Clay
    var fieldArea by remember { mutableStateOf("15.0") } // ha
    var ccePercent by remember { mutableStateOf("95.0") } // Calcium Carbonate Equivalent %

    val curPh = currentSoilPh.toDoubleOrNull() ?: 5.2
    val tarPh = targetSoilPh.toDoubleOrNull() ?: 6.5
    val area = fieldArea.toDoubleOrNull() ?: 0.0
    val cce = ccePercent.toDoubleOrNull() ?: 95.0

    // Buffer capacity factor (tonnes of pure CaCO3 required per 0.1 pH increase per ha):
    // Sandy: 0.20 t/ha per 0.1 pH
    // Loam: 0.38 t/ha per 0.1 pH
    // Clay: 0.55 t/ha per 0.1 pH
    val bufferFactor = when (selectedSoilType) {
        "Sandy" -> 2.0 // tonnes per 1.0 pH unit
        "Clay" -> 5.5
        else -> 3.8 // Loam
    }

    val deltaPh = max(0.0, tarPh - curPh)
    val pureLimeRatePerHa = deltaPh * bufferFactor
    val adjustedLimeRatePerHa = if (cce > 0) pureLimeRatePerHa * (100.0 / cce) else 0.0
    val totalLimeTonnes = adjustedLimeRatePerHa * area

    val primaryRateStr = String.format("%.2f", adjustedLimeRatePerHa)
    val totalTonnesStr = String.format("%.1f t", totalLimeTonnes)

    val soilTypes = listOf("Sandy", "Loam", "Clay")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SOIL ACIDITY & TEXTURE PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Current Soil pH (H₂O)",
                        value = currentSoilPh,
                        onValueChange = { currentSoilPh = it },
                        unit = "pH",
                        step = 0.1,
                        minValue = 3.0,
                        maxValue = 8.5,
                        presets = listOf("4.8 (Very Acidic)" to 4.8, "5.2 (Acidic)" to 5.2, "5.8" to 5.8)
                    )

                    FieldNumericInput(
                        label = "Target Optimal Soil pH",
                        value = targetSoilPh,
                        onValueChange = { targetSoilPh = it },
                        unit = "pH",
                        step = 0.1,
                        minValue = 5.0,
                        maxValue = 7.5,
                        presets = listOf("6.0 (Cereals)" to 6.0, "6.5 (Optimal)" to 6.5, "6.8 (Legumes)" to 6.8)
                    )

                    // Soil Type selector chips
                    Column {
                        Text(
                            text = "Soil Texture Class",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            soilTypes.forEach { type ->
                                val isSelected = selectedSoilType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) LeafGreenAccent else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.5.dp, if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .clickable { selectedSoilType = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    FieldNumericInput(
                        label = "Field Area to Amend",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 5.0,
                        presets = listOf("5 ha" to 5.0, "15 ha" to 15.0, "30 ha" to 30.0)
                    )

                    FieldNumericInput(
                        label = "Agricultural Lime CCE",
                        value = ccePercent,
                        onValueChange = { ccePercent = it },
                        unit = "% CCE",
                        step = 5.0,
                        hint = "Neutralizing value (typically 90-100%)"
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC SOIL LIME RECOMMENDATION
                • Recommended Lime Rate: $primaryRateStr t/ha
                • Total Field Requirement: $totalTonnesStr for $area ha
                • Soil Type: $selectedSoilType | Current pH: $curPh -> Target: $tarPh (Δ $deltaPh)
                • Lime Quality: $cce% CCE
                • Formula: Lime (t/ha) = (Target pH - Current pH) × BufferFactor × (100 / CCE)
            """.trimIndent()

            FormulaCard(
                title = "Agricultural Lime Requirement",
                primaryResult = primaryRateStr,
                primaryUnit = "t/ha",
                secondaryResults = listOf(
                    "Total Field Tonnes" to totalTonnesStr,
                    "pH Deficit (ΔpH)" to String.format("%.2f units", deltaPh),
                    "Soil Texture" to selectedSoilType,
                    "Buffer Capacity" to "$bufferFactor t/ha per pH unit",
                    "Lime Neutralizing Value" to "$cce % CCE"
                ),
                formula = "Rate (t/ha) = (Target pH - Current pH) × Buffer Factor × (100 / CCE%)",
                explanation = "Calculates buffer index based on cation exchange capacity (CEC) for $selectedSoilType soil.",
                warningMessage = if (adjustedLimeRatePerHa > 6.0) "High single application rate (>6 t/ha). Split application into two seasons to avoid shock." else null,
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Soil & Fertilizer",
                        calculationType = "Lime Requirement",
                        title = "Lime Recommendation ($primaryRateStr t/ha)",
                        inputsSummary = "Soil: $selectedSoilType, pH: $curPh -> $tarPh, Area: $area ha",
                        resultsSummary = "Rate: $primaryRateStr t/ha, Total: $totalTonnesStr",
                        formulaUsed = "Rate = ΔpH × BufferFactor × (100 / CCE)"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Soil Lime Recommendation",
                        category = "Soil & Fertilizer",
                        primaryMetric = "$primaryRateStr t/ha ($totalTonnesStr)",
                        formula = "Rate (t/ha) = (Target pH - Current pH) × Buffer Factor × (100 / CCE%)",
                        details = listOf(
                            "Current Soil pH" to "$curPh",
                            "Target Soil pH" to "$tarPh",
                            "Soil Texture" to selectedSoilType,
                            "Total Field Area" to "$area ha",
                            "Total Lime Required" to totalTonnesStr,
                            "Agricultural Lime Quality" to "$cce % CCE"
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
