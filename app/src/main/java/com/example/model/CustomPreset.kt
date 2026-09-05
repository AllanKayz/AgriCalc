package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PresetType(val label: String) {
    FIELD_SHAPE("Field Shape"),
    CROP_NPK("Crop NPK Target"),
    FERTILIZER_TYPE("Fertilizer Type")
}

@Entity(tableName = "custom_presets")
data class CustomPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: PresetType,
    val description: String = "",
    // Field Shape parameters
    val shapeType: String = "", // "RECTANGLE", "TRIANGLE", "TRAPEZOID"
    val dim1: Double = 0.0, // Length (Rectangle) or Base (Triangle) or Top Base (Trapezoid)
    val dim2: Double = 0.0, // Width (Rectangle) or Height (Triangle) or Bottom Base (Trapezoid)
    val dim3: Double = 0.0, // Height (Trapezoid)
    // Crop NPK Target parameters
    val targetN: Double = 0.0, // kg N/ha
    val targetP: Double = 0.0, // kg P2O5/ha
    val targetK: Double = 0.0, // kg K2O/ha
    val expectedYield: Double = 0.0, // t/ha
    // Fertilizer Type parameters
    val fertNPercent: Double = 0.0, // % N
    val fertPPercent: Double = 0.0, // % P2O5
    val fertKPercent: Double = 0.0, // % K2O
    val bagWeightKg: Double = 50.0, // kg
    val isPredefined: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
