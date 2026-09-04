package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserManagerProfile
import com.example.data.model.UserManagerUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class UserManagerMenuItem(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val iconTint: Color = Color(0xFF0C5A60)
)

data class GeneratedBatchRecord(
    val batchId: String,
    val date: String,
    val profileName: String,
    val count: Int,
    val pricePerCard: Int,
    val prefix: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagerScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit = {}
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val conn by viewModel.currentConnection.collectAsState()
    val userManagerUsers by viewModel.userManagerUsers.collectAsState()
    val userManagerProfiles by viewModel.userManagerProfiles.collectAsState()

    // Dialog & Screen states
    var showAddCardsDialog by remember { mutableStateOf(false) }
    var showManageCardsDialog by remember { mutableStateOf(false) }
    var showManageProfilesDialog by remember { mutableStateOf(false) }
    var showBatchReportsDialog by remember { mutableStateOf(false) }
    var showDeviceReportsDialog by remember { mutableStateOf(false) }
    var showIpBindingsDialog by remember { mutableStateOf(false) }
    var showAdvancedDialog by remember { mutableStateOf(false) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }

    var showPowerDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Batch records tracking
    var batchRecords by remember {
        mutableStateOf(
            listOf(
                GeneratedBatchRecord(
                    batchId = "BATCH-01",
                    date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                    profileName = "باقة 500 د.ع",
                    count = 25,
                    pricePerCard = 500,
                    prefix = "VIP"
                )
            )
        )
    }

    // 10 Cards strictly mapped to the screenshot
    val menuItems = remember {
        listOf(
            UserManagerMenuItem(
                id = 1,
                title = "إضافة كروت",
                icon = Icons.Filled.ConfirmationNumber,
                gradientColors = listOf(Color(0xFFD6EEF8), Color(0xFFB5DEF2)),
                iconTint = Color(0xFF0284C7)
            ),
            UserManagerMenuItem(
                id = 2,
                title = "إدارة الكروت",
                icon = Icons.Filled.Badge,
                gradientColors = listOf(Color(0xFFD5F3EE), Color(0xFFB6ECE3)),
                iconTint = Color(0xFF0D9488)
            ),
            UserManagerMenuItem(
                id = 3,
                title = "إدارة الباقات",
                icon = Icons.Filled.Assignment,
                gradientColors = listOf(Color(0xFFD6EFE5), Color(0xFFB6E3D1)),
                iconTint = Color(0xFF059669)
            ),
            UserManagerMenuItem(
                id = 4,
                title = "تقارير الدفعات",
                icon = Icons.Filled.AutoAwesome,
                gradientColors = listOf(Color(0xFFFDECDA), Color(0xFFFCDDC0)),
                iconTint = Color(0xFFEA580C)
            ),
            UserManagerMenuItem(
                id = 5,
                title = "تقارير الاجهزة",
                icon = Icons.Filled.Router,
                gradientColors = listOf(Color(0xFFDFE6F6), Color(0xFFC5D4F1)),
                iconTint = Color(0xFF4F46E5)
            ),
            UserManagerMenuItem(
                id = 6,
                title = "قائمة الحظر IP Bindings",
                icon = Icons.Filled.LinkOff,
                gradientColors = listOf(Color(0xFFFCE1E3), Color(0xFFF8C3C7)),
                iconTint = Color(0xFFE11D48)
            ),
            UserManagerMenuItem(
                id = 7,
                title = "متقدم",
                icon = Icons.Filled.Tune,
                gradientColors = listOf(Color(0xFFEBDDF4), Color(0xFFDCC2EF)),
                iconTint = Color(0xFF9333EA)
            ),
            UserManagerMenuItem(
                id = 8,
                title = "صيانة الكروت المنتهية",
                icon = Icons.Filled.Build,
                gradientColors = listOf(Color(0xFFD4EFEF), Color(0xFFB5E2E2)),
                iconTint = Color(0xFF0891B2)
            ),
            UserManagerMenuItem(
                id = 9,
                title = "النسخ الاحتياطية",
                icon = Icons.Filled.CloudDownload,
                gradientColors = listOf(Color(0xFFDCEEF8), Color(0xFFBEDFF2)),
                iconTint = Color(0xFF0284C7)
            ),
            UserManagerMenuItem(
                id = 10,
                title = "إضافة خدمة",
                icon = Icons.Filled.AddCircleOutline,
                gradientColors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
                iconTint = Color(0xFF475569)
            )
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                // Top Header Bar matching the screenshot
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right Side: Back Arrow Button ➔ + Green Badge "متصل •"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Back Button
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { onBack() }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowForward,
                                            contentDescription = "الرجوع",
                                            tint = Color(0xFF0C5A60),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Green Badge: متصل •
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF16A34A))
                                        )
                                        Text(
                                            text = "متصل",
                                            fontFamily = CairoFontFamily,
                                            color = Color(0xFF15803D),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Center: نظام اليوزرمانجر
                            Text(
                                text = "نظام اليوزرمانجر",
                                fontFamily = CairoFontFamily,
                                color = Color(0xFF0C5A60),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            // Left Side: 4 Action Buttons: [Power ⏻, Exit ➔, Dark Mode 🌙, Language 🌐]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Power
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { showPowerDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.PowerSettingsNew,
                                            contentDescription = "طاقة الراوتر",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                // 2. Logout
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { showExitDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.ExitToApp,
                                            contentDescription = "تسجيل الخروج",
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // 3. Theme Toggle
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { viewModel.toggleDarkMode() }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.NightlightRound,
                                            contentDescription = "الوضع الليلي",
                                            tint = if (isDarkMode) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                // 4. Language
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Language,
                                            contentDescription = "اللغة",
                                            tint = Color(0xFF334155),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                    }
                }
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    // Header Section: نظام اليوزرمانجر / إجراءات سريعة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "نظام اليوزرمانجر",
                                fontFamily = CairoFontFamily,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "إجراءات سريعة",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Summary pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "إجمالي الكروت: ${userManagerUsers.size}",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 2-Column Grid (10 cards)
                val rows = menuItems.chunked(2)
                items(rows) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { item ->
                            UserManagerCard(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    when (item.id) {
                                        1 -> showAddCardsDialog = true
                                        2 -> showManageCardsDialog = true
                                        3 -> showManageProfilesDialog = true
                                        4 -> showBatchReportsDialog = true
                                        5 -> showDeviceReportsDialog = true
                                        6 -> showIpBindingsDialog = true
                                        7 -> showAdvancedDialog = true
                                        8 -> showMaintenanceDialog = true
                                        9 -> showBackupDialog = true
                                        10 -> showAddServiceDialog = true
                                    }
                                }
                            )
                        }
                        // If row has only 1 item, pad empty space
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // --- 1. Dialog: إضافة كروت (Generate & Batch Management Full Screen Dialog) ---
    if (showAddCardsDialog) {
        UserManagerBatchFullDialog(
            viewModel = viewModel,
            batchRecords = batchRecords,
            onUpdateBatches = { updated ->
                batchRecords = updated
            },
            onDismiss = { showAddCardsDialog = false }
        )
    }

    // --- 2. Dialog: إدارة الكروت (Manage Cards) ---
    if (showManageCardsDialog) {
        Dialog(
            onDismissRequest = { showManageCardsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            UserManagerCardsScreen(
                viewModel = viewModel,
                onBack = { showManageCardsDialog = false }
            )
        }
    }

    // --- 3. Dialog: إدارة الباقات (Profiles Management Screen matching screenshots) ---
    if (showManageProfilesDialog) {
        UserManagerProfilesDialog(
            viewModel = viewModel,
            onDismiss = { showManageProfilesDialog = false }
        )
    }

    // --- 4. Dialog: تقارير الدفعات (Batch Reports) ---
    if (showBatchReportsDialog) {
        AlertDialog(
            onDismissRequest = { showBatchReportsDialog = false },
            title = {
                Text("تقارير دفعات الكروت", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "سجل الدفعات المولدة والكميات الصادرة:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(batchRecords) { batch ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF7ED),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "دفعة: ${batch.batchId} (${batch.prefix})", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "الباقة: ${batch.profileName} | العدد: ${batch.count} كرت", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(text = "التاريخ: ${batch.date}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0C5A60),
                                        modifier = Modifier.clickable {
                                            feedbackMessage = "تم تجهيز ملف طباعة الدفعة ${batch.batchId}"
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("طباعة", fontFamily = CairoFontFamily, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBatchReportsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 5. Dialog: تقارير الاجهزة (Device Reports) ---
    if (showDeviceReportsDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceReportsDialog = false },
            title = {
                Text("تقارير الأجهزة وجلسات المستخدمين", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الجلسات النشطة المتصلة عبر اليوزرمانجر:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    val activeUsers = userManagerUsers.filter { it.active }.take(6)
                    if (activeUsers.isEmpty()) {
                        Text("لا توجد أجهزة متصلة حالياً.", fontFamily = CairoFontFamily)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeUsers) { user ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "المستخدم: ${user.username}", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "الباقة: ${user.profile} | مدة الاتصال: ${user.uptimeUsed}", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.deleteUserManagerUser(user.username, onSuccess = {}, onError = {})
                                                feedbackMessage = "تم قطع اتصال المستخدم ${user.username}"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("قطع", fontFamily = CairoFontFamily, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeviceReportsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 6. Dialog: قائمة الحظر IP Bindings ---
    if (showIpBindingsDialog) {
        var bindingIp by remember { mutableStateOf("") }
        var bindingMac by remember { mutableStateOf("") }
        var bindingType by remember { mutableStateOf("blocked") } // blocked, bypassed, regular

        AlertDialog(
            onDismissRequest = { showIpBindingsDialog = false },
            title = {
                Text("قائمة الحظر والـ IP Bindings", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "حظر أو تجاوز أجهزة معينة عبر عنوان MAC أو IP:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = bindingMac,
                        onValueChange = { bindingMac = it },
                        label = { Text("عنوان MAC الجهاز", fontFamily = CairoFontFamily) },
                        placeholder = { Text("AA:BB:CC:11:22:33") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bindingIp,
                        onValueChange = { bindingIp = it },
                        label = { Text("عنوان IP (اختياري)", fontFamily = CairoFontFamily) },
                        placeholder = { Text("172.16.0.50") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = bindingType == "blocked",
                            onClick = { bindingType = "blocked" },
                            label = { Text("حظر (Blocked)", fontFamily = CairoFontFamily) }
                        )
                        FilterChip(
                            selected = bindingType == "bypassed",
                            onClick = { bindingType = "bypassed" },
                            label = { Text("تجاوز (Bypassed)", fontFamily = CairoFontFamily) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bindingMac.isNotBlank()) {
                            feedbackMessage = "تم إضافة قيد الحظر بنجاح للجهاز $bindingMac"
                            showIpBindingsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إضافة القيد", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpBindingsDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 7. Dialog: متقدم (Advanced Settings) ---
    if (showAdvancedDialog) {
        var radiusPort by remember { mutableStateOf("1812") }
        var radiusAcctPort by remember { mutableStateOf("1813") }
        var sharedSecret by remember { mutableStateOf("123456") }
        var interimUpdate by remember { mutableStateOf("5m") }

        AlertDialog(
            onDismissRequest = { showAdvancedDialog = false },
            title = {
                Text("إعدادات اليوزرمانجر المتقدمة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "إعدادات RADIUS وبروتوكولات الربط الداخلي:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = radiusPort,
                        onValueChange = { radiusPort = it },
                        label = { Text("بورت المصادقة (Auth Port)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = radiusAcctPort,
                        onValueChange = { radiusAcctPort = it },
                        label = { Text("بورت المحاسبة (Acct Port)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = sharedSecret,
                        onValueChange = { sharedSecret = it },
                        label = { Text("المفتاح المشترك (Shared Secret)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = interimUpdate,
                        onValueChange = { interimUpdate = it },
                        label = { Text("فاصل التحديث الدوري (Interim-Update)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        feedbackMessage = "تم حفظ إعدادات اليوزرمانجر المتقدمة بنجاح!"
                        showAdvancedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حفظ الإعدادات", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdvancedDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 8. Dialog: صيانة الكروت المنتهية (Maintenance) ---
    if (showMaintenanceDialog) {
        val expiredCount = userManagerUsers.count { !it.active || it.uptimeUsed == "expired" }

        AlertDialog(
            onDismissRequest = { showMaintenanceDialog = false },
            title = {
                Text("صيانة الكروت المنتهية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تقوم هذه الأداة بفحص وتنظيف الكروت التي انتهت صلاحيتها أو رصيدها، لتسريع قاعدة بيانات الراوتر.",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "تم العثور على $expiredCount كرت منتهي أو غير نشط في قاعدة البيانات.",
                            fontFamily = CairoFontFamily,
                            color = Color(0xFFB45309),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cleanExpiredUserManagerUsers { count ->
                            feedbackMessage = "تمت إزالة وتنظيف $count كرت منتهي بنجاح!"
                            showMaintenanceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("تنظيف الكروت الآن", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMaintenanceDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 9. Dialog: النسخ الاحتياطية (Backups) ---
    if (showBackupDialog) {
        var isGeneratingBackup by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text("النسخ الاحتياطية لليوزرمانجر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "حفظ نسخة احتياطية من كروت وباقات وقواعد بيانات اليوزرمانجر لحمايتها:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Button(
                        onClick = {
                            isGeneratingBackup = true
                            viewModel.backupUserManagerDatabase { filename ->
                                isGeneratingBackup = false
                                feedbackMessage = "تم إنشاء النسخة الاحتياطية: $filename"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                        enabled = !isGeneratingBackup
                    ) {
                        if (isGeneratingBackup) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أخذ نسخة احتياطية جديدة الآن", fontFamily = CairoFontFamily)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- 10. Dialog: إضافة خدمة (Add Service) ---
    if (showAddServiceDialog) {
        var serviceName by remember { mutableStateOf("") }
        var servicePort by remember { mutableStateOf("80") }

        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = {
                Text("إضافة خدمة جديدة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ربط خدمة أو منفذ جديد في لوحة التحكم:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("اسم الخدمة", fontFamily = CairoFontFamily) },
                        placeholder = { Text("مثال: بوابة الدفع، الويب كاش") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = servicePort,
                        onValueChange = { servicePort = it },
                        label = { Text("المنفذ (Port)", fontFamily = CairoFontFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        feedbackMessage = "تم تفعيل الخدمة $serviceName بنجاح!"
                        showAddServiceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إضافة وتفعيل", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServiceDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Power Dialog
    if (showPowerDialog) {
        AlertDialog(
            onDismissRequest = { showPowerDialog = false },
            title = { Text("التحكم بطاقة الراوتر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد إعادة تشغيل الراوتر أم إيقاف تشغيله؟", fontFamily = CairoFontFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        showPowerDialog = false
                        viewModel.rebootSystem(
                            onSuccess = { feedbackMessage = it },
                            onError = { feedbackMessage = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إعادة تشغيل (Reboot)", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPowerDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Exit Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("تسجيل الخروج", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد قطع الاتصال بالراوتر والعودة إلى شاشة الدخول؟", fontFamily = CairoFontFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                ) {
                    Text("تسجيل الخروج", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Feedback SnackBar/Dialog
    feedbackMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null },
            title = { Text("تنبيه النظام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text(msg, fontFamily = CairoFontFamily) },
            confirmButton = {
                Button(
                    onClick = { feedbackMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

@Composable
fun UserManagerCard(
    item: UserManagerMenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = item.gradientColors))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Top: Rounded Square Icon Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bottom: Title
                Text(
                    text = item.title,
                    fontFamily = CairoFontFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
