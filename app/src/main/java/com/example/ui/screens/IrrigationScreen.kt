package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FieldNumericInput
import com.example.ui.components.FormulaCard
import com.example.ui.theme.LeafGreenAccent
import com.example.util.ReportExporter
import com.example.viewmodel.AgriCalcViewModel

@Composable
fun IrrigationScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Crop ETc Water Need", "Pump Run-Time", "Dam & Pond Capacity")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("irrigation_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 0.dp,
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
            0 -> CropEtcNeedView(viewModel, context)
            1 -> PumpRunTimeView(viewModel, context)
            2 -> DamCapacityView(viewModel, context)
        }
    }
}

@Composable
private fun CropEtcNeedView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var refEt0 by remember { mutableStateOf("5.2") } // mm/day
    var cropKc by remember { mutableStateOf("1.15") } // crop coefficient
    var fieldArea by remember { mutableStateOf("12.0") } // ha

    val et0 = refEt0.toDoubleOrNull() ?: 0.0
    val kc = cropKc.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0

    // ETc (mm/day) = ET0 × Kc
    val etcMmDay = et0 * kc
    // 1 mm depth on 1 hectare = 10 m³
    val volumePerHaM3 = etcMmDay * 10.0
    val totalDailyVolumeM3 = volumePerHaM3 * area
    val totalWeeklyVolumeM3 = totalDailyVolumeM3 * 7.0

    val primaryResultStr = String.format("%.2f", etcMmDay)
    val dailyVolStr = String.format("%,.0f m³/day", totalDailyVolumeM3)
    val weeklyVolStr = String.format("%,.0f m³/week", totalWeeklyVolumeM3)

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
                        text = "EVAPOTRANSPIRATION & WEATHER PARAMETERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Reference Evapotranspiration (ET₀)",
                        value = refEt0,
                        onValueChange = { refEt0 = it },
                        unit = "mm/day",
                        step = 0.5,
                        presets = listOf("3.5 mm (Cool)" to 3.5, "5.2 mm (Moderate)" to 5.2, "7.0 mm (Hot/Dry)" to 7.0)
                    )

                    FieldNumericInput(
                        label = "Crop Coefficient (Kc)",
                        value = cropKc,
                        onValueChange = { cropKc = it },
                        unit = "Kc",
                        step = 0.05,
                        presets = listOf("0.45 (Emergence)" to 0.45, "0.80 (Vegetative)" to 0.80, "1.15 (Peak Canopy)" to 1.15, "0.65 (Maturity)" to 0.65)
                    )

                    FieldNumericInput(
                        label = "Field Irrigated Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 2.0,
                        presets = listOf("5 ha" to 5.0, "12 ha" to 12.0, "25 ha" to 25.0)
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC CROP EVAPOTRANSPIRATION (ETc) REPORT
                • Daily Water Need: $primaryResultStr mm/day
                • Total Field Daily Volume: $dailyVolStr (for $area ha)
                • Weekly Volume Needed: $weeklyVolStr
                • Formula: ETc (mm/day) = ET₀ × Kc | Volume: 1 mm/ha = 10 m³
            """.trimIndent()

            FormulaCard(
                title = "Crop Water Requirement (ETc)",
                primaryResult = primaryResultStr,
                primaryUnit = "mm/day",
                secondaryResults = listOf(
                    "Total Daily Volume" to dailyVolStr,
                    "Total Weekly Volume" to weeklyVolStr,
                    "Per Hectare Daily" to String.format("%.1f m³/ha/day", volumePerHaM3),
                    "Reference ET₀" to "$et0 mm/day",
                    "Crop Coefficient (Kc)" to "$kc"
                ),
                formula = "ETc (mm/day) = ET₀ × Kc | Volume (m³) = ETc × Area (ha) × 10",
                explanation = "FAO-56 Penman-Monteith crop water requirement equation.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Irrigation",
                        calculationType = "Crop ETc Water Need",
                        title = "Water Need ($primaryResultStr mm/day)",
                        inputsSummary = "ET0: $et0 mm/d, Kc: $kc, Area: $area ha",
                        resultsSummary = "Daily: $primaryResultStr mm/day ($dailyVolStr), Weekly: $weeklyVolStr",
                        formulaUsed = "ETc = ET0 × Kc, 1 mm/ha = 10 m³"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Crop ETc Water Requirement",
                        category = "Irrigation",
                        primaryMetric = "$primaryResultStr mm/day ($dailyVolStr)",
                        formula = "ETc (mm/day) = ET₀ × Kc | 1 mm/ha = 10 m³",
                        details = listOf(
                            "Reference ET₀" to "$et0 mm/day",
                            "Crop Coefficient (Kc)" to "$kc",
                            "Field Area" to "$area ha",
                            "Daily Field Water Volume" to dailyVolStr,
                            "Weekly Irrigation Volume" to weeklyVolStr
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
private fun PumpRunTimeView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var pumpFlowRateM3h by remember { mutableStateOf("45.0") } // m³/h
    var targetDepthMm by remember { mutableStateOf("30.0") } // mm net application depth
    var fieldArea by remember { mutableStateOf("8.0") } // ha
    var systemEfficiencyPercent by remember { mutableStateOf("80.0") } // %

    val flowRate = pumpFlowRateM3h.toDoubleOrNull() ?: 0.0
    val depth = targetDepthMm.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0
    val eff = systemEfficiencyPercent.toDoubleOrNull() ?: 80.0

    // Net Volume (m³) = Depth (mm) × Area (ha) × 10 m³/ha
    val netVolumeM3 = depth * area * 10.0
    // Gross Volume = Net Volume / (Eff / 100)
    val grossVolumeM3 = if (eff > 0) netVolumeM3 / (eff / 100.0) else 0.0

    // Run Time (hours) = Gross Volume / Pump Flow Rate
    val totalHours = if (flowRate > 0) grossVolumeM3 / flowRate else 0.0
    val hoursInt = totalHours.toInt()
    val minutesInt = ((totalHours - hoursInt) * 60).toInt()

    val primaryTimeStr = "${hoursInt}h ${minutesInt}m"
    val grossVolStr = String.format("%,.0f m³", grossVolumeM3)
    val netVolStr = String.format("%,.0f m³", netVolumeM3)

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
                        text = "PUMP DELIVERY & SYSTEM EFFICIENCY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Pump Delivery Flow Rate",
                        value = pumpFlowRateM3h,
                        onValueChange = { pumpFlowRateM3h = it },
                        unit = "m³/h",
                        step = 5.0,
                        presets = listOf("20 m³/h" to 20.0, "45 m³/h (Standard)" to 45.0, "90 m³/h (Large)" to 90.0)
                    )

                    FieldNumericInput(
                        label = "Target Gross Irrigation Depth",
                        value = targetDepthMm,
                        onValueChange = { targetDepthMm = it },
                        unit = "mm",
                        step = 5.0,
                        presets = listOf("15 mm (Light)" to 15.0, "30 mm (Standard)" to 30.0, "50 mm (Heavy)" to 50.0)
                    )

                    FieldNumericInput(
                        label = "Field Size to Irrigate",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 2.0,
                        presets = listOf("4 ha" to 4.0, "8 ha" to 8.0, "20 ha" to 20.0)
                    )

                    FieldNumericInput(
                        label = "Irrigation System Efficiency",
                        value = systemEfficiencyPercent,
                        onValueChange = { systemEfficiencyPercent = it },
                        unit = "%",
                        step = 5.0,
                        minValue = 40.0,
                        maxValue = 100.0,
                        presets = listOf("50% (Furrow)" to 50.0, "70% (Sprinkler)" to 70.0, "80% (Center Pivot)" to 80.0, "90% (Drip)" to 90.0)
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC PUMP RUN-TIME & HYDRAULICS
                • Required Run-Time: $primaryTimeStr
                • Gross Water Required: $grossVolStr
                • Net Crop Water: $netVolStr
                • Pump Flow Rate: $flowRate m³/h | Efficiency: $eff%
                • Formula: Hours = [Depth (mm) × Area (ha) × 10] / [Pump (m³/h) × (Eff% / 100)]
            """.trimIndent()

            FormulaCard(
                title = "Required Pump Run-Time",
                primaryResult = primaryTimeStr,
                primaryUnit = "Run Time",
                secondaryResults = listOf(
                    "Gross Water Volume" to grossVolStr,
                    "Net Water Volume" to netVolStr,
                    "System Efficiency" to "$eff %",
                    "Pump Flow Rate" to "$flowRate m³/h",
                    "Application Depth" to "$depth mm"
                ),
                formula = "Time (h) = (Depth (mm) × Area (ha) × 10) / (Pump (m³/h) × (Eff% / 100))",
                explanation = "Calculates operating pump duration compensating for evaporation and hydraulic transmission losses.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Irrigation",
                        calculationType = "Pump Run-Time",
                        title = "Pump Run-Time ($primaryTimeStr)",
                        inputsSummary = "Depth: $depth mm, Area: $area ha, Pump: $flowRate m³/h, Eff: $eff%",
                        resultsSummary = "Run-Time: $primaryTimeStr, Gross Water: $grossVolStr",
                        formulaUsed = "Time = (Depth × Area × 10) / (Pump × Eff)"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Pump Run-Time Schedule",
                        category = "Irrigation",
                        primaryMetric = "$primaryTimeStr ($grossVolStr)",
                        formula = "Time (h) = (Depth × Area × 10) / (Pump × (Eff% / 100))",
                        details = listOf(
                            "Pump Flow Rate" to "$flowRate m³/h",
                            "Target Application Depth" to "$depth mm",
                            "Field Area" to "$area ha",
                            "System Efficiency" to "$eff %",
                            "Gross Water Volume" to grossVolStr,
                            "Net Water Volume" to netVolStr
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
private fun DamCapacityView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var topLengthM by remember { mutableStateOf("60.0") } // m
    var topWidthM by remember { mutableStateOf("40.0") } // m
    var bottomLengthM by remember { mutableStateOf("40.0") } // m
    var bottomWidthM by remember { mutableStateOf("20.0") } // m
    var waterDepthM by remember { mutableStateOf("3.5") } // m

    val topL = topLengthM.toDoubleOrNull() ?: 0.0
    val topW = topWidthM.toDoubleOrNull() ?: 0.0
    val botL = bottomLengthM.toDoubleOrNull() ?: 0.0
    val botW = bottomWidthM.toDoubleOrNull() ?: 0.0
    val depth = waterDepthM.toDoubleOrNull() ?: 0.0

    // Prismoidal formula for trapezoidal dam / reservoir:
    // A_top = topL * topW
    // A_bottom = botL * botW
    // A_mid = ((topL + botL) / 2) * ((topW + botW) / 2)
    // Volume (m³) = (depth / 6) * (A_top + A_bottom + 4 * A_mid)
    val aTop = topL * topW
    val aBot = botL * botW
    val midL = (topL + botL) / 2.0
    val midW = (topW + botW) / 2.0
    val aMid = midL * midW

    val volumeM3 = if (depth > 0) {
        (depth / 6.0) * (aTop + aBot + 4 * aMid)
    } else 0.0

    val volumeMegalitres = volumeM3 / 1000.0
    val volumeLitres = volumeM3 * 1000.0

    // Irrigable hectares supported at standard 30 mm application (300 m³/ha)
    val irrigableHa = if (volumeM3 > 0) volumeM3 / 300.0 else 0.0

    val primaryVolStr = String.format("%,.0f", volumeM3)
    val megaLitresStr = String.format("%.2f ML", volumeMegalitres)
    val irrigableStr = String.format("%.1f ha (@ 30mm depth)", irrigableHa)

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
                        text = "TRAPEZOIDAL DAM & RESERVOIR DIMENSIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Top Crest Water Length",
                        value = topLengthM,
                        onValueChange = { topLengthM = it },
                        unit = "m",
                        step = 5.0,
                        presets = listOf("40 m" to 40.0, "60 m" to 60.0, "100 m" to 100.0)
                    )

                    FieldNumericInput(
                        label = "Top Crest Water Width",
                        value = topWidthM,
                        onValueChange = { topWidthM = it },
                        unit = "m",
                        step = 5.0,
                        presets = listOf("25 m" to 25.0, "40 m" to 40.0, "60 m" to 60.0)
                    )

                    FieldNumericInput(
                        label = "Bottom Base Length",
                        value = bottomLengthM,
                        onValueChange = { bottomLengthM = it },
                        unit = "m",
                        step = 5.0
                    )

                    FieldNumericInput(
                        label = "Bottom Base Width",
                        value = bottomWidthM,
                        onValueChange = { bottomWidthM = it },
                        unit = "m",
                        step = 5.0
                    )

                    FieldNumericInput(
                        label = "Water Operating Depth",
                        value = waterDepthM,
                        onValueChange = { waterDepthM = it },
                        unit = "m",
                        step = 0.5,
                        presets = listOf("2.5 m" to 2.5, "3.5 m" to 3.5, "5.0 m" to 5.0)
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC FARM DAM CAPACITY REPORT
                • Reservoir Storage: $primaryVolStr m³ ($megaLitresStr)
                • Irrigable Potential: $irrigableStr
                • Top Dimensions: $topL m × $topW m (${String.format("%,.0f", aTop)} m²)
                • Bottom Dimensions: $botL m × $botW m (${String.format("%,.0f", aBot)} m²)
                • Operating Depth: $depth m
                • Formula: Prismoidal V = (h/6) × (A_top + A_bottom + 4 × A_mid)
            """.trimIndent()

            FormulaCard(
                title = "Farm Dam & Storage Volume",
                primaryResult = primaryVolStr,
                primaryUnit = "m³",
                secondaryResults = listOf(
                    "Volume in Megalitres" to megaLitresStr,
                    "Volume in Litres" to String.format("%,.0f L", volumeLitres),
                    "Irrigation Capacity" to irrigableStr,
                    "Top Surface Area" to String.format("%,.0f m²", aTop),
                    "Bottom Base Area" to String.format("%,.0f m²", aBot)
                ),
                formula = "Volume (m³) = (Depth / 6) × (A_top + A_bottom + 4 × A_mid)",
                explanation = "Prismoidal formula for earth dams with sloped 1:2 or 1:3 batter banks.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Irrigation",
                        calculationType = "Dam & Pond Volume",
                        title = "Dam Capacity ($primaryVolStr m³)",
                        inputsSummary = "Top: ${topL}x${topW}m, Bottom: ${botL}x${botW}m, Depth: ${depth}m",
                        resultsSummary = "Volume: $primaryVolStr m³ ($megaLitresStr), Irrigates: $irrigableStr",
                        formulaUsed = "Prismoidal V = (h/6) × (A1 + A2 + 4×Amid)"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Farm Dam Storage Audit",
                        category = "Irrigation",
                        primaryMetric = "$primaryVolStr m³ ($megaLitresStr)",
                        formula = "V = (h/6) × (A_top + A_bottom + 4 × A_mid)",
                        details = listOf(
                            "Top Water Length & Width" to "$topL m × $topW m",
                            "Bottom Length & Width" to "$botL m × $botW m",
                            "Water Operating Depth" to "$depth m",
                            "Megalitres (ML)" to megaLitresStr,
                            "Irrigation Potential" to irrigableStr
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
