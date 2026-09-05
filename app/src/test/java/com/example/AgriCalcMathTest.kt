package com.example

import com.example.model.UnitConversionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.max

class AgriCalcMathTest {

    @Test
    fun testUnitConversions() {
        // 1 ha = 10,000 m²
        val haToM2 = 2.5 * UnitConversionType.HA_TO_M2.factorAtoB
        assertEquals(25000.0, haToM2, 0.001)

        // 1 L/ha = 0.1 mL/m²
        val lHaToMlM2 = 200.0 * UnitConversionType.LHA_TO_MLM2.factorAtoB
        assertEquals(20.0, lHaToMlM2, 0.001)

        // 1 t/ha = 1,000 kg/ha
        val tHaToKgHa = 3.5 * UnitConversionType.THA_TO_KGHA.factorAtoB
        assertEquals(3500.0, tHaToKgHa, 0.001)
    }

    @Test
    fun testBoomSprayerFormula() {
        // q (L/min) = (Rate (L/ha) * Spacing (cm) * Speed (km/h)) / 60,000
        val rate = 150.0 // L/ha
        val spacing = 50.0 // cm
        val speed = 8.0 // km/h
        val flowRate = (rate * spacing * speed) / 60000.0

        // 150 * 50 * 8 = 60,000 / 60,000 = 1.0 L/min
        assertEquals(1.0, flowRate, 0.001)
    }

    @Test
    fun testKnapsackTankMix() {
        // chemPerTank = (Dose / WaterRate) * TankCapacity
        val dose = 1.5 // L/ha
        val waterRate = 200.0 // L/ha
        val tankCapacity = 16.0 // L

        val chemPerTankL = (dose / waterRate) * tankCapacity
        val chemPerTankML = chemPerTankL * 1000.0

        // (1.5 / 200) * 16 = 0.0075 * 16 = 0.12 L = 120 mL
        assertEquals(120.0, chemPerTankML, 0.001)
    }

    @Test
    fun testNpkStoichiometricBalancer() {
        val targetN = 120.0 // kg/ha
        val targetP = 60.0 // kg P2O5/ha
        val targetK = 50.0 // kg K2O/ha

        // DAP supplies P first (46% P2O5)
        val dapKg = targetP / 0.46
        val nFromDap = dapKg * 0.18

        // Remaining N from Urea (46% N)
        val nRemaining = max(0.0, targetN - nFromDap)
        val ureaKg = nRemaining / 0.46

        // MOP supplies K (60% K2O)
        val mopKg = targetK / 0.60

        assertTrue("DAP should be approximately 130.4 kg/ha", dapKg > 130.0 && dapKg < 131.0)
        assertTrue("N from DAP should be approx 23.5 kg/ha", nFromDap > 23.0 && nFromDap < 24.0)
        assertTrue("Urea should be approx 210 kg/ha", ureaKg > 209.0 && ureaKg < 211.0)
        assertTrue("MOP should be approx 83.3 kg/ha", mopKg > 83.0 && mopKg < 84.0)
    }

    @Test
    fun testSeedingRateCalculation() {
        val targetPop = 2500000.0 // plants/ha (e.g. 250 plants/m² for wheat)
        val tgw = 45.0 // g
        val germ = 90.0 // %
        val purity = 98.0 // %
        val emergence = 85.0 // %

        val effectiveFactor = (germ / 100.0) * (purity / 100.0) * (emergence / 100.0)
        val seedRateKgHa = (targetPop * (tgw / 1000.0)) / (effectiveFactor * 1000.0)

        // Target seeds = 250,000 * 0.045 = 11,250 kg raw
        // Divided by (0.9 * 0.98 * 0.85) = 0.7497 => ~150 kg/ha
        assertTrue("Seed rate should be around 150 kg/ha", seedRateKgHa in 149.0..151.0)
    }

    @Test
    fun testIrrigationEtcAndPumpRunTime() {
        val et0 = 5.0 // mm/day
        val kc = 1.20
        val area = 10.0 // ha
        val pumpFlowM3h = 40.0 // m³/h
        val efficiency = 80.0 // %

        val etc = et0 * kc // 6.0 mm/day
        assertEquals(6.0, etc, 0.001)

        val netVolumeM3 = etc * area * 10.0 // 6.0 * 10 * 10 = 600 m³
        assertEquals(600.0, netVolumeM3, 0.001)

        val grossVolumeM3 = netVolumeM3 / (efficiency / 100.0) // 600 / 0.8 = 750 m³
        assertEquals(750.0, grossVolumeM3, 0.001)

        val runTimeHours = grossVolumeM3 / pumpFlowM3h // 750 / 40 = 18.75 hours
        assertEquals(18.75, runTimeHours, 0.001)
    }

    @Test
    fun testTrapezoidalDamVolumePrismoidal() {
        val topL = 50.0
        val topW = 30.0
        val botL = 30.0
        val botW = 10.0
        val depth = 3.0

        val aTop = topL * topW // 1500
        val aBot = botL * botW // 300
        val midL = (topL + botL) / 2.0 // 40
        val midW = (topW + botW) / 2.0 // 20
        val aMid = midL * midW // 800

        val volumeM3 = (depth / 6.0) * (aTop + aBot + 4 * aMid)
        // 0.5 * (1500 + 300 + 3200) = 0.5 * 5000 = 2500 m³
        assertEquals(2500.0, volumeM3, 0.001)
    }
}
