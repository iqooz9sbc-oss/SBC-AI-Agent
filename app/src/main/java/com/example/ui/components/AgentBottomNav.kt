package com.example.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.viewmodel.NavTab

@Composable
fun AgentBottomNav(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isBn: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .height(68.dp)
            .testTag("bottom_navigation_bar")
    ) {
        NavTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            val labelText = if (isBn) tab.titleBn else tab.titleEn

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = labelText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricViolet,
                    selectedTextColor = ElectricViolet,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                ),
                alwaysShowLabel = true,
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
