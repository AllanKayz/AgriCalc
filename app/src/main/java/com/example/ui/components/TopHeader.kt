package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.SlateDarkBackground

@Composable
fun TopHeader(
    modifier: Modifier = Modifier,
    sunlightMode: Boolean,
    onToggleSunlightMode: () -> Unit,
    historyCount: Int = 0,
    onOpenHistory: () -> Unit = {},
    presetsCount: Int = 0,
    onOpenPresets: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = ForestGreenPrimary,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = LeafGreenAccent.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Logo Badge & Title with Glowing Offline Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Logo framed in dark rounded-xl border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateDarkBackground)
                            .border(2.dp, LeafGreenAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AgriCalcLogo(
                            size = 28.dp,
                            modifier = Modifier.testTag("app_logo")
                        )
                    }

                    Column {
                        Text(
                            text = "AgriCalc",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 19.sp
                            ),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            // Glowing indicator dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(LeafGreenAccent)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            )
                            Text(
                                text = "OFFLINE ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 9.5.sp
                                ),
                                color = LeafGreenAccent
                            )
                        }
                    }
                }

                // Right: Metric SI pill, Presets, Sunlight & History buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // Metric SI Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LeafGreenAccent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "METRIC (SI)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = SlateDarkBackground
                            )
                        }
                        Text(
                            text = "v2.4.0",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sunlight / Contrast Toggle
                    IconButton(
                        onClick = onToggleSunlightMode,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SlateDarkBackground.copy(alpha = 0.6f))
                            .testTag("toggle_sunlight_mode")
                    ) {
                        Icon(
                            imageVector = if (sunlightMode) Icons.Default.Nightlight else Icons.Default.LightMode,
                            contentDescription = if (sunlightMode) "Switch to Dark" else "Switch to Sunlight",
                            tint = if (sunlightMode) EarthAmber else LeafGreenAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
