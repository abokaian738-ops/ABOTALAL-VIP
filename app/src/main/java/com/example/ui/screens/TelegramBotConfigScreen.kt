package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TelegramBotItem
import com.example.data.model.TelegramNotificationSettings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.MikroTikViewModel
import com.example.util.TelegramHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramBotConfigScreen(
    viewModel: MikroTikViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Saved Bots
    var savedBots by remember { mutableStateOf(TelegramHelper.loadSavedBots(context)) }
    var selectedBotIndex by remember { mutableIntStateOf(0) }
    val currentBot = savedBots.getOrNull(selectedBotIndex) ?: savedBots.firstOrNull() ?: TelegramBotItem(name = "بوت الإشعارات", token = "", chatId = "")

    // Fields for current bot
    var botName by remember(currentBot.id) { mutableStateOf(currentBot.name) }
    var botToken by remember(currentBot.id) { mutableStateOf(currentBot.token) }
    var chatId by remember(currentBot.id) { mutableStateOf(currentBot.chatId) }

    // Settings
    var notifSettings by remember { mutableStateOf(TelegramHelper.loadSettings(context)) }

    // UI States
    var selectedTab by remember { mutableIntStateOf(0) } // 0: الإعدادات والربط, 1: تخصيص التنبيهات, 2: أوامر البوت المباشرة
    var isTestingConnection by remember { mutableStateOf(false) }
    var isFetchingChatId by remember { mutableStateOf(false) }
    var isSendingTestMsg by remember { mutableStateOf(false) }
    var isInstallingScript by remember { mutableStateOf(false) }

    var testResultDialogText by remember { mutableStateOf<String?>(null) }
    var showAddBotDialog by remember { mutableStateOf(false) }
    var showScriptPreviewDialog by remember { mutableStateOf(false) }

    fun persistCurrentBot() {
        val updatedList = savedBots.toMutableList()
        val idx = selectedBotIndex.coerceIn(0, updatedList.lastIndex.coerceAtLeast(0))
        if (idx in updatedList.indices) {
            updatedList[idx] = currentBot.copy(name = botName, token = botToken, chatId = chatId)
            savedBots = updatedList
            TelegramHelper.saveBots(context, updatedList)
        }
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
                                        Color(0xFF006699),
                                        Color(0xFF0088CC),
                                        Color(0xFF0C5A60)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "ربط وإشعارات التيليجرام",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00C853)
                                        ) {
                                            Text(
                                                text = "VIP Bot",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "تنبيهات فورية للمبيعات وحالة السيرفر وتحكم مباشر",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            // Bot switcher / quick add
                            IconButton(
                                onClick = { showAddBotDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "إضافة بوت جديد",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
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
                    .background(Color(0xFFF4F6F9))
            ) {
                // Tab Selection Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0088CC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "إعدادات البوت والربط",
                                fontFamily = CairoFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "تخصيص التنبيهات",
                                fontFamily = CairoFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "أوامر السيرفر",
                                fontFamily = CairoFontFamily,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Filled.Terminal, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                when (selectedTab) {
                    0 -> BotConnectionTab(
                        savedBots = savedBots,
                        selectedBotIndex = selectedBotIndex,
                        onSelectBot = {
                            persistCurrentBot()
                            selectedBotIndex = it
                        },
                        botName = botName,
                        onBotNameChange = { botName = it; persistCurrentBot() },
                        botToken = botToken,
                        onBotTokenChange = { botToken = it; persistCurrentBot() },
                        chatId = chatId,
                        onChatIdChange = { chatId = it; persistCurrentBot() },
                        isTestingConnection = isTestingConnection,
                        isFetchingChatId = isFetchingChatId,
                        isSendingTestMsg = isSendingTestMsg,
                        isInstallingScript = isInstallingScript,
                        onTestConnection = {
                            scope.launch {
                                isTestingConnection = true
                                val res = TelegramHelper.testBot(botToken)
                                isTestingConnection = false
                                testResultDialogText = res.second
                                if (res.first) {
                                    val list = savedBots.toMutableList()
                                    list[selectedBotIndex] = list[selectedBotIndex].copy(isConnected = true)
                                    savedBots = list
                                    TelegramHelper.saveBots(context, list)
                                }
                            }
                        },
                        onFetchChatId = {
                            scope.launch {
                                isFetchingChatId = true
                                val res = TelegramHelper.fetchChatId(botToken)
                                isFetchingChatId = false
                                if (res.first) {
                                    chatId = res.second
                                    persistCurrentBot()
                                    Toast.makeText(context, "✅ تم جلب معرف الشات بنجاح: ${res.second}", Toast.LENGTH_LONG).show()
                                } else {
                                    testResultDialogText = res.second
                                }
                            }
                        },
                        onOpenBotInTelegram = {
                            TelegramHelper.openBotInTelegram(context, currentBot.botUsername.ifBlank { "BotFather" })
                        },
                        onSendTestMessage = {
                            scope.launch {
                                isSendingTestMsg = true
                                val testMsg = """
                                    🚀 <b>[ABO TALAL VIP] - تجربة الاتصال بالبوت</b>
                                    ━━━━━━━━━━━━━━━━━━━━
                                    ✅ تم ربط تطبيق إدارة شبكات المايكروتك مع البوت بنجاح!
                                    📡 <b>السيرفر:</b> متصل ومستقر
                                    ⏰ <b>التاريخ:</b> ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}
                                    ━━━━━━━━━━━━━━━━━━━━
                                    تهانينا! الإشعارات جاهزة للعمل ومراقبة شبكتك على مدار الساعة.
                                """.trimIndent()
                                val res = TelegramHelper.sendMessage(botToken, chatId, testMsg)
                                isSendingTestMsg = false
                                testResultDialogText = res.second
                            }
                        },
                        onInstallScriptOnRouter = {
                            showScriptPreviewDialog = true
                        },
                        onAddNewBot = { showAddBotDialog = true }
                    )

                    1 -> BotAlertsCustomizationTab(
                        settings = notifSettings,
                        onSettingsChange = {
                            notifSettings = it
                            TelegramHelper.saveSettings(context, it)
                            Toast.makeText(context, "تم حفظ تخصيصات التنبيهات", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> BotCommandsTab(
                        botToken = botToken,
                        chatId = chatId
                    )
                }
            }
        }
    }

    // Dialog: Result or Info
    testResultDialogText?.let { text ->
        AlertDialog(
            onDismissRequest = { testResultDialogText = null },
            title = {
                Text("نتيجة فحص التيليجرام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text, fontFamily = CairoFontFamily, fontSize = 14.sp)
            },
            confirmButton = {
                Button(onClick = { testResultDialogText = null }) {
                    Text("حسناً", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Dialog: Add New Bot
    if (showAddBotDialog) {
        var newName by remember { mutableStateOf("") }
        var newToken by remember { mutableStateOf("") }
        var newChatId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBotDialog = false },
            title = {
                Text("إضافة بوت تيليجرام جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم البوت (مثال: بوت المبيعات)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newToken,
                        onValueChange = { newToken = it },
                        label = { Text("توكن البوت (Bot Token)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newChatId,
                        onValueChange = { newChatId = it },
                        label = { Text("معرف المحادثة (Chat ID - اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newToken.isNotBlank()) {
                            val newBot = TelegramBotItem(
                                name = newName.trim(),
                                token = newToken.trim(),
                                chatId = newChatId.trim()
                            )
                            val updated = savedBots + newBot
                            savedBots = updated
                            selectedBotIndex = updated.lastIndex
                            TelegramHelper.saveBots(context, updated)
                            showAddBotDialog = false
                            Toast.makeText(context, "تمت إضافة البوت بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "يرجى ملء الاسم والتوكن", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("إضافة وحفظ", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBotDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Dialog: Script Preview & Direct Install to Router
    if (showScriptPreviewDialog) {
        val scriptSource = TelegramHelper.generateRouterOsScript(botToken, chatId, botName)
        AlertDialog(
            onDismissRequest = { showScriptPreviewDialog = false },
            title = {
                Text("تثبيت السكربت على راوتر المايكروتك", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "يقوم هذا الإجراء بإنشاء سكربت رسمي داخل نظام RouterOS باسم (telegram_notify) لإرسال التنبيهات من الراوتر مباشرة إلى التيليجرام حتى لو كان تطبيق الهاتف مغلقاً!",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        Text(
                            text = scriptSource,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isInstallingScript = true
                        viewModel.installTelegramScript(
                            scriptName = "telegram_notify",
                            scriptSource = scriptSource,
                            onSuccess = {
                                isInstallingScript = false
                                showScriptPreviewDialog = false
                                Toast.makeText(context, "✅ تم تثبيت السكربت على الراوتر بنجاح!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isInstallingScript = false
                                Toast.makeText(context, "فشل التثبيت: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                ) {
                    if (isInstallingScript) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("تثبيت على الراوتر الآن 🚀", fontFamily = CairoFontFamily)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(scriptSource))
                        Toast.makeText(context, "تم نسخ كود السكربت للحافظة", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نسخ الكود", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

@Composable
private fun BotConnectionTab(
    savedBots: List<TelegramBotItem>,
    selectedBotIndex: Int,
    onSelectBot: (Int) -> Unit,
    botName: String,
    onBotNameChange: (String) -> Unit,
    botToken: String,
    onBotTokenChange: (String) -> Unit,
    chatId: String,
    onChatIdChange: (String) -> Unit,
    isTestingConnection: Boolean,
    isFetchingChatId: Boolean,
    isSendingTestMsg: Boolean,
    isInstallingScript: Boolean,
    onTestConnection: () -> Unit,
    onFetchChatId: () -> Unit,
    onOpenBotInTelegram: () -> Unit,
    onSendTestMessage: () -> Unit,
    onInstallScriptOnRouter: () -> Unit,
    onAddNewBot: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-Bot Selection Row
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قائمة البوتات المحفوظة (${savedBots.size})",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = onAddNewBot) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة بوت", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        savedBots.forEachIndexed { index, bot ->
                            FilterChip(
                                selected = selectedBotIndex == index,
                                onClick = { onSelectBot(index) },
                                label = {
                                    Text(
                                        text = bot.name.ifBlank { "بوت ${index + 1}" },
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (bot.isConnected) Icons.Filled.CheckCircle else Icons.Filled.SmartToy,
                                        contentDescription = null,
                                        tint = if (bot.isConnected) Color(0xFF10B981) else Color(0xFF0088CC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Main Credentials Form
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "بيانات الربط والاعتماد",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0088CC)
                    )

                    OutlinedTextField(
                        value = botName,
                        onValueChange = onBotNameChange,
                        label = { Text("اسم البوت التوضيحي", fontFamily = CairoFontFamily) },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = botToken,
                        onValueChange = onBotTokenChange,
                        label = { Text("توكن البوت (API Token)", fontFamily = CairoFontFamily) },
                        placeholder = { Text("1234567890:AAHq...") },
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = onTestConnection) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.NetworkCheck, contentDescription = "فحص التوكن", tint = Color(0xFF0088CC))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = chatId,
                        onValueChange = onChatIdChange,
                        label = { Text("معرف المحادثة أو القناة (Chat ID)", fontFamily = CairoFontFamily) },
                        placeholder = { Text("مثال: 987654321 أو @channel_name") },
                        leadingIcon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = onFetchChatId) {
                                if (isFetchingChatId) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.AutoFixHigh, contentDescription = "جلب الآي دي تلقائياً", tint = Color(0xFF10B981))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Helper banner with instructions
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            Text(
                                text = "💡 للحصول على الآي دي: افتح محادثة البوت بالضغط أدناه، ثم أرسل له كلمة (/start)، ثم اضغط زر العصا السحرية لجلب الـ Chat ID فوراً!",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الإجراءات والاختبارات الفورية",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0C5A60)
                    )

                    // Button: Open Telegram
                    OutlinedButton(
                        onClick = onOpenBotInTelegram,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF0088CC))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فتح المحادثة في تطبيق تيليجرام", fontFamily = CairoFontFamily, color = Color(0xFF0088CC), fontWeight = FontWeight.Bold)
                    }

                    // Button: Test Send Message
                    Button(
                        onClick = onSendTestMessage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC))
                    ) {
                        if (isSendingTestMsg) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال رسالة تجريبية لهاتفي", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Button: Direct Router Script Installation
                    Button(
                        onClick = onInstallScriptOnRouter,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5A60))
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تثبيت السكربت على المايكروتك مباشرة 🚀", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BotAlertsCustomizationTab(
    settings: TelegramNotificationSettings,
    onSettingsChange: (TelegramNotificationSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "حدد الأحداث التي تود تلقي إشعارات فورية عنها:",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }

        item {
            AlertToggleCard(
                title = "تنبيه بيع وتفعيل كرت جديد",
                subtitle = "يرسل إشعاراً فورياً باسم الكرت وسعره والفئة والوقت",
                icon = Icons.Filled.ConfirmationNumber,
                checked = settings.notifyNewCardSold,
                onCheckedChange = { onSettingsChange(settings.copy(notifyNewCardSold = it)) }
            )
        }

        item {
            AlertToggleCard(
                title = "تنبيه إعادة تشغيل السيرفر أو انقطاع التيار",
                subtitle = "تنبيهك فور إعادة إقلاع الراوتر وعودته للعمل",
                icon = Icons.Filled.PowerSettingsNew,
                checked = settings.notifyServerReboot,
                onCheckedChange = { onSettingsChange(settings.copy(notifyServerReboot = it)) }
            )
        }

        item {
            AlertToggleCard(
                title = "تنبيه ارتفاع استهلاك المعالج CPU > 90%",
                subtitle = "حماية الراوتر والتنبيه عند وجود ضغط غير طبيعي",
                icon = Icons.Filled.Speed,
                checked = settings.notifyHighCpu,
                onCheckedChange = { onSettingsChange(settings.copy(notifyHighCpu = it)) }
            )
        }

        item {
            AlertToggleCard(
                title = "تنبيه تسجيل دخول مستخدم نشط",
                subtitle = "إشعار عند اتصال عميل جديد بالهوتسبوت أو البروباند",
                icon = Icons.Filled.PersonAdd,
                checked = settings.notifyNewActiveUser,
                onCheckedChange = { onSettingsChange(settings.copy(notifyNewActiveUser = it)) }
            )
        }

        item {
            AlertToggleCard(
                title = "تقرير الملخص اليومي للمبيعات والأرباح",
                subtitle = "ملخص في نهاية كل يوم بعدد الكروت المباعة وإجمالي الإيراد",
                icon = Icons.Filled.Summarize,
                checked = settings.notifyDailySummary,
                onCheckedChange = { onSettingsChange(settings.copy(notifyDailySummary = it)) }
            )
        }
    }
}

@Composable
private fun AlertToggleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (checked) Color(0xFF0088CC).copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (checked) Color(0xFF0088CC) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0088CC))
            )
        }
    }
}

@Composable
private fun BotCommandsTab(
    botToken: String,
    chatId: String
) {
    val commands = listOf(
        Pair("/status", "عرض حالة الراوتر: المعالج، الذاكرة، وقت التشغيل، والمستخدمين النشطين."),
        Pair("/sales", "عرض إحصائية مبيعات الكروت لليوم الحالي والإجمالي."),
        Pair("/active", "عرض قائمة أسماء المستخدمين المتصلين حالياً على الهوتسبوت."),
        Pair("/reboot", "إرسال أمر إعادة تشغيل السيرفر من هاتفك عن بعد (يتطلب تأكيد)."),
        Pair("/ping [IP]", "فحص استجابة المودم أو اتصال خادم خارجي عبر الراوتر."),
        Pair("/free_on", "تفعيل وضع الشبكة المجانية بدون كروت مؤقتاً."),
        Pair("/free_off", "إعادة إغلاق الشبكة وتفعيل نظام الكروت الفوري.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Terminal, contentDescription = null, tint = Color(0xFF0284C7))
                    Text(
                        text = "يمكنك إرسال هذه الأوامر مباشرة إلى محادثة البوت للتحكم في الراوتر وتلقي الردود اللحظية:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }
            }
        }

        items(commands) { cmd ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cmd.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0088CC)
                        )
                        Text(
                            text = cmd.second,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "أمر معتمد",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
