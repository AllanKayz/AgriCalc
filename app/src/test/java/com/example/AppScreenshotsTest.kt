package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.CropYieldScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FieldLandScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IrrigationScreen
import com.example.ui.screens.SoilFertilizerScreen
import com.example.ui.screens.SprayingScreen
import com.example.ui.theme.AgriCalcTheme
import com.example.viewmodel.AgriCalcViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppScreenshotsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: AgriCalcViewModel

  @Before
  fun setUp() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    viewModel = AgriCalcViewModel(application)
    ensureOutputDirectory()
  }

  private fun ensureOutputDirectory() {
    val dir = File("src/test/screenshots")
    if (!dir.exists()) {
      dir.mkdirs()
    }
  }

  @Test
  fun capture_dashboard_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        DashboardScreen(
          onNavigate = {},
          savedRecordsCount = 5,
          presetsCount = 12
        )
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
  }

  @Test
  fun capture_field_land_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        FieldLandScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/field_land.png")
  }

  @Test
  fun capture_spraying_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        SprayingScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/spraying.png")
  }

  @Test
  fun capture_soil_fertilizer_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        SoilFertilizerScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/soil_fertilizer.png")
  }

  @Test
  fun capture_crop_yield_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        CropYieldScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/crop_yield.png")
  }

  @Test
  fun capture_irrigation_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        IrrigationScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/irrigation.png")
  }

  @Test
  fun capture_history_screen() {
    composeTestRule.setContent {
      AgriCalcTheme(sunlightMode = false) {
        HistoryScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/history.png")
  }
}
