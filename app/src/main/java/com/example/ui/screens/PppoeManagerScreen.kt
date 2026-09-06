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
import com.example.data.model.PppoeUser
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PppoeManagerScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var usersList by remember { mutableStateOf<List<PppoeUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: الكل, 1: المتصلين حالياً
    var showAddDialog by remember { mutableStateOf(false) }

    fun refreshUsers() {
        isLoading = true
        viewModel.fetchPppoeUsers(
            onSuccess = {
                usersList = it
                isLoading = false
            },
            onError = {
                isLoading = false
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshUsers()
    }

    val activeCount = usersList.count { it.isActive }
    val filteredList = usersList.filter { u ->
        val matchSearch = searchQuery.isBlank() ||
                u.name.contains(searchQuery, ignoreCase = true) ||
                u.comment.contains(searchQuery, ignoreCase = true)
        val matchTab = if (selectedTab == 1) u.isActive else true
        matchSearch && matchTab
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
                                        Color(0xFF1E3A8A),
                                        Color(0xFF2563EB),
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
                                        text = "إدارة حسابات البروباند (PPPoE)",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "الاشتراكات المنزلية، السيرفرات، والاتصالات النشطة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFBFDBFE)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = "إضافة مشترك", tint = Color.White, modifier = Modifier.size(20.dp))
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
                // Summary bar
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي المشتركين: ${usersList.size} | متصل الآن: $activeCount",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E3A8A)
                        )

                        IconButton(onClick = { refreshUsers() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = "تحديث", tint = Color(0xFF2563EB))
                        }
                    }
                }

                // Filter & Search
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن اسم المشترك...", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("الكل (${usersList.size})", fontFamily = CairoFontFamily, fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("المتصلين ($activeCount)", fontFamily = CairoFontFamily, fontSize = 11.sp) }
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                } else if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد مستخدمي بروباند مطابقين للبحث", fontFamily = CairoFontFamily, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredList) { user ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
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
                                                color = if (user.isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Filled.AccountCircle,
                                                        contentDescription = null,
                                                        tint = if (user.isActive) Color(0xFF16A34A) else Color.Gray,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = user.name,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "البروفايل: ${user.profile}",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (user.isActive) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
                                        ) {
                                            Text(
                                                text = if (user.isActive) "متصل (${user.uptime})" else "غير متصل",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.isActive) Color(0xFF15803D) else Color.Gray,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    if (user.comment.isNotBlank() || user.callerId.isNotBlank() || user.localAddress.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = buildString {
                                                if (user.comment.isNotBlank()) append("ملاحظة: ${user.comment}  ")
                                                if (user.callerId.isNotBlank()) append("| MAC: ${user.callerId}  ")
                                                if (user.localAddress.isNotBlank()) append("| IP: ${user.localAddress}")
                                            },
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                viewModel.deletePppoeUser(
                                                    name = user.name,
                                                    onSuccess = {
                                                        Toast.makeText(context, "تم حذف المستخدم ${user.name}", Toast.LENGTH_SHORT).show()
                                                        refreshUsers()
                                                    },
                                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("حذف الحساب", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFFEF4444))
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

    // Add User Dialog
    if (showAddDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newProfile by remember { mutableStateOf("default") }
        var newComment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مشترك بروباند جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("اسم المستخدم (Username)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("كلمة المرور (Password)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProfile,
                        onValueChange = { newProfile = it },
                        label = { Text("بروفايل السرعة (Profile)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("ملاحظة / اسم المشترك (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isNotBlank()) {
                            viewModel.addPppoeUser(
                                user = PppoeUser(
                                    name = newUsername.trim(),
                                    password = newPassword.trim(),
                                    profile = newProfile.trim().ifBlank { "default" },
                                    comment = newComment.trim()
                                ),
                                onSuccess = {
                                    showAddDialog = false
                                    Toast.makeText(context, "تمت إضافة المستخدم بنجاح", Toast.LENGTH_SHORT).show()
                                    refreshUsers()
                                },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("إضافة وحفظ", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
