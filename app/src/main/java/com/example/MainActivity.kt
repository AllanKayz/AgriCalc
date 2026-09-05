package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.AgriNavDestination
import com.example.ui.components.AgriBottomNavBar
import com.example.ui.components.AgriNavigationRail
import com.example.ui.components.TopHeader
import com.example.ui.screens.CropYieldScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FieldLandScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IrrigationScreen
import com.example.ui.screens.SoilFertilizerScreen
import com.example.ui.screens.SprayingScreen
import com.example.ui.theme.AgriCalcTheme
import com.example.viewmodel.AgriCalcViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AgriCalcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sunlightMode by viewModel.sunlightMode.collectAsState()
            val currentDestination by viewModel.currentDestination.collectAsState()
            val historyRecords by viewModel.historyRecords.collectAsState()
            val userMessage by viewModel.userMessage.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(userMessage) {
                userMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearUserMessage()
                }
            }

            AgriCalcTheme(sunlightMode = sunlightMode) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 600.dp

                    if (isWideScreen) {
                        // Wide Screen: Navigation Rail Layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AgriNavigationRail(
                                currentDestination = currentDestination,
                                onNavigate = { viewModel.setDestination(it) },
                                savedCount = historyRecords.size
                            )

                            Column(modifier = Modifier.fillMaxSize()) {
                                TopHeader(
                                    sunlightMode = sunlightMode,
                                    onToggleSunlightMode = { viewModel.toggleSunlightMode() }
                                )

                                Box(modifier = Modifier.weight(1f)) {
                                    MainScreenContent(
                                        destination = currentDestination,
                                        viewModel = viewModel,
                                        historyCount = historyRecords.size
                                    )
                                }
                            }
                        }
                    } else {
                        // Compact Handheld Screen: Top Header + Bottom Navigation Bar
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopHeader(
                                    sunlightMode = sunlightMode,
                                    onToggleSunlightMode = { viewModel.toggleSunlightMode() }
                                )
                            },
                            bottomBar = {
                                AgriBottomNavBar(
                                    currentDestination = currentDestination,
                                    onNavigate = { viewModel.setDestination(it) },
                                    savedCount = historyRecords.size
                                )
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            containerColor = MaterialTheme.colorScheme.background
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                MainScreenContent(
                                    destination = currentDestination,
                                    viewModel = viewModel,
                                    historyCount = historyRecords.size
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(
    destination: AgriNavDestination,
    viewModel: AgriCalcViewModel,
    historyCount: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (destination) {
            AgriNavDestination.DASHBOARD -> {
                DashboardScreen(
                    onNavigate = { viewModel.setDestination(it) },
                    savedRecordsCount = historyCount
                )
            }
            AgriNavDestination.FIELD_LAND -> {
                FieldLandScreen(viewModel = viewModel)
            }
            AgriNavDestination.SPRAYING -> {
                SprayingScreen(viewModel = viewModel)
            }
            AgriNavDestination.SOIL_FERTILIZER -> {
                SoilFertilizerScreen(viewModel = viewModel)
            }
            AgriNavDestination.CROP_YIELD -> {
                CropYieldScreen(viewModel = viewModel)
            }
            AgriNavDestination.IRRIGATION -> {
                IrrigationScreen(viewModel = viewModel)
            }
            AgriNavDestination.HISTORY -> {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
