package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.model.UserManagerProfile
import com.example.data.model.UserManagerUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import com.example.util.CardPrintAndExportHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * شاشة إدارة الكروت (نظام اليوزرمانجر)
 * مطابقة تماماً للمظهر والوظائف في لقطة الشاشة مع دعم:
 * - الفلترة ومصدر الكروت
 * - البحث والترتيب
 * - التفعيل والتجديد واختيار باقة لتجديد الكرت
 * - الحذف الفردي والجماعي
 * - العرض الكامل والتفصيلي
 * - شريط الترقيم والتنقل بين الصفحات
 */
@Composable
fun UserManagerCardsScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rawUsers by viewModel.userManagerUsers.collectAsState()
    val profiles by viewModel.userManagerProfiles.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    // Filter states
    var selectedScope by remember { mutableStateOf("جميع الكروت من الروتر") }
    var scopeMenuExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchFieldType by remember { mutableStateOf("الاسم") }
    var searchMenuExpanded by remember { mutableStateOf(false) }

    var isFullView by remember { mutableStateOf(false) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    // Multi-selection state
    val selectedUsernames = remember { mutableStateOf(setOf<String>()) }

    // Pagination states
    var pageSize by remember { mutableIntStateOf(50) }
    var pageSizeMenuExpanded by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }

    // Dialog states
    var selectedCardForDetails by remember { mutableStateOf<UserManagerUser?>(null) }
    var cardToRenew by remember { mutableStateOf<UserManagerUser?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<String?>(null) }

    // Rotation animation for refresh
    val rotationAnim = remember { Animatable(0f) }

    // Filtered list computation
    val filteredUsers = remember(rawUsers, selectedScope, searchQuery, searchFieldType) {
        rawUsers.filter { user ->
            // 1. Scope filter
            val matchesScope = when (selectedScope) {
                "الكروت المفعلة (النشطة)" -> user.active
                "الكروت غير المفعلة" -> !user.active
                "الكروت المنتهية" -> user.uptimeUsed.contains("expired", ignoreCase = true) || !user.active
                else -> true
            }

            // 2. Search filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                when (searchFieldType) {
                    "الاسم" -> user.username.contains(searchQuery.trim(), ignoreCase = true)
                    "الباقة" -> user.profile.contains(searchQuery.trim(), ignoreCase = true)
                    "الرقم التسلسلي" -> user.serialNumber.contains(searchQuery.trim(), ignoreCase = true)
                    "الحالة" -> (if (user.active) "تم التفعيل" else "غير مفعل").contains(searchQuery.trim(), ignoreCase = true)
                    else -> user.username.contains(searchQuery.trim(), ignoreCase = true)
                }
            }

            matchesScope && matchesSearch
        }
    }

    // Pagination calculations
    val effectivePageSize = if (pageSize <= 0) filteredUsers.size.coerceAtLeast(1) else pageSize
    val totalPages = if (filteredUsers.isEmpty()) 1 else Math.ceil(filteredUsers.size.toDouble() / effectivePageSize).toInt().coerceAtLeast(1)
    val safeCurrentPage = currentPage.coerceIn(1, totalPages)

    val pagedUsers = remember(filteredUsers, safeCurrentPage, effectivePageSize) {
        val startIndex = (safeCurrentPage - 1) * effectivePageSize
        filteredUsers.drop(startIndex).take(effectivePageSize)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                // Top App Bar
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp),
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
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEDF5F8),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onBack() }
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
                                Column {
                                    Text(
                                        text = "إدارة الكروت",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "نظام اليوزرمانجر • MikroTik User Manager",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Connection Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isConnected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
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
                                            .background(if (isConnected) Color(0xFF16A34A) else Color(0xFF0C5A60))
                                    )
                                    Text(
                                        text = if (isConnected) "متصل" else "محلي",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isConnected) Color(0xFF15803D) else Color(0xFF0C5A60)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // -------------------------------------------------------------
                // 1. Top Scope / Source Dropdown Selector (كما في لقطة الشاشة)
                // -------------------------------------------------------------
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scopeMenuExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Icon and Arrow
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Router,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF475569)
                            )
                        }

                        // Right: Scope Label
                        Text(
                            text = selectedScope,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    DropdownMenu(
                        expanded = scopeMenuExpanded,
                        onDismissRequest = { scopeMenuExpanded = false }
                    ) {
                        listOf(
                            "جميع الكروت من الروتر",
                            "الكروت المفعلة (النشطة)",
                            "الكروت غير المفعلة",
                            "الكروت المنتهية"
                        ).forEach { scopeOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = scopeOption,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedScope == scopeOption) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedScope == scopeOption) Color(0xFF0C5A60) else Color(0xFF1E293B)
                                    )
                                },
                                onClick = {
                                    selectedScope = scopeOption
                                    currentPage = 1
                                    scopeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 2. Search & Filter Bar (كما في لقطة الشاشة)
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Action / Export Button on the left (Teal rounded square with down arrow)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0C5A60),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                exportCardsCsv(context, filteredUsers)
                            }
                            .testTag("cards_download_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = "تصدير",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search text field in the middle
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            currentPage = 1
                        },
                        placeholder = {
                            Text(
                                text = "بحث",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "بحث",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "مسح",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0C5A60),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cards_search_field")
                    )

                    // Filter Type Selector on the right (الاسم / الباقة / الخ)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = Color.White,
                            modifier = Modifier
                                .height(48.dp)
                                .clickable { searchMenuExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF475569)
                                )
                                Text(
                                    text = searchFieldType,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = searchMenuExpanded,
                            onDismissRequest = { searchMenuExpanded = false }
                        ) {
                            listOf("الاسم", "الباقة", "الرقم التسلسلي", "الحالة").forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = type,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (searchFieldType == type) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        searchFieldType = type
                                        searchMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. Action Buttons Bar (كما في لقطة الشاشة)
                // [تفعيل] [حذف] [عرض كامل] [🔄] [⋮]
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Actions: [تفعيل] [حذف]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // تفعيل (Activate / Renew)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF99F6E4), // Soft Cyan/Teal as in screenshot
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val selected = selectedUsernames.value
                                    if (selected.isEmpty()) {
                                        Toast.makeText(context, "يرجى تحديد كرت واحد على الأقل للتفعيل", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.bulkToggleUserManagerUsers(selected.toList(), active = true) { count ->
                                            Toast.makeText(context, "تم تفعيل $count كرت بنجاح", Toast.LENGTH_SHORT).show()
                                            selectedUsernames.value = emptySet()
                                        }
                                    }
                                }
                                .testTag("cards_activate_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircleOutline,
                                    contentDescription = "تفعيل",
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "تفعيل",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                        }

                        // حذف (Delete)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFCA5A5), // Soft Pink/Red as in screenshot
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val selected = selectedUsernames.value
                                    if (selected.isEmpty()) {
                                        Toast.makeText(context, "يرجى تحديد الكروت المراد حذفها", Toast.LENGTH_SHORT).show()
                                    } else {
                                        userToDelete = null // Bulk delete flag
                                        showDeleteConfirmDialog = true
                                    }
                                }
                                .testTag("cards_delete_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFB91C1C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "حذف",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }

                    // Right Actions: [عرض كامل] [🔄] [⋮]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // عرض كامل (Toggle Full View)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF0C5A60)),
                            color = if (isFullView) Color(0xFF0C5A60) else Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isFullView = !isFullView }
                                .testTag("cards_toggle_view_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "عرض كامل",
                                    tint = if (isFullView) Color.White else Color(0xFF0C5A60),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "عرض كامل",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFullView) Color.White else Color(0xFF0C5A60)
                                )
                            }
                        }

                        // Refresh circular button 🔄
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0C5A60),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    scope.launch {
                                        rotationAnim.animateTo(
                                            targetValue = rotationAnim.value + 360f,
                                            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                                        )
                                    }
                                    Toast.makeText(context, "جاري تحديث الكروت من السيرفر...", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("cards_refresh_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "تحديث",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(rotationAnim.value)
                                )
                            }
                        }

                        // 3-dots Menu ⋮
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                color = Color.White,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { overflowMenuExpanded = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "المزيد",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = overflowMenuExpanded,
                                onDismissRequest = { overflowMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("تحديد الكل في هذه الصفحة", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        selectedUsernames.value = pagedUsers.map { it.username }.toSet()
                                        overflowMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("إلغاء التحديد", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        selectedUsernames.value = emptySet()
                                        overflowMenuExpanded = false
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("تنظيف الكروت المنتهية", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.cleanExpiredUserManagerUsers { count ->
                                            Toast.makeText(context, "تم تنظيف $count كرت منتهي", Toast.LENGTH_SHORT).show()
                                        }
                                        overflowMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("تصدير ملف إكسيل (.csv)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    onClick = {
                                        exportCardsCsv(context, filteredUsers)
                                        overflowMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 4. Data Table Container (Header + List Rows)
                // -------------------------------------------------------------
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Table Header matching the screenshot:
                        // [اسم المستخدم]  |  [الرقم التسلسلي]  |  [الحالة]
                        Surface(
                            color = Color(0xFF0C5A60), // Dark Teal matching screenshot
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "اسم المستخدم",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.Start
                                )
                                Text(
                                    text = "الرقم التسلسلي",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "الحالة",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        // Table Data List
                        if (pagedUsers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "لا توجد كروت مطابقة لمعايير البحث",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(pagedUsers, key = { _, user -> user.username }) { index, user ->
                                    val isSelected = selectedUsernames.value.contains(user.username)
                                    val rowBg = if (isSelected) {
                                        Color(0xFFE0F2FE)
                                    } else if (index % 2 == 1) {
                                        Color(0xFFF8FAFC)
                                    } else {
                                        Color.White
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(rowBg)
                                            .clickable {
                                                selectedCardForDetails = user
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Username Column
                                            Row(
                                                modifier = Modifier.weight(1.2f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Checkbox for bulk selection
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        val cur = selectedUsernames.value.toMutableSet()
                                                        if (checked) cur.add(user.username) else cur.remove(user.username)
                                                        selectedUsernames.value = cur
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60)),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = user.username,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Serial Number Column
                                            Text(
                                                text = if (user.serialNumber.isNotBlank()) user.serialNumber else "-",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )

                                            // Status Column (Soft Cyan text "تم التفعيل" or "غير مفعل")
                                            Box(
                                                modifier = Modifier.weight(1f),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Text(
                                                    text = if (user.active) "تم التفعيل" else "غير مفعل",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = if (user.active) Color(0xFF0284C7) else Color(0xFFEF4444)
                                                )
                                            }
                                        }

                                        // Detailed expansion if "عرض كامل" is active
                                        AnimatedVisibility(visible = isFullView) {
                                            Surface(
                                                color = Color(0xFFF1F5F9),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text(
                                                            text = "الرمز: ${user.password}",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF0C5A60),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "الباقة: ${user.profile}",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF475569)
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        // Renew button
                                                        OutlinedButton(
                                                            onClick = { cardToRenew = user },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.height(26.dp)
                                                        ) {
                                                            Text("تجديد", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF0C5A60))
                                                        }

                                                        // Quick delete button
                                                        IconButton(
                                                            onClick = {
                                                                userToDelete = user.username
                                                                showDeleteConfirmDialog = true
                                                            },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Delete,
                                                                contentDescription = "حذف",
                                                                tint = Color(0xFFEF4444),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 5. Bottom Toolbar & Pagination (كما في لقطة الشاشة)
                // [🔍] [العدد: 4723]  [عرض: 50 ▼]
                // [>|] [>]  (95 / 1)  [<] [|<]
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Search inspect icon + Total Count + Page Size selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Count Pill with inspect icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Rounded inspect/search icon button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Total Count container
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                color = Color.White,
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0C5A60))
                                        )
                                        Text(
                                            text = "العدد",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Text(
                                        text = "${filteredUsers.size}",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        // Right: Page Size Selector (عرض: 50 ▼)
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                color = Color.White,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { pageSizeMenuExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "عرض",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = if (pageSize <= 0) "الكل" else "$pageSize",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = pageSizeMenuExpanded,
                                onDismissRequest = { pageSizeMenuExpanded = false }
                            ) {
                                listOf(20, 50, 100, 200).forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("$size كرت في الصفحة", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                        onClick = {
                                            pageSize = size
                                            currentPage = 1
                                            pageSizeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Bottom Pagination Controls [>|] [>]  (95 / 1)  [<] [|<]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // First Page [>|]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = if (safeCurrentPage > 1) Color.White else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = safeCurrentPage > 1) { currentPage = 1 }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.FirstPage,
                                    contentDescription = "الصفحة الأولى",
                                    tint = if (safeCurrentPage > 1) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Next Page [>]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = if (safeCurrentPage < totalPages) Color.White else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = safeCurrentPage < totalPages) { currentPage++ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "التالي",
                                    tint = if (safeCurrentPage < totalPages) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Current Page Pill (95 / 1)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = Color.White,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$safeCurrentPage / $totalPages",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        // Previous Page [<]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = if (safeCurrentPage > 1) Color.White else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = safeCurrentPage > 1) { currentPage-- }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = "السابق",
                                    tint = if (safeCurrentPage > 1) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Last Page [|<]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = if (safeCurrentPage < totalPages) Color.White else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = safeCurrentPage < totalPages) { currentPage = totalPages }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.LastPage,
                                    contentDescription = "الصفحة الأخيرة",
                                    tint = if (safeCurrentPage < totalPages) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Dialog 1: Card Details & Management Dialog
    // -------------------------------------------------------------
    selectedCardForDetails?.let { user ->
        CardDetailsDialog(
            user = user,
            profiles = profiles,
            onDismiss = { selectedCardForDetails = null },
            onRenew = {
                cardToRenew = user
                selectedCardForDetails = null
            },
            onToggleActive = {
                viewModel.toggleUserManagerUser(user.username) {
                    Toast.makeText(context, "تم تغيير حالة الكرت", Toast.LENGTH_SHORT).show()
                }
                selectedCardForDetails = null
            },
            onDelete = {
                userToDelete = user.username
                showDeleteConfirmDialog = true
                selectedCardForDetails = null
            },
            onPrint = {
                CardPrintAndExportHelper.printSingleCardDirectly(context, user)
            }
        )
    }

    // -------------------------------------------------------------
    // Dialog 2: Renew Card Dialog (تجديد الكرت واختيار باقة)
    // -------------------------------------------------------------
    cardToRenew?.let { user ->
        RenewCardDialog(
            user = user,
            profiles = profiles,
            onDismiss = { cardToRenew = null },
            onConfirmRenew = { targetProfile, resetUptime ->
                viewModel.renewUserManagerUser(
                    username = user.username,
                    newProfile = targetProfile,
                    resetUptime = resetUptime,
                    onSuccess = {
                        Toast.makeText(context, "✅ تم تجديد الكرت (${user.username}) بالباقة ($targetProfile) بنجاح!", Toast.LENGTH_LONG).show()
                        cardToRenew = null
                    },
                    onError = { err ->
                        Toast.makeText(context, "تعذر التجديد: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // -------------------------------------------------------------
    // Dialog 3: Delete Confirmation Dialog
    // -------------------------------------------------------------
    if (showDeleteConfirmDialog) {
        val count = if (userToDelete != null) 1 else selectedUsernames.value.size
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                userToDelete = null
            },
            title = {
                Text(
                    text = "تأكيد حذف الكروت",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = if (userToDelete != null) {
                        "هل أنت متأكد من حذف الكرت (${userToDelete}) نهائياً من نظام اليوزرمانجر والروتر؟"
                    } else {
                        "هل أنت متأكد من حذف $count كرت محدد نهائياً من نظام اليوزرمانجر والروتر؟"
                    },
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userToDelete != null) {
                            viewModel.deleteUserManagerUser(
                                username = userToDelete!!,
                                onSuccess = {
                                    Toast.makeText(context, "تم حذف الكرت بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                onError = { Toast.makeText(context, "فشل الحذف", Toast.LENGTH_SHORT).show() }
                            )
                        } else {
                            viewModel.bulkDeleteUserManagerUsers(
                                usernames = selectedUsernames.value.toList(),
                                onSuccess = { deletedCount ->
                                    Toast.makeText(context, "تم حذف $deletedCount كرت بنجاح", Toast.LENGTH_SHORT).show()
                                    selectedUsernames.value = emptySet()
                                },
                                onError = { Toast.makeText(context, "فشل الحذف الجماعي", Toast.LENGTH_SHORT).show() }
                            )
                        }
                        showDeleteConfirmDialog = false
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("نعم، احذف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userToDelete = null
                    }
                ) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

/**
 * Dialog: تفاصيل وإدارة الكرت
 */
@Composable
fun CardDetailsDialog(
    user: UserManagerUser,
    profiles: List<UserManagerProfile>,
    onDismiss: () -> Unit,
    onRenew: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onPrint: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تفاصيل الكرت",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "بيانات الحساب والاستهلاك",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // User Info Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Username & Copy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "اسم المستخدم:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = user.username,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0C5A60)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("username", user.username))
                                            Toast.makeText(context, "تم نسخ اسم المستخدم", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Password & Copy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "كلمة المرور:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = user.password.ifEmpty { user.username },
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("password", user.password.ifEmpty { user.username }))
                                            Toast.makeText(context, "تم نسخ كلمة المرور", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Profile
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الباقة الحالية:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = user.profile,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFD97706)
                                )
                            }

                            // Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "حالة الكرت:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (user.active) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = if (user.active) "نشط ومفعل" else "معطل وغير مفعل",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.active) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Uptime Used
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الوقت المستهلك:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = user.uptimeUsed,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }

                    // Action Buttons Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. زر تجديد الكرت (Renew)
                        Button(
                            onClick = onRenew,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تجديد الكرت واختيار باقة جديدة",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // 2. زر تفعيل / تعطيل + طباعة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleActive,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (user.active) "تعطيل الكرت" else "تفعيل الكرت",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = if (user.active) Color(0xFFD97706) else Color(0xFF16A34A)
                                )
                            }

                            OutlinedButton(
                                onClick = onPrint,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طباعة الكرت", fontFamily = CairoFontFamily, fontSize = 12.sp)
                            }
                        }

                        // 3. زر حذف الكرت
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حذف الكرت نهائياً",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog: تجديد الكرت واختيار باقة من باقات اليوزرمانجر
 */
@Composable
fun RenewCardDialog(
    user: UserManagerUser,
    profiles: List<UserManagerProfile>,
    onDismiss: () -> Unit,
    onConfirmRenew: (targetProfile: String, resetUptime: Boolean) -> Unit
) {
    var selectedProfileName by remember {
        mutableStateOf(if (profiles.any { it.name == user.profile }) user.profile else profiles.firstOrNull()?.name ?: "500RY")
    }
    var resetUptime by remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFCCFBF1),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Autorenew,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "تجديد الكرت",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "الكرت: ${user.username}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0C5A60),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "اختر الباقة المطلوبة لتجديد الكرت بها:",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )

                    // Profile options list
                    val displayProfiles = if (profiles.isNotEmpty()) profiles else listOf(
                        UserManagerProfile(name = "500RY", validity = "10 ايام", price = 500.0, downloadLimit = "2GB"),
                        UserManagerProfile(name = "300RY", validity = "1 اسبوع", price = 300.0, downloadLimit = "1.5GB"),
                        UserManagerProfile(name = "3000RY", validity = "30 يوم", price = 3000.0, downloadLimit = "10GB")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayProfiles.forEach { profile ->
                            val isSelected = selectedProfileName == profile.name
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF0C5A60) else Color(0xFFCBD5E1)),
                                color = if (isSelected) Color(0xFFF0FDFA) else Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedProfileName = profile.name }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedProfileName = profile.name },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0C5A60))
                                        )
                                        Column {
                                            Text(
                                                text = profile.name,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "الصلاحية: ${profile.validity} | التحميل: ${profile.downloadLimit}",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${profile.price.toInt()} د.ع",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0C5A60)
                                    )
                                }
                            }
                        }
                    }

                    // Reset Uptime Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { resetUptime = !resetUptime },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = resetUptime,
                            onCheckedChange = { resetUptime = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0C5A60))
                        )
                        Text(
                            text = "تصفير وقت الاستهلاك والعدادات (Start Fresh)",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    // Confirm Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontFamily = CairoFontFamily)
                        }

                        Button(
                            onClick = {
                                onConfirmRenew(selectedProfileName, resetUptime)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تأكيد التجديد",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper: Export cards list to CSV file with UTF-8 BOM so Excel opens it with Arabic text cleanly
 */
private fun exportCardsCsv(context: Context, users: List<UserManagerUser>) {
    try {
        val fileName = "UserManager_Cards_${System.currentTimeMillis()}.csv"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("م,اسم المستخدم,الرقم التسلسلي,كلمة المرور,الباقة,الحالة,الوقت المستهلك\n")
                users.forEachIndexed { index, user ->
                    val num = index + 1
                    val uName = sanitize(user.username)
                    val uSerial = sanitize(user.serialNumber)
                    val uPass = sanitize(user.password)
                    val uProf = sanitize(user.profile)
                    val uStatus = if (user.active) "تم التفعيل" else "غير مفعل"
                    val uUptime = sanitize(user.uptimeUsed)
                    writer.write("$num,$uName,$uSerial,$uPass,$uProf,$uStatus,$uUptime\n")
                }
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة أو فتح ملف الكروت (Excel)"))
        Toast.makeText(context, "تم تجهيز ملف Excel بنجاح", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "حدث خطأ أثناء تصدير الملف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun sanitize(input: String?): String {
    if (input == null) return ""
    var s = input.replace("\"", "\"\"")
    if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
        s = "\"$s\""
    }
    return s
}
