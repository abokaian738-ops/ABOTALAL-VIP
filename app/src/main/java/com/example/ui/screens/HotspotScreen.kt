package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HotspotActive
import com.example.data.model.HotspotProfile
import com.example.data.model.HotspotUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit = {}
) {
    val activeUsers by viewModel.activeHotspot.collectAsState()
    val hotspotUsers by viewModel.hotspotUsers.collectAsState()
    val profiles by viewModel.hotspotProfiles.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("المتصلون حالياً", "قائمة الحسابات", "بروفايلات الخدمة", "توليد وطباعة الكروت")

    // Adding Modals User
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }

    // Search query states
    var searchUserQuery by remember { mutableStateOf("") }
    var searchActiveQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(hotspotUsers, searchUserQuery) {
        hotspotUsers.filter { it.name.contains(searchUserQuery, ignoreCase = true) }
    }

    val filteredActive = remember(activeUsers, searchActiveQuery) {
        activeUsers.filter { it.user.contains(searchActiveQuery, ignoreCase = true) }
    }

    // Snackbar notifications
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF063B40),
                                        Color(0xFF0C5A60),
                                        Color(0xFF0284C7)
                                    )
                                )
                            )
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { onBack() }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "الرجوع للرئيسية",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "نظام الهوتسبوت",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "المستخدمون والبروفايلات والكروت",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFBAE6FD)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "المتصلون: ${activeUsers.size}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF08A),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (activeTab == 1) { // Add User FAB
                    ExtendedFloatingActionButton(
                        onClick = { showAddUserDialog = true },
                        icon = { Icon(Icons.Filled.Add, "إضافة حساب") },
                        text = { Text("إضافة مستخدم هوتسبوت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else if (activeTab == 2) { // Add Profile FAB
                    ExtendedFloatingActionButton(
                        onClick = { showAddProfileDialog = true },
                        icon = { Icon(Icons.Filled.Category, "إضافة بروفايل") },
                        text = { Text("إضافة بروفايل سرعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Sub-tabs segment switcher inside dynamic pills
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[activeTab])
                                .height(3.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    tabs.forEachIndexed { index, text ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    text = text,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (activeTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Render matching Tab Screens
                when (activeTab) {
                    0 -> ActiveConnectionsTab(
                        activeList = filteredActive,
                        searchQuery = searchActiveQuery,
                        onSearchChange = { searchActiveQuery = it },
                        onKick = { id, user ->
                            viewModel.kickActiveUser(id, user,
                                onSuccess = {
                                    scope.launch { snackbarHostState.showSnackbar("تم فصل المستخدم '$user' من الشبكة") }
                                },
                                onError = { err ->
                                    scope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            )
                        }
                    )
                    1 -> HotspotUsersTab(
                        usersList = filteredUsers,
                        searchQuery = searchUserQuery,
                        onSearchChange = { searchUserQuery = it },
                        onDelete = { id, name ->
                            viewModel.deleteHotspotUser(id, name,
                                onSuccess = {
                                    scope.launch { snackbarHostState.showSnackbar("تم حذف الحساب '$name' نهائياً") }
                                },
                                onError = { err ->
                                    scope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            )
                        }
                    )
                    2 -> HotspotProfilesTab(profiles)
                    3 -> VoucherPrinterScreen(viewModel = viewModel, initialMode = VoucherMode.HOTSPOT)
                }
            }

            // User Creation Modal Dialog
            if (showAddUserDialog) {
                AddUserDialog(
                    profiles = profiles,
                    onDismiss = { showAddUserDialog = false },
                    onConfirm = { name, password, profile, limit, comment ->
                        viewModel.addHotspotUser(
                            HotspotUser(
                                name = name,
                                password = password,
                                profile = profile,
                                limitUptime = limit,
                                comment = comment
                            ),
                            onSuccess = {
                                showAddUserDialog = false
                                scope.launch { snackbarHostState.showSnackbar("تم إنشاء كارت هوتسبوت جديد: $name") }
                            },
                            onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            }
                        )
                    }
                )
            }

            // Speed Profile Creation Modal Dialog
            if (showAddProfileDialog) {
                AddProfileDialog(
                    onDismiss = { showAddProfileDialog = false },
                    onConfirm = { name, sharedUsers, rateLimit, keepalive ->
                        viewModel.addHotspotProfile(
                            HotspotProfile(
                                name = name,
                                sharedUsers = sharedUsers,
                                rateLimit = rateLimit,
                                keepaliveTimeout = keepalive
                            ),
                            onSuccess = {
                                showAddProfileDialog = false
                                scope.launch { snackbarHostState.showSnackbar("تم إضافة بروفايل السرعة الجديد: $name") }
                            },
                            onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            }
                        )
                    }
                )
            }
        }
    }
}

/* Tab View 1: Active Users representation */
@Composable
fun ActiveConnectionsTab(
    activeList: List<HotspotActive>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onKick: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        // Search filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث عن مستخدم نشط...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (activeList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.SignalWifiOff, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), modifier = Modifier.size(60.dp))
                    Text("لا يوجد مستخدمون متصلون بالشبكة حالياً", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeList) { active ->
                    ActiveUserItemCard(active, onKick)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ActiveUserItemCard(
    active: HotspotActive,
    onKick: (String, String) -> Unit
) {
    val downloadedMb = active.bytesOut / 1024 / 1024
    val uploadedMb = active.bytesIn / 1024 / 1024

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                    Text(
                        text = active.user,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // IP & MAC
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text("عنوان IP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(active.address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("عنوان MAC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(active.macAddress, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats: Uptime, RX, TX
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text("زمن الاتصال", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(active.uptime, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("مجموع التنزيل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("$downloadedMb MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Column {
                        Text("مجموع الرفع", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("$uploadedMb MB", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF59E0B))
                    }
                }
            }

            // Kick user out button
            Button(
                onClick = { onKick(active.id, active.user) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.PowerSettingsNew, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("فصل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


/* Tab View 2: Configured Users List */
@Composable
fun HotspotUsersTab(
    usersList: List<HotspotUser>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDelete: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث باسم الحساب...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (usersList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.PeopleOutline, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), modifier = Modifier.size(60.dp))
                    Text("لا توجد حسابات مطابقة للبحث", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(usersList) { u ->
                    HotspotUserItemCard(u, onDelete)
                }
                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }
    }
}

@Composable
fun HotspotUserItemCard(
    user: HotspotUser,
    onDelete: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.disabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (user.disabled) Color.Gray else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (user.disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    // If comment is set, show tag
                    if (user.comment.isNotBlank()) {
                        Text(
                            text = user.comment,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text("الباقة / البروفايل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(user.profile, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("الحد الأقصى للإنترنت", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(user.limitUptime ?: "unlimited", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("حالة الكارت", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(
                            text = if (user.disabled) "معطل" else "مفعل ومتاح",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (user.disabled) Color.Red else Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = { onDelete(user.id, user.name) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Outlined.Delete, "حذف كارت")
            }
        }
    }
}


/* Tab View 3: Hotspot Speed Profiles representation */
@Composable
fun HotspotProfilesTab(profilesList: List<HotspotProfile>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(profilesList) { profile ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تحديد سرعة العميل: " + if (profile.rateLimit == "unlimited") "سرعة مفتوحة بلا قيود" else profile.rateLimit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("أجهزة متزامنة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text("${profile.sharedUsers} عميل", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("توقيت الخروج", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(profile.keepaliveTimeout, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}


/* MODALS DIALOGS */

@Composable
fun AddUserDialog(
    profiles: List<HotspotProfile>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedProfile by remember { mutableStateOf(profiles.firstOrNull()?.name ?: "default") }
    var limitUptime by remember { mutableStateOf("unlimited") }
    var comment by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء حساب مستخدم هوتسبوت جديد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المستخدم / اسم الكارت (مطلوب)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Profile Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("البروفايل المختار: $selectedProfile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        profiles.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    selectedProfile = p.name
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitUptime,
                    onValueChange = { limitUptime = it },
                    label = { Text("الحد الزمني للكارت (مثال: 12h, 1d, unlimited)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("ملاحظات / فئة الكارت") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, password, selectedProfile, limitUptime, comment) },
                enabled = name.isNotBlank()
            ) {
                Text("إضافة وحفظ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sharedUsers by remember { mutableStateOf("1") }
    var rateLimit by remember { mutableStateOf("1M/1M") }
    var keepalive by remember { mutableStateOf("2m") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة بروفايل سرعة للعملاء", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الباقة / البروفايل (مطلوب)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rateLimit,
                    onValueChange = { rateLimit = it },
                    label = { Text("تحديد السرعة (أبلود/داونلود) (مثال: 2M/2M)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sharedUsers,
                    onValueChange = { sharedUsers = it },
                    label = { Text("عدد الأجهزة المتاحة لكل مستخدم بالبروفايل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keepalive,
                    onValueChange = { keepalive = it },
                    label = { Text("زمن انتهاء خمول الجلسة (مثال: 2m)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && rateLimit.isNotBlank()) onConfirm(name, sharedUsers, rateLimit, keepalive) },
                enabled = name.isNotBlank() && rateLimit.isNotBlank()
            ) {
                Text("إنشاء البروفايل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}
