package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FieldNumericInput
import com.example.ui.components.FormulaCard
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.util.ReportExporter
import com.example.viewmodel.AgriCalcViewModel

@Composable
fun SprayingScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Boom Calibration", "Knapsack & Tank Mix")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("spraying_screen")
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
            0 -> BoomSprayerCalibrationView(viewModel, context)
            1 -> KnapsackTankMixView(viewModel, context)
        }
    }
}

@Composable
private fun BoomSprayerCalibrationView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var targetRate by remember { mutableStateOf("150.0") } // L/ha
    var nozzleSpacing by remember { mutableStateOf("50.0") } // cm
    var tractorSpeed by remember { mutableStateOf("8.0") } // km/h

    val rate = targetRate.toDoubleOrNull() ?: 0.0
    val spacing = nozzleSpacing.toDoubleOrNull() ?: 0.0
    val speed = tractorSpeed.toDoubleOrNull() ?: 0.0

    // Flow rate per nozzle (L/min) = (Rate (L/ha) × Speed (km/h) × Spacing (cm)) / 60,000
    val flowRateLPerMin = if (rate > 0 && spacing > 0 && speed > 0) {
        (rate * speed * spacing) / 60000.0
    } else 0.0

    val flowRateMLPerMin = flowRateLPerMin * 1000.0

    // Recommended ISO tip based on flow rate at 3 bar standard pressure
    val (isoColor, isoCode) = when {
        flowRateLPerMin <= 0.0 -> Pair(Color.Gray, "None")
        flowRateLPerMin < 0.6 -> Pair(Color(0xFFFFA500), "Orange ISO 01 (0.40 L/min)")
        flowRateLPerMin < 0.9 -> Pair(Color(0xFFFFD700), "Yellow ISO 02 (0.80 L/min)")
        flowRateLPerMin < 1.4 -> Pair(Color(0xFF1E90FF), "Blue ISO 03 (1.20 L/min)")
        flowRateLPerMin < 1.8 -> Pair(Color(0xFFFF4500), "Red ISO 04 (1.60 L/min)")
        flowRateLPerMin < 2.2 -> Pair(Color(0xFF8B4513), "Brown ISO 05 (2.00 L/min)")
        else -> Pair(Color.Gray, "Grey ISO 06 (2.40+ L/min)")
    }

    val primaryResultStr = String.format("%.2f", flowRateLPerMin)
    val secondaryMLStr = String.format("%,.0f mL/min", flowRateMLPerMin)

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
                        text = "BOOM SPRAYER PARAMETERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Target Application Rate",
                        value = targetRate,
                        onValueChange = { targetRate = it },
                        unit = "L/ha",
                        step = 10.0,
                        presets = listOf("100 L/ha" to 100.0, "150 L/ha" to 150.0, "200 L/ha" to 200.0)
                    )

                    FieldNumericInput(
                        label = "Nozzle Spacing on Boom",
                        value = nozzleSpacing,
                        onValueChange = { nozzleSpacing = it },
                        unit = "cm",
                        step = 5.0,
                        presets = listOf("50 cm (Standard)" to 50.0, "75 cm (Wide)" to 75.0, "100 cm" to 100.0)
                    )

                    FieldNumericInput(
                        label = "Tractor Ground Speed",
                        value = tractorSpeed,
                        onValueChange = { tractorSpeed = it },
                        unit = "km/h",
                        step = 0.5,
                        presets = listOf("6 km/h" to 6.0, "8 km/h" to 8.0, "10 km/h" to 10.0, "12 km/h" to 12.0)
                    )
                }
            }
        }

        // ISO Nozzle Recommendation Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(isoColor)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ISO 10625 NOZZLE TIP GUIDELINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = LeafGreenBright
                        )
                        Text(
                            text = isoCode,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC BOOM SPRAYER CALIBRATION
                • Required Nozzle Flow: $primaryResultStr L/min ($secondaryMLStr)
                • Recommended Tip: $isoCode
                • Application Rate: $rate L/ha | Spacing: $spacing cm | Speed: $speed km/h
                • Formula: q (L/min) = (Rate × Speed × Spacing) / 60,000
            """.trimIndent()

            FormulaCard(
                title = "Required Nozzle Flow Rate",
                primaryResult = primaryResultStr,
                primaryUnit = "L/min",
                secondaryResults = listOf(
                    "Metric Flow Rate" to secondaryMLStr,
                    "Recommended ISO Nozzle" to isoCode,
                    "Application Rate" to "$rate L/ha",
                    "Boom Spacing" to "$spacing cm",
                    "Tractor Speed" to "$speed km/h"
                ),
                formula = "q (L/min) = (Rate (L/ha) × Speed (km/h) × Spacing (cm)) / 60,000",
                explanation = "ISO standard sprayer calibration formula for uniform swath distribution.",
                warningMessage = if (flowRateLPerMin > 2.5) "Very high nozzle flow rate. Consider reducing speed or increasing nozzle count." else null,
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Spraying",
                        calculationType = "Boom Sprayer Calibration",
                        title = "Sprayer Nozzle ($primaryResultStr L/min)",
                        inputsSummary = "Rate: $rate L/ha, Speed: $speed km/h, Spacing: $spacing cm",
                        resultsSummary = "Flow: $primaryResultStr L/min ($secondaryMLStr), Tip: $isoCode",
                        formulaUsed = "q = (R × S × W) / 60000"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Boom Sprayer Calibration",
                        category = "Spraying",
                        primaryMetric = "$primaryResultStr L/min ($secondaryMLStr)",
                        formula = "q (L/min) = (Rate × Speed × Spacing) / 60,000",
                        details = listOf(
                            "Target Application Rate" to "$rate L/ha",
                            "Nozzle Spacing" to "$spacing cm",
                            "Tractor Ground Speed" to "$speed km/h",
                            "Recommended ISO Tip" to isoCode
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun KnapsackTankMixView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var tankCapacity by remember { mutableStateOf("16.0") } // Litres
    var fieldArea by remember { mutableStateOf("2.5") } // ha
    var chemicalDoseRate by remember { mutableStateOf("1.5") } // L/ha or kg/ha
    var chemicalUnit by remember { mutableStateOf("L/ha") } // "L/ha" or "g/ha"
    var waterVolumeRate by remember { mutableStateOf("200.0") } // L/ha total spray volume

    val tankSize = tankCapacity.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0
    val dose = chemicalDoseRate.toDoubleOrNull() ?: 0.0
    val waterRate = waterVolumeRate.toDoubleOrNull() ?: 0.0

    // Calculations
    val totalWaterRequiredL = area * waterRate
    val totalTanksNeeded = if (tankSize > 0) totalWaterRequiredL / tankSize else 0.0
    val fullTanks = totalTanksNeeded.toInt()
    val partialTankVolume = (totalTanksNeeded - fullTanks) * tankSize

    // Chemical per tank
    // chemicalPerLitre = dose / waterRate
    val chemPerTank = if (waterRate > 0) (dose / waterRate) * tankSize else 0.0

    // Formatted strings
    val (primaryResultStr, primaryUnitStr) = if (chemicalUnit == "L/ha") {
        val mlPerTank = chemPerTank * 1000.0
        if (mlPerTank < 1000) {
            Pair(String.format("%.1f", mlPerTank), "mL / tank")
        } else {
            Pair(String.format("%.2f", chemPerTank), "L / tank")
        }
    } else { // g/ha
        Pair(String.format("%.1f", chemPerTank), "g / tank")
    }

    val totalChemForField = dose * area
    val totalChemStr = if (chemicalUnit == "L/ha") {
        String.format("%.2f L total", totalChemForField)
    } else {
        String.format("%.2f kg total", totalChemForField / 1000.0)
    }

    val tankSummaryStr = if (partialTankVolume > 0.1) {
        "$fullTanks full tanks + 1 tank @ ${String.format("%.1f", partialTankVolume)} L (${String.format("%.1f", totalTanksNeeded)} total)"
    } else {
        "$fullTanks full tanks"
    }

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
                        text = "TANK & MIX RATIO SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Sprayer Tank Capacity",
                        value = tankCapacity,
                        onValueChange = { tankCapacity = it },
                        unit = "L",
                        step = 2.0,
                        presets = listOf("16 L (Knapsack)" to 16.0, "20 L" to 20.0, "600 L (Tractor)" to 600.0, "1000 L" to 1000.0)
                    )

                    FieldNumericInput(
                        label = "Field Application Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 0.5,
                        presets = listOf("0.5 ha" to 0.5, "2.5 ha" to 2.5, "10.0 ha" to 10.0)
                    )

                    // Chemical Dose & Unit toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FieldNumericInput(
                                label = "Chemical Dosage Rate",
                                value = chemicalDoseRate,
                                onValueChange = { chemicalDoseRate = it },
                                unit = chemicalUnit,
                                step = 0.5
                            )
                        }
                    }

                    FieldNumericInput(
                        label = "Water Carrier Spray Volume",
                        value = waterVolumeRate,
                        onValueChange = { waterVolumeRate = it },
                        unit = "L/ha",
                        step = 20.0,
                        presets = listOf("150 L/ha" to 150.0, "200 L/ha (Standard)" to 200.0, "300 L/ha" to 300.0)
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC TANK MIX & CHEMICAL DOSAGE
                • Chemical per Tank: $primaryResultStr $primaryUnitStr
                • Total Tanks Required: $tankSummaryStr
                • Total Spray Volume: ${String.format("%,.0f", totalWaterRequiredL)} L water
                • Total Active Chemical: $totalChemStr
                • Formula: Chem/Tank = (Dose / Water Carrier) × Tank Capacity
            """.trimIndent()

            FormulaCard(
                title = "Knapsack & Tank Mix Batch",
                primaryResult = primaryResultStr,
                primaryUnit = primaryUnitStr,
                secondaryResults = listOf(
                    "Tanks Needed" to tankSummaryStr,
                    "Total Water Needed" to "${String.format("%,.0f", totalWaterRequiredL)} L",
                    "Total Chemical Needed" to totalChemStr,
                    "Carrier Rate" to "$waterRate L/ha"
                ),
                formula = "Chem/Tank = (Dosage / Spray Volume) × Tank Size",
                explanation = "Ensures correct dilution concentration across tank refills without chemical under- or over-dosing.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Spraying",
                        calculationType = "Knapsack Tank Mix",
                        title = "Tank Mix ($primaryResultStr $primaryUnitStr)",
                        inputsSummary = "Tank: $tankSize L, Area: $area ha, Dose: $dose $chemicalUnit, Water: $waterRate L/ha",
                        resultsSummary = "Per Tank: $primaryResultStr $primaryUnitStr, Total: $tankSummaryStr ($totalChemStr)",
                        formulaUsed = "ChemPerTank = (Dose / WaterRate) × TankSize"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Chemical Tank Mix Batch",
                        category = "Spraying",
                        primaryMetric = "$primaryResultStr $primaryUnitStr",
                        formula = "Chem/Tank = (Dose / Spray Volume) × Tank Capacity",
                        details = listOf(
                            "Tank Capacity" to "$tankSize L",
                            "Field Area" to "$area ha",
                            "Chemical Dosage" to "$dose $chemicalUnit",
                            "Total Spray Water" to "${String.format("%,.0f", totalWaterRequiredL)} L",
                            "Total Tanks Needed" to tankSummaryStr,
                            "Total Field Chemical" to totalChemStr
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
