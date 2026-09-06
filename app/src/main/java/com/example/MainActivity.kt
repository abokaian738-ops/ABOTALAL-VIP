package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppExitConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MikroTikViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as Application
            val viewModel: MikroTikViewModel = viewModel(
                factory = MikroTikViewModel.Factory(application)
            )
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppNavigation(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(viewModel: MikroTikViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val conn by viewModel.currentConnection.collectAsState()

    var showSplash by remember { mutableStateOf(true) }
    // 0 = الرئيسية, 1 = الخدمات, 2 = التقارير, 3 = الملف
    var currentTab by remember { mutableIntStateOf(0) }

    // Dialogs for Header Actions
    var showPowerDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showExitAppDialog by remember { mutableStateOf(false) }
    var showQuickActionDialog by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    // First: Splash Screen
    if (showSplash) {
        SplashScreen(
            onFinished = {
                showSplash = false
            }
        )
        return
    }

    // Enforce Arabic RTL direction by default
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (!isConnected) {
            // Login & Router Selection Screen
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    currentTab = 0 // Navigate to Dashboard
                }
            )
        } else {
            // Back handler when logged in
            BackHandler {
                if (currentTab != 0) {
                    currentTab = 0
                } else {
                    showExitAppDialog = true
                }
            }

            // Main Authenticated Scaffold
            Scaffold(
                topBar = {
                    if (currentTab == 0) {
                        // Top Header Bar for Home/Dashboard with luxurious teal/blue horizontal gradient
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp),
                            color = Color(0xFF063B40)
                        ) {
                            Column(
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right Side (in RTL): Green "متصل •" Badge + Username & IP
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.22f),
                                            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF34D399))
                                                )
                                                Text(
                                                    text = "متصل",
                                                    fontFamily = CairoFontFamily,
                                                    color = Color(0xFFECFDF5),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = conn?.username?.takeIf { it.isNotBlank() } ?: "المدير",
                                                fontFamily = CairoFontFamily,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = conn?.host?.takeIf { it.isNotBlank() } ?: "192.168.88.1",
                                                fontFamily = CairoFontFamily,
                                                color = Color(0xFFE0F2FE),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // Center: App Brand ABO TALAL VIP with VIP Star
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "ABO TALAL VIP",
                                                fontFamily = CairoFontFamily,
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Text(
                                            text = "إدارة وطباعة الكروت",
                                            fontFamily = CairoFontFamily,
                                            color = Color(0xFFBAE6FD),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Left Side: 4 Luxury Action Icons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // 1. Red Power Button (Reboot / Shutdown)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.18f),
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { showPowerDialog = true }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.PowerSettingsNew,
                                                    contentDescription = "طاقة الراوتر",
                                                    tint = Color(0xFFFCA5A5),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        // 2. Exit / Disconnect Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.18f),
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { showExitDialog = true }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.ExitToApp,
                                                    contentDescription = "تسجيل الخروج",
                                                    tint = Color(0xFFFDBA74),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        // 3. Dark / Light Mode Toggle
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.18f),
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.toggleDarkMode() }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.NightlightRound,
                                                    contentDescription = "الوضع الليلي",
                                                    tint = if (isDarkMode) Color(0xFFFDE047) else Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        // 4. Refresh / Sync Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.18f),
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.refreshAllData()
                                                    actionMessage = "تم تحديث كافة بيانات الشبكة والراوتر بنجاح"
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = "مزامنة وتحديث",
                                                    tint = Color(0xFFBAE6FD),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (currentTab == 0) {
                        // Custom Bottom Navigation Bar only shown on the Main Screen
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            color = if (isDarkMode) Color(0xFF1E293B) else Color.White,
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. الرئيسية
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { currentTab = 0 }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkMode) Color(0xFF0C5A60).copy(alpha = 0.6f) else Color(0xFFE0F2FE)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Home,
                                                contentDescription = "الرئيسية",
                                                tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0C5A60),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "الرئيسية",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0C5A60)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7))
                                    )
                                }

                                // 2. الهوتسبوت
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { currentTab = 1 }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Wifi,
                                        contentDescription = "الهوتسبوت",
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "الهوتسبوت",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }

                                // 3. زر إجراء سريع مركزي بتصميم كبسولة متدرجة فاخرة
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showQuickActionDialog = true },
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF063B40), Color(0xFF0C5A60), Color(0xFF0284C7))
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "إجراء سريع",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "إجراء سريع",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // 4. الأدوات
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { currentTab = 6 }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Build,
                                        contentDescription = "الأدوات",
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "الأدوات",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }

                                // 5. الشبكة
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { currentTab = 3 }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Router,
                                        contentDescription = "الشبكة",
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "الشبكة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            navigateToTab = { tabIndex ->
                                currentTab = when (tabIndex) {
                                    1 -> 1 // Services / Hotspot
                                    2 -> 4 // Services / UserManagerScreen
                                    3 -> 2 // Reports / LogScreen
                                    4 -> 3 // Interfaces / Network
                                    5 -> 5 // CardDesignScreen
                                    6 -> 6 // ToolsScreen
                                    else -> 0
                                }
                            }
                        )
                        1 -> HotspotScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = 0 }
                        )
                        2 -> LogScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = 0 }
                        )
                        3 -> InterfacesScreen(
                            viewModel = viewModel,
                            onRouterDisconnected = { viewModel.logout() },
                            onBack = { currentTab = 0 }
                        )
                        4 -> UserManagerScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = 0 }
                        )
                        5 -> CardDesignScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentTab = 0 }
                        )
                        6 -> ToolsScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = 0 }
                        )
                    }
                }
            }
        }
    }

    // Quick Action Dialog (FAB +)
    if (showQuickActionDialog) {
        AlertDialog(
            onDismissRequest = { showQuickActionDialog = false },
            title = { Text("إجراء سريع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showQuickActionDialog = false
                            currentTab = 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.AddCard, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة / توليد كروت هوتسبوت", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showQuickActionDialog = false
                            currentTab = 4
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.People, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نظام اليوزرمانجر والكروت", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showQuickActionDialog = false
                            currentTab = 5
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Palette, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصاميم وهوية الكروت VIP", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showQuickActionDialog = false
                            viewModel.refreshAllData()
                            actionMessage = "تم تحديث كافة بيانات الشبكة والراوتر بنجاح"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مزامنة وتحديث البيانات فوراً", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickActionDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Power Dialog: Reboot or Shutdown Router
    if (showPowerDialog) {
        AlertDialog(
            onDismissRequest = { showPowerDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("التحكم بطاقة الراوتر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "اختر الإجراء الذي ترغب بتنفيذه على الراوتر المتصل (${conn?.host ?: "172.16.0.1"}):",
                        fontFamily = CairoFontFamily
                    )

                    // Reboot Button
                    Button(
                        onClick = {
                            showPowerDialog = false
                            viewModel.rebootSystem(
                                onSuccess = { msg -> actionMessage = msg },
                                onError = { err -> actionMessage = err }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة تشغيل الراوتر (Reboot)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }

                    // Shutdown Button
                    Button(
                        onClick = {
                            showPowerDialog = false
                            viewModel.shutdownSystem(
                                onSuccess = { msg -> actionMessage = msg },
                                onError = { err -> actionMessage = err }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PowerSettingsNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إيقاف تشغيل الراوتر (Shutdown)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPowerDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Exit Dialog: Disconnect and return to Login
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFF0C5A60),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("تسجيل الخروج", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("هل تريد قطع الاتصال بالراوتر والعودة إلى شاشة تسجيل الدخول؟", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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

    // App Exit Confirmation Dialog
    if (showExitAppDialog) {
        AppExitConfirmationDialog(
            onDismiss = { showExitAppDialog = false }
        )
    }

    // Result Notification Dialog
    actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            title = { Text("تنبيه النظام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text(msg, fontFamily = CairoFontFamily) },
            confirmButton = {
                Button(
                    onClick = { actionMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
