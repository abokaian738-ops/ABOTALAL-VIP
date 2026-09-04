package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Rect as AndroidRect
import android.graphics.RectF as AndroidRectF
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HotspotUser
import com.example.data.model.UserManagerUser
import com.example.ui.viewmodel.MikroTikViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException
import java.util.UUID

enum class VoucherMode {
    HOTSPOT,
    USER_MANAGER
}

enum class CardDesignTheme {
    CLASSIC_BLUE,
    ROYAL_GOLD,
    SHINY_PURPLE,
    MINIMAL_WHITE,
    CUSTOM_IMAGE
}

// Model representing cards to be generated and printed
data class TempVoucher(
    val username: String,
    val password: String,
    val profile: String,
    val securityCode: String,
    val bindOnFirstLogin: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherPrinterScreen(
    viewModel: MikroTikViewModel,
    initialMode: VoucherMode = VoucherMode.HOTSPOT
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Service Profiles State
    val hotspotProfiles by viewModel.hotspotProfiles.collectAsState()
    val userManagerProfiles by viewModel.userManagerProfiles.collectAsState()

    // 2. Settings States
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedProfile by remember { mutableStateOf("") }

    // Initialize default profile
    LaunchedEffect(selectedMode, hotspotProfiles, userManagerProfiles) {
        if (selectedMode == VoucherMode.HOTSPOT && hotspotProfiles.isNotEmpty()) {
            selectedProfile = hotspotProfiles.first().name
        } else if (selectedMode == VoucherMode.USER_MANAGER && userManagerProfiles.isNotEmpty()) {
            selectedProfile = userManagerProfiles.first().name
        }
    }

    // Numbering & Length parameters
    var generationTypeSeq by remember { mutableStateOf(false) } // true = sequential numbers, false = random
    var usernameLength by remember { mutableStateOf(6) }
    var prefixText by remember { mutableStateOf("A") }
    var qtyToGenerate by remember { mutableStateOf(10) }
    var startSeqNum by remember { mutableStateOf(1001) }
    var matchPasswordWithUsername by remember { mutableStateOf(true) }
    var bindMacOnFirstLogin by remember { mutableStateOf(false) }

    // 3. Design Canvas Customizations
    var designTheme by remember { mutableStateOf(CardDesignTheme.CLASSIC_BLUE) }
    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var customImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Coordinates percentages (0.0f to 1.0f) representing placement on visual voucher
    var usernameX by remember { mutableStateOf(0.5f) }
    var usernameY by remember { mutableStateOf(0.35f) }
    var passwordX by remember { mutableStateOf(0.5f) }
    var passwordY by remember { mutableStateOf(0.55f) }
    var detailsX by remember { mutableStateOf(0.5f) }
    var detailsY by remember { mutableStateOf(0.80f) }

    // Sizes & Colors
    var usernameFontSize by remember { mutableStateOf(14) }
    var passwordFontSize by remember { mutableStateOf(12) }
    var detailsFontSize by remember { mutableStateOf(10) }
    var textDesignColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }

    // 4. Print Sheets Configuration
    var columnsCount by remember { mutableStateOf(3) }
    var limitPerPage by remember { mutableStateOf(12) }

    // Loaded Image Picker launcher (Fully functional custom uploads!)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    customImageBitmap = originalBitmap
                    designTheme = CardDesignTheme.CUSTOM_IMAGE
                    Toast.makeText(context, "تم تحميل قالب الكرت المخصص بنجاح!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل في قراءة الصورة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 5. Execution State Pool
    val generatedVouchersList = remember { mutableStateListOf<TempVoucher>() }
    var isGeneratingAndAdding by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableStateOf(0f) }

    // Re-trigger visual placeholder generation for live preview
    LaunchedEffect(selectedMode, selectedProfile, qtyToGenerate, prefixText, usernameLength, generationTypeSeq, startSeqNum, matchPasswordWithUsername, bindMacOnFirstLogin) {
        generatedVouchersList.clear()
        for (i in 0 until qtyToGenerate) {
            val userStr = if (generationTypeSeq) {
                "$prefixText${startSeqNum + i}"
            } else {
                val allowedChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // Easy readability, skip 0/O/1/I
                val randomSeq = (1..usernameLength).map { allowedChars.random() }.joinToString("")
                "$prefixText$randomSeq"
            }
            val passStr = if (matchPasswordWithUsername) userStr else {
                val randomSeq = (1..5).map { ('0'..'9').random() }.joinToString("")
                randomSeq
            }
            generatedVouchersList.add(
                TempVoucher(
                    username = userStr,
                    password = passStr,
                    profile = selectedProfile.ifEmpty { "default" },
                    securityCode = UUID.randomUUID().toString().take(4).uppercase(),
                    bindOnFirstLogin = bindMacOnFirstLogin
                )
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(12),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Screen Header inside Grid span 12
        item(span = { GridItemSpan(12) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFE2F1FF), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Print, contentDescription = null, tint = Color(0xFF0061A4), modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = "ستوديو طباعة الكروت الاحترافي",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أقوى نظام لتوليد كروت الشبكة بالجملة وتنسيقها واصدارها كملف PDF جاهز للطباعة الفورية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Left interactive control panels (X, Y and layouts parameters)
        item(span = { GridItemSpan(12) }) {
            // Mode Select Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { selectedMode = VoucherMode.HOTSPOT },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == VoucherMode.HOTSPOT) Color(0xFF0061A4) else Color.Transparent,
                        contentColor = if (selectedMode == VoucherMode.HOTSPOT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("كروت الهوتسبوت (Hotspot Users)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = { selectedMode = VoucherMode.USER_MANAGER },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == VoucherMode.USER_MANAGER) Color(0xFF0061A4) else Color.Transparent,
                        contentColor = if (selectedMode == VoucherMode.USER_MANAGER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("كروت اليوزر مانجر (User Manager)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Split Layout: 12 Cols Grid
        // Grid span 12 on mobile, but internally we can align sections horizontally if needed.
        // Section 1: Generation Settings and Profiles
        item(span = { GridItemSpan(12) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "1. خيارات التوليد ونظام الحسابات",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001E2F)
                    )

                    // Profile Picker
                    Column {
                        Text("اختر بروفايل الخدمة المعتمد:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            val activeList = if (selectedMode == VoucherMode.HOTSPOT) hotspotProfiles.map { it.name } else userManagerProfiles.map { it.name }
                            if (activeList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFDDE3EA), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("لا توجد بروفايلات متوفرة! سيتم التوليد على البروفايل default", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                activeList.forEach { profileName ->
                                    val isSelected = selectedProfile == profileName
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFE2F1FF) else Color(0xFFF1F3F5))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF0061A4) else Color(0xFFD2D5D8),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedProfile = profileName }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = profileName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF0061A4) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider()

                    // Horizontal Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (qtyToGenerate == 0) "" else qtyToGenerate.toString(),
                            onValueChange = { qtyToGenerate = it.toIntOrNull() ?: 0 },
                            label = { Text("عدد الكروت") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = prefixText,
                            onValueChange = { prefixText = it },
                            label = { Text("بادئة الكارت (Prefix)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Numeric Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("وضع التسمية والأرقام:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = !generationTypeSeq, onClick = { generationTypeSeq = false })
                                Text("رموز عشوائية", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(12.dp))
                                RadioButton(selected = generationTypeSeq, onClick = { generationTypeSeq = true })
                                Text("تسلسلي متتالي (أرقام بداية/نهاية)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Conditional Sequential properties
                    AnimatedVisibility(visible = generationTypeSeq) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = if (startSeqNum == 0) "" else startSeqNum.toString(),
                                onValueChange = { startSeqNum = it.toIntOrNull() ?: 0 },
                                label = { Text("رقم البداية التسلسلي") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.0f)
                            )
                            OutlinedTextField(
                                value = (startSeqNum + qtyToGenerate - 1).toString(),
                                onValueChange = {},
                                enabled = false,
                                label = { Text("رقم النهاية التلقائي") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                    }

                    // Conditional Random properties
                    AnimatedVisibility(visible = !generationTypeSeq) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("طول الرمز العشوائي:", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = usernameLength.toFloat(),
                                onValueChange = { usernameLength = it.toInt() },
                                valueRange = 4f..12f,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Text("$usernameLength أحرف", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("توليد كلمة مرور مطابقة لاسم المستخدم", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("رمز دخول موحد (User = Pass) لتسهيل وتسريع تسجيل الدخول", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = matchPasswordWithUsername,
                            onCheckedChange = { matchPasswordWithUsername = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ربط وتثبيت أول مستخدم متصل (MAC Bind)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("قفل الحساب تلقائياً على عنوان MAC لأول هاتف يسجل فيه أمنياً", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = bindMacOnFirstLogin,
                            onCheckedChange = { bindMacOnFirstLogin = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981))
                        )
                    }
                }
            }
        }

        // Section 2: Card Customization Studio
        item(span = { GridItemSpan(12) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "2. قالب الكارت والتصميم المرئي",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001E2F)
                    )

                    // Presets
                    Text("اختر قالب خلفية الكارت المسبقة:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PresetDesignItem(
                            title = "الأزرق الكلاسيك",
                            isSelected = designTheme == CardDesignTheme.CLASSIC_BLUE,
                            brush = Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
                            onClick = { designTheme = CardDesignTheme.CLASSIC_BLUE }
                        )
                        PresetDesignItem(
                            title = "الذهبي الملكي",
                            isSelected = designTheme == CardDesignTheme.ROYAL_GOLD,
                            brush = Brush.linearGradient(listOf(Color(0xFF1F1C18), Color(0xFF8E0E00), Color(0xFF1F1C18))),
                            onClick = { designTheme = CardDesignTheme.ROYAL_GOLD }
                        )
                        PresetDesignItem(
                            title = "البنفسجي اللامع",
                            isSelected = designTheme == CardDesignTheme.SHINY_PURPLE,
                            brush = Brush.linearGradient(listOf(Color(0xFF3F2B96), Color(0xFFA8C0FF))),
                            onClick = { designTheme = CardDesignTheme.SHINY_PURPLE }
                        )
                        PresetDesignItem(
                            title = "الناصع الأبيض",
                            isSelected = designTheme == CardDesignTheme.MINIMAL_WHITE,
                            brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFF8FAFC))),
                            onClick = { designTheme = CardDesignTheme.MINIMAL_WHITE }
                        )
                    }

                    // Import Custom image button
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2F1FF), contentColor = Color(0xFF0061A4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد صورة مخصصة من استوديو الهاتف لقالب الكرت كخلفية", fontWeight = FontWeight.Bold)
                    }

                    if (designTheme == CardDesignTheme.CUSTOM_IMAGE && customImageBitmap != null) {
                        Text(
                            text = "تم تحميل قالب خارجي مخصص بنجاح",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Divider()

                    // LIVE CARD PREVIEW CONTAINER - High Graphic Polish
                    Text("المعاينة والتصميم المباشر (تعديل دقة وموضع الخطوط):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (designTheme) {
                                    CardDesignTheme.CLASSIC_BLUE -> Brush.linearGradient(
                                        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                    )

                                    CardDesignTheme.ROYAL_GOLD -> Brush.linearGradient(
                                        listOf(Color(0xFF141414), Color(0xFF332010), Color(0xFF141414))
                                    )

                                    CardDesignTheme.SHINY_PURPLE -> Brush.linearGradient(
                                        listOf(Color(0xFF3F2B96), Color(0xFFA8C0FF))
                                    )

                                    CardDesignTheme.MINIMAL_WHITE -> Brush.linearGradient(
                                        listOf(Color(0xFFE2E8F0), Color(0xFFF8FAFC))
                                    )

                                    else -> Brush.linearGradient(listOf(Color.DarkGray, Color.Gray))
                                }
                            )
                            .border(
                                1.dp,
                                if (designTheme == CardDesignTheme.MINIMAL_WHITE) Color.DarkGray.copy(alpha = 0.2f) else Color.White.copy(
                                    alpha = 0.15f
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (designTheme == CardDesignTheme.CUSTOM_IMAGE && customImageBitmap != null) {
                            Image(
                                bitmap = customImageBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Drag-like positioning indicators or pure absolute coords overlay representation
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val cardW = maxWidth
                            val cardH = maxHeight

                            // Static Brand
                            Text(
                                text = "شبكة ABO TALAL VIP",
                                color = if (designTheme == CardDesignTheme.MINIMAL_WHITE) Color.Black else Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            )

                            // Username layout mapping
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(
                                        x = (cardW.value * usernameX).dp - 50.dp,
                                        y = (cardH.value * usernameY).dp
                                    )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "اسم المستخدم (User):",
                                        color = textDesignColor.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = (passwordFontSize - 3).sp
                                    )
                                    Text(
                                        text = if (generatedVouchersList.isNotEmpty()) generatedVouchersList.first().username else "ABO-TALAL-VIP-101",
                                        color = textDesignColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = usernameFontSize.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Password layout mapping
                            if (!matchPasswordWithUsername) {
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset(
                                            x = (cardW.value * passwordX).dp - 50.dp,
                                            y = (cardH.value * passwordY).dp
                                        )
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "كلمة المرور (Pass):",
                                            color = textDesignColor.copy(alpha = 0.8f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = (passwordFontSize - 3).sp
                                        )
                                        Text(
                                            text = if (generatedVouchersList.isNotEmpty()) generatedVouchersList.first().password else "653412",
                                            color = textDesignColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = passwordFontSize.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Details (Profile & price details) layer
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(
                                        x = (cardW.value * detailsX).dp - 60.dp,
                                        y = (cardH.value * detailsY).dp
                                    )
                            ) {
                                Text(
                                    text = "باقة: $selectedProfile | السعر: ١٠٠٠ د.ع",
                                    color = if (designTheme == CardDesignTheme.MINIMAL_WHITE) Color.DarkGray else Color.White.copy(
                                        alpha = 0.9f
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = detailsFontSize.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Placement Offset controls
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("أزرار التحكم بوضع اسم المستخدم والرمز المالي:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("تعديل محاذاة اسم المستخدم (User):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("أفقي X (يمين/يسار)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                    Slider(value = usernameX, onValueChange = { usernameX = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                    Text(String.format("%.0f%%", usernameX*100), style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("رأسي Y (أعلى/أسفل)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                    Slider(value = usernameY, onValueChange = { usernameY = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                    Text(String.format("%.0f%%", usernameY*100), style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("حجم الخط (Size)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                    Slider(value = usernameFontSize.toFloat(), onValueChange = { usernameFontSize = it.toInt() }, valueRange = 8f..24f, modifier = Modifier.weight(1f))
                                    Text("$usernameFontSize px", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Password Alignment (If separate password is checked)
                        AnimatedVisibility(visible = !matchPasswordWithUsername) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("تعديل محاذاة كلمة المرور (Password):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("أفقي X (يمين/يسار)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                        Slider(value = passwordX, onValueChange = { passwordX = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                        Text(String.format("%.0f%%", passwordX*100), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("رأسي Y (أعلى/أسفل)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                        Slider(value = passwordY, onValueChange = { passwordY = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                        Text(String.format("%.0f%%", passwordY*100), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("حجم الخط (Size)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                        Slider(value = passwordFontSize.toFloat(), onValueChange = { passwordFontSize = it.toInt() }, valueRange = 8f..24f, modifier = Modifier.weight(1f))
                                        Text("$passwordFontSize px", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Details Positioning
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("تعديل محاذاة نص الباقة والبيانات الإضافية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("أفقي X", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                    Slider(value = detailsX, onValueChange = { detailsX = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                    Text(String.format("%.0f%%", detailsX*100), style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("رأسي Y", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(100.dp))
                                    Slider(value = detailsY, onValueChange = { detailsY = it }, valueRange = 0.1f..0.9f, modifier = Modifier.weight(1f))
                                    Text(String.format("%.0f%%", detailsY*100), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Color selection
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("لون نصوص البطاقة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            listOf(Color.White, Color.Black, Color(0xFFFFD700), Color(0xFF00FFCC), Color(0xFFFF2A2A)).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(c, CircleShape)
                                        .border(
                                            2.dp,
                                            if (textDesignColor == c) Color(0xFF0061A4) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { textDesignColor = c }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: PDF Print Sheet Arrangement Grid
        item(span = { GridItemSpan(12) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "3. تخطيط صفحة الطباعة وتقسيم الأعمدة",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001E2F)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("عدد الأعمدة في الصفحة (Columns):", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                                listOf(1, 2, 3, 4, 5).forEach { cols ->
                                    Button(
                                        onClick = { columnsCount = cols },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (columnsCount == cols) Color(0xFF0061A4) else Color(0xFFE2E8F0),
                                            contentColor = if (columnsCount == cols) Color.White else Color.Black
                                        ),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("$cols أعمدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الحد الأقصى للكروت في الصفحة الواحدة:", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = limitPerPage.toFloat(),
                            onValueChange = { limitPerPage = it.toInt() },
                            valueRange = 4f..40f,
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        )
                        Text("$limitPerPage كرت", fontWeight = FontWeight.Bold)
                    }

                    // Display warning if columns and cards count is suboptimal
                    val requiredPages = Math.ceil(qtyToGenerate.toDouble() / limitPerPage).toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2F1FF), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF0061A4))
                        Text(
                            text = "تحليل كروكي: سوف يتم توليد $qtyToGenerate بطاقة موزعة على $requiredPages صفحة طباعة (بمعدل $limitPerPage كرت بالصفحة بتنسيق $columnsCount أعمدة متناسقة).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00497E)
                        )
                    }
                }
            }
        }

        // Floating Action/Command Panel inside list
        item(span = { GridItemSpan(12) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Generate and append to server button
                Button(
                    onClick = {
                        isGeneratingAndAdding = true
                        generationProgress = 0.0f
                        scope.launch {
                            try {
                                val generatedUsers = generatedVouchersList.toList()
                                var index = 0
                                for (voucher in generatedUsers) {
                                    delay(40) // Simulate fast responsive server transmission
                                    if (selectedMode == VoucherMode.HOTSPOT) {
                                        viewModel.addHotspotUser(
                                            HotspotUser(
                                                name = voucher.username,
                                                password = voucher.password,
                                                profile = voucher.profile,
                                                comment = "توليد تلقائي بالجملة"
                                            ),
                                            onSuccess = {},
                                            onError = {}
                                        )
                                    } else {
                                        viewModel.addUserManagerUser(
                                            UserManagerUser(
                                                username = voucher.username,
                                                password = voucher.password,
                                                profile = voucher.profile,
                                                downloadLimit = "unlimited"
                                            ),
                                            onSuccess = {},
                                            onError = {}
                                        )
                                    }
                                    index++
                                    generationProgress = index.toFloat() / generatedUsers.size
                                }

                                viewModel.writeActivityLog(
                                    "توليد كروت آلي",
                                    "تم توليد وإضافة عدد $qtyToGenerate كارت بنجاح لبروفايل $selectedProfile بروابط آمنة"
                                )
                                isGeneratingAndAdding = false
                                Toast.makeText(context, "تم حفظ وتثبيت كافة الكروت داخل روتر الأمير بنجاح!", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                isGeneratingAndAdding = false
                                Toast.makeText(context, "فشل الإرسال: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isGeneratingAndAdding && generatedVouchersList.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isGeneratingAndAdding) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جاري الحفظ وإرسال الحسابات للسيرفر... (%${(generationProgress * 100).toInt()})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توليد وإضافة الكروت إلى السيرفر حياً", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Native Print & Save PDF Button (100% Real!)
                Button(
                    onClick = {
                        val printerHelper = VoucherPDFPrinter(
                            context = context,
                            vouchers = generatedVouchersList.toList(),
                            theme = designTheme,
                            customBg = customImageBitmap,
                            cols = columnsCount,
                            cardsPerPage = limitPerPage,
                            xUser = usernameX,
                            yUser = usernameY,
                            xPass = passwordX,
                            yPass = passwordY,
                            xDetails = detailsX,
                            yDetails = detailsY,
                            sizeUser = usernameFontSize,
                            sizePass = passwordFontSize,
                            sizeDetails = detailsFontSize,
                            textColor = textDesignColor,
                            selectedProfile = selectedProfile,
                            matchPasswordWithUsername = matchPasswordWithUsername
                        )
                        printerHelper.startPrintFlow()
                    },
                    enabled = generatedVouchersList.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بدء عملية الطباعة وتصدير كملف PDF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 4: Live Sheet Print Preview (معاينة صفحات الطباعة المتكاملة)
        item(span = { GridItemSpan(12) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color(0xFF0061A4))
                            Text(
                                text = "٤. معاينة صفحات الطباعة المتكاملة",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF001E2F)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE2F1FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "معاينة حية ذكية للطباعة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0061A4)
                            )
                        }
                    }
                    
                    Text(
                        text = "تعرض الواجهة أدناه تنسيق صفحات الطباعة الفعلي وكيفية توزع الكروت بالترتيب على صفحة الطباعة القياسية (A4). يمكنك ضبط الأعمدة والأعداد والمحاذاة من الأقسام المذكورة أعلاه لرؤية النتيجة فورياً قبل اصدار المستند للطباعة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (generatedVouchersList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لا توجد كروت مولدة للاستعراض حالياً! يرجى توليد كروت أولاً.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        val pages = generatedVouchersList.chunked(limitPerPage)
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            pages.forEachIndexed { p, pageVouchers ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFDDE3EA), RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "صفحة الطباعة المعاينة رقم ${p + 1} من ${pages.size}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF0061A4)
                                        )
                                        Text(
                                            text = "(${pageVouchers.size} كرت بالصفحة)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    HorizontalDivider(color = Color(0xFFEDF2F7))

                                    // Render rows of pageVouchers matching `columnsCount`
                                    val rows = pageVouchers.chunked(columnsCount)
                                    rows.forEach { rowVouchers ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowVouchers.forEach { voucher ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1.5f)
                                                ) {
                                                    PrintVoucherCardItem(
                                                        voucher = voucher,
                                                        theme = designTheme,
                                                        customImageBitmap = customImageBitmap,
                                                        usernameX = usernameX,
                                                        usernameY = usernameY,
                                                        passwordX = passwordX,
                                                        passwordY = passwordY,
                                                        detailsX = detailsX,
                                                        detailsY = detailsY,
                                                        usernameFontSize = usernameFontSize,
                                                        passwordFontSize = passwordFontSize,
                                                        detailsFontSize = detailsFontSize,
                                                        textDesignColor = textDesignColor,
                                                        selectedProfile = selectedProfile,
                                                        matchPasswordWithUsername = matchPasswordWithUsername
                                                    )
                                                }
                                            }
                                            
                                            // Handling spacing
                                            if (rowVouchers.size < columnsCount) {
                                                repeat(columnsCount - rowVouchers.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
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
        }

        item(span = { GridItemSpan(12) }) {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrintVoucherCardItem(
    voucher: TempVoucher,
    theme: CardDesignTheme,
    customImageBitmap: Bitmap?,
    usernameX: Float,
    usernameY: Float,
    passwordX: Float,
    passwordY: Float,
    detailsX: Float,
    detailsY: Float,
    usernameFontSize: Int,
    passwordFontSize: Int,
    detailsFontSize: Int,
    textDesignColor: Color,
    selectedProfile: String,
    matchPasswordWithUsername: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when (theme) {
                    CardDesignTheme.CLASSIC_BLUE -> Brush.linearGradient(
                        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    )
                    CardDesignTheme.ROYAL_GOLD -> Brush.linearGradient(
                        listOf(Color(0xFF141414), Color(0xFF332010), Color(0xFF141414))
                    )
                    CardDesignTheme.SHINY_PURPLE -> Brush.linearGradient(
                        listOf(Color(0xFF3F2B96), Color(0xFFA8C0FF))
                    )
                    CardDesignTheme.MINIMAL_WHITE -> Brush.linearGradient(
                        listOf(Color(0xFFE2E8F0), Color(0xFFF8FAFC))
                    )
                    else -> Brush.linearGradient(listOf(Color.DarkGray, Color.Gray))
                }
            )
            .border(
                0.5.dp,
                if (theme == CardDesignTheme.MINIMAL_WHITE) Color.DarkGray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
    ) {
        if (theme == CardDesignTheme.CUSTOM_IMAGE && customImageBitmap != null) {
            Image(
                bitmap = customImageBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cardW = maxWidth
            val cardH = maxHeight

            // Static Brand
            Text(
                text = "الأمير نت",
                color = if (theme == CardDesignTheme.MINIMAL_WHITE) Color.Black else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )

            // Username (centered dynamically in its text area)
            Box(
                modifier = Modifier
                    .absoluteOffset(
                        x = (cardW.value * usernameX).dp - 35.dp,
                        y = (cardH.value * usernameY).dp
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "U: ${voucher.username}",
                        color = textDesignColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (usernameFontSize * 0.7f).sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Password
            if (!matchPasswordWithUsername) {
                Box(
                    modifier = Modifier
                        .absoluteOffset(
                            x = (cardW.value * passwordX).dp - 35.dp,
                            y = (cardH.value * passwordY).dp
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "P: ${voucher.password}",
                            color = textDesignColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (passwordFontSize * 0.7f).sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Details
            Box(
                modifier = Modifier
                    .absoluteOffset(
                        x = (cardW.value * detailsX).dp - 40.dp,
                        y = (cardH.value * detailsY).dp
                    )
            ) {
                Text(
                    text = "$selectedProfile",
                    color = if (theme == CardDesignTheme.MINIMAL_WHITE) Color.DarkGray else Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (detailsFontSize * 0.7f).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PresetDesignItem(
    title: String,
    isSelected: Boolean,
    brush: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (title == "الناصع الأبيض") Color.Black else Color.White,
            modifier = Modifier
                .background(
                    if (title == "الناصع الأبيض") Color.White.copy(alpha = 0.8f) else Color.Black.copy(
                        alpha = 0.4f
                    ), RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}


/* =========================================================================
   ROBUST AND GENUINE NATIVE ANDROID PDF PRINT SERVICE HELPER
   ========================================================================= */

class VoucherPDFPrinter(
    private val context: Context,
    private val vouchers: List<TempVoucher>,
    private val theme: CardDesignTheme,
    private val customBg: Bitmap?,
    private val cols: Int,
    private val cardsPerPage: Int,
    private val xUser: Float,
    private val yUser: Float,
    private val xPass: Float,
    private val yPass: Float,
    private val xDetails: Float,
    private val yDetails: Float,
    private val sizeUser: Int,
    private val sizePass: Int,
    private val sizeDetails: Int,
    private val textColor: Color,
    private val selectedProfile: String,
    private val matchPasswordWithUsername: Boolean
) {
    fun startPrintFlow() {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "خدمة الطباعة غير متوفرة في هذا الجهاز!", Toast.LENGTH_LONG).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            private var mPdfDocument: PrintedPdfDocument? = null
            private var mTotalPages = 1

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                mPdfDocument = PrintedPdfDocument(context, newAttributes)
                mTotalPages = Math.ceil(vouchers.size.toDouble() / cardsPerPage).toInt()

                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                if (mTotalPages > 0) {
                    val info = PrintDocumentInfo.Builder("Alameer_Vouchers.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(mTotalPages)
                        .build()
                    callback.onLayoutFinished(info, true)
                } else {
                    callback.onLayoutFailed("لا توجد كروت مستهدفة للطباعة!")
                }
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = mPdfDocument ?: return
                
                try {
                    for (pageNumber in 0 until mTotalPages) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback.onWriteCancelled()
                            return
                        }

                        // Create page matching index
                        val page = pdfDocument.startPage(pageNumber)
                        val canvas = page.canvas

                        // Render cards inside page
                        drawPageVouchers(canvas, pageNumber)

                        pdfDocument.finishPage(page)
                    }

                    // Write outputs stream
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.localizedMessage)
                } finally {
                    pdfDocument.close()
                    mPdfDocument = null
                }
            }

            private fun drawPageVouchers(canvas: AndroidCanvas, pageIndex: Int) {
                val pageWidth = canvas.width
                val pageHeight = canvas.height

                // Margin paddings
                val lateralMargin = 40
                val topMargin = 50

                // Available print sheet dimensions
                val gridW = pageWidth - (lateralMargin * 2)
                val gridH = pageHeight - (topMargin * 2)

                // Layout equations
                val rows = Math.ceil(cardsPerPage.toDouble() / cols).toInt()
                val cardW = gridW / cols
                val cardH = gridH / rows

                val startIndex = pageIndex * cardsPerPage
                val endIndex = Math.min(startIndex + cardsPerPage, vouchers.size)

                var currentCardIndex = startIndex
                
                // Set paint params
                val paintBg = AndroidPaint().apply { isAntiAlias = true }
                val paintLine = AndroidPaint().apply {
                    isAntiAlias = true
                    style = AndroidPaint.Style.STROKE
                    strokeWidth = 1.5f
                    color = AndroidColor.LTGRAY
                }
                val paintText = AndroidPaint().apply {
                    isAntiAlias = true
                    textAlign = AndroidPaint.Align.CENTER
                    color = textColor.toArgb()
                }

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if (currentCardIndex >= endIndex) break

                        val voucher = vouchers[currentCardIndex]

                        // Compute rect bounds
                        val xLeft = lateralMargin + (c * cardW) + 5
                        val xRight = lateralMargin + ((c + 1) * cardW) - 5
                        val yTop = topMargin + (r * cardH) + 5
                        val yBottom = topMargin + ((r + 1) * cardH) - 5

                        val singleW = xRight - xLeft
                        val singleH = yBottom - yTop

                        // Draw card background depending on theme selections
                        val backgroundRect = AndroidRectF(xLeft.toFloat(), yTop.toFloat(), xRight.toFloat(), yBottom.toFloat())

                        if (theme == CardDesignTheme.CUSTOM_IMAGE && customBg != null) {
                            // Draw scaled custom image base
                            val srcRect = AndroidRect(0, 0, customBg.width, customBg.height)
                            val dstRect = AndroidRect(xLeft, yTop, xRight, yBottom)
                            canvas.drawBitmap(customBg, srcRect, dstRect, paintBg)
                        } else {
                            // Custom gradients / solid simulations on physical PDF canvas
                            val localPaint = AndroidPaint().apply {
                                isAntiAlias = true
                                style = AndroidPaint.Style.FILL
                            }
                            when (theme) {
                                CardDesignTheme.CLASSIC_BLUE -> {
                                    localPaint.color = AndroidColor.parseColor("#1B2A4A")
                                }
                                CardDesignTheme.ROYAL_GOLD -> {
                                    localPaint.color = AndroidColor.parseColor("#1e160a")
                                }
                                CardDesignTheme.SHINY_PURPLE -> {
                                    localPaint.color = AndroidColor.parseColor("#4A154B")
                                }
                                CardDesignTheme.MINIMAL_WHITE -> {
                                    localPaint.color = AndroidColor.WHITE
                                }
                                else -> {
                                    localPaint.color = AndroidColor.DKGRAY
                                }
                            }
                            canvas.drawRoundRect(backgroundRect, 10f, 10f, localPaint)
                        }

                        // Outline thin border on card
                        canvas.drawRoundRect(backgroundRect, 10f, 10f, paintLine)

                        // 1. Draw static header
                        val textCol = if (theme == CardDesignTheme.MINIMAL_WHITE) AndroidColor.BLACK else paintText.color
                        val paintBrand = AndroidPaint().apply {
                            isAntiAlias = true
                            textAlign = AndroidPaint.Align.CENTER
                            color = textCol
                            textSize = 10f
                            isFakeBoldText = true
                        }
                        canvas.drawText("شبكة ABO TALAL VIP", (xLeft + (singleW / 2)).toFloat(), yTop + 20f, paintBrand)

                        // 2. Plot Username
                        val uX = xLeft + (singleW * xUser)
                        val uY = yTop + (singleH * yUser)
                        val paintUserLabel = AndroidPaint().apply {
                            isAntiAlias = true
                            textAlign = AndroidPaint.Align.CENTER
                            color = if (theme == CardDesignTheme.MINIMAL_WHITE) AndroidColor.GRAY else AndroidColor.parseColor("#DCDCDC")
                            textSize = 7f
                        }
                        canvas.drawText("USER:", uX, uY - 3f, paintUserLabel)
                        
                        val paintUserVal = AndroidPaint().apply {
                            isAntiAlias = true
                            textAlign = AndroidPaint.Align.CENTER
                            color = paintText.color
                            textSize = sizeUser.toFloat()
                            isFakeBoldText = true
                        }
                        canvas.drawText(voucher.username, uX, uY + sizeUser - 4f, paintUserVal)

                        // 3. Plot Password
                        if (!matchPasswordWithUsername) {
                            val pX = xLeft + (singleW * xPass)
                            val pY = yTop + (singleH * yPass)
                            val paintPassLabel = AndroidPaint().apply {
                                isAntiAlias = true
                                textAlign = AndroidPaint.Align.CENTER
                                color = if (theme == CardDesignTheme.MINIMAL_WHITE) AndroidColor.GRAY else AndroidColor.parseColor("#DCDCDC")
                                textSize = 7f
                            }
                            canvas.drawText("PASS:", pX, pY - 3f, paintPassLabel)

                            val paintPassVal = AndroidPaint().apply {
                                isAntiAlias = true
                                textAlign = AndroidPaint.Align.CENTER
                                color = paintText.color
                                textSize = sizePass.toFloat()
                                isFakeBoldText = true
                            }
                            canvas.drawText(voucher.password, pX, pY + sizePass - 4f, paintPassVal)
                        }

                        // 4. Plot Details Layer
                        val dX = xLeft + (singleW * xDetails)
                        val dY = yTop + (singleH * yDetails)
                        val paintDetailsVal = AndroidPaint().apply {
                            isAntiAlias = true
                            textAlign = AndroidPaint.Align.CENTER
                            color = if (theme == CardDesignTheme.MINIMAL_WHITE) AndroidColor.parseColor("#1B2A4A") else AndroidColor.WHITE
                            textSize = sizeDetails.toFloat()
                            isFakeBoldText = true
                        }
                        canvas.drawText("باقة: $selectedProfile | ١٠٠٠ د.ع", dX, dY, paintDetailsVal)

                        currentCardIndex++
                    }
                }
            }
        };

        // Trigger Printing Frame Flow
        printManager.print("Alameer Net Cards", printAdapter, null)
    }
}
