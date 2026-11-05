package com.neighborhero.neighborhero.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.model.RequestCategory
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.HelperCard
import com.neighborhero.neighborhero.ui.components.RequestCard
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    requests: List<HelpRequest>,
    helpers: List<HelperProfile>,
    onRequestClick: (HelpRequest) -> Unit,
    onHelperClick: (HelperProfile) -> Unit,
    onCreateRequestClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onActiveTaskClick: (HelpRequest?) -> Unit = { onNavigate(Routes.ACTIVE_TASK) }
) {
    val activeTask = requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val isDark = LocalIsDarkTheme.current

    val filteredRequests = remember(requests, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") requests
        else requests.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NeighborHero",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = if (isDark) 0.12f else 0.20f),
                                    border = BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = if (isDark) 0.25f else 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Maplewood District",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Frosted Glass Circular Notifications Button
                            IconButton(
                                onClick = { onNavigate(Routes.NOTIFICATIONS) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.22f))
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            Color.White.copy(alpha = if (isDark) 0.25f else 0.40f)
                                        ),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                BottomNavigationBar(currentRoute = Routes.HOME, onNavigate = onNavigate)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateRequestClick,
                    containerColor = GlassTheme.pillButtonBg,
                    contentColor = GlassTheme.pillButtonText,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Request",
                        tint = GlassTheme.pillButtonText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Water-Inspired Multi-Layered Glass Hero Focus Card
                item {
                    if (activeTask != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = RoundedCornerShape(30.dp),
                                    spotColor = if (isDark) Color(0x66000000) else Color(0x242C4663),
                                    ambientColor = if (isDark) Color(0x66000000) else Color(0x102C4663)
                                ),
                            shape = RoundedCornerShape(30.dp),
                            color = if (isDark) Color(0xFF0C1F32).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.45f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Progress Ring + Status Label
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { 0.65f },
                                                modifier = Modifier.fillMaxSize(),
                                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A),
                                                trackColor = if (isDark) Color(0x4038BDF8) else Color(0x280F172A),
                                                strokeWidth = 2.2.dp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Overall progress",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = GlassTheme.textPrimary
                                        )
                                    }

                                    Text(
                                        text = "In Progress",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Inner Nested Focus Card
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = GlassTheme.focusCardBackground,
                                    border = BorderStroke(1.dp, GlassTheme.focusCardBorder),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Today's focus",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = GlassTheme.textMuted
                                            )

                                            // Small Avatar Indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Color(0xFF132B42) else Color(0xFFE2E8F0))
                                                    .border(
                                                        BorderStroke(
                                                            1.dp,
                                                            if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color.White
                                                        ),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = activeTask.requesterName.take(1).uppercase().ifEmpty { "H" },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = activeTask.title,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 21.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = (-0.4).sp
                                            ),
                                            color = GlassTheme.textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = activeTask.description,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                lineHeight = 19.sp
                                            ),
                                            color = GlassTheme.textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(18.dp))

                                        // Action Button + Time
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = GlassTheme.pillButtonBg,
                                                modifier = Modifier.clickable { onActiveTaskClick(activeTask) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .background(
                                                                if (isDark) Color(0xFF0F172A) else Color.White,
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = null,
                                                            tint = if (isDark) Color.White else Color(0xFF0F172A),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Start task",
                                                        style = MaterialTheme.typography.labelLarge.copy(
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = GlassTheme.pillButtonText
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = GlassTheme.textMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = activeTask.requestedTime,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = GlassTheme.textMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Bottom Row of Outer Card: "Next Up"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Next Up",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Verify completion and exchange community points",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = GlassTheme.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        // Community Aid Pulse Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(26.dp),
                            alpha = if (isDark) 0.85f else 0.65f
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF10283C) else Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Community Aid Pulse",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Neighbors nearby are active. Need assistance or ready to help?",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                        color = GlassTheme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Category Quick Filter Day Capsules
                item {
                    val categoryFilters = listOf("All") + RequestCategory.CANONICAL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryFilters.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            val capsuleBg = if (isSelected) {
                                if (isDark) Color(0xFF132B42) else Color.White.copy(alpha = 0.95f)
                            } else {
                                if (isDark) Color(0xFF0A1826).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.45f)
                            }
                            val capsuleBorder = if (isSelected) {
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color.White
                            } else {
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.60f)
                            }
                            val capsuleTextColor = if (isSelected) {
                                if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            } else {
                                if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = capsuleBg,
                                border = BorderStroke(1.dp, capsuleBorder),
                                shadowElevation = if (isSelected) 4.dp else 0.dp,
                                modifier = Modifier.clickable { selectedCategoryFilter = cat }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = capsuleTextColor
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .background(
                                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Nearby Requests Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby Help Requests",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = GlassTheme.textPrimary
                        )
                        TextButton(onClick = { onNavigate(Routes.NEARBY_REQUESTS) }) {
                            Text(
                                text = "See All",
                                color = Color(0xFF38BDF8),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Request Cards
                if (filteredRequests.isEmpty()) {
                    item {
                        GlassEmptyState(
                            icon = Icons.Default.AutoAwesome,
                            title = "No Requests in this Category",
                            subtitle = "Be the first neighbor to post a request or tap '+' to create one."
                        )
                    }
                } else {
                    items(filteredRequests.take(2)) { req ->
                        RequestCard(request = req, onClick = { onRequestClick(req) })
                    }
                }

                // 4. AI Recommended Helpers Section Header
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Top AI Matched Helpers",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }
                        TextButton(onClick = { onNavigate(Routes.RECOMMENDED_HELPERS) }) {
                            Text(
                                text = "See All",
                                color = Color(0xFF38BDF8),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Helper Cards
                if (helpers.isEmpty()) {
                    item {
                        GlassEmptyState(
                            icon = Icons.Default.AutoAwesome,
                            title = "Discovering Community Helpers",
                            subtitle = "Local verified helpers will appear here as they become available."
                        )
                    }
                } else {
                    items(helpers.take(2)) { helper ->
                        HelperCard(helper = helper, onClick = { onHelperClick(helper) })
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
