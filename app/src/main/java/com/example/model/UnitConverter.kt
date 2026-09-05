package com.example.model

enum class UnitConversionType(
    val title: String,
    val unitA: String,
    val unitB: String,
    val factorAtoB: Double, // valueInA * factor = valueInB
    val description: String,
    val category: String = "General"
) {
    HA_TO_M2(
        title = "Land Area",
        unitA = "ha",
        unitB = "m²",
        factorAtoB = 10000.0,
        description = "1 hectare = 10,000 m²",
        category = "Land"
    ),
    LHA_TO_MLM2(
        title = "Liquid Application Rate",
        unitA = "L/ha",
        unitB = "mL/m²",
        factorAtoB = 0.1, // 1 L/ha = 1000 mL / 10000 m² = 0.1 mL/m²
        description = "1 L/ha = 0.1 mL/m²",
        category = "Application"
    ),
    KG_TO_G(
        title = "Mass / Weight",
        unitA = "kg",
        unitB = "g",
        factorAtoB = 1000.0,
        description = "1 kilogram = 1,000 grams",
        category = "Mass"
    ),
    BU_WHEAT_SOY_TO_KG(
        title = "Bushel to kg (Wheat / Soybeans)",
        unitA = "bu (Wheat/Soy)",
        unitB = "kg",
        factorAtoB = 27.2155, // 60 lb/bu standard test weight = 27.2155 kg
        description = "1 bushel (60 lb wheat/soy) = 27.216 kg",
        category = "Grain"
    ),
    BU_CORN_TO_KG(
        title = "Bushel to kg (Corn / Maize)",
        unitA = "bu (Corn)",
        unitB = "kg",
        factorAtoB = 25.4012, // 56 lb/bu standard test weight = 25.4012 kg
        description = "1 bushel (56 lb corn/maize) = 25.401 kg",
        category = "Grain"
    ),
    GAL_US_TO_L(
        title = "Liquid Volume (Gallon to Liter)",
        unitA = "gal (US)",
        unitB = "L",
        factorAtoB = 3.78541,
        description = "1 US gallon = 3.7854 Liters",
        category = "Volume"
    ),
    FLOZ_TO_ML(
        title = "Ag-Chemical Dosing (Fl Oz to mL)",
        unitA = "fl oz",
        unitB = "mL",
        factorAtoB = 29.5735,
        description = "1 US fluid ounce = 29.574 mL",
        category = "Chemical"
    ),
    THA_TO_KGHA(
        title = "Crop Yield Rate",
        unitA = "t/ha",
        unitB = "kg/ha",
        factorAtoB = 1000.0,
        description = "1 tonne/ha = 1,000 kg/ha",
        category = "Yield"
    ),
    M3_TO_L(
        title = "Water Volume",
        unitA = "m³",
        unitB = "L",
        factorAtoB = 1000.0,
        description = "1 cubic meter = 1,000 Liters",
        category = "Volume"
    ),
    KMH_TO_MS(
        title = "Tractor Ground Speed",
        unitA = "km/h",
        unitB = "m/s",
        factorAtoB = 1.0 / 3.6, // km/h to m/s
        description = "1 km/h = 0.278 m/s",
        category = "Speed"
    )
}
