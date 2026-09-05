package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_records")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // e.g. "Field & Land", "Spraying", "Soil NPK", "Crop Yield", "Irrigation"
    val calculationType: String, // e.g. "GPS Field Area", "Manual Trapezoid Area", "Boom Sprayer", etc.
    val title: String,
    val inputsSummary: String,
    val resultsSummary: String,
    val formulaUsed: String,
    val fieldNotes: String = ""
)
