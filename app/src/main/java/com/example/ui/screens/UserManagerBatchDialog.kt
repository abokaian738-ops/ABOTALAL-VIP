package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.UserManagerProfile
import com.example.data.model.UserManagerUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import com.example.util.CardPrintAndExportHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagerBatchFullDialog(
    viewModel: MikroTikViewModel,
    batchRecords: List<GeneratedBatchRecord>,
    onUpdateBatches: (List<GeneratedBatchRecord>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE) }
    val userManagerProfiles by viewModel.userManagerProfiles.collectAsState()
    val userManagerUsers by viewModel.userManagerUsers.collectAsState()

    // Top Tabs: 0 -> إنشاء دفعة (Create Batch), 1 -> استيراد دفعة (Import Batch)
    var selectedTopTab by remember { mutableIntStateOf(0) }

    // Section 1: خيارات الباقة
    var selectedProfileName by remember {
        mutableStateOf(
            if (userManagerProfiles.isNotEmpty()) userManagerProfiles.first().name else "باقة 500 د.ع"
        )
    }
    var profileDropdownExpanded by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var newProfileNameInput by remember { mutableStateOf("") }
    var newProfilePriceInput by remember { mutableStateOf("500") }

    // POS / نقطة البيع
    var posList by remember {
        mutableStateOf(
            listOf("بدون نقطة بيع", "المركز الرئيسي", "كشك رقم 1", "متجر الأمل", "بقالة النور", "نقطة بيع رقم 2")
        )
    }
    var selectedPos by remember { mutableStateOf("بدون نقطة بيع") }
    var posDropdownExpanded by remember { mutableStateOf(false) }
    var showAddPosDialog by remember { mutableStateOf(false) }
    var newPosInput by remember { mutableStateOf("") }
    var showPosOnCard by remember { mutableStateOf(false) }

    // Template Binding
    var linkedTemplateName by remember {
        mutableStateOf(sharedPrefs.getString("${selectedProfileName}_template", "كروت_فئة_100") ?: "كروت_فئة_100")
    }
    var showLinkTemplateDialog by remember { mutableStateOf(false) }

    // Section 2: مواصفات الكروت
    val cardTypeOptions = listOf("كلمة سر فارغة", "اسم المستخدم = كلمة السر", "كلمة سر عشوائية منفصلة", "أرقام وحروف مدمجة")
    var selectedCardType by remember { mutableStateOf("كلمة سر فارغة") }
    var cardTypeDropdownExpanded by remember { mutableStateOf(false) }

    val namePatternOptions = listOf("ارقام فقط", "احرف وارقام", "احرف صغيرة فقط", "احرف كبيرة فقط")
    var selectedNamePattern by remember { mutableStateOf("ارقام فقط") }
    var namePatternDropdownExpanded by remember { mutableStateOf(false) }
    var nameLengthInput by remember { mutableStateOf("9") }

    // Page Calculation
    val templateCols = remember(linkedTemplateName) { sharedPrefs.getInt("${linkedTemplateName}_columns", 3).coerceAtLeast(1) }
    val templateRows = remember(linkedTemplateName) { sharedPrefs.getInt("${linkedTemplateName}_rows", 17).coerceAtLeast(1) }
    val cardsPerPage = remember(templateCols, templateRows) { templateCols * templateRows }

    var pageCountInput by remember { mutableStateOf("1") }
    var cardCountInput by remember { mutableStateOf("51") }
    var cardPrefixInput by remember { mutableStateOf("") }
    var cardSuffixInput by remember { mutableStateOf("") }

    var excludeCharEnabled by remember { mutableStateOf(false) }
    var excludedCharInput by remember { mutableStateOf("0") }

    // UI Feedback & Dialogs
    var isGenerating by remember { mutableStateOf(false) }
    var successNotification by remember { mutableStateOf<String?>(null) }
    var previewBatchForPrint by remember { mutableStateOf<GeneratedBatchRecord?>(null) }
    var previewBatchUsers by remember { mutableStateOf<List<UserManagerUser>>(emptyList()) }
    var showInAppPrintPreview by remember { mutableStateOf(false) }

    // Import Batch State
    var importRawText by remember { mutableStateOf("") }

    // Update template link when profile changes
    LaunchedEffect(selectedProfileName) {
        linkedTemplateName = sharedPrefs.getString("${selectedProfileName}_template", "كروت_فئة_100") ?: "كروت_فئة_100"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F8)),
                color = Color(0xFFF0F4F8)
            ) {
                Scaffold(
                    topBar = {
                        // Top Luxury Header with ABO TALAL TK Branding
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right: Back Button & Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE2E8F0),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onDismiss() }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "الرجوع",
                                                    tint = Color(0xFF0C5A60),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "إضافة وتوليد كروت يوزرمانجر",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF0C5A60)
                                        )
                                    }

                                    // Left: System Brand Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = "ABO TALAL VIP",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0284C7),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Top Tabs Bar: [إنشاء دفعة | استيراد دفعة] - Matching Screenshot
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Tab 1: إنشاء دفعة
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedTopTab = 0 }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AddCard,
                                                contentDescription = null,
                                                tint = if (selectedTopTab == 0) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "إنشاء دفعة",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (selectedTopTab == 0) Color(0xFF0C5A60) else Color(0xFF94A3B8)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (selectedTopTab == 0) Color(0xFF0C5A60) else Color.Transparent
                                                )
                                        )
                                    }

                                    // Tab 2: استيراد دفعة
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedTopTab = 1 }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.UploadFile,
                                                contentDescription = null,
                                                tint = if (selectedTopTab == 1) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "استيراد دفعة",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (selectedTopTab == 1) Color(0xFF0C5A60) else Color(0xFF94A3B8)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (selectedTopTab == 1) Color(0xFF0C5A60) else Color.Transparent
                                                )
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            }
                        }
                    },
                    containerColor = Color(0xFFF8FAFC)
                ) { innerPadding ->
                    if (selectedTopTab == 0) {
                        // --- TAB 1: إنشاء دفعة (Create Batch UI strictly matching Screenshots) ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Success Notification Banner
                            if (successNotification != null) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF15803D),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = successNotification!!,
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                            IconButton(
                                                onClick = { successNotification = null },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. خيارات الباقة Container Card (Matching Screenshot 1)
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Card Header: خيارات الباقة
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Tune,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "خيارات الباقة",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F5F9))

                                        // باقات اليوزارمنجر (Selector with EQ Icon & '+' button)
                                        Text(
                                            text = "باقات اليوزارمنجر",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // EQ Icon Box
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF1F5F9),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Equalizer,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0C5A60),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Dropdown Button
                                            Box(modifier = Modifier.weight(1f)) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(46.dp)
                                                        .clickable { profileDropdownExpanded = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = selectedProfileName,
                                                            fontFamily = CairoFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Filled.UnfoldMore,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B)
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = profileDropdownExpanded,
                                                    onDismissRequest = { profileDropdownExpanded = false }
                                                ) {
                                                    if (userManagerProfiles.isEmpty()) {
                                                        listOf("باقة 100 د.ع", "باقة 200 د.ع", "باقة 500 د.ع", "باقة 1000 د.ع", "باقة VIP غير محدودة").forEach { prof ->
                                                            DropdownMenuItem(
                                                                text = { Text(prof, fontFamily = CairoFontFamily) },
                                                                onClick = {
                                                                    selectedProfileName = prof
                                                                    profileDropdownExpanded = false
                                                                }
                                                            )
                                                        }
                                                    } else {
                                                        userManagerProfiles.forEach { prof ->
                                                            DropdownMenuItem(
                                                                text = { Text(prof.name, fontFamily = CairoFontFamily) },
                                                                onClick = {
                                                                    selectedProfileName = prof.name
                                                                    profileDropdownExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Add New Profile '+' Button
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFF0C5A60)),
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clickable { showAddProfileDialog = true }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        contentDescription = "إضافة باقة",
                                                        tint = Color(0xFF0C5A60),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // قالب الطباعة Field
                                        Text(
                                            text = "قالب الطباعة",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp)
                                                .clickable { showLinkTemplateDialog = true }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = linkedTemplateName.ifEmpty { "-" },
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (linkedTemplateName.isNotEmpty()) Color(0xFF0C5A60) else Color(0xFF94A3B8)
                                                )
                                                Text(
                                                    text = "تغيير القالب",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0284C7),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // نقطة البيع (POS Selector)
                                        Text(
                                            text = "نقطة البيع",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Store Icon Box
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF1F5F9),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Storefront,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0C5A60),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Dropdown Button
                                            Box(modifier = Modifier.weight(1f)) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(46.dp)
                                                        .clickable { posDropdownExpanded = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = selectedPos,
                                                            fontFamily = CairoFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Filled.UnfoldMore,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B)
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = posDropdownExpanded,
                                                    onDismissRequest = { posDropdownExpanded = false }
                                                ) {
                                                    posList.forEach { pos ->
                                                        DropdownMenuItem(
                                                            text = { Text(pos, fontFamily = CairoFontFamily) },
                                                            onClick = {
                                                                selectedPos = pos
                                                                posDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            // Add New POS '+' Button
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFF0C5A60)),
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clickable { showAddPosDialog = true }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        contentDescription = "إضافة نقطة بيع",
                                                        tint = Color(0xFF0C5A60),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Checkbox: إظهار نقطة البيع على الكرت
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showPosOnCard = !showPosOnCard }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "إظهار نقطة البيع على الكرت",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Checkbox(
                                                checked = showPosOnCard,
                                                onCheckedChange = { showPosOnCard = it },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. مواصفات الكرت والتوليد Container Card (Matching Screenshot 1 & 2)
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // صنف الكرت (Card Password Type)
                                        Text(
                                            text = "صنف الكرت",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Password Key Icon Box
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF1F5F9),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Password,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0C5A60),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Dropdown Button
                                            Box(modifier = Modifier.weight(1f)) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(46.dp)
                                                        .clickable { cardTypeDropdownExpanded = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = selectedCardType,
                                                            fontFamily = CairoFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Filled.UnfoldMore,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B)
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = cardTypeDropdownExpanded,
                                                    onDismissRequest = { cardTypeDropdownExpanded = false }
                                                ) {
                                                    cardTypeOptions.forEach { type ->
                                                        DropdownMenuItem(
                                                            text = { Text(type, fontFamily = CairoFontFamily) },
                                                            onClick = {
                                                                selectedCardType = type
                                                                cardTypeDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // نمط الاسم و عدد (Row with Name Pattern & Username Length)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // نمط الاسم (Pattern Selector)
                                            Column(modifier = Modifier.weight(0.68f)) {
                                                Text(
                                                    text = "نمط الاسم",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(46.dp)
                                                            .clickable { namePatternDropdownExpanded = true }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(horizontal = 12.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = selectedNamePattern,
                                                                fontFamily = CairoFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = Color(0xFF0F172A)
                                                            )
                                                            Icon(
                                                                imageVector = Icons.Filled.ArrowDropDown,
                                                                contentDescription = null,
                                                                tint = Color(0xFF64748B)
                                                            )
                                                        }
                                                    }

                                                    DropdownMenu(
                                                        expanded = namePatternDropdownExpanded,
                                                        onDismissRequest = { namePatternDropdownExpanded = false }
                                                    ) {
                                                        namePatternOptions.forEach { pat ->
                                                            DropdownMenuItem(
                                                                text = { Text(pat, fontFamily = CairoFontFamily) },
                                                                onClick = {
                                                                    selectedNamePattern = pat
                                                                    namePatternDropdownExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // عدد (Length)
                                            Column(modifier = Modifier.weight(0.32f)) {
                                                Text(
                                                    text = "عدد",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                OutlinedTextField(
                                                    value = nameLengthInput,
                                                    onValueChange = { nameLengthInput = it },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(52.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    textStyle = LocalTextStyle.current.copy(
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                )
                                            }
                                        }

                                        // عدد الصفحات (Pages Count)
                                        Text(
                                            text = "عدد الصفحات",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                        OutlinedTextField(
                                            value = pageCountInput,
                                            onValueChange = {
                                                pageCountInput = it
                                                val p = it.toIntOrNull() ?: 1
                                                cardCountInput = "${p * cardsPerPage}"
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            placeholder = { Text("1 صفحة A4 ($cardsPerPage كرت)", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                                        )

                                        // عدد الكروت (Cards Count)
                                        Text(
                                            text = "عدد الكروت",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                        OutlinedTextField(
                                            value = cardCountInput,
                                            onValueChange = {
                                                cardCountInput = it
                                                val c = it.toIntOrNull() ?: 51
                                                pageCountInput = "${Math.ceil(c.toDouble() / cardsPerPage).toInt().coerceAtLeast(1)}"
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            placeholder = { Text("مثلاً 51 أو 650", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                                        )

                                        // نهاية الكرت (Suffix)
                                        Text(
                                            text = "نهاية الكرت",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                        OutlinedTextField(
                                            value = cardSuffixInput,
                                            onValueChange = { cardSuffixInput = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            placeholder = { Text("نهاية اختيارية (مثلاً 99)", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                                        )

                                        // بداية الكرت (Prefix)
                                        Text(
                                            text = "بداية الكرت",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                        OutlinedTextField(
                                            value = cardPrefixInput,
                                            onValueChange = { cardPrefixInput = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            placeholder = { Text("بادئة اختيارية (مثلاً TK أو 77)", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                                        )

                                        // استبعاد حرف/رقم (Exclude Ambiguous Characters)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { excludeCharEnabled = !excludeCharEnabled }
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "استبعاد حرف/رقم",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Checkbox(
                                                checked = excludeCharEnabled,
                                                onCheckedChange = { excludeCharEnabled = it },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                                            )
                                        }

                                        if (excludeCharEnabled) {
                                            OutlinedTextField(
                                                value = excludedCharInput,
                                                onValueChange = { excludedCharInput = it },
                                                singleLine = true,
                                                placeholder = { Text("رقم واحد أو حرف واحد (مثال: 0 أو O أو 1)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Main Action Button: [ [+] اضافة ] (Matching Screenshots)
                            item {
                                Button(
                                    onClick = {
                                        val count = cardCountInput.toIntOrNull() ?: 51
                                        val len = nameLengthInput.toIntOrNull() ?: 9
                                        val price = 500 // or derived from profile
                                        val excluded = if (excludeCharEnabled) excludedCharInput.toSet() else emptySet()

                                        isGenerating = true

                                        // Generate Users
                                        val generatedUsers = (1..count).map {
                                            val uname = generateCardCode(
                                                prefix = cardPrefixInput,
                                                suffix = cardSuffixInput,
                                                length = len,
                                                pattern = selectedNamePattern,
                                                excludedChars = excluded
                                            )

                                            val pwd = when (selectedCardType) {
                                                "كلمة سر فارغة" -> ""
                                                "اسم المستخدم = كلمة السر" -> uname
                                                "أرقام وحروف مدمجة" -> generateRandomAlphaNumeric(6, excluded)
                                                else -> Random.nextInt(1000, 9999).toString()
                                            }

                                            UserManagerUser(
                                                username = uname,
                                                password = pwd,
                                                profile = selectedProfileName,
                                                active = true,
                                                uptimeUsed = "0s",
                                                downloadLimit = "unlimited"
                                            )
                                        }

                                        val batchCode = if (cardPrefixInput.isNotEmpty()) "${cardPrefixInput}-x$count" else "BATCH-${System.currentTimeMillis() % 10000}"

                                        // Send to router via ViewModel
                                        viewModel.batchAddUserManagerUsers(
                                            users = generatedUsers,
                                            batchName = batchCode,
                                            onSuccess = {
                                                val newRecord = GeneratedBatchRecord(
                                                    batchId = batchCode,
                                                    date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                                                    profileName = selectedProfileName,
                                                    count = count,
                                                    pricePerCard = price,
                                                    prefix = cardPrefixInput
                                                )
                                                onUpdateBatches(listOf(newRecord) + batchRecords)
                                                isGenerating = false
                                                successNotification = "تم توليد وإضافة $count كرت بنجاح في راوتر المايكروتيك!"
                                                previewBatchForPrint = newRecord
                                                previewBatchUsers = generatedUsers
                                            },
                                            onError = { err ->
                                                isGenerating = false
                                                Toast.makeText(context, "خطأ في التوليد: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isGenerating
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جاري توليد الكروت في الراوتر...", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Filled.AddCard, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("اضافة +", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    }
                                }
                            }

                            // 4. قالب الطباعة Status Card (Matching Screenshot 2)
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Visibility,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "قالب الطباعة",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F5F9))

                                        if (linkedTemplateName.isEmpty() || linkedTemplateName == "-") {
                                            // Warning Banner matching screenshot
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.WarningAmber,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "لا يمكنك المتابعة. الباقة \"$selectedProfileName\" غير مرتبطة بقالب طباعة.",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B),
                                                    lineHeight = 18.sp
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { showLinkTemplateDialog = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, Color(0xFF0284C7))
                                            ) {
                                                Text("ربط الباقة الآن", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                            }
                                        } else {
                                            // Linked Template Active Status
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF0FDF4),
                                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                                        Column {
                                                            Text(
                                                                text = "قالب الطباعة المرتبط: $linkedTemplateName",
                                                                fontFamily = CairoFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF166534)
                                                            )
                                                            Text(
                                                                text = "$templateCols أعمدة × $templateRows صفوف ($cardsPerPage كرت في الورقة)",
                                                                fontFamily = CairoFontFamily,
                                                                fontSize = 11.sp,
                                                                color = Color(0xFF15803D)
                                                            )
                                                        }
                                                    }

                                                    TextButton(onClick = { showLinkTemplateDialog = true }) {
                                                        Text("تغيير", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF0C5A60))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. آخر دفعات يوزارمنجر Table Card (Matching Screenshot 2)
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.History,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "آخر دفعات يوزارمنجر",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }

                                            Text(
                                                text = "${batchRecords.size} دفعات",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        // Table Header (Matching Screenshot: الحالة | الباقة | التاريخ | العدد | الدفعة)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF0C5A60),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "الدفعة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                                                Text(text = "العدد", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center)
                                                Text(text = "التاريخ", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.28f), textAlign = TextAlign.Center)
                                                Text(text = "الباقة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.22f), textAlign = TextAlign.Center)
                                                Text(text = "الحالة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center)
                                            }
                                        }

                                        // Table Rows
                                        if (batchRecords.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("لا توجد دفعات مسجلة حالياً", fontFamily = CairoFontFamily, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                            }
                                        } else {
                                            batchRecords.forEach { batch ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFF8FAFC),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(text = batch.batchId, fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                                                            Text(text = "${batch.count}", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center)
                                                            Text(text = batch.date.substringBefore(" "), fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.28f), textAlign = TextAlign.Center)
                                                            Text(text = batch.profileName, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.22f), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFFDCFCE7),
                                                                modifier = Modifier.weight(0.15f)
                                                            ) {
                                                                Text(text = "جاهز", fontFamily = CairoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 2.dp))
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        // Action Row for each batch
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            // 1. In-App Print Preview & Direct Print
                                                            TextButton(
                                                                onClick = {
                                                                    // Get users for this batch
                                                                    val usersForBatch = userManagerUsers.filter { it.profile == batch.profileName || it.username.startsWith(batch.prefix) }
                                                                        .ifEmpty {
                                                                            // Fallback mock representation for printing
                                                                            (1..batch.count).map {
                                                                                UserManagerUser(
                                                                                    username = "${batch.prefix}${1000 + it}",
                                                                                    password = "${5000 + it}",
                                                                                    profile = batch.profileName,
                                                                                    active = true,
                                                                                    uptimeUsed = "0s",
                                                                                    downloadLimit = "unlimited"
                                                                                )
                                                                            }
                                                                        }
                                                                    val templateCfg = CardPrintAndExportHelper.loadTemplateConfig(context, linkedTemplateName)
                                                                    CardPrintAndExportHelper.printVoucherBatchDirectly(
                                                                        context = context,
                                                                        batch = batch,
                                                                        users = usersForBatch,
                                                                        config = templateCfg
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(Icons.Filled.Print, contentDescription = null, tint = Color(0xFF0C5A60), modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("معاينة وطباعة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C5A60))
                                                            }

                                                            // 2. Export to PDF
                                                            TextButton(
                                                                onClick = {
                                                                    val usersForBatch = userManagerUsers.filter { it.profile == batch.profileName || it.username.startsWith(batch.prefix) }
                                                                        .ifEmpty {
                                                                            (1..batch.count).map {
                                                                                UserManagerUser(
                                                                                    username = "${batch.prefix}${1000 + it}",
                                                                                    password = "${5000 + it}",
                                                                                    profile = batch.profileName,
                                                                                    active = true,
                                                                                    uptimeUsed = "0s",
                                                                                    downloadLimit = "unlimited"
                                                                                )
                                                                            }
                                                                        }
                                                                    val templateCfg = CardPrintAndExportHelper.loadTemplateConfig(context, linkedTemplateName)
                                                                    CardPrintAndExportHelper.saveBatchAsPdf(
                                                                        context = context,
                                                                        batch = batch,
                                                                        users = usersForBatch,
                                                                        config = templateCfg,
                                                                        onComplete = {}
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("تصدير PDF", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                                            }

                                                            // 3. Export to Excel
                                                            TextButton(
                                                                onClick = {
                                                                    val usersForBatch = userManagerUsers.filter { it.profile == batch.profileName || it.username.startsWith(batch.prefix) }
                                                                        .ifEmpty {
                                                                            (1..batch.count).map {
                                                                                UserManagerUser(
                                                                                    username = "${batch.prefix}${1000 + it}",
                                                                                    password = "${5000 + it}",
                                                                                    profile = batch.profileName,
                                                                                    active = true,
                                                                                    uptimeUsed = "0s",
                                                                                    downloadLimit = "unlimited"
                                                                                )
                                                                            }
                                                                        }
                                                                    CardPrintAndExportHelper.exportBatchToExcel(
                                                                        context = context,
                                                                        batch = batch,
                                                                        users = usersForBatch,
                                                                        posName = selectedPos
                                                                    )
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("تصدير Excel", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                                            }

                                                            // 3. Delete Batch
                                                            IconButton(
                                                                onClick = {
                                                                    onUpdateBatches(batchRecords.filter { it.batchId != batch.batchId })
                                                                    Toast.makeText(context, "تم حذف الدفعة ${batch.batchId}", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    } else {
                        // --- TAB 2: استيراد دفعة (Import Batch UI) ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "استيراد كروت من ملف أو نص",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0C5A60)
                                        )
                                        Text(
                                            text = "الصق أسطر الكروت بصيغة: اسم_المستخدم,كلمة_المرور أو اسم_المستخدم فقط:",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        OutlinedTextField(
                                            value = importRawText,
                                            onValueChange = { importRawText = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            placeholder = {
                                                Text(
                                                    "TK1001,4829\nTK1002,9182\nTK1003,7712",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        )

                                        Button(
                                            onClick = {
                                                val lines = importRawText.lines().filter { it.isNotBlank() }
                                                if (lines.isEmpty()) {
                                                    Toast.makeText(context, "يرجى إدخال بيانات الكروت أولاً", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }

                                                val importedUsers = lines.map { line ->
                                                    val parts = line.split(",", " ", "\t").map { it.trim() }
                                                    val u = parts[0]
                                                    val p = if (parts.size > 1) parts[1] else u
                                                    UserManagerUser(
                                                        username = u,
                                                        password = p,
                                                        profile = selectedProfileName,
                                                        active = true,
                                                        uptimeUsed = "0s",
                                                        downloadLimit = "unlimited"
                                                    )
                                                }

                                                viewModel.batchAddUserManagerUsers(
                                                    users = importedUsers,
                                                    batchName = "IMPORT-${System.currentTimeMillis() % 1000}",
                                                    onSuccess = {
                                                        val record = GeneratedBatchRecord(
                                                            batchId = "IMPORT-${System.currentTimeMillis() % 1000}",
                                                            date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                                                            profileName = selectedProfileName,
                                                            count = importedUsers.size,
                                                            pricePerCard = 500,
                                                            prefix = "IMP"
                                                        )
                                                        onUpdateBatches(listOf(record) + batchRecords)
                                                        importRawText = ""
                                                        selectedTopTab = 0
                                                        successNotification = "تم استيراد ${importedUsers.size} كرت وإضافتها للراوتر بنجاح!"
                                                    },
                                                    onError = {
                                                        Toast.makeText(context, "خطأ أثناء الاستيراد: $it", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.CloudUpload, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("استيراد وإضافة للراوتر الآن", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Link Template Dialog (ربط الباقة بقالب الطباعة المحفوظ) ---
    if (showLinkTemplateDialog) {
        val availableTemplates = listOf("كروت_فئة_100", "كروت_فئة_200", "كروت_فئة_500", "Abu_Talal_VIP", "Default_Design")

        AlertDialog(
            onDismissRequest = { showLinkTemplateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Palette, contentDescription = null, tint = Color(0xFF0C5A60))
                    Text("ربط الباقة بقالب الطباعة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اختر القالب الذي ترغب بربطه مع الباقة \"$selectedProfileName\":",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    availableTemplates.forEach { tmpl ->
                        val isSelected = linkedTemplateName == tmpl
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    linkedTemplateName = tmpl
                                    sharedPrefs.edit().putString("${selectedProfileName}_template", tmpl).apply()
                                    showLinkTemplateDialog = false
                                    Toast.makeText(context, "تم ربط الباقة $selectedProfileName بقالب $tmpl بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0C5A60) else Color(0xFFE2E8F0)
                            ),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val bgRes = when (tmpl) {
                                        "كروت_فئة_200" -> R.drawable.bg_card_200
                                        "كروت_فئة_500" -> R.drawable.bg_card_500
                                        else -> R.drawable.bg_card_100
                                    }
                                    Image(
                                        painter = painterResource(id = bgRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(50.dp, 32.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.FillBounds
                                    )
                                    Column {
                                        Text(text = tmpl, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                        Text(text = "جاهز ومتزامن مع قسم التصاميم", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLinkTemplateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("تم", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- DIALOG: Add POS Dialog ---
    if (showAddPosDialog) {
        AlertDialog(
            onDismissRequest = { showAddPosDialog = false },
            title = { Text("إضافة نقطة بيع جديدة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPosInput,
                    onValueChange = { newPosInput = it },
                    label = { Text("اسم نقطة البيع أو المتجر", fontFamily = CairoFontFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPosInput.isNotBlank()) {
                            posList = posList + newPosInput.trim()
                            selectedPos = newPosInput.trim()
                            newPosInput = ""
                            showAddPosDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إضافة", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPosDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- DIALOG: Add Profile Dialog ---
    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text("إضافة باقة يوزرمانجر جديدة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newProfileNameInput,
                        onValueChange = { newProfileNameInput = it },
                        label = { Text("اسم الباقة (مثلاً: باقة 2000 د.ع)", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newProfilePriceInput,
                        onValueChange = { newProfilePriceInput = it },
                        label = { Text("السعر (د.ع)", fontFamily = CairoFontFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileNameInput.isNotBlank()) {
                            val profName = newProfileNameInput.trim()
                            val price = newProfilePriceInput.toIntOrNull() ?: 500
                            viewModel.addUserManagerProfile(
                                profile = UserManagerProfile(
                                    name = profName,
                                    validity = "30d",
                                    price = price.toDouble(),
                                    sharedUsers = 1
                                ),
                                onSuccess = {
                                    selectedProfileName = profName
                                    newProfileNameInput = ""
                                    showAddProfileDialog = false
                                },
                                onError = { errMsg ->
                                    Toast.makeText(context, "خطأ: $errMsg", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("حفظ الباقة", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- NATIVE PRINT PREVIEW TRIGGER ---
    if (showInAppPrintPreview && previewBatchForPrint != null) {
        val cfg = remember(linkedTemplateName) { CardPrintAndExportHelper.loadTemplateConfig(context, linkedTemplateName) }
        LaunchedEffect(previewBatchForPrint) {
            CardPrintAndExportHelper.printVoucherBatchDirectly(
                context = context,
                batch = previewBatchForPrint!!,
                users = previewBatchUsers,
                config = cfg
            )
            showInAppPrintPreview = false
        }
    }
}

// -------------------------------------------------------------
// Generator Helpers
// -------------------------------------------------------------
private fun generateCardCode(
    prefix: String,
    suffix: String,
    length: Int,
    pattern: String,
    excludedChars: Set<Char>
): String {
    val allowedLength = (length - prefix.length - suffix.length).coerceAtLeast(3)
    val pool = when (pattern) {
        "احرف وارقام" -> "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        "احرف صغيرة فقط" -> "abcdefghijkmnopqrstuvwxyz"
        "احرف كبيرة فقط" -> "ABCDEFGHJKLMNPQRSTUVWXYZ"
        else -> "0123456789"
    }.filter { it !in excludedChars }

    val core = (1..allowedLength).map {
        pool.random()
    }.joinToString("")

    return "$prefix$core$suffix"
}

private fun generateRandomAlphaNumeric(length: Int, excludedChars: Set<Char>): String {
    val pool = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".filter { it !in excludedChars }
    return (1..length).map { pool.random() }.joinToString("")
}
