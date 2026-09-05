package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgriNavDestination
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.SlateDarkBackground

@Composable
fun AgriBottomNavBar(
    currentDestination: AgriNavDestination,
    onNavigate: (AgriNavDestination) -> Unit,
    savedCount: Int,
    modifier: Modifier = Modifier
) {
    // Show high-priority items in compact bottom bar
    val mainDestinations = listOf(
        AgriNavDestination.DASHBOARD,
        AgriNavDestination.FIELD_LAND,
        AgriNavDestination.SPRAYING,
        AgriNavDestination.SOIL_FERTILIZER,
        AgriNavDestination.CROP_YIELD,
        AgriNavDestination.IRRIGATION,
        AgriNavDestination.HISTORY
    )

    NavigationBar(
        modifier = modifier
            .height(72.dp)
            .border(width = 1.dp, color = LeafGreenAccent.copy(alpha = 0.20f))
            .testTag("agri_bottom_nav_bar"),
        containerColor = ForestGreenPrimary,
        tonalElevation = 8.dp
    ) {
        mainDestinations.forEach { destination ->
            val isSelected = currentDestination == destination

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == AgriNavDestination.HISTORY && savedCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = EarthAmber,
                                    contentColor = Color.White
                                ) {
                                    Text("$savedCount", fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.shortLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SlateDarkBackground,
                    selectedTextColor = LeafGreenAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.50f),
                    unselectedTextColor = Color.White.copy(alpha = 0.50f),
                    indicatorColor = LeafGreenAccent
                ),
                modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
            )
        }
    }
}

@Composable
fun AgriNavigationRail(
    currentDestination: AgriNavDestination,
    onNavigate: (AgriNavDestination) -> Unit,
    savedCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = LeafGreenAccent.copy(alpha = 0.20f))
            .testTag("agri_navigation_rail"),
        containerColor = ForestGreenPrimary,
        header = {
            AgriCalcLogo(size = 40.dp)
        }
    ) {
        AgriNavDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination

            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == AgriNavDestination.HISTORY && savedCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = EarthAmber,
                                    contentColor = Color.White
                                ) {
                                    Text("$savedCount", fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(destination.icon, contentDescription = destination.title)
                        }
                    } else {
                        Icon(destination.icon, contentDescription = destination.title)
                    }
                },
                label = {
                    Text(
                        text = destination.shortLabel.uppercase(),
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = SlateDarkBackground,
                    selectedTextColor = LeafGreenAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.50f),
                    unselectedTextColor = Color.White.copy(alpha = 0.50f),
                    indicatorColor = LeafGreenAccent
                )
            )
        }
    }
}
