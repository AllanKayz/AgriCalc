package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgriCalcDatabase
import com.example.data.CalculationRepository
import com.example.data.PresetDao
import com.example.model.AgriNavDestination
import com.example.model.CalculationRecord
import com.example.model.CustomPreset
import com.example.model.PresetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

class AgriCalcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository
    private val presetDao: PresetDao

    init {
        val db = AgriCalcDatabase.getDatabase(application)
        repository = CalculationRepository(db.calculationDao())
        presetDao = db.presetDao()

        // Seed common agricultural presets if empty
        viewModelScope.launch {
            if (presetDao.getPresetCount() == 0) {
                seedInitialPresets()
            }
        }
    }

    // Active Navigation Tab
    private val _currentDestination = MutableStateFlow(AgriNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AgriNavDestination> = _currentDestination.asStateFlow()

    // Sunlight High-Contrast Mode Toggle
    private val _sunlightMode = MutableStateFlow(false)
    val sunlightMode: StateFlow<Boolean> = _sunlightMode.asStateFlow()

    // History Records from Room
    val historyRecords: StateFlow<List<CalculationRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Custom Presets from Room
    val allPresets: StateFlow<List<CustomPreset>> = presetDao.getAllPresets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User Feedback Toast / SnackBar message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // GPS Perimeter Tracking State
    private val _gpsCoordinates = MutableStateFlow<List<GpsCoordinate>>(emptyList())
    val gpsCoordinates: StateFlow<List<GpsCoordinate>> = _gpsCoordinates.asStateFlow()

    private val _isGpsTracking = MutableStateFlow(false)
    val isGpsTracking: StateFlow<Boolean> = _isGpsTracking.asStateFlow()

    fun setDestination(destination: AgriNavDestination) {
        _currentDestination.value = destination
    }

    fun toggleSunlightMode() {
        _sunlightMode.value = !_sunlightMode.value
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // --- History Persistence Operations ---
    fun saveCalculation(
        category: String,
        calculationType: String,
        title: String,
        inputsSummary: String,
        resultsSummary: String,
        formulaUsed: String,
        fieldNotes: String = ""
    ) {
        viewModelScope.launch {
            val record = CalculationRecord(
                category = category,
                calculationType = calculationType,
                title = title,
                inputsSummary = inputsSummary,
                resultsSummary = resultsSummary,
                formulaUsed = formulaUsed,
                fieldNotes = fieldNotes
            )
            repository.insert(record)
            _userMessage.value = "Saved to Field History!"
        }
    }

    fun deleteRecord(record: CalculationRecord) {
        viewModelScope.launch {
            repository.delete(record)
            _userMessage.value = "Record deleted"
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.value = "History cleared"
        }
    }

    // --- Custom Preset Operations ---
    fun savePreset(preset: CustomPreset) {
        viewModelScope.launch {
            presetDao.insertPreset(preset)
            _userMessage.value = "Preset '${preset.name}' saved!"
        }
    }

    fun updatePreset(preset: CustomPreset) {
        viewModelScope.launch {
            presetDao.updatePreset(preset)
            _userMessage.value = "Preset '${preset.name}' updated!"
        }
    }

    fun deletePreset(preset: CustomPreset) {
        viewModelScope.launch {
            presetDao.deletePreset(preset)
            _userMessage.value = "Preset '${preset.name}' removed"
        }
    }

    fun deletePresetById(id: Long) {
        viewModelScope.launch {
            presetDao.deletePresetById(id)
            _userMessage.value = "Preset removed"
        }
    }

    private suspend fun seedInitialPresets() {
        val defaultPresets = listOf(
            // Field Shapes
            CustomPreset(
                name = "Standard 10ha Field",
                type = PresetType.FIELD_SHAPE,
                shapeType = "RECTANGLE",
                dim1 = 500.0,
                dim2 = 200.0,
                description = "500m × 200m rectangle (10.0 ha)",
                isPredefined = true
            ),
            CustomPreset(
                name = "Pivot Wedge Paddock",
                type = PresetType.FIELD_SHAPE,
                shapeType = "TRIANGLE",
                dim1 = 400.0,
                dim2 = 300.0,
                description = "400m base × 300m height triangle (6.0 ha)",
                isPredefined = true
            ),
            CustomPreset(
                name = "Valley Contour Block",
                type = PresetType.FIELD_SHAPE,
                shapeType = "TRAPEZOID",
                dim1 = 350.0,
                dim2 = 550.0,
                dim3 = 220.0,
                description = "Top 350m, Bottom 550m, Height 220m (9.9 ha)",
                isPredefined = true
            ),

            // Crop NPK Targets
            CustomPreset(
                name = "Maize / Corn (Grain High Yield)",
                type = PresetType.CROP_NPK,
                targetN = 160.0,
                targetP = 70.0,
                targetK = 60.0,
                expectedYield = 10.0,
                description = "160 kg N, 70 kg P₂O₅, 60 kg K₂O / ha (10 t/ha yield)",
                isPredefined = true
            ),
            CustomPreset(
                name = "Wheat (Milling Protein)",
                type = PresetType.CROP_NPK,
                targetN = 140.0,
                targetP = 60.0,
                targetK = 50.0,
                expectedYield = 6.0,
                description = "140 kg N, 60 kg P₂O₅, 50 kg K₂O / ha (6 t/ha yield)",
                isPredefined = true
            ),
            CustomPreset(
                name = "Soybeans (Inoculated)",
                type = PresetType.CROP_NPK,
                targetN = 20.0,
                targetP = 55.0,
                targetK = 75.0,
                expectedYield = 3.5,
                description = "20 kg N starter, 55 kg P₂O₅, 75 kg K₂O / ha",
                isPredefined = true
            ),
            CustomPreset(
                name = "Canola / Oilseed",
                type = PresetType.CROP_NPK,
                targetN = 130.0,
                targetP = 50.0,
                targetK = 45.0,
                expectedYield = 3.2,
                description = "130 kg N, 50 kg P₂O₅, 45 kg K₂O / ha",
                isPredefined = true
            ),
            CustomPreset(
                name = "Potato (Tuber Demand)",
                type = PresetType.CROP_NPK,
                targetN = 180.0,
                targetP = 90.0,
                targetK = 150.0,
                expectedYield = 35.0,
                description = "180 kg N, 90 kg P₂O₅, 150 kg K₂O / ha",
                isPredefined = true
            ),

            // Frequently used Fertilizer Types
            CustomPreset(
                name = "Urea (Granular)",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 46.0,
                fertPPercent = 0.0,
                fertKPercent = 0.0,
                bagWeightKg = 50.0,
                description = "46-0-0 High analysis nitrogen",
                isPredefined = true
            ),
            CustomPreset(
                name = "DAP (Diammonium Phosphate)",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 18.0,
                fertPPercent = 46.0,
                fertKPercent = 0.0,
                bagWeightKg = 50.0,
                description = "18-46-0 Nitrogen and high phosphorus",
                isPredefined = true
            ),
            CustomPreset(
                name = "MOP (Muriate of Potash)",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 0.0,
                fertPPercent = 0.0,
                fertKPercent = 60.0,
                bagWeightKg = 50.0,
                description = "0-0-60 Potassium chloride source",
                isPredefined = true
            ),
            CustomPreset(
                name = "CAN (Calcium Ammonium Nitrate)",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 27.0,
                fertPPercent = 0.0,
                fertKPercent = 0.0,
                bagWeightKg = 50.0,
                description = "27-0-0 Non-acidifying nitrogen",
                isPredefined = true
            ),
            CustomPreset(
                name = "NPK 15-15-15 Triple Compound",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 15.0,
                fertPPercent = 15.0,
                fertKPercent = 15.0,
                bagWeightKg = 50.0,
                description = "15-15-15 Balanced starter compound",
                isPredefined = true
            ),
            CustomPreset(
                name = "NPK 20-10-10 Vegetative Blend",
                type = PresetType.FERTILIZER_TYPE,
                fertNPercent = 20.0,
                fertPPercent = 10.0,
                fertKPercent = 10.0,
                bagWeightKg = 50.0,
                description = "20-10-10 High nitrogen blend",
                isPredefined = true
            )
        )

        defaultPresets.forEach { preset ->
            presetDao.insertPreset(preset)
        }
    }

    // --- GPS Field Tracker Operations ---
    fun startGpsTracking() {
        _isGpsTracking.value = true
    }

    fun stopGpsTracking() {
        _isGpsTracking.value = false
    }

    fun addGpsCoordinate(lat: Double, lng: Double, alt: Double = 0.0, accuracy: Float = 0f) {
        val coord = GpsCoordinate(lat, lng, alt, accuracy)
        _gpsCoordinates.value = _gpsCoordinates.value + coord
    }

    fun resetGpsTrack() {
        _isGpsTracking.value = false
        _gpsCoordinates.value = emptyList()
    }

    fun loadSamplePaddock() {
        // Loads a realistic 4.15 ha irregular 5-point agricultural paddock
        val baseLat = -17.8252
        val baseLng = 31.0530
        _gpsCoordinates.value = listOf(
            GpsCoordinate(baseLat, baseLng),
            GpsCoordinate(baseLat + 0.0020, baseLng + 0.0005),
            GpsCoordinate(baseLat + 0.0024, baseLng + 0.0028),
            GpsCoordinate(baseLat + 0.0004, baseLng + 0.0031),
            GpsCoordinate(baseLat - 0.0002, baseLng + 0.0016)
        )
        _userMessage.value = "Sample 4.15 ha field boundary loaded!"
    }

    /**
     * Computes polygon perimeter in meters using Haversine distance
     */
    fun computePerimeterMeters(points: List<GpsCoordinate>): Double {
        if (points.size < 2) return 0.0
        var totalDist = 0.0
        val earthRadius = 6371000.0 // meters

        for (i in 0 until points.size) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]

            val dLat = Math.toRadians(p2.latitude - p1.latitude)
            val dLon = Math.toRadians(p2.longitude - p1.longitude)
            val lat1 = Math.toRadians(p1.latitude)
            val lat2 = Math.toRadians(p2.latitude)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            totalDist += earthRadius * c
        }
        return totalDist
    }

    /**
     * Computes polygon area in square meters using Shoelace formula on equirectangular planar projection
     */
    fun computeAreaSquareMeters(points: List<GpsCoordinate>): Double {
        if (points.size < 3) return 0.0
        val earthRadius = 6371000.0
        val avgLat = points.map { it.latitude }.average()
        val cosAvgLat = cos(Math.toRadians(avgLat))

        // Convert lat/long to local planar (x, y) meters from first point
        val ref = points[0]
        val xyPoints = points.map { p ->
            val x = Math.toRadians(p.longitude - ref.longitude) * earthRadius * cosAvgLat
            val y = Math.toRadians(p.latitude - ref.latitude) * earthRadius
            Pair(x, y)
        }

        // Shoelace algorithm
        var area = 0.0
        val n = xyPoints.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += xyPoints[i].first * xyPoints[j].second
            area -= xyPoints[j].first * xyPoints[i].second
        }
        return kotlin.math.abs(area) / 2.0
    }
}
