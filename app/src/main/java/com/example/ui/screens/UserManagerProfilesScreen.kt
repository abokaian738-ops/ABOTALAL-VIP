package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagerProfilesDialog(
    viewModel: MikroTikViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profiles by viewModel.userManagerProfiles.collectAsState()
    val userManagerUsers by viewModel.userManagerUsers.collectAsState()

    // Dialogs state
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<UserManagerProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<UserManagerProfile?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Pagination state
    val itemsPerPage = 4
    var currentPage by remember { mutableIntStateOf(1) }

    val totalPages = remember(profiles) {
        val count = (profiles.size + itemsPerPage - 1) / itemsPerPage
        if (count < 1) 1 else count
    }

    // Keep currentPage within bounds
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) {
            currentPage = totalPages
        }
    }

    val currentProfilesPage = remember(profiles, currentPage) {
        val startIndex = (currentPage - 1) * itemsPerPage
        profiles.drop(startIndex).take(itemsPerPage)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                color = Color(0xFFF1F5F9)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // --- 1. Top Header Bar matching Screenshot ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Title & Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE0F2FE),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Layers,
                                            contentDescription = null,
                                            tint = Color(0xFF0C5A60),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "الباقات",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // --- Scrollable Content ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // --- 2. Top 3 Action Buttons matching Screenshot ---
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Button 1: استيراد باقات اليوزرمانجر (Dark Teal)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0C5A60),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            isImporting = true
                                            viewModel.importUserManagerProfiles(
                                                onSuccess = { count ->
                                                    isImporting = false
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "تم استيراد ومزامنة $count باقة يوزرمانجر بنجاح!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                },
                                                onError = { err ->
                                                    isImporting = false
                                                    Toast
                                                        .makeText(context, "خطأ: $err", Toast.LENGTH_SHORT)
                                                        .show()
                                                }
                                            )
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isImporting) "جاري الاستيراد..." else "استيراد باقات اليوزارمنجر",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.FileDownload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Button 2: تحديث (Ocean Blue)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            isRefreshing = true
                                            viewModel.refreshAllData()
                                            Toast
                                                .makeText(
                                                    context,
                                                    "تم تحديث باقات اليوزرمانجر بنجاح",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                            isRefreshing = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تحديث",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                // Button 3: + إضافة باقة (Emerald Green)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showAddDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إضافة باقة",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // --- 3. List of Profile Cards matching Screenshot ---
                        if (profiles.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(30.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LayersClear,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "لا توجد باقات حالياً في نظام اليوزرمانجر",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "انقر على 'استيراد باقات اليوزارمنجر' أو 'إضافة باقة' للبدء",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(currentProfilesPage, key = { it.name }) { profile ->
                                val linkedCardsCount = remember(userManagerUsers, profile.name) {
                                    userManagerUsers.count { it.profile == profile.name }
                                }

                                ProfileDetailCard(
                                    profile = profile,
                                    linkedCardsCount = linkedCardsCount,
                                    onEdit = { profileToEdit = profile },
                                    onDelete = { profileToDelete = profile }
                                )
                            }
                        }

                        // --- 4. Pagination Bar matching Screenshot ---
                        if (totalPages > 1 || profiles.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Previous Page Button
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentPage > 1) Color.White else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(enabled = currentPage > 1) {
                                                    if (currentPage > 1) currentPage--
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ChevronRight,
                                                    contentDescription = "السابق",
                                                    tint = if (currentPage > 1) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "السابق",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (currentPage > 1) Color(0xFF0C5A60) else Color(0xFF94A3B8)
                                                )
                                            }
                                        }

                                        // Page Numbers Row
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            (1..totalPages).forEach { pageNum ->
                                                val isSelected = pageNum == currentPage
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) Color(0xFF0C5A60) else Color.White,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) Color(0xFF0C5A60) else Color(0xFFCBD5E1)
                                                    ),
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { currentPage = pageNum }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "$pageNum",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else Color(0xFF0C5A60)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Next Page Button
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentPage < totalPages) Color.White else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(enabled = currentPage < totalPages) {
                                                    if (currentPage < totalPages) currentPage++
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "التالي",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (currentPage < totalPages) Color(0xFF0C5A60) else Color(0xFF94A3B8)
                                                )
                                                Icon(
                                                    imageVector = Icons.Filled.ChevronLeft,
                                                    contentDescription = "التالي",
                                                    tint = if (currentPage < totalPages) Color(0xFF0C5A60) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialog: إضافة باقة جديدة ---
    if (showAddDialog) {
        ProfileEditDialog(
            title = "إضافة باقة يوزرمانجر جديدة",
            initialProfile = UserManagerProfile(
                name = "",
                validity = "1 اسبوع",
                price = 500.0,
                downloadLimit = "2 جيجابايت",
                uptimeLimit = "72 ساعات",
                templateName = "",
                sharedUsers = 1
            ),
            onSave = { newProf ->
                viewModel.addUserManagerProfile(
                    profile = newProf,
                    onSuccess = {
                        showAddDialog = false
                        Toast.makeText(context, "تمت إضافة الباقة '${newProf.name}' بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // --- Dialog: تعديل باقة ---
    profileToEdit?.let { prof ->
        ProfileEditDialog(
            title = "تعديل باقة: ${prof.name}",
            initialProfile = prof,
            onSave = { updatedProf ->
                viewModel.updateUserManagerProfile(
                    oldName = prof.name,
                    profile = updatedProf,
                    onSuccess = {
                        profileToEdit = null
                        Toast.makeText(context, "تم حفظ تعديلات الباقة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = { profileToEdit = null }
        )
    }

    // --- Dialog: تأكيد حذف الباقة ---
    profileToDelete?.let { prof ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "حذف الباقة: ${prof.name}",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك بحذف هذه الباقة من راوتر المايكروتيك وقائمة الباقات؟",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUserManagerProfile(
                            profileName = prof.name,
                            onSuccess = {
                                profileToDelete = null
                                Toast.makeText(context, "تم حذف الباقة بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("تأكيد الحذف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

/**
 * Profile Card matching Screenshot_٢٠٢٦٠٨٠٥-١٧١٥٤٠.jpg exactly
 */
@Composable
fun ProfileDetailCard(
    profile: UserManagerProfile,
    linkedCardsCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // --- Header Row: Avatar + Name + Edit/Delete Icons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Avatar Icon + Profile Name + Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Green Avatar Icon
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.WifiProtectedSetup,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = profile.name,
                            fontFamily = CairoFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "User Manager",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Right: Edit & Delete Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit (Green Pencil)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "تعديل",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete (Red Trash)
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "حذف",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 6 Info Pills (2 columns x 3 rows) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: عدد الكروت المرتبطة | قالب الطباعة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(
                        icon = Icons.Outlined.ConfirmationNumber,
                        iconTint = Color(0xFF0D9488),
                        text = "$linkedCardsCount عدد الكروت المرتبطة",
                        modifier = Modifier.weight(1f)
                    )
                    InfoPill(
                        icon = Icons.Outlined.Palette,
                        iconTint = Color(0xFF0284C7),
                        text = "${profile.templateName.ifBlank { profile.name }} قالب الطباعة",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: التحميل | السعر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(
                        icon = Icons.Filled.ArrowDownward,
                        iconTint = Color(0xFF10B981),
                        text = "${profile.downloadLimit.ifBlank { "بلا حدود" }} التحميل",
                        modifier = Modifier.weight(1f)
                    )
                    InfoPill(
                        icon = Icons.Outlined.MonetizationOn,
                        iconTint = Color(0xFFF59E0B),
                        text = "${profile.price.toInt()} السعر",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: الوقت | الصلاحية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(
                        icon = Icons.Outlined.Schedule,
                        iconTint = Color(0xFF06B6D4),
                        text = "${profile.uptimeLimit.ifBlank { "بلا حدود" }} الوقت",
                        modifier = Modifier.weight(1f)
                    )
                    InfoPill(
                        icon = Icons.Outlined.CalendarMonth,
                        iconTint = Color(0xFF0C5A60),
                        text = "${profile.validity.ifBlank { "30 يوم" }} الصلاحية",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Modern pill badge inside profile card
 */
@Composable
fun InfoPill(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF0F9FF),
        border = BorderStroke(1.dp, Color(0xFFE0F2FE)),
        modifier = modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Add / Edit Profile Dialog
 */
@Composable
fun ProfileEditDialog(
    title: String,
    initialProfile: UserManagerProfile,
    onSave: (UserManagerProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialProfile.name) }
    var price by remember { mutableStateOf(if (initialProfile.price > 0) initialProfile.price.toInt().toString() else "500") }
    var downloadLimit by remember { mutableStateOf(initialProfile.downloadLimit.ifBlank { "2 جيجابايت" }) }
    var uptimeLimit by remember { mutableStateOf(initialProfile.uptimeLimit.ifBlank { "72 ساعات" }) }
    var validity by remember { mutableStateOf(initialProfile.validity.ifBlank { "10 ايام" }) }
    var templateName by remember { mutableStateOf(initialProfile.templateName.ifBlank { initialProfile.name }) }
    var sharedUsers by remember { mutableIntStateOf(initialProfile.sharedUsers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (templateName.isBlank() || templateName == initialProfile.name) {
                            templateName = it
                        }
                    },
                    label = { Text("اسم الباقة (مثال: 500RY)", fontFamily = CairoFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Price & Shared Users Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("السعر (د.ع / ريال)", fontFamily = CairoFontFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = downloadLimit,
                        onValueChange = { downloadLimit = it },
                        label = { Text("التحميل (مثال: 2 جيجابايت)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Validity & Uptime Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = validity,
                        onValueChange = { validity = it },
                        label = { Text("الصلاحية (مثال: 10 ايام)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uptimeLimit,
                        onValueChange = { uptimeLimit = it },
                        label = { Text("الوقت (مثال: 72 ساعات)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Template Name Field
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("قالب الطباعة المرتبط", fontFamily = CairoFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Preset Chips for Quick Validity
                Text("خيارات سريعة للصلاحية:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("4 ايام", "5 ايام", "10 ايام", "1 اسبوع", "4 اسبوع").forEach { opt ->
                        FilterChip(
                            selected = validity == opt,
                            onClick = { validity = opt },
                            label = { Text(opt, fontFamily = CairoFontFamily, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val prof = UserManagerProfile(
                            name = name.trim(),
                            validity = validity.trim(),
                            price = price.toDoubleOrNull() ?: 0.0,
                            sharedUsers = sharedUsers,
                            downloadLimit = downloadLimit.trim(),
                            uptimeLimit = uptimeLimit.trim(),
                            templateName = templateName.ifBlank { name }.trim()
                        )
                        onSave(prof)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
            ) {
                Text("حفظ الباقة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", fontFamily = CairoFontFamily)
            }
        }
    )
}
