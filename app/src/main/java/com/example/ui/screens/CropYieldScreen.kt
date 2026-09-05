package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
fun CropYieldScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Seeding Rate", "Yield Estimator", "Harvest Loss Frame")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("crop_yield_screen")
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
            0 -> SeedingRateView(viewModel, context)
            1 -> PreHarvestYieldView(viewModel, context)
            2 -> HarvestLossFrameView(viewModel, context)
        }
    }
}

@Composable
private fun SeedingRateView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var targetPopulation by remember { mutableStateOf("2500000") } // plants/ha
    var germinationPercent by remember { mutableStateOf("92.0") } // %
    var purityPercent by remember { mutableStateOf("98.0") } // %
    var emergencePercent by remember { mutableStateOf("88.0") } // % field survival
    var tgwGrams by remember { mutableStateOf("45.0") } // thousand grain weight (g)
    var fieldArea by remember { mutableStateOf("20.0") } // ha

    val pop = targetPopulation.toDoubleOrNull() ?: 0.0
    val germ = germinationPercent.toDoubleOrNull() ?: 0.0
    val purity = purityPercent.toDoubleOrNull() ?: 0.0
    val emerg = emergencePercent.toDoubleOrNull() ?: 0.0
    val tgw = tgwGrams.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0

    // Seed Rate (kg/ha) = (Target Pop (plants/ha) × TGW (g)) / (Germ% × Purity% × Emergence% × 100)
    // Because Germ% is in 0..100, (Germ/100) * (Purity/100) * (Emerg/100) = divisor / 1,000,000.
    // TGW is grams per 1,000 seeds.
    // Seeds/ha = Pop / (Germ/100 * Purity/100 * Emerg/100)
    // Mass kg/ha = Seeds/ha * (TGW / 1000) / 1000 = (Pop * TGW) / (Germ * Purity * Emerg * 10)
    val effectiveFactor = (germ / 100.0) * (purity / 100.0) * (emerg / 100.0)
    val seedingRateKgHa = if (effectiveFactor > 0 && pop > 0 && tgw > 0) {
        (pop * (tgw / 1000.0)) / (effectiveFactor * 1000.0)
    } else 0.0

    val seedsPerM2 = if (effectiveFactor > 0) (pop / 10000.0) / effectiveFactor else 0.0
    val totalSeedMassKg = seedingRateKgHa * area

    val primaryResultStr = String.format("%.1f", seedingRateKgHa)
    val totalMassStr = String.format("%,.0f kg", totalSeedMassKg)
    val seedsM2Str = String.format("%.0f seeds/m²", seedsPerM2)

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
                        text = "SEED SPECIFICATIONS & TARGET POPULATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Target Established Population",
                        value = targetPopulation,
                        onValueChange = { targetPopulation = it },
                        unit = "plants/ha",
                        step = 50000.0,
                        presets = listOf("75k (Maize)" to 75000.0, "350k (Soy)" to 350000.0, "700k (Canola)" to 700000.0, "2.5M (Wheat)" to 2500000.0)
                    )

                    FieldNumericInput(
                        label = "Thousand Grain Weight (TGW)",
                        value = tgwGrams,
                        onValueChange = { tgwGrams = it },
                        unit = "g",
                        step = 5.0,
                        presets = listOf("4.0 g (Canola)" to 4.0, "45 g (Wheat)" to 45.0, "160 g (Soy)" to 160.0, "300 g (Maize)" to 300.0)
                    )

                    FieldNumericInput(
                        label = "Laboratory Germination",
                        value = germinationPercent,
                        onValueChange = { germinationPercent = it },
                        unit = "%",
                        step = 1.0,
                        minValue = 50.0,
                        maxValue = 100.0
                    )

                    FieldNumericInput(
                        label = "Certified Seed Purity",
                        value = purityPercent,
                        onValueChange = { purityPercent = it },
                        unit = "%",
                        step = 1.0,
                        minValue = 50.0,
                        maxValue = 100.0
                    )

                    FieldNumericInput(
                        label = "Expected Field Emergence / Survival",
                        value = emergencePercent,
                        onValueChange = { emergencePercent = it },
                        unit = "%",
                        step = 1.0,
                        minValue = 40.0,
                        maxValue = 100.0,
                        hint = "Seedbed condition survival factor (typically 80-90%)"
                    )

                    FieldNumericInput(
                        label = "Total Seeding Field Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 5.0,
                        presets = listOf("10 ha" to 10.0, "20 ha" to 20.0, "50 ha" to 50.0)
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC SEEDING RATE REPORT
                • Required Seed Rate: $primaryResultStr kg/ha ($seedsM2Str)
                • Total Seed Required: $totalMassStr (for $area ha)
                • Target Population: ${String.format("%,.0f", pop)} plants/ha
                • Seed Quality: $germ% Germ, $purity% Purity, $emerg% Emergence, TGW $tgw g
                • Formula: Rate (kg/ha) = (Target Pop × TGW) / (Germ% × Purity% × Emerg% × 10)
            """.trimIndent()

            FormulaCard(
                title = "Required Seeding Rate",
                primaryResult = primaryResultStr,
                primaryUnit = "kg/ha",
                secondaryResults = listOf(
                    "Total Seed Mass" to totalMassStr,
                    "Metering Density" to seedsM2Str,
                    "Target Population" to "${String.format("%,.0f", pop)} plants/ha",
                    "Thousand Grain Weight" to "$tgw g",
                    "Combined Seed Quality" to String.format("%.1f %%", effectiveFactor * 100.0)
                ),
                formula = "Rate (kg/ha) = (Target Population × TGW (g)) / (Germ% × Purity% × Emerg% × 10)",
                explanation = "Accounts for non-viable seeds and seedbed field loss to achieve precise harvest plant density.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Crop Yield",
                        calculationType = "Seeding Rate",
                        title = "Seeding Rate ($primaryResultStr kg/ha)",
                        inputsSummary = "Pop: ${String.format("%,.0f", pop)}/ha, TGW: $tgw g, Area: $area ha",
                        resultsSummary = "Rate: $primaryResultStr kg/ha, Total: $totalMassStr ($seedsM2Str)",
                        formulaUsed = "Rate = (Pop × TGW) / (Germ × Purity × Emerg × 10)"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Crop Seeding Rate Schedule",
                        category = "Crop & Yield",
                        primaryMetric = "$primaryResultStr kg/ha ($totalMassStr)",
                        formula = "Rate (kg/ha) = (Target Pop × TGW) / (Germ × Purity × Emerg × 10)",
                        details = listOf(
                            "Target Population" to "${String.format("%,.0f", pop)} plants/ha",
                            "Thousand Grain Weight (TGW)" to "$tgw g",
                            "Laboratory Germination" to "$germ %",
                            "Seed Purity" to "$purity %",
                            "Field Emergence Survival" to "$emerg %",
                            "Total Field Area" to "$area ha",
                            "Metering Target" to seedsM2Str,
                            "Total Seed Mass Needed" to totalMassStr
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
private fun PreHarvestYieldView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var rowSpacingCm by remember { mutableStateOf("75.0") } // cm
    var sampleLengthM by remember { mutableStateOf("10.0") } // m
    var earsCount by remember { mutableStateOf("56.0") } // ears in sample row
    var grainsPerEar by remember { mutableStateOf("540.0") } // grains per ear
    var tgwGrams by remember { mutableStateOf("300.0") } // TGW (g)
    var fieldArea by remember { mutableStateOf("15.0") } // ha

    val rowSpacing = rowSpacingCm.toDoubleOrNull() ?: 0.0
    val sampleLen = sampleLengthM.toDoubleOrNull() ?: 0.0
    val ears = earsCount.toDoubleOrNull() ?: 0.0
    val grains = grainsPerEar.toDoubleOrNull() ?: 0.0
    val tgw = tgwGrams.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0

    // Sample Area (m²) = (Row Spacing (m)) × Sample Length (m)
    val sampleAreaM2 = (rowSpacing / 100.0) * sampleLen
    val earsPerM2 = if (sampleAreaM2 > 0) ears / sampleAreaM2 else 0.0
    val totalGrainsM2 = earsPerM2 * grains

    // Yield (t/ha) = (Grains/m² × TGW (g)) / 100,000
    // (10,000 m²/ha * Grains/m² * (TGW / 1000 g) / 1000 kg/t = / 100,000)
    val yieldTonnesPerHa = if (totalGrainsM2 > 0 && tgw > 0) {
        (totalGrainsM2 * tgw) / 100000.0
    } else 0.0

    val totalFieldTonnes = yieldTonnesPerHa * area
    val primaryYieldStr = String.format("%.2f", yieldTonnesPerHa)
    val totalTonnesStr = String.format("%.1f tonnes", totalFieldTonnes)

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
                        text = "FIELD SAMPLE QUADRAT PARAMETERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Crop Row Spacing",
                        value = rowSpacingCm,
                        onValueChange = { rowSpacingCm = it },
                        unit = "cm",
                        step = 5.0,
                        presets = listOf("18 cm (Wheat)" to 18.0, "30 cm" to 30.0, "75 cm (Maize)" to 75.0, "90 cm" to 90.0)
                    )

                    FieldNumericInput(
                        label = "Sample Row Length Measured",
                        value = sampleLengthM,
                        onValueChange = { sampleLengthM = it },
                        unit = "m",
                        step = 1.0,
                        presets = listOf("5.0 m" to 5.0, "10.0 m (Standard)" to 10.0, "17.4 m" to 17.4)
                    )

                    FieldNumericInput(
                        label = "Ears / Heads / Pods Counted",
                        value = earsCount,
                        onValueChange = { earsCount = it },
                        unit = "ears",
                        step = 5.0
                    )

                    FieldNumericInput(
                        label = "Average Grain / Seed Count per Head",
                        value = grainsPerEar,
                        onValueChange = { grainsPerEar = it },
                        unit = "seeds",
                        step = 20.0
                    )

                    FieldNumericInput(
                        label = "Thousand Grain Weight (TGW)",
                        value = tgwGrams,
                        onValueChange = { tgwGrams = it },
                        unit = "g",
                        step = 10.0,
                        presets = listOf("45 g (Wheat)" to 45.0, "160 g (Soy)" to 160.0, "300 g (Maize)" to 300.0)
                    )

                    FieldNumericInput(
                        label = "Total Field Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 5.0
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC PRE-HARVEST YIELD ESTIMATE
                • Projected Yield: $primaryYieldStr t/ha
                • Total Field Production: $totalTonnesStr (for $area ha)
                • Head Density: ${String.format("%.1f", earsPerM2)} heads/m²
                • Grain Density: ${String.format("%,.0f", totalGrainsM2)} grains/m²
                • Sample Area: ${String.format("%.2f", sampleAreaM2)} m²
                • Formula: Yield (t/ha) = (Grains/m² × TGW) / 100,000
            """.trimIndent()

            FormulaCard(
                title = "Projected Pre-Harvest Yield",
                primaryResult = primaryYieldStr,
                primaryUnit = "t/ha",
                secondaryResults = listOf(
                    "Total Production" to totalTonnesStr,
                    "Ear Density" to "${String.format("%.1f", earsPerM2)} heads/m²",
                    "Grain Density" to "${String.format("%,.0f", totalGrainsM2)} grains/m²",
                    "Sample Quadrat Area" to "${String.format("%.2f", sampleAreaM2)} m²"
                ),
                formula = "Yield (t/ha) = (Heads/m² × Grains/Head × TGW (g)) / 100,000",
                explanation = "Standard agronomist sample row crop yield forecast algorithm.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Crop Yield",
                        calculationType = "Pre-Harvest Yield Estimator",
                        title = "Yield Estimate ($primaryYieldStr t/ha)",
                        inputsSummary = "Spacing: $rowSpacing cm, Heads: $ears in $sampleLen m, Area: $area ha",
                        resultsSummary = "Yield: $primaryYieldStr t/ha, Total: $totalTonnesStr",
                        formulaUsed = "Yield = (Heads/m² × Grains × TGW) / 100000"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Pre-Harvest Yield Estimate",
                        category = "Crop & Yield",
                        primaryMetric = "$primaryYieldStr t/ha ($totalTonnesStr)",
                        formula = "Yield (t/ha) = (Heads/m² × Grains/Head × TGW (g)) / 100,000",
                        details = listOf(
                            "Row Spacing" to "$rowSpacing cm",
                            "Sample Row Length" to "$sampleLen m",
                            "Ears Counted" to "$ears",
                            "Grains per Head" to "$grains",
                            "Thousand Grain Weight" to "$tgw g",
                            "Total Field Area" to "$area ha",
                            "Total Projected Production" to totalTonnesStr
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
private fun HarvestLossFrameView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var seedsCountedInFrame by remember { mutableStateOf("120.0") } // seeds in 1m x 1m frame
    var frameAreaM2 by remember { mutableStateOf("1.0") } // m²
    var tgwGrams by remember { mutableStateOf("45.0") } // g
    var cropPricePerTonne by remember { mutableStateOf("260.0") } // USD/EUR per tonne

    val seeds = seedsCountedInFrame.toDoubleOrNull() ?: 0.0
    val frameArea = frameAreaM2.toDoubleOrNull() ?: 1.0
    val tgw = tgwGrams.toDoubleOrNull() ?: 45.0
    val price = cropPricePerTonne.toDoubleOrNull() ?: 0.0

    // Loss (kg/ha) = (Seeds/m² × TGW (g) × 10,000 m²/ha) / (1,000 seeds × 1,000 g/kg)
    // Loss (kg/ha) = (Seeds / FrameArea) × TGW × 0.01
    val seedsPerM2 = if (frameArea > 0) seeds / frameArea else 0.0
    val lossKgHa = seedsPerM2 * tgw * 0.01
    val lossTonnesHa = lossKgHa / 1000.0
    val financialLossPerHa = lossTonnesHa * price

    val lossStatus = when {
        lossKgHa < 50.0 -> "Acceptable Combine Loss (< 50 kg/ha)"
        lossKgHa < 120.0 -> "Moderate Loss (Adjust combine fan / sieve)"
        else -> "Excessive Combine Loss (Immediate adjustment required)"
    }

    val primaryLossStr = String.format("%.1f", lossKgHa)
    val financialStr = String.format("$%.2f / ha", financialLossPerHa)

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
                        text = "1M × 1M COMBINE LOSS FRAME COUNTER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Loose Seeds Counted Inside Frame",
                        value = seedsCountedInFrame,
                        onValueChange = { seedsCountedInFrame = it },
                        unit = "seeds",
                        step = 10.0,
                        hint = "Count grains dropped behind combine discharge"
                    )

                    FieldNumericInput(
                        label = "Drop Frame Area",
                        value = frameAreaM2,
                        onValueChange = { frameAreaM2 = it },
                        unit = "m²",
                        step = 0.1,
                        presets = listOf("0.25 m² (50×50cm)" to 0.25, "0.5 m²" to 0.5, "1.0 m² (Standard)" to 1.0)
                    )

                    FieldNumericInput(
                        label = "Thousand Grain Weight (TGW)",
                        value = tgwGrams,
                        onValueChange = { tgwGrams = it },
                        unit = "g",
                        step = 5.0,
                        presets = listOf("4.0 g (Canola)" to 4.0, "30 g (Barley)" to 30.0, "45 g (Wheat)" to 45.0, "300 g (Corn)" to 300.0)
                    )

                    FieldNumericInput(
                        label = "Crop Grain Value",
                        value = cropPricePerTonne,
                        onValueChange = { cropPricePerTonne = it },
                        unit = "$/tonne",
                        step = 20.0,
                        hint = "Market price to calculate financial loss per ha"
                    )
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC HARVEST LOSS FRAME ANALYSIS
                • Combine Harvest Loss: $primaryLossStr kg/ha
                • Status: $lossStatus
                • Financial Loss: $financialStr
                • Seed Count: ${String.format("%.0f", seedsPerM2)} seeds/m² (TGW $tgw g)
                • Formula: Loss (kg/ha) = Seeds/m² × TGW (g) × 0.01
            """.trimIndent()

            FormulaCard(
                title = "Combine Harvest Loss",
                primaryResult = primaryLossStr,
                primaryUnit = "kg/ha",
                secondaryResults = listOf(
                    "Loss Status" to lossStatus,
                    "Estimated Cost" to financialStr,
                    "Ground Density" to "${String.format("%.0f", seedsPerM2)} seeds/m²",
                    "Thousand Grain Weight" to "$tgw g"
                ),
                formula = "Harvest Loss (kg/ha) = Seeds/m² × TGW (g) × 0.01",
                explanation = "Translates loose grains counted on ground into aggregate field harvest loss per hectare.",
                warningMessage = if (lossKgHa >= 100.0) "Warning: High combine loss ($primaryLossStr kg/ha). Check concave clearance and cleaning shoe wind speed." else null,
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Crop Yield",
                        calculationType = "Harvest Loss Frame",
                        title = "Combine Loss ($primaryLossStr kg/ha)",
                        inputsSummary = "Seeds: ${String.format("%.0f", seedsPerM2)}/m², TGW: $tgw g, Price: $$price/t",
                        resultsSummary = "Loss: $primaryLossStr kg/ha ($financialStr), Status: $lossStatus",
                        formulaUsed = "Loss = Seeds/m² × TGW × 0.01"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Combine Harvest Loss Audit",
                        category = "Crop & Yield",
                        primaryMetric = "$primaryLossStr kg/ha ($financialStr)",
                        formula = "Loss (kg/ha) = Seeds/m² × TGW (g) × 0.01",
                        details = listOf(
                            "Seeds Counted in Frame" to "$seeds seeds",
                            "Frame Area" to "$frameArea m²",
                            "Seeds per m²" to "${String.format("%.0f", seedsPerM2)}",
                            "Thousand Grain Weight" to "$tgw g",
                            "Combine Assessment" to lossStatus,
                            "Financial Loss Impact" to financialStr
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
