package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: MikroTikViewModel,
    navigateToTab: (Int) -> Unit
) {
    val stats by viewModel.routerStats.collectAsState()
    val conn by viewModel.currentConnection.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val hotspotUsers by viewModel.hotspotUsers.collectAsState()
    val userManagerUsers by viewModel.userManagerUsers.collectAsState()
    val activeHotspot by viewModel.activeHotspot.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    // Colors based on theme
    val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Dialog states for services
    var showExpensesDialog by remember { mutableStateOf(false) }
    var showCardsDesignDialog by remember { mutableStateOf(false) }
    var showActiveUsersDialog by remember { mutableStateOf(false) }
    var showFinancialReportDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRecentActivitiesDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showPrintingSystemDialog by remember { mutableStateOf(false) }

    // Refresh animation rotation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_spin"
    )

    fun doRefresh() {
        coroutineScope.launch {
            isRefreshing = true
            viewModel.refreshAllData()
            delay(800)
            isRefreshing = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. شريط السحب للتحديث
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { doRefresh() },
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(cardBorderColor, cardBorderColor))
                ),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "تحديث",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRefreshing) "جارٍ التحديث الآن..." else "اسحب للأسفل للتحديث",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }
        }

        // 2. بطاقة معلومات الراوتر (Dark Teal Router Info Card - مطابقة تماماً للشاشة)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A5860))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0C5A60),
                                    Color(0xFF0A4E53),
                                    Color(0xFF07383C)
                                )
                            )
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // السطر العلوي للبطاقة: عنوان الـ IP وزر التحديث
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // اليمين: أيقونة الراوتر + الـ IP
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Router,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = conn?.host?.ifEmpty { "172.16.0.1" } ?: "172.16.0.1",
                                fontFamily = CairoFontFamily,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // اليسار: زر التحديث الدائري
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { doRefresh() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "تحديث الراوتر",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                                )
                            }
                        }
                    }

                    // شبكة تفاصيل الراوتر (3 صفوف × عمودين كبسولية)
                    // الصف 1: مدة التشغيل | اصدار الراوتر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Schedule,
                            label = "مدة التشغيل",
                            value = stats?.uptime?.ifEmpty { "1d18h59m52s" } ?: "1d18h59m52s"
                        )
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Settings,
                            label = "اصدار الراوتر",
                            value = "(stable) ${stats?.version?.ifEmpty { "6.49.19" } ?: "6.49.19"}"
                        )
                    }

                    // الصف 2: المعالج | الحرارة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Memory,
                            label = "المعالج",
                            value = "${stats?.cpuUsage ?: 30}%"
                        )
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.DeviceThermostat,
                            label = "الحرارة",
                            value = "-"
                        )
                    }

                    // الصف 3: الذاكرة | المستخدمون النشطون
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.FormatListBulleted,
                            label = "الذاكرة",
                            value = "MB / 128.0 MB 48.0",
                            hasProgressBar = true,
                            progressFraction = 48f / 128f
                        )
                        RouterDetailPill(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Group,
                            label = "المستخدمون النشطون",
                            value = "${activeHotspot.size}"
                        )
                    }
                }
            }
        }

        // 3. شبكة إحصائيات الكروت (4 بطاقات 2 × 2 Grid - مطابقة تماماً للشاشة)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // الصف العلوي من الكروت: إجمالي البيع | الكروت المولدة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // كرت إجمالي البيع (يمين)
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي البيع",
                        icon = Icons.Filled.PointOfSale,
                        iconTint = Color(0xFF0284C7),
                        iconBackground = Color(0xFFE0F2FE),
                        value = "-",
                        footer = "نظام اليوزرمانجر",
                        isDarkMode = isDarkMode,
                        onClick = { showFinancialReportDialog = true }
                    )

                    // كرت الكروت المولدة (يسار)
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        title = "الكروت المولدة",
                        icon = Icons.Filled.CreditCard,
                        iconTint = Color(0xFFEA580C),
                        iconBackground = Color(0xFFFFEDD5),
                        value = "${userManagerUsers.size}",
                        footer = "الكروت خلال 30 يوماً: ${userManagerUsers.size}",
                        isDarkMode = isDarkMode,
                        onClick = { navigateToTab(2) }
                    )
                }

                // الصف السفلي من الكروت: الكروت المباعة | الكروت المتبقية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // كرت الكروت المباعة (يمين)
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        title = "الكروت المباعة",
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        iconBackground = Color(0xFFD1FAE5),
                        value = "-",
                        footer = "كروت يوزرمنجر: 0",
                        isDarkMode = isDarkMode,
                        onClick = { navigateToTab(2) }
                    )

                    // كرت الكروت المتبقية (يسار)
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        title = "الكروت المتبقية",
                        icon = Icons.Filled.Article,
                        iconTint = Color(0xFF0284C7),
                        iconBackground = Color(0xFFE0F2FE),
                        value = "-",
                        footer = "كروت الهوتسبوت: ${hotspotUsers.size}",
                        isDarkMode = isDarkMode,
                        onClick = { navigateToTab(1) }
                    )
                }
            }
        }

        // 4. شريط نظام الطباعة ABO TALAL VIP
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showPrintingSystemDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFEF3C7),
                border = if (isDarkMode) BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // اليمين: مكبر الصوت مع النص
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDarkMode) Color(0xFFD97706).copy(alpha = 0.25f) else Color(0xFFFDE68A),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "معا نظام الطباعة ABO TALAL VIP",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // اليسار: زر عرض بني برتقالي
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFB45309), // Brown/Deep Amber
                        modifier = Modifier.clickable { showPrintingSystemDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "عرض",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "عرض",
                                fontFamily = CairoFontFamily,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. عنوان قسم الخدمات
        item {
            Text(
                text = "الخدمات",
                fontFamily = CairoFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textPrimary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // 6. شبكة أزرار الخدمات
        // الصف الأول من الخدمات: نظام اليوزرمانجر | نظام الهوتسبوت | المصروفات
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // نظام اليوزرمانجر (يمين - أزرق فاتح)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "نظام اليوزرمانجر",
                    icon = Icons.Filled.People,
                    backgroundColor = Color(0xFFBAE6FD),
                    contentColor = Color(0xFF0284C7),
                    isDarkMode = isDarkMode,
                    onClick = { navigateToTab(2) }
                )

                // نظام الهوتسبوت (وسط - تركواز فاتح)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "نظام الهوتسبوت",
                    icon = Icons.Filled.Wifi,
                    backgroundColor = Color(0xFF99F6E4),
                    contentColor = Color(0xFF0D9488),
                    isDarkMode = isDarkMode,
                    onClick = { navigateToTab(1) }
                )

                // المصروفات (يسار - وردي فاتح)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "المصروفات",
                    icon = Icons.Filled.ReceiptLong,
                    backgroundColor = Color(0xFFFECDD3),
                    contentColor = Color(0xFFE11D48),
                    isDarkMode = isDarkMode,
                    onClick = { showExpensesDialog = true }
                )
            }
        }

        // الصف الثاني من الخدمات: تصاميم الكروت | الاكتف والهوتسبوت | الحسابات المالية
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // تصاميم الكروت (يمين)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "تصاميم الكروت",
                    icon = Icons.Filled.Palette,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF10B981),
                    hasBorder = true,
                    isDarkMode = isDarkMode,
                    onClick = { navigateToTab(5) }
                )

                // الاكتف والهوتسبوت (وسط)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "الاكتف والهوتسبوت",
                    icon = Icons.Filled.WifiTethering,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFFEA580C),
                    hasBorder = true,
                    isDarkMode = isDarkMode,
                    onClick = { showActiveUsersDialog = true }
                )

                // الحسابات المالية (يسار)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "الحسابات المالية",
                    icon = Icons.Filled.AccountBalanceWallet,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFFD97706),
                    hasBorder = true,
                    isDarkMode = isDarkMode,
                    onClick = { showFinancialReportDialog = true }
                )
            }
        }

        // الصف الثالث من الخدمات: الملف الشخصي | النسخ الاحتياطية | الأنشطة الحديثة
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // الملف الشخصي (يمين)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "الملف الشخصي",
                    icon = Icons.Filled.Person,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF0EA5E9),
                    hasBorder = true,
                    isDarkMode = isDarkMode,
                    onClick = { showProfileDialog = true }
                )

                // النسخ الاحتياطية (وسط - بيج فاتح)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "النسخ الاحتياطية",
                    icon = Icons.Filled.Save,
                    backgroundColor = Color(0xFFFEF3C7),
                    contentColor = Color(0xFFD97706),
                    isDarkMode = isDarkMode,
                    onClick = { showBackupDialog = true }
                )

                // الأنشطة الحديثة (يسار - تركواز مائي فاتح)
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "الأنشطة الحديثة",
                    icon = Icons.Filled.History,
                    backgroundColor = Color(0xFFCCFBF1),
                    contentColor = Color(0xFF0D9488),
                    isDarkMode = isDarkMode,
                    onClick = { showRecentActivitiesDialog = true }
                )
            }
        }

        // الصف الرابع من الخدمات: الإرشاد والمساعدة | حول التطبيق (عنصران عريضان)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // الإرشاد والمساعدة (يمين - أخضر فاتح ناعم)
                WideServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "الإرشاد والمساعدة",
                    icon = Icons.Filled.HeadsetMic,
                    backgroundColor = Color(0xFFD1FAE5),
                    contentColor = Color(0xFF059669),
                    isDarkMode = isDarkMode,
                    onClick = { showSupportDialog = true }
                )

                // حول التطبيق (يسار - أزرق فاتح ناعم)
                WideServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "حول التطبيق",
                    icon = Icons.Filled.Info,
                    backgroundColor = Color(0xFFE0F2FE),
                    contentColor = Color(0xFF0284C7),
                    isDarkMode = isDarkMode,
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    // --- Dialogs ---
    if (showPrintingSystemDialog) {
        AlertDialog(
            onDismissRequest = { showPrintingSystemDialog = false },
            title = { Text("نظام الطباعة ABO TALAL VIP", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "يتيح نظام الطباعة ABO TALAL VIP تصميم وطباعة كروت الهوتسبوت واليوزرمانجر بتنسيقات متعددة وقوالب جاهزة للطباعة الحرارية ومقاس A4.",
                    fontFamily = CairoFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrintingSystemDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showExpensesDialog) {
        AlertDialog(
            onDismissRequest = { showExpensesDialog = false },
            title = { Text("سجل المصروفات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("لا توجد حركات مصروفات مسجلة حالياً.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showExpensesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showCardsDesignDialog) {
        AlertDialog(
            onDismissRequest = { showCardsDesignDialog = false },
            title = { Text("تصاميم الكروت", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("نماذج كروت الهوتسبوت جاهزة للطباعة والمزامنة مع الراوتر.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showCardsDesignDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showActiveUsersDialog) {
        AlertDialog(
            onDismissRequest = { showActiveUsersDialog = false },
            title = { Text("المستخدمون النشطون (Active)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("المتصلون النشطون الآن: ${activeHotspot.size} مستخدم.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showActiveUsersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showFinancialReportDialog) {
        AlertDialog(
            onDismissRequest = { showFinancialReportDialog = false },
            title = { Text("الحسابات المالية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("إجمالي المبيعات المحصلة لكروت النظام: 0 ريال.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showFinancialReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("الملف الشخصي", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("المستخدم: ${conn?.username ?: "abdulhamid"}", fontFamily = CairoFontFamily)
                    Text("الراوتر: ${conn?.name ?: "172.16.0.1"}", fontFamily = CairoFontFamily)
                    Text("الصلاحية: مدير النظام (Full Admin)", fontFamily = CairoFontFamily)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProfileDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("النسخ الاحتياطية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("يمكنك أخذ نسخة احتياطية من بيانات الراوتر أو استعادتها.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showBackupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showRecentActivitiesDialog) {
        AlertDialog(
            onDismissRequest = { showRecentActivitiesDialog = false },
            title = { Text("الأنشطة الحديثة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text("عدد السجلات المحفوظة: ${logs.size} عملية.", fontFamily = CairoFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = { showRecentActivitiesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("الإرشاد والمساعدة والدعم الفني", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("برمجة وتطوير: المهندس عبدالحميد داوؤد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    Text("هاتف التواصل: 778215553", fontFamily = CairoFontFamily)
                    Text("فريق الدعم الفني جاهز لمساعدتكم في أي استفسارات أو دعم للشبكة.", fontFamily = CairoFontFamily)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("حول التطبيق", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("نظام ABO TALAL VIP", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    Text("الإصدار: v1.0.1", fontFamily = CairoFontFamily)
                    Text("تطوير: المهندس عبدالحميد داوؤد (778215553)", fontFamily = CairoFontFamily)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// مكونات واجهة المستخدم المخصصة المطابقة لقطة الشاشة
// -------------------------------------------------------------

@Composable
fun RouterDetailPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    hasProgressBar: Boolean = false,
    progressFraction: Float = 0f
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.22f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF99F6E4),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = label,
                        fontFamily = CairoFontFamily,
                        color = Color(0xFFCCFBF1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = value,
                    fontFamily = CairoFontFamily,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasProgressBar) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Color(0xFF38BDF8))
                    )
                }
            }
        }
    }
}

@Composable
fun StatCardItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    value: String,
    footer: String,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val titleColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val valueColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val footerColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF334155)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) iconTint.copy(alpha = 0.2f) else iconBackground,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = value,
                fontFamily = CairoFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )

            Text(
                text = footer,
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = footerColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    hasBorder: Boolean = false,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    val actualBg = if (isDarkMode) {
        Color(0xFF1E293B)
    } else {
        backgroundColor
    }
    val titleColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Surface(
        modifier = modifier
            .height(100.dp)
            .shadow(elevation = if (hasBorder) 1.dp else 2.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = actualBg,
        border = if (isDarkMode) {
            BorderStroke(1.dp, Color(0xFF334155))
        } else if (hasBorder) {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9)))
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = if (isDarkMode) 0.22f else 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WideServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    val actualBg = if (isDarkMode) Color(0xFF1E293B) else backgroundColor
    val titleColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Surface(
        modifier = modifier
            .height(68.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = actualBg,
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF334155)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = if (isDarkMode) 0.22f else 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
        }
    }
}
