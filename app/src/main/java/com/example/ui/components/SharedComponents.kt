package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwissTopBar(
    currentScreen: AppScreen,
    activeBusiness: BusinessEntity?,
    onSwitchBusinessClick: () -> Unit,
    onNavigateLanding: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SwissSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateLanding() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SwissAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Logo UsahaPilot",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "UsahaPilot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwissPrimaryText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SwissAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SwissAccentMuted)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = activeBusiness?.name ?: "Pilih Bisnis",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp)
                    )
                }
            }

            // Right Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeBusiness?.isDemo == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SwissWarning.copy(alpha = 0.15f))
                            .border(1.dp, SwissWarning.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DEMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwissWarning
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onSwitchBusinessClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("switch_business_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = "Ganti Usaha",
                        tint = SwissPrimaryText
                    )
                }

                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Pengaturan",
                        tint = SwissPrimaryText
                    )
                }
            }
        }
    }
}

@Composable
fun SwissBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SwissSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Filled.Dashboard,
                label = "Dashboard",
                isSelected = currentScreen == AppScreen.DASHBOARD,
                testTag = "nav_dashboard",
                onClick = { onNavigate(AppScreen.DASHBOARD) }
            )

            NavItem(
                icon = Icons.Outlined.Inventory2,
                selectedIcon = Icons.Filled.Inventory2,
                label = "Produk",
                isSelected = currentScreen == AppScreen.PRODUCTS,
                testTag = "nav_products",
                onClick = { onNavigate(AppScreen.PRODUCTS) }
            )

            NavItem(
                icon = Icons.Outlined.ReceiptLong,
                selectedIcon = Icons.Filled.ReceiptLong,
                label = "Transaksi",
                isSelected = currentScreen == AppScreen.TRANSACTIONS,
                testTag = "nav_transactions",
                onClick = { onNavigate(AppScreen.TRANSACTIONS) }
            )

            // Special highlighted AI Chat Assistant button
            AiChatNavItem(
                isSelected = currentScreen == AppScreen.CHAT_ASSISTANT,
                testTag = "nav_ai_chat",
                onClick = { onNavigate(AppScreen.CHAT_ASSISTANT) }
            )

            NavItem(
                icon = Icons.Outlined.Campaign,
                selectedIcon = Icons.Filled.Campaign,
                label = "Marketing",
                isSelected = currentScreen == AppScreen.MARKETING_GENERATOR,
                testTag = "nav_marketing",
                onClick = { onNavigate(AppScreen.MARKETING_GENERATOR) }
            )

            NavItem(
                icon = Icons.Outlined.Assessment,
                selectedIcon = Icons.Filled.Assessment,
                label = "Laporan",
                isSelected = currentScreen == AppScreen.BUSINESS_REPORTS,
                testTag = "nav_reports",
                onClick = { onNavigate(AppScreen.BUSINESS_REPORTS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = if (isSelected) SwissAccent else SwissSecondaryText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) SwissAccent else SwissSecondaryText
        )
    }
}

@Composable
private fun AiChatNavItem(
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) SwissAccent else SwissPrimaryText),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Chat Assistant",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "AI Chat",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) SwissAccent else SwissPrimaryText
        )
    }
}

@Composable
fun SwissCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SwissSurface,
    borderColor: Color = SwissBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SwissStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = SwissAccent,
    modifier: Modifier = Modifier
) {
    SwissCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = SwissSecondaryText,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SwissSecondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = SwissPrimaryText
        )
        if (subtitle != null || badgeText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = SwissSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
