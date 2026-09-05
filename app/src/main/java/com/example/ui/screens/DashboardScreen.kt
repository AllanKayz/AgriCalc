package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgriNavDestination
import com.example.model.UnitConversionType
import com.example.ui.components.FieldNumericInput
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HydraulicBlue
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.ui.theme.SlateDarkBackground

@Composable
fun DashboardScreen(
    onNavigate: (AgriNavDestination) -> Unit,
    savedRecordsCount: Int,
    modifier: Modifier = Modifier,
    presetsCount: Int = 0,
    onOpenPresets: () -> Unit = {}
) {
    var quickCycleIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedUnitIndex by remember { mutableIntStateOf(0) }
    var inputUnitValA by remember { mutableStateOf("1.0") }
    var isReverseConvert by remember { mutableStateOf(false) }

    // Quick cycle includes common agricultural metric conversions
    val quickConversions = listOf(
        "1.0 bu (Wheat/Soy) = 27.216 kg",
        "1.0 bu (Corn) = 25.401 kg",
        "1.0 gal (US) = 3.785 L",
        "1.0 fl oz = 29.574 mL",
        "1.0 ha = 10,000 m²",
        "100 L/ha = 10 mL/m²",
        "1.0 t/ha = 1,000 kg/ha",
        "1.0 m³ = 1,000 L",
        "10 km/h = 2.78 m/s",
        "100 kg = 100,000 g"
    )

    val allConversions = UnitConversionType.entries
    val categoryList = listOf("All", "Grain", "Volume", "Chemical", "Land", "Yield", "Speed")

    val filteredConversions = if (selectedCategoryFilter == "All") {
        allConversions
    } else {
        allConversions.filter { it.category == selectedCategoryFilter }
    }

    val activeIndex = selectedUnitIndex.coerceIn(0, (filteredConversions.size - 1).coerceAtLeast(0))
    val activeConversion = if (filteredConversions.isNotEmpty()) filteredConversions[activeIndex] else allConversions[0]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Quick Converter Hero Card (Live Carousel with Ag Metric Conversions)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, LeafGreenAccent.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                    .clickable {
                        quickCycleIndex = (quickCycleIndex + 1) % quickConversions.size
                    }
                    .testTag("quick_converter_hero"),
                colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "QUICK AGRI-CONVERTER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = LeafGreenAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• TAP TO CYCLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = quickConversions[quickCycleIndex],
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SlateDarkBackground)
                            .border(2.dp, LeafGreenAccent, CircleShape)
                            .clickable {
                                quickCycleIndex = (quickCycleIndex + 1) % quickConversions.size
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Next Quick Conversion",
                            tint = LeafGreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 2. 2-Column Grid (6 Polish Module Cards: Field, Spray, Soil, Crop, Irrigation, History)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Field & Land + Spray & Mix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Place,
                        iconBg = LeafGreenAccent,
                        iconTint = SlateDarkBackground,
                        title = "Field & Land\nMeasure",
                        subtitle = "HA, M², WORK RATE",
                        subtitleColor = LeafGreenAccent,
                        testTag = "module_card_field_land",
                        onClick = { onNavigate(AgriNavDestination.FIELD_LAND) }
                    )

                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        iconBg = EarthAmber,
                        iconTint = SlateDarkBackground,
                        title = "Spray & Mix\nCalibrator",
                        subtitle = "L/HA, NOZZLES, TANKS",
                        subtitleColor = EarthAmber,
                        testTag = "module_card_spraying",
                        onClick = { onNavigate(AgriNavDestination.SPRAYING) }
                    )
                }

                // Row 2: Soil NPK + Crop Yield
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Eco,
                        iconBg = LeafGreenAccent,
                        iconTint = SlateDarkBackground,
                        title = "Soil NPK &\nFertilizer",
                        subtitle = "KG/HA, PH, BAGS",
                        subtitleColor = LeafGreenAccent,
                        testTag = "module_card_soil_fertilizer",
                        onClick = { onNavigate(AgriNavDestination.SOIL_FERTILIZER) }
                    )

                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Agriculture,
                        iconBg = Color.White,
                        iconTint = SlateDarkBackground,
                        title = "Crop Yield\nEstimator",
                        subtitle = "T/HA, SEEDING, LOSS",
                        subtitleColor = Color.White.copy(alpha = 0.65f),
                        testTag = "module_card_crop_yield",
                        onClick = { onNavigate(AgriNavDestination.CROP_YIELD) }
                    )
                }

                // Row 3: Hydraulics & Irrigation + Calculation History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Shower,
                        iconBg = HydraulicBlue,
                        iconTint = Color.White,
                        title = "Hydraulics &\nIrrigation",
                        subtitle = "MM/DAY, M³/H",
                        subtitleColor = HydraulicBlue,
                        testTag = "module_card_irrigation",
                        onClick = { onNavigate(AgriNavDestination.IRRIGATION) }
                    )

                    PolishModuleCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.History,
                        iconBg = ForestGreenPrimary,
                        iconTint = LeafGreenAccent,
                        title = "Calculation\nHistory",
                        subtitle = if (savedRecordsCount > 0) "$savedRecordsCount LOGS SAVED" else "OFFLINE HISTORY",
                        subtitleColor = Color.White.copy(alpha = 0.50f),
                        testTag = "module_card_history",
                        onClick = { onNavigate(AgriNavDestination.HISTORY) }
                    )
                }
            }
        }

        // 3. Custom Presets Banner (Quick Access to Manage Presets)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, LeafGreenAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { onOpenPresets() }
                    .testTag("manage_presets_banner"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ForestGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CUSTOM PRESETS & TARGETS",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Field shapes, Crop NPK targets & fertilizer blends",
                                style = MaterialTheme.typography.bodySmall,
                                color = LeafGreenBright
                            )
                        }
                    }

                    Button(
                        onClick = onOpenPresets,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeafGreenAccent,
                            contentColor = SlateDarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (presetsCount > 0) "$presetsCount Presets" else "Manage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 4. Enhanced Quick Unit Converter Widget (with Bushels, Gallons, Fluid Ounces)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, ForestGreenPrimary, RoundedCornerShape(20.dp))
                    .testTag("unit_converter_widget"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = LeafGreenAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AGRICULTURAL METRIC CONVERTER",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LeafGreenAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ISO Metric",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = LeafGreenAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Grain", "Volume", "Chemical", "Land").forEach { cat ->
                            val isSel = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) ForestGreenPrimary else Color.Transparent)
                                    .border(1.dp, if (isSel) LeafGreenAccent else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedCategoryFilter = cat
                                        selectedUnitIndex = 0
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSel) LeafGreenAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unit category tabs
                    ScrollableTabRow(
                        selectedTabIndex = activeIndex,
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = LeafGreenAccent,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        filteredConversions.forEachIndexed { idx, type ->
                            Tab(
                                selected = activeIndex == idx,
                                onClick = {
                                    selectedUnitIndex = idx
                                    isReverseConvert = false
                                },
                                text = {
                                    Text(
                                        text = "${type.unitA} ⇄ ${type.unitB}",
                                        fontWeight = if (activeIndex == idx) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val fromUnit = if (isReverseConvert) activeConversion.unitB else activeConversion.unitA
                    val toUnit = if (isReverseConvert) activeConversion.unitA else activeConversion.unitB

                    FieldNumericInput(
                        label = "Input Value ($fromUnit)",
                        value = inputUnitValA,
                        onValueChange = { inputUnitValA = it },
                        unit = fromUnit,
                        step = 1.0,
                        hint = activeConversion.title
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reverse toggle button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                .clickable {
                                    isReverseConvert = !isReverseConvert
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Swap conversion direction",
                                    tint = LeafGreenAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Swap Direction ($fromUnit ⇄ $toUnit)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation Result Box
                    val inVal = inputUnitValA.toDoubleOrNull() ?: 0.0
                    val convertedVal = if (isReverseConvert) {
                        inVal / activeConversion.factorAtoB
                    } else {
                        inVal * activeConversion.factorAtoB
                    }

                    val formattedResult = if (convertedVal >= 10000 || (convertedVal < 0.01 && convertedVal > 0)) {
                        String.format("%.4f", convertedVal)
                    } else if (convertedVal % 1.0 == 0.0) {
                        convertedVal.toLong().toString()
                    } else {
                        String.format("%.3f", convertedVal)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D1C14))
                            .border(1.5.dp, LeafGreenAccent, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "METRIC CONVERTED RESULT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = LeafGreenBright
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedResult,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = toUnit,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = LeafGreenAccent,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ratio: ${activeConversion.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )

                            // Specific agricultural agronomic note
                            val agronomicNote = when (activeConversion) {
                                UnitConversionType.BU_WHEAT_SOY_TO_KG -> "Wheat & Soybeans: 60 lb/bu standard test weight (0.772 kg/L bulk density)."
                                UnitConversionType.BU_CORN_TO_KG -> "Corn (Maize) & Sorghum: 56 lb/bu standard test weight (0.721 kg/L bulk density)."
                                UnitConversionType.GAL_US_TO_L -> "1 US Liquid Gallon = 3.78541 Liters (common for chemical spray containers)."
                                UnitConversionType.FLOZ_TO_ML -> "1 US Fluid Ounce = 29.5735 mL (precision tank mix dosing cup scale)."
                                else -> null
                            }

                            if (agronomicNote != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ForestGreenPrimary.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = LeafGreenAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = agronomicNote,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PolishModuleCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(148.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(2.dp, ForestGreenPrimary, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon tile
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title and category subtitle
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
    }
}
