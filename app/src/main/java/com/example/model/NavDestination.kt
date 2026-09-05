package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class AgriNavDestination(
    val title: String,
    val shortLabel: String,
    val icon: ImageVector,
    val description: String
) {
    DASHBOARD(
        title = "Dashboard",
        shortLabel = "Home",
        icon = Icons.Default.Dashboard,
        description = "Overview & Quick Metric Tools"
    ),
    FIELD_LAND(
        title = "Field & Land",
        shortLabel = "Field",
        icon = Icons.Default.Landscape,
        description = "GPS Area Tracker & Geometry"
    ),
    SPRAYING(
        title = "Spraying & Chemical",
        shortLabel = "Spraying",
        icon = Icons.Default.WaterDrop,
        description = "Boom Calibration & Tank Mix"
    ),
    SOIL_FERTILIZER(
        title = "Soil & Fertilizer",
        shortLabel = "Soil NPK",
        icon = Icons.Default.Science,
        description = "NPK Balancer & Lime Needs"
    ),
    CROP_YIELD(
        title = "Crop & Yield",
        shortLabel = "Yield",
        icon = Icons.Default.Agriculture,
        description = "Seeding Rate & Harvest Loss"
    ),
    IRRIGATION(
        title = "Irrigation Hydraulics",
        shortLabel = "Irrigation",
        icon = Icons.Default.Waves,
        description = "ETc Need, Pump Hours & Dams"
    ),
    HISTORY(
        title = "Saved Records",
        shortLabel = "History",
        icon = Icons.Default.History,
        description = "In-App Database History & Export"
    )
}
