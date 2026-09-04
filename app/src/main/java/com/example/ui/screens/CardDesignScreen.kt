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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CairoFontFamily
import com.example.util.CardElementConfig
import com.example.util.CardPrintAndExportHelper
import com.example.util.CardTemplateConfig
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDesignScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Templates list
    val templatesList = listOf(
        "كروت_فئة_100",
        "كروت_فئة_200",
        "كروت_فئة_500",
        "تصميم_مخصص_1",
        "تصميم_مخصص_2"
    )
    var selectedTemplateName by remember { mutableStateOf(templatesList[0]) }
    var templateDropdownExpanded by remember { mutableStateOf(false) }

    // Active Tab: 0 = مواصفات الصفحة والخلفية, 1 = عناصر الكرت
    var selectedTabIndex by remember { mutableStateOf(1) }

    // Loaded Template Configuration
    var config by remember { mutableStateOf(CardPrintAndExportHelper.loadTemplateConfig(context, selectedTemplateName)) }
    var customBgBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Reload configuration when template changes
    LaunchedEffect(selectedTemplateName) {
        config = CardPrintAndExportHelper.loadTemplateConfig(context, selectedTemplateName)
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

    // Expand states for each of the 10 independent elements
    var expUsername by remember { mutableStateOf(true) }
    var expPassword by remember { mutableStateOf(false) }
    var expPrice by remember { mutableStateOf(false) }
    var expProfile by remember { mutableStateOf(false) }
    var expValidity by remember { mutableStateOf(false) }
    var expQuota by remember { mutableStateOf(false) }
    var expQr by remember { mutableStateOf(false) }
    var expSerial by remember { mutableStateOf(false) }
    var expBatch by remember { mutableStateOf(false) }
    var expPos by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "تصاميم الكروت والطباعة",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
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
                        // Quick Save
                        IconButton(onClick = {
                            CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            Toast.makeText(context, "تم حفظ إعدادات التصميم بنجاح!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "حفظ", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C5A60))
                )
            },
            containerColor = Color(0xFFF1F5F9)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // 1. TOP LIVE CARD PREVIEW CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0C5A60).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = selectedTemplateName.replace("_", " "),
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0C5A60),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // The Interactive Preview Box
                            LiveCardVisualPreview(
                                config = config,
                                customBgBitmap = customBgBitmap
                            )
                        }
                    }
                }

                // 2. TEMPLATE SELECTOR & ACTION BUTTONS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Main Action Buttons: [معاينة PDF (نظام الطباعة)] and [حفظ التعديلات]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. معاينة PDF (Calls Android Native System Print Spooler Preview)
                                Button(
                                    onClick = {
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        CardPrintAndExportHelper.previewDesignInSystemPrint(
                                            context = context,
                                            config = config,
                                            customBgBitmap = customBgBitmap
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "معاينة PDF وطباعة",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                // 2. حفظ التعديلات
                                OutlinedButton(
                                    onClick = {
                                        CardPrintAndExportHelper.saveTemplateConfig(context, config)
                                        Toast.makeText(context, "تم حفظ التعديلات بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "حفظ القالب",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. SECTION TABS
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
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("عناصر الكرت (10)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مواصفات الصفحة والخلفية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        )
                    }
                }

                // 4. TAB CONTENT
                if (selectedTabIndex == 0) {
                    // TAB 0: مواصفات الصفحة والخلفية
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
                                config = config.copy(
                                    bgPreset = "bg_card_100",
                                    customBgPath = null
                                )
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            }
                        )
                    }
                } else {
                    // TAB 1: عناصر الكرت (كل عنصر مستقل تماماً مع إظهار/إخفاء وإعداداته)
                    item {
                        Text(
                            text = "تحكم في إظهار وموقع وتنسيق كل عنصر من عناصر الكرت باستقلالية تامة:",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // 1. اسم المستخدم / رمز الدخول
                    item {
                        IndependentElementEditorCard(
                            title = "اسم المستخدم / رمز الدخول",
                            icon = "👤",
                            config = config.usernameConfig,
                            defaultTitle = "رمز الدخول ↓",
                            onConfigChange = { updated ->
                                config = config.copy(usernameConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expUsername,
                            onToggleExpand = { expUsername = !expUsername }
                        )
                    }

                    // 2. كلمة المرور
                    item {
                        IndependentElementEditorCard(
                            title = "كلمة المرور",
                            icon = "🔑",
                            config = config.passwordConfig,
                            defaultTitle = "كلمة المرور:",
                            onConfigChange = { updated ->
                                config = config.copy(passwordConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expPassword,
                            onToggleExpand = { expPassword = !expPassword }
                        )
                    }

                    // 3. سعر الكرت / الفئة
                    item {
                        IndependentElementEditorCard(
                            title = "سعر الكرت / الفئة",
                            icon = "💰",
                            config = config.priceConfig,
                            defaultTitle = "ريال",
                            onConfigChange = { updated ->
                                config = config.copy(priceConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expPrice,
                            onToggleExpand = { expPrice = !expPrice }
                        )
                    }

                    // 4. اسم الباقة / البروفايل
                    item {
                        IndependentElementEditorCard(
                            title = "اسم الباقة / البروفايل",
                            icon = "📦",
                            config = config.profileConfig,
                            defaultTitle = "الباقة:",
                            onConfigChange = { updated ->
                                config = config.copy(profileConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expProfile,
                            onToggleExpand = { expProfile = !expProfile }
                        )
                    }

                    // 5. مدة الصلاحية / الوقت
                    item {
                        IndependentElementEditorCard(
                            title = "مدة الصلاحية / الوقت",
                            icon = "⏱️",
                            config = config.validityConfig,
                            defaultTitle = "الصلاحية:",
                            onConfigChange = { updated ->
                                config = config.copy(validityConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expValidity,
                            onToggleExpand = { expValidity = !expValidity }
                        )
                    }

                    // 6. الرصيد / حجم البيانات
                    item {
                        IndependentElementEditorCard(
                            title = "الرصيد / حجم البيانات",
                            icon = "📊",
                            config = config.quotaConfig,
                            defaultTitle = "الرصيد:",
                            onConfigChange = { updated ->
                                config = config.copy(quotaConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expQuota,
                            onToggleExpand = { expQuota = !expQuota }
                        )
                    }

                    // 7. الباركود / رمز الاستجابة السريعة (QR Code)
                    item {
                        IndependentQrEditorCard(
                            config = config.qrConfig,
                            onConfigChange = { updated ->
                                config = config.copy(qrConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expQr,
                            onToggleExpand = { expQr = !expQr }
                        )
                    }

                    // 8. الرقم التسلسلي
                    item {
                        IndependentElementEditorCard(
                            title = "الرقم التسلسلي",
                            icon = "🔢",
                            config = config.serialConfig,
                            defaultTitle = "تسلسلي:",
                            onConfigChange = { updated ->
                                config = config.copy(serialConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expSerial,
                            onToggleExpand = { expSerial = !expSerial }
                        )
                    }

                    // 9. رقم الدفعة
                    item {
                        IndependentElementEditorCard(
                            title = "رقم الدفعة",
                            icon = "🏷️",
                            config = config.batchConfig,
                            defaultTitle = "الدفعة:",
                            onConfigChange = { updated ->
                                config = config.copy(batchConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expBatch,
                            onToggleExpand = { expBatch = !expBatch }
                        )
                    }

                    // 10. نقطة البيع
                    item {
                        IndependentPosEditorCard(
                            config = config.posConfig,
                            onConfigChange = { updated ->
                                config = config.copy(posConfig = updated)
                                CardPrintAndExportHelper.saveTemplateConfig(context, config)
                            },
                            isExpanded = expPos,
                            onToggleExpand = { expPos = !expPos }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Top Live Preview of Card with real background & coordinates
 */
@Composable
fun LiveCardVisualPreview(
    config: CardTemplateConfig,
    customBgBitmap: Bitmap?
) {
    val borderColor = parseHexToColor(config.borderColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3.2f) // Standard voucher aspect ratio
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (config.borderEnabled) (config.borderSizeMm * 1.5f).dp.coerceAtLeast(1.dp) else 0.dp,
                color = if (config.borderEnabled) borderColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (config.backgroundEnabled) Color(0xFF0C2340) else Color.White)
    ) {
        // Background Image if enabled
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
                    // Default fallback navy pill voucher visual
                    DefaultVoucherBoxVisual()
                }
            }
        }

        // Live Dynamic Overlays based on independent element configs:

        // 1. Username
        if (config.usernameConfig.visible) {
            val uCfg = config.usernameConfig
            val color = parseHexToColor(uCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (uCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (uCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uCfg.showTitle && uCfg.titleText.isNotEmpty()) {
                        Text(
                            text = uCfg.titleText,
                            fontSize = (uCfg.fontSize * 0.6f).sp,
                            color = color.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFontFamily
                        )
                    }
                    Text(
                        text = "1541523324",
                        fontSize = (uCfg.fontSize * 0.9f).sp,
                        color = color,
                        fontWeight = if (uCfg.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = CairoFontFamily
                    )
                }
            }
        }

        // 2. Password (if visible)
        if (config.passwordConfig.visible) {
            val pCfg = config.passwordConfig
            val color = parseHexToColor(pCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (pCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (pCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pCfg.showTitle && pCfg.titleText.isNotEmpty()) "${pCfg.titleText} 5432" else "5432",
                    fontSize = (pCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontWeight = if (pCfg.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 3. Price / الفئة
        if (config.priceConfig.visible) {
            val prCfg = config.priceConfig
            val color = parseHexToColor(prCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (prCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (prCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "100",
                        fontSize = (prCfg.fontSize * 0.9f).sp,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                    if (prCfg.showTitle && prCfg.titleText.isNotEmpty()) {
                        Text(
                            text = prCfg.titleText,
                            fontSize = (prCfg.fontSize * 0.5f).sp,
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFontFamily
                        )
                    }
                }
            }
        }

        // 4. Profile
        if (config.profileConfig.visible) {
            val pfCfg = config.profileConfig
            val color = parseHexToColor(pfCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (pfCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (pfCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pfCfg.showTitle && pfCfg.titleText.isNotEmpty()) "${pfCfg.titleText} باقة VIP" else "باقة VIP",
                    fontSize = (pfCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontWeight = if (pfCfg.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 5. QR / Barcode (ONLY if visible!)
        if (config.qrConfig.visible) {
            val qrCfg = config.qrConfig
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (qrCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (qrCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = Color.White,
                    modifier = Modifier.size((qrCfg.widthMm * 1.8f).dp.coerceIn(16.dp, 40.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }

        // 6. Validity
        if (config.validityConfig.visible) {
            val vCfg = config.validityConfig
            val color = parseHexToColor(vCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (vCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (vCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (vCfg.showTitle && vCfg.titleText.isNotEmpty()) "${vCfg.titleText} 7 أيام" else "7 أيام",
                    fontSize = (vCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 7. Quota
        if (config.quotaConfig.visible) {
            val qCfg = config.quotaConfig
            val color = parseHexToColor(qCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (qCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (qCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (qCfg.showTitle && qCfg.titleText.isNotEmpty()) "${qCfg.titleText} 2GB" else "2GB",
                    fontSize = (qCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 8. Serial
        if (config.serialConfig.visible) {
            val sCfg = config.serialConfig
            val color = parseHexToColor(sCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (sCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (sCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (sCfg.showTitle && sCfg.titleText.isNotEmpty()) "${sCfg.titleText} #1024" else "#1024",
                    fontSize = (sCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 9. Batch
        if (config.batchConfig.visible) {
            val bCfg = config.batchConfig
            val color = parseHexToColor(bCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (bCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (bCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (bCfg.showTitle && bCfg.titleText.isNotEmpty()) "${bCfg.titleText} VIP-1" else "VIP-1",
                    fontSize = (bCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontFamily = CairoFontFamily
                )
            }
        }

        // 10. POS
        if (config.posConfig.visible && config.posConfig.titleText.isNotEmpty()) {
            val pCfg = config.posConfig
            val color = parseHexToColor(pCfg.colorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (pCfg.xPercent * 240).dp.coerceIn(0.dp, 280.dp),
                        top = (pCfg.yPercent * 60).dp.coerceIn(0.dp, 80.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pCfg.titleText,
                    fontSize = (pCfg.fontSize * 0.8f).sp,
                    color = color,
                    fontFamily = CairoFontFamily
                )
            }
        }
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
        // Left network badge
        Text(
            text = "📶 VIP",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )

        // Center PIN white pill
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .padding(horizontal = 8.dp)
        ) {}

        // Right Price white pill
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
                .width(42.dp)
                .height(42.dp)
        ) {}
    }
}

/**
 * Page & Background Specifications Editor (TAB 0)
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

        // 1. أبعاد وتوزيع الصفحة
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "📄 أبعاد وتوزيع الصفحة (A4)",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // عدد الأعمدة
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عدد الأعمدة: ${config.columns}",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = config.columns.toFloat(),
                            onValueChange = { onConfigChange(config.copy(columns = it.toInt())) },
                            valueRange = 1f..6f,
                            steps = 4,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    // عدد الصفوف
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عدد الصفوف: ${config.rows}",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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

                // Summary badge
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
                            text = "${config.columns * config.rows} كرت",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C5A60)
                        )
                    }
                }
            }
        }

        // 2. خلفية الكرت (رفع من الهاتف + قوالب جاهزة)
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
                    // Upload button
                    Button(
                        onClick = onPickCustomImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رفع خلفية الكرت من الهاتف",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (customBgBitmap != null || config.bgPreset == "custom") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تم تفعيل خلفية مخصصة من الهاتف", fontFamily = CairoFontFamily, fontSize = 12.sp)
                                }
                                TextButton(onClick = onClearCustomBg) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مسح", fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    // Built-in presets
                    Text(
                        text = "أو اختر قالباً جاهزاً:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "قالب فئة 100",
                            isSelected = config.bgPreset == "bg_card_100",
                            onClick = { onConfigChange(config.copy(bgPreset = "bg_card_100", customBgPath = null)) }
                        )
                        PresetChip(
                            label = "قالب فئة 200",
                            isSelected = config.bgPreset == "bg_card_200",
                            onClick = { onConfigChange(config.copy(bgPreset = "bg_card_200", customBgPath = null)) }
                        )
                        PresetChip(
                            label = "قالب فئة 500",
                            isSelected = config.bgPreset == "bg_card_500",
                            onClick = { onConfigChange(config.copy(bgPreset = "bg_card_500", customBgPath = null)) }
                        )
                    }
                }
            }
        }

        // 3. إطار الكرت
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔲 إطار وحدود الكرت",
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
                        text = "سمك الإطار: ${String.format("%.2f", config.borderSizeMm)} مم",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = config.borderSizeMm,
                        onValueChange = { onConfigChange(config.copy(borderSizeMm = it)) },
                        valueRange = 0.1f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                    )
                }
            }
        }

        // 4. ملاحظة وترقيم الصفحة
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📝 تذييل الصفحة والملاحظات",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                // Page note switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "إظهار ملاحظة الصفحة", fontFamily = CairoFontFamily, fontSize = 13.sp)
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

                // Page numbering switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "إظهار ترقيم الصفحات", fontFamily = CairoFontFamily, fontSize = 13.sp)
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

/**
 * Reusable Independent Element Editor Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndependentElementEditorCard(
    title: String,
    icon: String,
    config: CardElementConfig,
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
                    // Visibility switch
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

                    // Title Toggle & Text
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
                            label = { Text("نص العنوان", fontFamily = CairoFontFamily) },
                            placeholder = { Text(defaultTitle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Position Controls (X and Y with D-Pad buttons)
                    Text(
                        text = "📍 موضع العنصر على الكرت:",
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
                        IconButton(
                            onClick = { onConfigChange(config.copy(xPercent = (config.xPercent - 0.02f).coerceIn(0f, 1f))) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null)
                        }
                        Slider(
                            value = config.xPercent,
                            onValueChange = { onConfigChange(config.copy(xPercent = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                        IconButton(
                            onClick = { onConfigChange(config.copy(xPercent = (config.xPercent + 0.02f).coerceIn(0f, 1f))) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }

                    // Vertical Y
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "رأسي (Y):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        IconButton(
                            onClick = { onConfigChange(config.copy(yPercent = (config.yPercent - 0.02f).coerceIn(0f, 1f))) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                        }
                        Slider(
                            value = config.yPercent,
                            onValueChange = { onConfigChange(config.copy(yPercent = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                        IconButton(
                            onClick = { onConfigChange(config.copy(yPercent = (config.yPercent + 0.02f).coerceIn(0f, 1f))) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    }

                    // Font Size & Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حجم الخط: ${config.fontSize.toInt()} sp",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "خط عريض (Bold)", fontFamily = CairoFontFamily, fontSize = 11.sp)
                            Checkbox(
                                checked = config.isBold,
                                onCheckedChange = { onConfigChange(config.copy(isBold = it)) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                            )
                        }
                    }

                    Slider(
                        value = config.fontSize,
                        onValueChange = { onConfigChange(config.copy(fontSize = it)) },
                        valueRange = 6f..24f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                    )

                    // Color Picker Palette
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
                        "#D97706" to "ذهبي/برتقالي",
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
 * QR / Barcode Independent Editor Card
 */
@Composable
fun IndependentQrEditorCard(
    config: CardElementConfig,
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
                    Text(text = "📱", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الباركود / رمز الاستجابة السريعة (QR Code)",
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
                        text = "📍 موضع الباركود على الكرت:",
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
                            value = config.xPercent,
                            onValueChange = { onConfigChange(config.copy(xPercent = it)) },
                            valueRange = 0f..1f,
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
                            value = config.yPercent,
                            onValueChange = { onConfigChange(config.copy(yPercent = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    // Width / Height in mm
                    Text(
                        text = "حجم الباركود: ${config.widthMm.toInt()} مم",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = config.widthMm,
                        onValueChange = { onConfigChange(config.copy(widthMm = it, heightMm = it)) },
                        valueRange = 6f..30f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                    )
                }
            }
        }
    }
}

/**
 * Point of Sale (POS) Independent Editor Card
 */
@Composable
fun IndependentPosEditorCard(
    config: CardElementConfig,
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
                    Text(text = "🏪", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نقطة البيع (POS)",
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

                    OutlinedTextField(
                        value = config.titleText,
                        onValueChange = { onConfigChange(config.copy(titleText = it)) },
                        label = { Text("نص نقطة البيع على الكرت", fontFamily = CairoFontFamily) },
                        placeholder = { Text("نقطة البيع: المركز الرئيسي") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Position Controls
                    Text(
                        text = "📍 موضع نقطة البيع على الكرت:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C5A60)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "أفقي (X):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = config.xPercent,
                            onValueChange = { onConfigChange(config.copy(xPercent = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "رأسي (Y):", fontFamily = CairoFontFamily, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = config.yPercent,
                            onValueChange = { onConfigChange(config.copy(yPercent = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0C5A60), activeTrackColor = Color(0xFF0C5A60))
                        )
                    }
                }
            }
        }
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
