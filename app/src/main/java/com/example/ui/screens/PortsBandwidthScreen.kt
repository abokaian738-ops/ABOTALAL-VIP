package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterfaceStats
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortsBandwidthScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val interfaces by viewModel.interfaces.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") } // الكل, إيثرنت, وايرلس, جسور

    // Target for action dialogs
    var actionTargetInterface by remember { mutableStateOf<InterfaceStats?>(null) }
    var showToggleDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isOperating by remember { mutableStateOf(false) }

    // Summary stats
    val totalRxBytes = interfaces.sumOf { it.rxByte }
    val totalTxBytes = interfaces.sumOf { it.txByte }
    val totalRxSpeed = interfaces.sumOf { it.rxSpeedKbps }
    val totalTxSpeed = interfaces.sumOf { it.txSpeedKbps }
    val activeCount = interfaces.count { it.running }

    val filteredList = interfaces.filter { iface ->
        val matchesSearch = searchQuery.isBlank() ||
                iface.name.contains(searchQuery, ignoreCase = true) ||
                iface.type.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "إيثرنت" -> iface.name.contains("ether", ignoreCase = true) || iface.type.contains("ether", ignoreCase = true)
            "وايرلس" -> iface.name.contains("wlan", ignoreCase = true) || iface.type.contains("wireless", ignoreCase = true)
            "جسور" -> iface.name.contains("bridge", ignoreCase = true) || iface.type.contains("bridge", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF034E54),
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
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable { onBack() }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "رجوع",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "عرض المنافذ واستهلاك الإنترنت",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "مراقبة مباشرة للحركة والبيانات والتحكم بكل منفذ",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFBAE6FD)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                    Text(
                                        text = "مباشر 3s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFEF08A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF1F5F9))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Network Throughput Summary Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "إجمالي حركة مرور الشبكة اللحظية",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0C5A60)
                                    )
                                    Text(
                                        text = "المنافذ: $activeCount / ${interfaces.size} نشطة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Total Download Box
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.TrendingDown,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "سرعة التنزيل (Rx)",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0369A1)
                                                )
                                            }
                                            Text(
                                                text = formatSpeed(totalRxSpeed),
                                                fontFamily = CairoFontFamily,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                            Text(
                                                text = "الحجم: ${formatBytes(totalRxBytes)}",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                color = Color(0xFF0284C7)
                                            )
                                        }
                                    }

                                    // Total Upload Box
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF3E8FF),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    tint = Color(0xFF9333EA),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "سرعة الرفع (Tx)",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF7E22CE)
                                                )
                                            }
                                            Text(
                                                text = formatSpeed(totalTxSpeed),
                                                fontFamily = CairoFontFamily,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF7E22CE)
                                            )
                                            Text(
                                                text = "الحجم: ${formatBytes(totalTxBytes)}",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                color = Color(0xFF9333EA)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search & Filter Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("بحث عن اسم المنفذ...", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )

                            listOf("الكل", "إيثرنت", "وايرلس", "جسور").forEach { filterName ->
                                FilterChip(
                                    selected = selectedFilter == filterName,
                                    onClick = { selectedFilter = filterName },
                                    label = { Text(filterName, fontFamily = CairoFontFamily, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Interface Port Cards
                    items(filteredList) { iface ->
                        PortCard(
                            iface = iface,
                            onToggle = {
                                actionTargetInterface = iface
                                showToggleDialog = true
                            },
                            onResetCounters = {
                                actionTargetInterface = iface
                                showResetDialog = true
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Toggle Port Dialog
    if (showToggleDialog && actionTargetInterface != null) {
        val target = actionTargetInterface!!
        val actionText = if (target.running) "تعطيل المنفذ" else "تشغيل المنفذ"
        AlertDialog(
            onDismissRequest = { showToggleDialog = false },
            title = { Text("$actionText (${target.name})", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (target.running) {
                        "هل أنت متأكد من رغبتك في تعطيل المنفذ (${target.name})؟ سيؤدي ذلك لقطع الاتصال عن الكيبل أو الأجهزة المتصلة به."
                    } else {
                        "هل تريد تفعيل وتشغيل المنفذ (${target.name}) الآن؟"
                    },
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperating = true
                        viewModel.toggleInterface(
                            interfaceName = target.name,
                            enable = !target.running,
                            onSuccess = {
                                isOperating = false
                                showToggleDialog = false
                                Toast.makeText(context, "تم تنفيذ الأمر على المنفذ بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                isOperating = false
                                Toast.makeText(context, "فشل: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target.running) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                ) {
                    Text(if (target.running) "نعم، عطل المنفذ" else "نعم، شغل المنفذ", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Reset Counters Dialog
    if (showResetDialog && actionTargetInterface != null) {
        val target = actionTargetInterface!!
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("إعادة ضبط عدادات المنفذ (${target.name})", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "سيتم تصفير حجم استهلاك الإنترنت المسجل على هذا المنفذ (Rx/Tx) وإعادة العدادات إلى 0 بايت.",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperating = true
                        viewModel.resetInterfaceCounters(
                            interfaceName = target.name,
                            onSuccess = {
                                isOperating = false
                                showResetDialog = false
                                Toast.makeText(context, "تم تصفير عدادات المنفذ بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                isOperating = false
                                Toast.makeText(context, "فشل: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("تصفير العدادات الآن", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

@Composable
private fun PortCard(
    iface: InterfaceStats,
    onToggle: () -> Unit,
    onResetCounters: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (iface.running) Color(0xFFE0F2FE) else Color(0xFFFEE2E2),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (iface.name.contains("wlan")) Icons.Filled.Wifi else Icons.Filled.Router,
                                contentDescription = null,
                                tint = if (iface.running) Color(0xFF0284C7) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = iface.name,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "نوع الواجهة: ${iface.type}",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (iface.running) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (iface.running) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                        )
                        Text(
                            text = if (iface.running) "متصل وفعال" else "معطل / مفصول",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (iface.running) Color(0xFF15803D) else Color(0xFFB91C1C)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Speeds & Data Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rx Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                            Text("تنزيل (Rx)", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF0284C7))
                        }
                        Text(
                            text = formatSpeed(iface.rxSpeedKbps),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "الحجم: ${formatBytes(iface.rxByte)}",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Tx Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                            Text("رفع (Tx)", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF9333EA))
                        }
                        Text(
                            text = formatSpeed(iface.txSpeedKbps),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "الحجم: ${formatBytes(iface.txByte)}",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Total Port Traffic Progress Indicator
            val totalPortBytes = iface.rxByte + iface.txByte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إجمالي البيانات المستهلكة:",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = formatBytes(totalPortBytes),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0C5A60)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (iface.running) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                ) {
                    Icon(
                        imageVector = if (iface.running) Icons.Filled.PowerSettingsNew else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (iface.running) "تعطيل المنفذ" else "تشغيل المنفذ", fontFamily = CairoFontFamily, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onResetCounters,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصفير العداد", fontFamily = CairoFontFamily, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun formatSpeed(kbps: Double): String {
    return if (kbps >= 1024.0) {
        String.format(Locale.US, "%.1f Mbps", kbps / 1024.0)
    } else {
        String.format(Locale.US, "%.0f Kbps", kbps)
    }
}

private fun formatBytes(bytes: Long): String {
    val b = bytes.toDouble()
    return when {
        b >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", b / (1024 * 1024 * 1024))
        b >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", b / (1024 * 1024))
        b >= 1024 -> String.format(Locale.US, "%.0f KB", b / 1024)
        else -> "$bytes B"
    }
}
