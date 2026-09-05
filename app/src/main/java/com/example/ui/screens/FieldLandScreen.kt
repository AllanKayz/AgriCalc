package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.model.CustomPreset
import com.example.model.PresetType
import com.example.ui.components.FieldNumericInput
import com.example.ui.components.FormulaCard
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.ui.theme.SlateDarkBackground
import com.example.util.ReportExporter
import com.example.viewmodel.AgriCalcViewModel
import kotlin.math.sqrt

@Composable
fun FieldLandScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("GPS Perimeter", "Shape Geometry", "Work Capacity")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("field_land_screen")
    ) {
        // Module SubTabs
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
            0 -> GpsFieldTrackerView(viewModel, context)
            1 -> ManualShapeSolverView(viewModel, context)
            2 -> FieldCapacityView(viewModel, context)
        }
    }
}

@Composable
private fun GpsFieldTrackerView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    val coords by viewModel.gpsCoordinates.collectAsState()
    val isTracking by viewModel.isGpsTracking.collectAsState()

    val perimeterM = viewModel.computePerimeterMeters(coords)
    val areaM2 = viewModel.computeAreaSquareMeters(coords)
    val areaHa = areaM2 / 10000.0

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startGpsTracking()
            Toast.makeText(context, "GPS Tracking started! Walk the boundary.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission needed for live tracking. Use 'Sample Paddock' to test.", Toast.LENGTH_LONG).show()
        }
    }

    // Android native Location Listener
    DisposableEffect(isTracking) {
        if (!isTracking) return@DisposableEffect onDispose {}

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                viewModel.addGpsCoordinate(
                    lat = location.latitude,
                    lng = location.longitude,
                    alt = location.altitude,
                    accuracy = location.accuracy
                )
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        1.5f,
                        locationListener
                    )
                } else if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        1.5f,
                        locationListener
                    )
                }
            } catch (_: SecurityException) {}
        }

        onDispose {
            try {
                locationManager?.removeUpdates(locationListener)
            } catch (_: Exception) {}
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Controls Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isTracking) LeafGreenAccent else EarthAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTracking) "GPS TRACKING ACTIVE" else "GPS ENGINE STANDBY",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ForestGreenPrimary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${coords.size} Points Recorded",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LeafGreenBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isTracking) {
                            Button(
                                onClick = {
                                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasFine) {
                                        viewModel.startGpsTracking()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LeafGreenAccent,
                                    contentColor = ForestGreenPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("start_gps_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Walk", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopGpsTracking() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EarthAmber,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("pause_gps_button")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause Walk", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Reset Button
                        OutlinedButton(
                            onClick = { viewModel.resetGpsTrack() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reset_gps_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick simulation & Manual point buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loadSamplePaddock() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("load_sample_paddock_button")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sample 4.15 ha Paddock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                // Add current offset point
                                val last = coords.lastOrNull()
                                if (last == null) {
                                    viewModel.addGpsCoordinate(-17.8252, 31.0530)
                                } else {
                                    viewModel.addGpsCoordinate(last.latitude + 0.0006, last.longitude + 0.0005)
                                }
                                Toast.makeText(context, "Waypoint added!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = "Add Corner Point", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Polygon Canvas Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(2.dp, LeafGreenAccent, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1C14)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (coords.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SquareFoot,
                                contentDescription = null,
                                tint = LeafGreenAccent.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Field Perimeter Canvas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Walk field or tap 'Sample 4.15 ha Paddock' to draw boundary polygon.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            val w = size.width
                            val h = size.height

                            val minLat = coords.minOf { it.latitude }
                            val maxLat = coords.maxOf { it.latitude }
                            val minLng = coords.minOf { it.longitude }
                            val maxLng = coords.maxOf { it.longitude }

                            val spanLat = (maxLat - minLat).coerceAtLeast(0.0001)
                            val spanLng = (maxLng - minLng).coerceAtLeast(0.0001)

                            // Transform coords to Canvas points
                            val screenPoints = coords.map { c ->
                                val normX = ((c.longitude - minLng) / spanLng).toFloat()
                                val normY = (1f - ((c.latitude - minLat) / spanLat).toFloat())
                                Offset(normX * w, normY * h)
                            }

                            // Draw closed polygon path
                            if (screenPoints.size >= 3) {
                                val polyPath = Path().apply {
                                    moveTo(screenPoints[0].x, screenPoints[0].y)
                                    for (i in 1 until screenPoints.size) {
                                        lineTo(screenPoints[i].x, screenPoints[i].y)
                                    }
                                    close()
                                }
                                // Fill
                                drawPath(
                                    path = polyPath,
                                    color = LeafGreenAccent.copy(alpha = 0.35f),
                                    style = Fill
                                )
                                // Stroke
                                drawPath(
                                    path = polyPath,
                                    color = LeafGreenBright,
                                    style = Stroke(width = 4.dp.toPx(), join = StrokeJoin.Round)
                                )
                            } else if (screenPoints.size == 2) {
                                drawLine(
                                    color = LeafGreenBright,
                                    start = screenPoints[0],
                                    end = screenPoints[1],
                                    strokeWidth = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Draw numbered vertex markers
                            screenPoints.forEachIndexed { i, pt ->
                                drawCircle(
                                    color = EarthAmber,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }

                        // Mini Canvas Overlay Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .border(1.dp, LeafGreenAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SVG Planar Projection",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = LeafGreenBright
                            )
                        }
                    }
                }
            }
        }

        // Calculation Results Card
        item {
            val primaryAreaStr = String.format("%.2f", areaHa)
            val secondaryM2Str = String.format("%,.0f m²", areaM2)
            val perimeterStr = if (perimeterM >= 1000) {
                String.format("%.2f km (%,.0f m)", perimeterM / 1000.0, perimeterM)
            } else {
                String.format("%.1f m", perimeterM)
            }

            val fullSummary = """
                AGRICALC GPS FIELD PERIMETER REPORT
                • Calculated Area: $primaryAreaStr ha ($secondaryM2Str)
                • Boundary Perimeter: $perimeterStr
                • Recorded Waypoints: ${coords.size} points
                • Formula: Planar Equirectangular Projection + Shoelace Algorithm
            """.trimIndent()

            FormulaCard(
                title = "GPS Field Perimeter & Area",
                primaryResult = primaryAreaStr,
                primaryUnit = "ha",
                secondaryResults = listOf(
                    "Total Perimeter" to perimeterStr,
                    "Surface Area" to secondaryM2Str,
                    "Boundary Points" to "${coords.size} waypoints"
                ),
                formula = "Area = 0.5 × |Σ(x_i·y_{i+1} - x_{i+1}·y_i)|",
                explanation = "Calculates planar projection using WGS84 geodesic coordinates and Haversine perimeter.",
                warningMessage = if (coords.size in 1..2) "Need at least 3 boundary waypoints to compute closed polygon area." else null,
                onSaveToHistory = if (areaHa > 0) {
                    {
                        viewModel.saveCalculation(
                            category = "Field & Land",
                            calculationType = "GPS Field Area",
                            title = "GPS Field Perimeter ($primaryAreaStr ha)",
                            inputsSummary = "Boundary Points: ${coords.size}, Perimeter: $perimeterStr",
                            resultsSummary = "Area: $primaryAreaStr ha ($secondaryM2Str)",
                            formulaUsed = "Shoelace Polygon Formula + Haversine Boundary Distance"
                        )
                    }
                } else null,
                onPrintReport = if (areaHa > 0) {
                    {
                        ReportExporter.printReport(
                            context = context,
                            reportTitle = "GPS Field Measurement",
                            category = "Field & Land",
                            primaryMetric = "$primaryAreaStr ha ($secondaryM2Str)",
                            formula = "Shoelace Planar Polygon Area Algorithm",
                            details = listOf(
                                "Total Perimeter" to perimeterStr,
                                "Waypoints" to "${coords.size}",
                                "Projection" to "Planar Equirectangular Meter Transform"
                            )
                        )
                    }
                } else null,
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ManualShapeSolverView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var selectedShape by remember { mutableStateOf("Rectangle") } // Rectangle, Triangle, Trapezoid

    // Rectangle inputs
    var rectLength by remember { mutableStateOf("250.0") }
    var rectWidth by remember { mutableStateOf("120.0") }

    // Triangle inputs
    var triBase by remember { mutableStateOf("180.0") }
    var triHeight by remember { mutableStateOf("95.0") }

    // Trapezoid inputs
    var trapSideA by remember { mutableStateOf("300.0") }
    var trapSideB by remember { mutableStateOf("180.0") }
    var trapHeight by remember { mutableStateOf("140.0") }

    val allPresets by viewModel.allPresets.collectAsState()
    val fieldShapePresets = allPresets.filter { it.type == PresetType.FIELD_SHAPE }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val shapes = listOf("Rectangle", "Triangle", "Trapezoid")

    val (areaM2, perimeterM, formulaStr, detailsList) = when (selectedShape) {
        "Rectangle" -> {
            val l = rectLength.toDoubleOrNull() ?: 0.0
            val w = rectWidth.toDoubleOrNull() ?: 0.0
            val a = l * w
            val p = 2 * (l + w)
            Quadruple(a, p, "Area = Length (m) × Width (m)", listOf("Length" to "$l m", "Width" to "$w m"))
        }
        "Triangle" -> {
            val b = triBase.toDoubleOrNull() ?: 0.0
            val h = triHeight.toDoubleOrNull() ?: 0.0
            val a = 0.5 * b * h
            val approxPerim = b + 2 * sqrt((b / 2) * (b / 2) + h * h)
            Quadruple(a, approxPerim, "Area = 0.5 × Base (m) × Height (m)", listOf("Base" to "$b m", "Height" to "$h m"))
        }
        else -> { // Trapezoid
            val a = trapSideA.toDoubleOrNull() ?: 0.0
            val b = trapSideB.toDoubleOrNull() ?: 0.0
            val h = trapHeight.toDoubleOrNull() ?: 0.0
            val area = ((a + b) / 2.0) * h
            val approxPerim = a + b + 2 * sqrt(((a - b) / 2) * ((a - b) / 2) + h * h)
            Quadruple(area, approxPerim, "Area = ((a + b) / 2) × Height (m)", listOf("Parallel Side a" to "$a m", "Parallel Side b" to "$b m", "Height" to "$h m"))
        }
    }

    val areaHa = areaM2 / 10000.0
    val primaryAreaStr = String.format("%.3f", areaHa)
    val secondaryM2Str = String.format("%,.1f m²", areaM2)
    val perimeterStr = String.format("%,.1f m", perimeterM)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Field Shape Presets Bar
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
                                text = "FIELD SHAPE PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        TextButton(
                            onClick = { showSavePresetDialog = true },
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
                                text = "Save Current",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LeafGreenAccent
                                )
                            )
                        }
                    }

                    if (fieldShapePresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(fieldShapePresets) { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ForestGreenPrimary)
                                        .border(1.dp, LeafGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            when (preset.shapeType.uppercase()) {
                                                "RECTANGLE" -> {
                                                    selectedShape = "Rectangle"
                                                    rectLength = preset.dim1.toString()
                                                    rectWidth = preset.dim2.toString()
                                                }
                                                "TRIANGLE" -> {
                                                    selectedShape = "Triangle"
                                                    triBase = preset.dim1.toString()
                                                    triHeight = preset.dim2.toString()
                                                }
                                                "TRAPEZOID" -> {
                                                    selectedShape = "Trapezoid"
                                                    trapSideA = preset.dim1.toString()
                                                    trapSideB = preset.dim2.toString()
                                                    trapHeight = preset.dim3.toString()
                                                }
                                            }
                                            Toast.makeText(context, "Loaded preset: ${preset.name}", Toast.LENGTH_SHORT).show()
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
                                            text = "${preset.shapeType} • ${preset.dim1.toInt()}×${preset.dim2.toInt()}m",
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

        // Shape selector chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shapes.forEach { shape ->
                    val isSelected = selectedShape == shape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LeafGreenAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { selectedShape = shape }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shape,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Shape Inputs
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
                    when (selectedShape) {
                        "Rectangle" -> {
                            FieldNumericInput(
                                label = "Length",
                                value = rectLength,
                                onValueChange = { rectLength = it },
                                unit = "m",
                                step = 10.0,
                                presets = listOf("100 m" to 100.0, "250 m" to 250.0, "500 m" to 500.0)
                            )
                            FieldNumericInput(
                                label = "Width",
                                value = rectWidth,
                                onValueChange = { rectWidth = it },
                                unit = "m",
                                step = 10.0,
                                presets = listOf("50 m" to 50.0, "120 m" to 120.0, "200 m" to 200.0)
                            )
                        }
                        "Triangle" -> {
                            FieldNumericInput(
                                label = "Base Length",
                                value = triBase,
                                onValueChange = { triBase = it },
                                unit = "m",
                                step = 10.0
                            )
                            FieldNumericInput(
                                label = "Perpendicular Height",
                                value = triHeight,
                                onValueChange = { triHeight = it },
                                unit = "m",
                                step = 10.0
                            )
                        }
                        "Trapezoid" -> {
                            FieldNumericInput(
                                label = "Parallel Side a",
                                value = trapSideA,
                                onValueChange = { trapSideA = it },
                                unit = "m",
                                step = 10.0
                            )
                            FieldNumericInput(
                                label = "Parallel Side b",
                                value = trapSideB,
                                onValueChange = { trapSideB = it },
                                unit = "m",
                                step = 10.0
                            )
                            FieldNumericInput(
                                label = "Perpendicular Height",
                                value = trapHeight,
                                onValueChange = { trapHeight = it },
                                unit = "m",
                                step = 10.0
                            )
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            val fullSummary = """
                AGRICALC $selectedShape FIELD AREA
                • Area: $primaryAreaStr ha ($secondaryM2Str)
                • Estimated Perimeter: $perimeterStr
                • Formula: $formulaStr
            """.trimIndent()

            FormulaCard(
                title = "Manual $selectedShape Area",
                primaryResult = primaryAreaStr,
                primaryUnit = "ha",
                secondaryResults = listOf(
                    "Metric Area" to secondaryM2Str,
                    "Estimated Perimeter" to perimeterStr
                ) + detailsList,
                formula = formulaStr,
                explanation = "Strict SI metric geometry conversion (1 ha = 10,000 m²).",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Field & Land",
                        calculationType = "Manual $selectedShape Area",
                        title = "$selectedShape Field ($primaryAreaStr ha)",
                        inputsSummary = detailsList.joinToString(", ") { "${it.first}: ${it.second}" },
                        resultsSummary = "Area: $primaryAreaStr ha ($secondaryM2Str), Perimeter: $perimeterStr",
                        formulaUsed = formulaStr
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Field $selectedShape Calculation",
                        category = "Field & Land",
                        primaryMetric = "$primaryAreaStr ha ($secondaryM2Str)",
                        formula = formulaStr,
                        details = detailsList + listOf("Perimeter" to perimeterStr)
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = "Save $selectedShape Preset",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Name your custom field boundary for instant loading in future calculations:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FieldNumericInput(
                        label = "Preset Name",
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        hint = "e.g. North 12ha Plot",
                        isTextOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (newPresetName.isBlank()) "My $selectedShape" else newPresetName.trim()
                        val (d1, d2, d3, desc) = when (selectedShape) {
                            "Rectangle" -> {
                                val l = rectLength.toDoubleOrNull() ?: 0.0
                                val w = rectWidth.toDoubleOrNull() ?: 0.0
                                Quadruple(l, w, 0.0, "${l.toInt()}m × ${w.toInt()}m ($primaryAreaStr ha)")
                            }
                            "Triangle" -> {
                                val b = triBase.toDoubleOrNull() ?: 0.0
                                val h = triHeight.toDoubleOrNull() ?: 0.0
                                Quadruple(b, h, 0.0, "Base ${b.toInt()}m, Height ${h.toInt()}m ($primaryAreaStr ha)")
                            }
                            else -> {
                                val a = trapSideA.toDoubleOrNull() ?: 0.0
                                val b = trapSideB.toDoubleOrNull() ?: 0.0
                                val h = trapHeight.toDoubleOrNull() ?: 0.0
                                Quadruple(a, b, h, "Top ${a.toInt()}m, Bottom ${b.toInt()}m, H ${h.toInt()}m ($primaryAreaStr ha)")
                            }
                        }

                        val preset = CustomPreset(
                            name = name,
                            type = PresetType.FIELD_SHAPE,
                            shapeType = selectedShape.uppercase(),
                            dim1 = d1,
                            dim2 = d2,
                            dim3 = d3,
                            description = desc,
                            isPredefined = false
                        )
                        viewModel.savePreset(preset)
                        newPresetName = ""
                        showSavePresetDialog = false
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
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = ForestGreenPrimary,
            titleContentColor = Color.White
        )
    }
}

@Composable
private fun FieldCapacityView(
    viewModel: AgriCalcViewModel,
    context: Context
) {
    var implementWidth by remember { mutableStateOf("6.0") } // meters
    var tractorSpeed by remember { mutableStateOf("8.5") } // km/h
    var fieldEfficiency by remember { mutableStateOf("80.0") } // %
    var fieldArea by remember { mutableStateOf("25.0") } // ha
    var fuelBurnRate by remember { mutableStateOf("14.0") } // L/h

    val width = implementWidth.toDoubleOrNull() ?: 0.0
    val speed = tractorSpeed.toDoubleOrNull() ?: 0.0
    val eff = fieldEfficiency.toDoubleOrNull() ?: 0.0
    val area = fieldArea.toDoubleOrNull() ?: 0.0
    val fuelRate = fuelBurnRate.toDoubleOrNull() ?: 0.0

    // Effective Field Capacity (ha/h) = (Width (m) × Speed (km/h) × (Eff / 100)) / 10
    val capacityHaPerHour = if (width > 0 && speed > 0 && eff > 0) {
        (width * speed * (eff / 100.0)) / 10.0
    } else 0.0

    val totalHours = if (capacityHaPerHour > 0 && area > 0) {
        area / capacityHaPerHour
    } else 0.0

    val hoursInt = totalHours.toInt()
    val minutesInt = ((totalHours - hoursInt) * 60).toInt()
    val totalFuelLitres = totalHours * fuelRate

    val capStr = String.format("%.2f", capacityHaPerHour)
    val timeStr = "${hoursInt}h ${minutesInt}m"
    val fuelStr = String.format("%.1f L", totalFuelLitres)

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
                        text = "IMPLEMENT & TRACTOR DYNAMICS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LeafGreenAccent
                    )

                    FieldNumericInput(
                        label = "Implement Working Width",
                        value = implementWidth,
                        onValueChange = { implementWidth = it },
                        unit = "m",
                        step = 0.5,
                        presets = listOf("3.0 m (Plow)" to 3.0, "6.0 m (Seeder)" to 6.0, "12.0 m (Boom)" to 12.0)
                    )

                    FieldNumericInput(
                        label = "Tractor Ground Speed",
                        value = tractorSpeed,
                        onValueChange = { tractorSpeed = it },
                        unit = "km/h",
                        step = 0.5,
                        presets = listOf("6 km/h" to 6.0, "8.5 km/h" to 8.5, "12 km/h" to 12.0)
                    )

                    FieldNumericInput(
                        label = "Field Operational Efficiency",
                        value = fieldEfficiency,
                        onValueChange = { fieldEfficiency = it },
                        unit = "%",
                        step = 1.0,
                        minValue = 50.0,
                        maxValue = 100.0,
                        presets = listOf("75% (Turning)" to 75.0, "80% (Average)" to 80.0, "85% (Long Run)" to 85.0)
                    )

                    FieldNumericInput(
                        label = "Total Field Area",
                        value = fieldArea,
                        onValueChange = { fieldArea = it },
                        unit = "ha",
                        step = 5.0,
                        presets = listOf("10 ha" to 10.0, "25 ha" to 25.0, "100 ha" to 100.0)
                    )

                    FieldNumericInput(
                        label = "Estimated Fuel Consumption",
                        value = fuelBurnRate,
                        onValueChange = { fuelBurnRate = it },
                        unit = "L/h",
                        step = 1.0,
                        hint = "Optional for total fuel projection"
                    )
                }
            }
        }

        item {
            val fullSummary = """
                AGRICALC EFFECTIVE FIELD CAPACITY
                • Work Rate: $capStr ha/h
                • Field Completion Time: $timeStr (for $area ha)
                • Estimated Fuel Required: $fuelStr (@ $fuelRate L/h)
                • Formula: Capacity = (Width × Speed × Efficiency%) / 10
            """.trimIndent()

            FormulaCard(
                title = "Effective Field Capacity",
                primaryResult = capStr,
                primaryUnit = "ha/h",
                secondaryResults = listOf(
                    "Field Completion Time" to "$timeStr ($area ha)",
                    "Total Fuel Burn" to fuelStr,
                    "Hourly Fuel Rate" to "$fuelRate L/h",
                    "Efficiency Factor" to "$eff %"
                ),
                formula = "Capacity (ha/h) = (Width × Speed × (Eff% / 100)) / 10",
                explanation = "Standard ASABE D497 implement capacity formula incorporating headland turn and refill delays.",
                onSaveToHistory = {
                    viewModel.saveCalculation(
                        category = "Field & Land",
                        calculationType = "Effective Field Capacity",
                        title = "Implement Capacity ($capStr ha/h)",
                        inputsSummary = "Width: $width m, Speed: $speed km/h, Eff: $eff%, Area: $area ha",
                        resultsSummary = "Work Rate: $capStr ha/h, Time: $timeStr, Fuel: $fuelStr",
                        formulaUsed = "C = (W × S × E) / 10"
                    )
                },
                onPrintReport = {
                    ReportExporter.printReport(
                        context = context,
                        reportTitle = "Implement Work Capacity",
                        category = "Field & Land",
                        primaryMetric = "$capStr ha/h",
                        formula = "C (ha/h) = (W × S × E%) / 10",
                        details = listOf(
                            "Implement Width" to "$width m",
                            "Tractor Speed" to "$speed km/h",
                            "Operational Efficiency" to "$eff %",
                            "Field Area" to "$area ha",
                            "Total Estimated Time" to timeStr,
                            "Total Fuel Requirement" to fuelStr
                        )
                    )
                },
                fullSummaryText = fullSummary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
