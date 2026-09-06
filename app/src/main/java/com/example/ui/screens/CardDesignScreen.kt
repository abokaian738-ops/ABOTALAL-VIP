package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.UserManagerUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import com.example.util.CardElementConfig
import com.example.util.CardPrintAndExportHelper
import com.example.util.CardTemplateConfig
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDesignScreen(
    viewModel: MikroTikViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Observe real users and profiles from router/UserManager
    val realUsers by viewModel?.userManagerUsers?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val userManagerProfiles by viewModel?.userManagerProfiles?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val hotspotProfiles by viewModel?.hotspotProfiles?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }

    // Dynamic Templates list from persistent storage
    var templatesList by remember { mutableStateOf(CardPrintAndExportHelper.getTemplatesList(context)) }
    var selectedTemplateName by remember { mutableStateOf(templatesList.firstOrNull() ?: "كروت_فئة_100") }
    var templateDropdownExpanded by remember { mutableStateOf(false) }

    // Dialogs
    var showNewTemplateDialog by remember { mutableStateOf(false) }
    var newTemplateInputName by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPackageLinkDialog by remember { mutableStateOf(false) }

    // Active Tab: 0 = عناصر الكرت, 1 = مواصفات الصفحة والشبكة والخلفية
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Loaded Template Configuration
    var config by remember { mutableStateOf(CardPrintAndExportHelper.loadTemplateConfig(context, selectedTemplateName)) }
    var customBgBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Reload configuration when selected template changes
    fun reloadConfig(name: String) {
        selectedTemplateName = name
        config = CardPrintAndExportHelper.loadTemplateConfig(context, name)
        if (config.customBgPath != null) {
            try {
                customBgBitmap = BitmapFactory.decodeFile(config.customBgPath)
            } catch (_: Exception) {
                customBgBitmap = null
            }
        } else {
            customBgBitmap = null
        }
    }

    LaunchedEffect(selectedTemplateName) {
        reloadConfig(selectedTemplateName)
    }

    // Photo picker launcher for uploading custom card background
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val file = File(context.filesDir, "custom_bg_${selectedTemplateName}_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    customBgBitmap = bitmap
                    config = config.copy(
                        backgroundEnabled = true,
                        bgPreset = "custom",
                        customBgPath = file.absolutePath
                    )
                    CardPrintAndExportHelper.saveTemplateConfig(context, config)
                    Toast.makeText(context, "تم رفع خلفية الكرت بنجاح!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر قراءة الصورة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Expand states for independent element editors
    var expUsername by remember { mutableStateOf(true) }
    var expPassword by remember { mutableStateOf(false) }
    var expTitle by remember { mutableStateOf(false) }
    var expPrice by remember { mutableStateOf(false) }
    var expProfile by remember { mutableStateOf(false) }
    var expPayment by remember { mutableStateOf(false) }
    var expSerial by remember { mutableStateOf(false) }
    var expPos by remember { mutableStateOf(false) }
    var expBarcode by remember { mutableStateOf(false) }
    var expLogo by remember { mutableStateOf(false) }

    // Pick real card for realistic display (no fake dummy text)
    val displayUser = remember(realUsers, config.userManagerPackageId) {
        val matched = if (config.userManagerPackageId.isNotEmpty()) {
            realUsers.firstOrNull { it.profile.equals(config.userManagerPackageId, ignoreCase = true) }
        } else null
        matched ?: realUsers.firstOrNull() ?: UserManagerUser(
            username = "1541523324",
            password = "5432",
            profile = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
            active = true
        )
    }

    val displayBatch = remember(config, displayUser) {
        GeneratedBatchRecord(
            batchId = if (config.paymentConfig.titleText.isNotEmpty()) config.paymentConfig.titleText else "دفعة VIP",
            profileName = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else displayUser.profile,
            pricePerCard = if (config.priceConfig.titleText.isNotEmpty()) config.priceConfig.titleText.filter { it.isDigit() }.toIntOrNull() ?: 100 else 100,
            count = 1,
            date = "2026/09/03",
            prefix = ""
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "تصاميم الكروت والطباعة",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "القالب النشط: ${selectedTemplateName.replace("_", " ")}",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Quick Save button
                        IconButton(onClick = {
                            CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            Toast.makeText(context, "تم حفظ إعدادات القالب بنجاح!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "حفظ", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C5A60))
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // 1. PINNED / STICKY LIVE CARD VISUAL PREVIEW
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header of Preview Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = Color(0xFF0C5A60),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "معاينة شكل الكرت المباشرة",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                // Card Dimensions Badge
                                val cardW = if (config.autoFitA4) {
                                    val usableW = 210f - (config.pageMarginMm * 2) - (config.horizontalMarginMm * (config.columns - 1))
                                    (usableW / config.columns.coerceAtLeast(1)).roundToInt()
                                } else config.cardWidthMm.roundToInt()

                                val cardH = if (config.autoFitA4) {
                                    val usableH = 297f - (config.pageMarginMm * 2) - (config.verticalMarginMm * (config.rows - 1))
                                    (usableH / config.rows.coerceAtLeast(1)).roundToInt()
                                } else config.cardHeightMm.roundToInt()

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0C5A60).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "$cardW × $cardH مم",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0C5A60),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // The Interactive Preview Box with Real Background & Precise Mm Positioning
                            LiveCardVisualPreview(
                                config = config,
                                customBgBitmap = customBgBitmap,
                                displayUser = displayUser,
                                displayBatch = displayBatch,
                                onUsernamePositionChange = { newX, newY ->
                                    val updated = config.copy(
                                        usernameConfig = config.usernameConfig.copy(
                                            xMm = newX,
                                            yMm = newY
                                        )
                                    )
                                    config = updated
                                    CardPrintAndExportHelper.saveTemplateConfig(context, updated)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Real-time Coordinate Status Strip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "موضع الكود: س = ${config.usernameConfig.xMm} مم | ص = ${config.usernameConfig.yMm} مم",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0C5A60),
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "الخط: ${config.usernameConfig.fontSize.toInt()} pt",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. TEMPLATES MANAGEMENT & CONTROLS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Template selector dropdown
                            ExposedDropdownMenuBox(
                                expanded = templateDropdownExpanded,
                                onExpandedChange = { templateDropdownExpanded = !templateDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedTemplateName.replace("_", " "),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("قالب التصميم المختار", fontFamily = CairoFontFamily) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = templateDropdownExpanded,
                                    onDismissRequest = { templateDropdownExpanded = false }
                                ) {
                                    templatesList.forEach { tmpl ->
                                        DropdownMenuItem(
                                            text = { Text(tmpl.replace("_", " "), fontFamily = CairoFontFamily) },
                                            onClick = {
                                                selectedTemplateName = tmpl
                                                templateDropdownExpanded = false
                                                reloadConfig(tmpl)
                                            }
                                        )
                                    }
                                }
                            }

                            // Template Action Buttons: [حفظ | جديد | حذف | ربط باقة]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Save Template Button
                                Button(
                                    onClick = {
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        Toast.makeText(context, "تم حفظ القالب $selectedTemplateName بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حفظ", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // New Template Button
                                OutlinedButton(
                                    onClick = {
                                        newTemplateInputName = ""
                                        showNewTemplateDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Link Package Button
                                OutlinedButton(
                                    onClick = { showPackageLinkDialog = true },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ربط باقة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Delete Template Button (if more than 1 template)
                                if (templatesList.size > 1) {
                                    IconButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "حذف القالب", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. PDF PREVIEW & PRINT ACTIONS CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "🖨️ تصدير وطباعة الكروت",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Button 1: Open built-in PDF Reader
                                Button(
                                    onClick = {
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        CardPrintAndExportHelper.openPdfPreviewWithBuiltInViewer(
                                            context = context,
                                            config = config,
                                            customBgBitmap = customBgBitmap,
                                            users = if (realUsers.isNotEmpty()) realUsers else emptyList(),
                                            batch = displayBatch
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("معاينة PDF", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Button 2: Print & Save PDF (Native Android Spooler + Downloads)
                                Button(
                                    onClick = {
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        CardPrintAndExportHelper.printAndSavePdfDocument(
                                            context = context,
                                            config = config,
                                            customBgBitmap = customBgBitmap,
                                            users = if (realUsers.isNotEmpty()) realUsers else emptyList(),
                                            batch = displayBatch
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                                ) {
                                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("طباعة وحفظ PDF", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 4. MAIN TABS
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color(0xFF0C5A60),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .shadow(2.dp)
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("عناصر الكرت (10)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("الصفحة والخلفية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        )
                    }
                }

                // 5. TAB CONTENT
                if (selectedTabIndex == 0) {
                    // TAB 0: عناصر الكرت الـ 10
                    val cardW = if (config.autoFitA4) {
                        val usableW = 210f - (config.pageMarginMm * 2) - (config.horizontalMarginMm * (config.columns - 1))
                        (usableW / config.columns.coerceAtLeast(1))
                    } else config.cardWidthMm

                    val cardH = if (config.autoFitA4) {
                        val usableH = 297f - (config.pageMarginMm * 2) - (config.verticalMarginMm * (config.rows - 1))
                        (usableH / config.rows.coerceAtLeast(1))
                    } else config.cardHeightMm

                    // 1. اسم المستخدم / الكود
                    item {
                        MmElementEditorCard(
                            title = "اسم المستخدم (كود الكرت)",
                            icon = "🔑",
                            config = config.usernameConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "كود الدخول:",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(usernameConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expUsername,
                            onToggleExpand = { expUsername = !expUsername }
                        )
                    }

                    // 2. كلمة المرور
                    item {
                        MmElementEditorCard(
                            title = "كلمة المرور (PIN)",
                            icon = "🔒",
                            config = config.passwordConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "الرمز:",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(passwordConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expPassword,
                            onToggleExpand = { expPassword = !expPassword }
                        )
                    }

                    // 3. نص العنوان
                    item {
                        MmElementEditorCard(
                            title = "نص العنوان / اسم الشبكة",
                            icon = "🏷️",
                            config = config.titleConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "شبكة ABO TALAL VIP",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(titleConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expTitle,
                            onToggleExpand = { expTitle = !expTitle }
                        )
                    }

                    // 4. السعر / الفئة
                    item {
                        MmElementEditorCard(
                            title = "فئة الكرت / السعر",
                            icon = "💰",
                            config = config.priceConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "500 ر.ي",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(priceConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expPrice,
                            onToggleExpand = { expPrice = !expPrice }
                        )
                    }

                    // 5. اسم الباقة
                    item {
                        MmElementEditorCard(
                            title = "اسم الباقة / البروفايل",
                            icon = "📦",
                            config = config.profileConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "باقة VIP 2GB",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(profileConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expProfile,
                            onToggleExpand = { expProfile = !expProfile }
                        )
                    }

                    // 6. رقم الدفعة
                    item {
                        MmElementEditorCard(
                            title = "رقم الدفعة / الدفع",
                            icon = "💳",
                            config = config.paymentConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "دفعة 102",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(paymentConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expPayment,
                            onToggleExpand = { expPayment = !expPayment }
                        )
                    }

                    // 7. الرقم التسلسلي
                    item {
                        MmElementEditorCard(
                            title = "الرقم التسلسلي",
                            icon = "🔢",
                            config = config.serialConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "#1024",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(serialConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expSerial,
                            onToggleExpand = { expSerial = !expSerial }
                        )
                    }

                    // 8. نقطة البيع
                    item {
                        MmElementEditorCard(
                            title = "نقطة البيع (POS)",
                            icon = "🏪",
                            config = config.posConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            defaultTitle = "المركز الرئيسي",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(posConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expPos,
                            onToggleExpand = { expPos = !expPos }
                        )
                    }

                    // 9. الباركود
                    item {
                        MmBarcodeEditorCard(
                            config = config.barcodeConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            onConfigChange = { updated ->
                                val newCfg = config.copy(barcodeConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expBarcode,
                            onToggleExpand = { expBarcode = !expBarcode }
                        )
                    }

                    // 10. الشعار
                    item {
                        MmBarcodeEditorCard(
                            config = config.logoConfig,
                            cardWidthMm = cardW,
                            cardHeightMm = cardH,
                            title = "شعار الكرت (Logo)",
                            icon = "🖼️",
                            onConfigChange = { updated ->
                                val newCfg = config.copy(logoConfig = updated)
                                config = newCfg
                                CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            },
                            isExpanded = expLogo,
                            onToggleExpand = { expLogo = !expLogo }
                        )
                    }
                } else {
                    // TAB 1: مواصفات الصفحة والشبكة والخلفية
                    item {
                        CardPageAndBackgroundSettings(
                            config = config,
                            onConfigChange = { updated ->
                                config = updated
                                CardPrintAndExportHelper.saveTemplateConfig(context, updated)
                            },
                            customBgBitmap = customBgBitmap,
                            onPickCustomImage = { imagePickerLauncher.launch("image/*") },
                            onClearCustomBg = {
                                customBgBitmap = null
                                val updated = config.copy(bgPreset = "bg_card_100", customBgPath = null)
                                config = updated
                                CardPrintAndExportHelper.saveTemplateConfig(context, updated)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // --- DIALOG: New Template Dialog ---
    if (showNewTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showNewTemplateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF0C5A60))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إنشاء قالب تصميم جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل اسم القالب الجديد (مثال: كروت_محل_الرئيسي):", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newTemplateInputName,
                        onValueChange = { newTemplateInputName = it.replace(" ", "_") },
                        label = { Text("اسم القالب", fontFamily = CairoFontFamily) },
                        placeholder = { Text("قالب_جديد") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newTemplateInputName.trim()
                        if (name.isNotEmpty()) {
                            val curTemplates = CardPrintAndExportHelper.getTemplatesList(context).toMutableList()
                            if (!curTemplates.contains(name)) {
                                curTemplates.add(name)
                                CardPrintAndExportHelper.saveTemplatesList(context, curTemplates)
                                templatesList = curTemplates
                            }
                            // Save with current config
                            val newCfg = config.copy(templateName = name)
                            CardPrintAndExportHelper.saveTemplateConfig(context, newCfg)
                            reloadConfig(name)
                            showNewTemplateDialog = false
                            Toast.makeText(context, "تم إنشاء القالب $name بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("إنشاء", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTemplateDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- DIALOG: Delete Template Confirmation ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد حذف القالب", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Text(
                    "هل أنت متأكد من حذف قالب \"$selectedTemplateName\"؟ لن تتمكن من استعادته.",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        CardPrintAndExportHelper.deleteTemplate(context, selectedTemplateName)
                        templatesList = CardPrintAndExportHelper.getTemplatesList(context)
                        val next = templatesList.firstOrNull() ?: "كروت_فئة_100"
                        reloadConfig(next)
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "تم حذف القالب بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف نهائياً", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // --- DIALOG: Link Package with Template ---
    if (showPackageLinkDialog) {
        val sp = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)

        AlertDialog(
            onDismissRequest = { showPackageLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = Color(0xFF0C5A60))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ربط الباقات بالقالب الحالي", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "اختر الباقة التي ترغب بربطها مع قالب \"$selectedTemplateName\" ليتم استخدامه تلقائياً عند طباعتها:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    // UserManager profiles
                    Text("باقات User Manager:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (userManagerProfiles.isEmpty()) {
                        Text("لا توجد باقات يوزر مانجر محملة", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    } else {
                        userManagerProfiles.forEach { prof ->
                            val isLinked = config.userManagerPackageId == prof.name
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        config = config.copy(userManagerPackageId = prof.name)
                                        sp.edit().putString("${prof.name}_template", selectedTemplateName).apply()
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        Toast.makeText(context, "تم ربط باقة ${prof.name} بهذا القالب!", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isLinked) Color(0xFFE0F2FE) else Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, if (isLinked) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(prof.name, fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    if (isLinked) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Hotspot profiles
                    if (hotspotProfiles.isNotEmpty()) {
                        Text("بروفايلات الهوتسبوت:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        hotspotProfiles.forEach { prof ->
                            val isLinked = config.hotspotPackageId == prof.name
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        config = config.copy(hotspotPackageId = prof.name)
                                        sp.edit().putString("${prof.name}_template", selectedTemplateName).apply()
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        Toast.makeText(context, "تم ربط بروفايل ${prof.name} بهذا القالب!", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isLinked) Color(0xFFE0F2FE) else Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, if (isLinked) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(prof.name, fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    if (isLinked) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPackageLinkDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    Text("تم", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Top Live Interactive Preview of Card with real background, exact mm positioning, and drag support
 */
@Composable
fun LiveCardVisualPreview(
    config: CardTemplateConfig,
    customBgBitmap: Bitmap?,
    displayUser: UserManagerUser,
    displayBatch: GeneratedBatchRecord,
    onUsernamePositionChange: (newX: Float, newY: Float) -> Unit
) {
    val borderColor = parseHexToColor(config.borderColorHex)

    // Calculate dimensions in mm
    val cardWidthMm = if (config.autoFitA4) {
        val usableW = 210f - (config.pageMarginMm * 2) - (config.horizontalMarginMm * (config.columns - 1))
        (usableW / config.columns.coerceAtLeast(1)).coerceAtLeast(10f)
    } else config.cardWidthMm

    val cardHeightMm = if (config.autoFitA4) {
        val usableH = 297f - (config.pageMarginMm * 2) - (config.verticalMarginMm * (config.rows - 1))
        (usableH / config.rows.coerceAtLeast(1)).coerceAtLeast(10f)
    } else config.cardHeightMm

    val aspectRatio = (cardWidthMm / cardHeightMm).coerceIn(1.2f, 4.2f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (config.borderEnabled) (config.borderSizeMm * 1.5f).dp.coerceAtLeast(1.dp) else 0.dp,
                color = if (config.borderEnabled) borderColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (config.backgroundEnabled) Color(0xFF0C2340) else Color.White)
    ) {
        val boxWidthPx = constraints.maxWidth.toFloat()
        val boxHeightPx = constraints.maxHeight.toFloat()

        val scaleX = boxWidthPx / cardWidthMm
        val scaleY = boxHeightPx / cardHeightMm

        // 1. Background Graphic / Bitmap
        if (config.backgroundEnabled) {
            if (customBgBitmap != null) {
                Image(
                    bitmap = customBgBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                val res = when (config.bgPreset) {
                    "bg_card_200" -> R.drawable.bg_card_200
                    "bg_card_500" -> R.drawable.bg_card_500
                    "none" -> 0
                    else -> R.drawable.bg_card_100
                }
                if (res != 0) {
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    DefaultVoucherBoxVisual()
                }
            }
        }

        // Helper to place text element using RTL coordinate (xMm from right edge, yMm from top edge)
        @Composable
        fun DrawPreviewText(
            elem: CardElementConfig,
            displayText: String,
            isDraggable: Boolean = false,
            onDragEnd: ((Float, Float) -> Unit)? = null
        ) {
            if (!elem.visible || displayText.isEmpty()) return

            val color = parseHexToColor(elem.colorHex)
            val density = LocalDensity.current

            // Calculate offset in dp
            // In RTL, xMm is measured from right edge
            val offsetXdp = with(density) { ((boxWidthPx - (elem.xMm * scaleX)) / density.density).dp }
            val offsetYdp = with(density) { ((elem.yMm * scaleY) / density.density).dp }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDraggable && onDragEnd != null) {
                            Modifier.pointerInput(elem.xMm, elem.yMm) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // In RTL, dragging left increases xMm from right
                                    val deltaMmX = -dragAmount.x / scaleX
                                    val deltaMmY = dragAmount.y / scaleY
                                    val newX = (elem.xMm + deltaMmX).coerceIn(0f, cardWidthMm)
                                    val newY = (elem.yMm + deltaMmY).coerceIn(0f, cardHeightMm)
                                    onDragEnd((newX * 2).roundToInt() / 2f, (newY * 2).roundToInt() / 2f)
                                }
                            }
                        } else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (boxWidthPx - (elem.xMm * scaleX)).roundToInt(),
                                (elem.yMm * scaleY).roundToInt()
                            )
                        },
                    horizontalAlignment = Alignment.End
                ) {
                    if (elem.showTitle && elem.titleText.isNotEmpty()) {
                        Text(
                            text = elem.titleText,
                            fontSize = (elem.fontSize * 0.65f).sp,
                            color = color.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFontFamily,
                            textAlign = TextAlign.Right
                        )
                    }
                    Text(
                        text = displayText,
                        fontSize = elem.fontSize.sp,
                        color = color,
                        fontWeight = if (elem.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (elem.isItalic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = CairoFontFamily,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }

        // 1. Username / Real Code (Draggable directly on card!)
        if (config.usernameConfig.visible) {
            DrawPreviewText(
                elem = config.usernameConfig,
                displayText = displayUser.username,
                isDraggable = true,
                onDragEnd = onUsernamePositionChange
            )
        }

        // 2. Title
        if (config.titleConfig.visible) {
            DrawPreviewText(
                elem = config.titleConfig,
                displayText = config.titleConfig.titleText
            )
        }

        // 3. Password
        if (config.passwordConfig.visible) {
            val pText = if (displayUser.password.isNotEmpty()) displayUser.password else displayUser.username
            DrawPreviewText(
                elem = config.passwordConfig,
                displayText = pText
            )
        }

        // 4. Price
        if (config.priceConfig.visible) {
            val prText = if (config.priceConfig.titleText.isNotEmpty()) {
                config.priceConfig.titleText
            } else if (displayBatch.pricePerCard > 0) {
                "${displayBatch.pricePerCard} ر.ي"
            } else {
                "500 ر.ي"
            }
            DrawPreviewText(
                elem = config.priceConfig,
                displayText = prText
            )
        }

        // 5. Profile Name
        if (config.profileConfig.visible) {
            val pfText = if (config.profileConfig.titleText.isNotEmpty()) {
                config.profileConfig.titleText
            } else if (displayUser.profile.isNotEmpty()) {
                displayUser.profile
            } else {
                displayBatch.profileName
            }
            DrawPreviewText(
                elem = config.profileConfig,
                displayText = pfText
            )
        }

        // 6. Payment / Batch
        if (config.paymentConfig.visible) {
            val payText = if (config.paymentConfig.titleText.isNotEmpty()) {
                config.paymentConfig.titleText
            } else {
                displayBatch.batchId
            }
            DrawPreviewText(
                elem = config.paymentConfig,
                displayText = payText
            )
        }

        // 7. Serial
        if (config.serialConfig.visible) {
            val sText = if (config.serialConfig.titleText.isNotEmpty()) {
                config.serialConfig.titleText
            } else {
                displayUser.username.takeLast(4)
            }
            DrawPreviewText(
                elem = config.serialConfig,
                displayText = sText
            )
        }

        // 8. POS
        if (config.posConfig.visible) {
            val posText = if (config.posConfig.titleText.isNotEmpty()) {
                config.posConfig.titleText
            } else {
                "المركز الرئيسي"
            }
            DrawPreviewText(
                elem = config.posConfig,
                displayText = posText
            )
        }

        // 9. Barcode
        if (config.barcodeConfig.visible) {
            val bCfg = config.barcodeConfig
            val bwPx = (bCfg.widthMm * scaleX).coerceAtLeast(20f)
            val bhPx = (bCfg.heightMm * scaleY).coerceAtLeast(14f)
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (boxWidthPx - (bCfg.xMm * scaleX) - bwPx).roundToInt(),
                            (bCfg.yMm * scaleY).roundToInt()
                        )
                    }
                    .size(
                        width = with(density) { (bwPx / density.density).dp },
                        height = with(density) { (bhPx / density.density).dp }
                    )
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 10. Logo
        if (config.logoConfig.visible) {
            val lCfg = config.logoConfig
            val lwPx = (lCfg.widthMm * scaleX).coerceAtLeast(16f)
            val lhPx = (lCfg.heightMm * scaleY).coerceAtLeast(16f)
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (boxWidthPx - (lCfg.xMm * scaleX) - lwPx).roundToInt(),
                            (lCfg.yMm * scaleY).roundToInt()
                        )
                    }
                    .size(
                        width = with(density) { (lwPx / density.density).dp },
                        height = with(density) { (lhPx / density.density).dp }
                    )
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LOGO",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }
}

/**
 * Independent Element Editor Card with Exact Millimeter (mm) Coordinates, D-Pad, 9 Predefined Presets, Font Size, and Colors
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MmElementEditorCard(
    title: String,
    icon: String,
    config: CardElementConfig,
    cardWidthMm: Float,
    cardHeightMm: Float,
    defaultTitle: String,
    onConfigChange: (CardElementConfig) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon, Title, Visibility Switch, Expand Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (config.visible) Color(0xFF0F172A) else Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = config.visible,
                        onCheckedChange = { onConfigChange(config.copy(visible = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Expanded Options
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = Color(0xFFE2E8F0))

                    // Title Toggle & Custom Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إظهار عنوان العنصر",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Checkbox(
                            checked = config.showTitle,
                            onCheckedChange = { onConfigChange(config.copy(showTitle = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                        )
                    }

                    if (config.showTitle) {
                        OutlinedTextField(
                            value = config.titleText,
                            onValueChange = { onConfigChange(config.copy(titleText = it)) },
                            label = { Text("نص التسمية أو القيمة", fontFamily = CairoFontFamily) },
                            placeholder = { Text(defaultTitle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // --- POSITION CONTROLS (IN EXACT MILLIMETERS) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 موضع العنصر بالمليمتر (مم):",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C5A60)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0C5A60).copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "س = ${config.xMm} مم | ص = ${config.yMm} مم",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0C5A60),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // 9 Predefined Positioning Presets Matrix
                    Text("المواضع السريعة الجاهزة:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF64748B))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PresetPositionButton("↗ أعلى يمين", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = 4f, yMm = 3f))
                            }
                            PresetPositionButton("↑ أعلى وسط", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = cardWidthMm / 2f, yMm = 3f))
                            }
                            PresetPositionButton("↖ أعلى يسار", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = (cardWidthMm - 20f).coerceAtLeast(4f), yMm = 3f))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PresetPositionButton("→ وسط يمين", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = 4f, yMm = cardHeightMm / 2f - 4f))
                            }
                            PresetPositionButton("🎯 المركز", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = cardWidthMm / 2f, yMm = cardHeightMm / 2f - 4f))
                            }
                            PresetPositionButton("← وسط يسار", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = (cardWidthMm - 20f).coerceAtLeast(4f), yMm = cardHeightMm / 2f - 4f))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PresetPositionButton("↘ أسفل يمين", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = 4f, yMm = (cardHeightMm - 8f).coerceAtLeast(2f)))
                            }
                            PresetPositionButton("↓ أسفل وسط", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = cardWidthMm / 2f, yMm = (cardHeightMm - 8f).coerceAtLeast(2f)))
                            }
                            PresetPositionButton("↙ أسفل يسار", Modifier.weight(1f)) {
                                onConfigChange(config.copy(xMm = (cardWidthMm - 20f).coerceAtLeast(4f), yMm = (cardHeightMm - 8f).coerceAtLeast(2f)))
                            }
                        }
                    }

                    // Stepper for X (Horizontal from right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "أفقي (X):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        StepButton("-1") { onConfigChange(config.copy(xMm = (config.xMm - 1f).coerceIn(0f, cardWidthMm))) }
                        StepButton("-0.5") { onConfigChange(config.copy(xMm = (config.xMm - 0.5f).coerceIn(0f, cardWidthMm))) }
                        Slider(
                            value = config.xMm,
                            onValueChange = { onConfigChange(config.copy(xMm = (it * 2).roundToInt() / 2f)) },
                            valueRange = 0f..cardWidthMm,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                        StepButton("+0.5") { onConfigChange(config.copy(xMm = (config.xMm + 0.5f).coerceIn(0f, cardWidthMm))) }
                        StepButton("+1") { onConfigChange(config.copy(xMm = (config.xMm + 1f).coerceIn(0f, cardWidthMm))) }
                    }

                    // Stepper for Y (Vertical from top)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "رأسي (Y):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        StepButton("-1") { onConfigChange(config.copy(yMm = (config.yMm - 1f).coerceIn(0f, cardHeightMm))) }
                        StepButton("-0.5") { onConfigChange(config.copy(yMm = (config.yMm - 0.5f).coerceIn(0f, cardHeightMm))) }
                        Slider(
                            value = config.yMm,
                            onValueChange = { onConfigChange(config.copy(yMm = (it * 2).roundToInt() / 2f)) },
                            valueRange = 0f..cardHeightMm,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                        StepButton("+0.5") { onConfigChange(config.copy(yMm = (config.yMm + 0.5f).coerceIn(0f, cardHeightMm))) }
                        StepButton("+1") { onConfigChange(config.copy(yMm = (config.yMm + 1f).coerceIn(0f, cardHeightMm))) }
                    }

                    // Directional D-Pad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onConfigChange(config.copy(xMm = (config.xMm + 1f).coerceIn(0f, cardWidthMm))) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "يمين")
                                }
                                IconButton(
                                    onClick = { onConfigChange(config.copy(yMm = (config.yMm - 1f).coerceIn(0f, cardHeightMm))) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "أعلى")
                                }
                                IconButton(
                                    onClick = { onConfigChange(config.copy(yMm = (config.yMm + 1f).coerceIn(0f, cardHeightMm))) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "أسفل")
                                }
                                IconButton(
                                    onClick = { onConfigChange(config.copy(xMm = (config.xMm - 1f).coerceIn(0f, cardWidthMm))) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "يسار")
                                }
                            }
                        }
                    }

                    // Font Size & Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حجم الخط: ${config.fontSize.toInt()} pt",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "عريض", fontFamily = CairoFontFamily, fontSize = 11.sp)
                                Checkbox(
                                    checked = config.isBold,
                                    onCheckedChange = { onConfigChange(config.copy(isBold = it)) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "مائل", fontFamily = CairoFontFamily, fontSize = 11.sp)
                                Checkbox(
                                    checked = config.isItalic,
                                    onCheckedChange = { onConfigChange(config.copy(isItalic = it)) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                                )
                            }
                        }
                    }

                    Slider(
                        value = config.fontSize,
                        onValueChange = { onConfigChange(config.copy(fontSize = it.roundToInt().toFloat())) },
                        valueRange = 6f..26f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                    )

                    // Color Palette
                    Text(
                        text = "لون الخط:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val colorOptions = listOf(
                        "#0F172A" to "أسود داكن",
                        "#0C5A60" to "كحلي فيروزي",
                        "#0284C7" to "أزرق سماوي",
                        "#10B981" to "أخضر زمردي",
                        "#D97706" to "ذهبي برتقالي",
                        "#EF4444" to "أحمر",
                        "#FFFFFF" to "أبيض"
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { (hex, _) ->
                            val color = parseHexToColor(hex)
                            val isSelected = config.colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF0C5A60) else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { onConfigChange(config.copy(colorHex = hex)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barcode / Logo Independent Editor Card
 */
@Composable
fun MmBarcodeEditorCard(
    config: CardElementConfig,
    cardWidthMm: Float,
    cardHeightMm: Float,
    title: String = "الباركود / رمز الاستجابة السريعة (QR Code)",
    icon: String = "📱",
    onConfigChange: (CardElementConfig) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (config.visible) Color(0xFF0F172A) else Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = config.visible,
                        onCheckedChange = { onConfigChange(config.copy(visible = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "📍 موضع العنصر بالمليمتر (س = ${config.xMm} مم | ص = ${config.yMm} مم):",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C5A60)
                    )

                    // Horizontal X
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "أفقي (X):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = config.xMm,
                            onValueChange = { onConfigChange(config.copy(xMm = (it * 2).roundToInt() / 2f)) },
                            valueRange = 0f..cardWidthMm,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    // Vertical Y
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "رأسي (Y):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = config.yMm,
                            onValueChange = { onConfigChange(config.copy(yMm = (it * 2).roundToInt() / 2f)) },
                            valueRange = 0f..cardHeightMm,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    // Dimensions (Width and Height in mm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "العرض: ${config.widthMm.toInt()} مم",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = config.widthMm,
                            onValueChange = { onConfigChange(config.copy(widthMm = it.roundToInt().toFloat())) },
                            valueRange = 6f..40f,
                            modifier = Modifier.weight(2f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "الارتفاع: ${config.heightMm.toInt()} مم",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = config.heightMm,
                            onValueChange = { onConfigChange(config.copy(heightMm = it.roundToInt().toFloat())) },
                            valueRange = 6f..40f,
                            modifier = Modifier.weight(2f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Page & Background Specifications Editor (TAB 1)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardPageAndBackgroundSettings(
    config: CardTemplateConfig,
    onConfigChange: (CardTemplateConfig) -> Unit,
    customBgBitmap: Bitmap?,
    onPickCustomImage: () -> Unit,
    onClearCustomBg: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 1. أبعاد وتوزيع شبكة الكروت على ورقة A4
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "📄 أبعاد وتوزيع شبكة الكروت (A4)",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                // Auto Fit Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ضبط تلقائي لأبعاد الكرت على A4", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("يحسب العرض والارتفاع تلقائياً بحسب عدد الصفوف والأعمدة", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = config.autoFitA4,
                        onCheckedChange = { onConfigChange(config.copy(autoFitA4 = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                }

                Divider(color = Color(0xFFE2E8F0))

                // Columns & Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الأعمدة: ${config.columns}",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = config.columns.toFloat(),
                            onValueChange = { onConfigChange(config.copy(columns = it.toInt())) },
                            valueRange = 1f..6f,
                            steps = 4,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الصفوف: ${config.rows}",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = config.rows.toFloat(),
                            onValueChange = { onConfigChange(config.copy(rows = it.toInt())) },
                            valueRange = 2f..25f,
                            steps = 22,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }
                }

                // Margins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "هامش أفقي: ${config.horizontalMarginMm.toInt()} مم",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = config.horizontalMarginMm,
                            onValueChange = { onConfigChange(config.copy(horizontalMarginMm = it.roundToInt().toFloat())) },
                            valueRange = 0f..15f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "هامش رأسي: ${config.verticalMarginMm.toInt()} مم",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = config.verticalMarginMm,
                            onValueChange = { onConfigChange(config.copy(verticalMarginMm = it.roundToInt().toFloat())) },
                            valueRange = 0f..15f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }
                }

                // Summary badge
                val totalCards = config.columns * config.rows
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0C5A60).copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي الكروت في الصفحة الواحدة:",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF0C5A60)
                        )
                        Text(
                            text = "$totalCards كرت (${config.columns} × ${config.rows})",
                            fontFamily = CairoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C5A60)
                        )
                    }
                }
            }
        }

        // 2. خلفية الكرت (رفع صورة من الهاتف + قوالب ملونة جاهزة)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎨 خلفية الكرت",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Switch(
                        checked = config.backgroundEnabled,
                        onCheckedChange = { onConfigChange(config.copy(backgroundEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                }

                if (config.backgroundEnabled) {
                    Text(
                        text = "اختر قالب الخلفية أو ارفع صورة خاصة:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PresetChip(
                            label = "كرت فئة 100 ر.ي (أزرق)",
                            isSelected = config.bgPreset == "bg_card_100",
                            onClick = {
                                onClearCustomBg()
                                onConfigChange(config.copy(bgPreset = "bg_card_100", customBgPath = null))
                            }
                        )
                        PresetChip(
                            label = "كرت فئة 200 ر.ي (أخضر)",
                            isSelected = config.bgPreset == "bg_card_200",
                            onClick = {
                                onClearCustomBg()
                                onConfigChange(config.copy(bgPreset = "bg_card_200", customBgPath = null))
                            }
                        )
                        PresetChip(
                            label = "كرت فئة 500 ر.ي (بنفسجي)",
                            isSelected = config.bgPreset == "bg_card_500",
                            onClick = {
                                onClearCustomBg()
                                onConfigChange(config.copy(bgPreset = "bg_card_500", customBgPath = null))
                            }
                        )
                        PresetChip(
                            label = "بدون خلفية (أبيض)",
                            isSelected = config.bgPreset == "none",
                            onClick = {
                                onClearCustomBg()
                                onConfigChange(config.copy(bgPreset = "none", customBgPath = null))
                            }
                        )
                    }

                    // Upload Custom Image Button
                    Button(
                        onClick = onPickCustomImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (customBgBitmap != null) "تغيير الصورة المخصصة المرفوعة" else "رفع صورة خلفية خاصة من الهاتف",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. إطار الكرت والحدود
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔲 إطار وحدود الكرت (Border)",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Switch(
                        checked = config.borderEnabled,
                        onCheckedChange = { onConfigChange(config.copy(borderEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                }

                if (config.borderEnabled) {
                    Text(
                        text = "سماكة الإطار: ${config.borderSizeMm} مم",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = config.borderSizeMm,
                        onValueChange = { onConfigChange(config.copy(borderSizeMm = (it * 10).roundToInt() / 10f)) },
                        valueRange = 0.2f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                    )
                }
            }
        }

        // 4. ملاحظة أسفل الصفحة وترقيم الصفحات
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ملاحظة في أسفل الورقة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(
                        checked = config.pageNoteEnabled,
                        onCheckedChange = { onConfigChange(config.copy(pageNoteEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                }

                if (config.pageNoteEnabled) {
                    OutlinedTextField(
                        value = config.pageNoteText,
                        onValueChange = { onConfigChange(config.copy(pageNoteText = it)) },
                        label = { Text("نص الملاحظة في أسفل الصفحة", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Divider(color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "إظهار ترقيم الصفحات (1/X)", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = config.pageNumberingEnabled,
                        onCheckedChange = { onConfigChange(config.copy(pageNumberingEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0C5A60))
                    )
                }
            }
        }
    }
}

@Composable
fun PresetPositionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
        )
    }
}

@Composable
fun StepButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE2E8F0),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DefaultVoucherBoxVisual() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📶 VIP",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .padding(horizontal = 8.dp)
        ) {}

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
                .width(42.dp)
                .height(42.dp)
        ) {}
    }
}

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF0C5A60) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0C5A60) else Color(0xFFCBD5E1)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

fun parseHexToColor(hex: String): Color {
    return try {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (_: Exception) {
        Color.Black
    }
}
