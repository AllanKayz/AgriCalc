package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculationRecord
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright
import com.example.util.ReportExporter
import com.example.viewmodel.AgriCalcViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: AgriCalcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allRecords by viewModel.historyRecords.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Field & Land", "Spraying", "Soil & Fertilizer", "Crop Yield", "Irrigation")

    val filteredRecords = allRecords.filter { record ->
        val matchesCat = selectedCategoryFilter == "All" || record.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                record.title.contains(searchQuery, ignoreCase = true) ||
                record.resultsSummary.contains(searchQuery, ignoreCase = true) ||
                record.inputsSummary.contains(searchQuery, ignoreCase = true) ||
                record.category.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Field History?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text("This will permanently delete all saved calculation logs from local offline database storage.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EarthAmber)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Search & Clear Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("history_search_input"),
                    placeholder = { Text("Search field records...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LeafGreenAccent)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreenAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (allRecords.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showClearAllConfirmDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = EarthAmber)
                    }
                }
            }

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LeafGreenAccent else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            ),
                            color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = LeafGreenAccent.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (allRecords.isEmpty()) "No Saved Calculations Yet" else "No Matching Records Found",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (allRecords.isEmpty()) {
                        "Run any calculation in Field, Spraying, Soil NPK, Crop Yield, or Irrigation and tap 'Save to History' to log your offline records."
                    } else {
                        "Try a different search term or select 'All' categories."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "SAVED CALCULATIONS (${filteredRecords.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = LeafGreenAccent
                    )
                }

                items(filteredRecords, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onDelete = { viewModel.deleteRecord(record) },
                        context = context
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(
    record: CalculationRecord,
    onDelete: () -> Unit,
    context: Context
) {
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    }

    val fullSummary = """
        AGRICALC SAVED RECORD
        • Category: ${record.category} - ${record.calculationType}
        • Title: ${record.title}
        • Date: $dateStr
        • Result: ${record.resultsSummary}
        • Inputs: ${record.inputsSummary}
        • Formula: ${record.formulaUsed}
    """.trimIndent()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ForestGreenPrimary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = LeafGreenBright
                    )
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Result Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0D1C14))
                    .border(1.dp, LeafGreenAccent, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "RESULT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                        color = LeafGreenAccent
                    )
                    Text(
                        text = record.resultsSummary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            if (record.inputsSummary.isNotBlank()) {
                Text(
                    text = "Inputs: ${record.inputsSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record.formulaUsed.isNotBlank()) {
                Text(
                    text = "Formula: ${record.formulaUsed}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeafGreenAccent
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Action
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("AgriCalc Record", fullSummary)
                        clipboard?.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Record",
                        tint = LeafGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share Action
                IconButton(
                    onClick = {
                        ReportExporter.shareSummary(context, record.title, fullSummary)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Record",
                        tint = LeafGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Print Action
                IconButton(
                    onClick = {
                        ReportExporter.printReport(
                            context = context,
                            reportTitle = record.title,
                            category = record.category,
                            primaryMetric = record.resultsSummary,
                            formula = record.formulaUsed,
                            details = listOf(
                                "Inputs" to record.inputsSummary,
                                "Calculation Type" to record.calculationType
                            )
                        )
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print Report",
                        tint = LeafGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Action
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = EarthAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
