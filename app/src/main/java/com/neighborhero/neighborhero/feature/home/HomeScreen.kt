package com.neighborhero.neighborhero.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
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
    selectedLocation: String = "Maplewood District",
    unreadNotificationCount: Int = 0,
    onLocationChange: (String, Double?, Double?) -> Unit = { _, _, _ -> },
    onRequestClick: (HelpRequest) -> Unit,
    onHelperClick: (HelperProfile) -> Unit,
    onCreateRequestClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onActiveTaskClick: (HelpRequest?) -> Unit = { onNavigate(Routes.ACTIVE_TASK) }
) {
    var showLocationSelector by remember { mutableStateOf(false) }
    val activeTask = requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS }
    val displayActiveTask = activeTask ?: requests.firstOrNull() ?: HelpRequest(
        id = "default_focus_task",
        title = "Help with groceries",
        category = "Groceries",
        description = "Buy some groceries from the store.",
        requesterName = "Bessie Cooper",
        requesterRating = 4.9f,
        requesterTaskCount = 28,
        location = "Maplewood District",
        distance = "0.4 mi away",
        requestedTime = "today 5pm",
        isUrgent = false,
        status = RequestStatus.IN_PROGRESS
    )
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val isDark = LocalIsDarkTheme.current

    val sampleReferenceRequests = remember {
        listOf(
            HelpRequest(
                id = "ref_groceries_1",
                title = "Need groceries ASAP",
                category = "Groceries",
                description = "Need someone to buy groceries for my family.",
                requesterName = "David Cooper",
                requesterRating = 4.9f,
                requesterTaskCount = 15,
                location = "Maplewood District",
                distance = "2.5 km",
                requestedTime = "today 4pm",
                isUrgent = true,
                status = RequestStatus.OPEN
            ),
            HelpRequest(
                id = "ref_books_1",
                title = "Old Textbooks",
                category = "Books",
                description = "Looking for 2nd year textbooks.",
                requesterName = "Sarah Jenkins",
                requesterRating = 4.8f,
                requesterTaskCount = 8,
                location = "Oakridge Avenue",
                distance = "3.1 km",
                requestedTime = "today 6pm",
                isUrgent = false,
                status = RequestStatus.OPEN
            ),
            HelpRequest(
                id = "ref_repairs_1",
                title = "Garden Fence Repair",
                category = "Repairs",
                description = "Need a handy neighbor to assist with securing fence posts.",
                requesterName = "Marcus Vance",
                requesterRating = 4.7f,
                requesterTaskCount = 22,
                location = "Pinecrest Ave",
                distance = "4.0 km",
                requestedTime = "today 7pm",
                isUrgent = false,
                status = RequestStatus.OPEN
            )
        )
    }

    val combinedRequests = remember(requests, sampleReferenceRequests) {
        val openRequests = requests.filter { it.status == RequestStatus.OPEN }
        val list = openRequests.toMutableList()
        for (sample in sampleReferenceRequests) {
            if (list.none { it.id == sample.id || it.title.equals(sample.title, ignoreCase = true) }) {
                list.add(sample)
            }
        }
        if (list.isEmpty()) sampleReferenceRequests else list
    }

    val filteredRequests = remember(combinedRequests, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") combinedRequests
        else combinedRequests.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Official Circular NeighborHero Logo Badge
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = if (isDark) 0.16f else 0.22f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        Color.White.copy(alpha = if (isDark) 0.35f else 0.55f)
                                    ),
                                    shadowElevation = 3.dp
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_header_logo_badge),
                                        contentDescription = "NeighborHero Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "NeighborHero",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 23.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White.copy(alpha = if (isDark) 0.14f else 0.22f),
                                        border = BorderStroke(
                                            1.dp,
                                            Color.White.copy(alpha = if (isDark) 0.28f else 0.38f)
                                        ),
                                        modifier = Modifier.clickable { showLocationSelector = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = selectedLocation.ifBlank { "Maplewood District" },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Change Location",
                                                tint = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Frosted Glass Circular Notifications Button with Unread Badge
                            Box(contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { onNavigate(Routes.NOTIFICATIONS) },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = if (isDark) 0.16f else 0.25f))
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                Color.White.copy(alpha = if (isDark) 0.30f else 0.45f)
                                            ),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alerts",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Red unread notification dot (only when there are actual unread notifications)
                                if (unreadNotificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                            .border(1.5.dp, Color.White, CircleShape)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-3).dp, y = 3.dp)
                                    )
                                }
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
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .padding(bottom = 4.dp, end = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Request",
                        tint = Color.White,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Water-Inspired Multi-Layered Glass Hero Focus Card
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isDark) 4.dp else 6.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    spotColor = if (isDark) Color(0x66000000) else Color(0x242C4663),
                                    ambientColor = if (isDark) Color(0x66000000) else Color(0x102C4663)
                                ),
                            shape = RoundedCornerShape(26.dp),
                            color = if (isDark) Color(0xFF0C1F32).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.55f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color.White.copy(alpha = 0.85f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top Row: 75% Circular Ring + Title + Status Badge
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(46.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { 0.75f },
                                                modifier = Modifier.fillMaxSize(),
                                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF00C6FF),
                                                trackColor = if (isDark) Color(0xFF1E3A52) else Color(0xFFBAE6FD),
                                                strokeWidth = 4.5.dp,
                                                strokeCap = StrokeCap.Round
                                            )
                                            Text(
                                                text = "75%",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = GlassTheme.textPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Overall progress",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = GlassTheme.textPrimary
                                        )
                                    }

                                    // Status Pill Badge
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isDark) Color(0xFF0C2438) else Color(0xFFE0F2FE),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isDark) Color(0x3338BDF8) else Color(0x400284C7)
                                        )
                                    ) {
                                        Text(
                                            text = "In Progress",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Inner Nested Focus Card
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = if (isDark) Color(0xFF0E2235).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.88f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White
                                    ),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isDark) Color(0xFF132B42) else Color(0xFFE0F2FE))
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
                                                            text = displayActiveTask.requesterName.take(1).uppercase().ifEmpty { "B" },
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Today's focus",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = displayActiveTask.title.ifEmpty { "Help with groceries" },
                                                    style = MaterialTheme.typography.headlineSmall.copy(
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = (-0.3).sp
                                                    ),
                                                    color = GlassTheme.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(modifier = Modifier.height(3.dp))

                                                Text(
                                                    text = displayActiveTask.description.ifEmpty { "Buy some groceries from the store." },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.5.sp,
                                                        lineHeight = 16.sp
                                                    ),
                                                    color = GlassTheme.textSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Grocery Image Thumbnail on right
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)
                                                ),
                                                shadowElevation = 2.dp,
                                                modifier = Modifier.size(62.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_thumb_groceries),
                                                    contentDescription = "Groceries Focus",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Action Row: "➔ Start task" + "🕒 today 5pm"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = GlassTheme.pillButtonBg,
                                                modifier = Modifier.clickable { onActiveTaskClick(displayActiveTask) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
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
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = displayActiveTask.requestedTime.ifEmpty { "today 5pm" },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = GlassTheme.textMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dedicated "Next Up" Capsule Bar below Progress Card
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) Color(0xFF0C1F32).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)
                            ),
                            shadowElevation = if (isDark) 2.dp else 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clickable { onActiveTaskClick(displayActiveTask) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Next Up",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(12.dp)
                                        .background(GlassTheme.textMuted.copy(alpha = 0.45f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verify completion and exchange...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = GlassTheme.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GlassTheme.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Category Quick Filter Day Capsules
                item {
                    val categoryFilters = listOf("All", "Groceries", "Books", "Repairs") +
                        RequestCategory.CANONICAL.filter { it !in listOf("Groceries", "Books", "Repairs") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categoryFilters.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            val capsuleBg = if (isSelected) {
                                if (isDark) Color(0xFF132B42) else Color.White
                            } else {
                                if (isDark) Color(0xFF0A1826).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.50f)
                            }
                            val capsuleBorder = if (isSelected) {
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color.White
                            } else {
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f)
                            }
                            val capsuleTextColor = if (isSelected) {
                                if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            } else {
                                if (isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B)
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = capsuleBg,
                                border = BorderStroke(1.dp, capsuleBorder),
                                shadowElevation = if (isSelected) 4.dp else 0.dp,
                                modifier = Modifier.clickable { selectedCategoryFilter = cat }
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = if (isSelected) 20.dp else 18.dp,
                                        vertical = if (isSelected) 8.dp else 11.dp
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = capsuleTextColor
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
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

                // 3. Nearby Requests Section Header with Accent Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nearby Help Requests",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 19.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }
                        TextButton(onClick = { onNavigate(Routes.NEARBY_REQUESTS) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "See All",
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
                    Spacer(modifier = Modifier.height(108.dp))
                }
            }
        }

        if (showLocationSelector) {
            LocationSelectorBottomSheet(
                currentLocation = selectedLocation.ifBlank { "Maplewood District" },
                onLocationConfirmed = { newLocation, lat, lng ->
                    onLocationChange(newLocation, lat, lng)
                    showLocationSelector = false
                },
                onDismiss = { showLocationSelector = false }
            )
        }
    }
}
