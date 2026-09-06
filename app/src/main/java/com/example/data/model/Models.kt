package com.example.data.model

import java.util.UUID

data class HotspotUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val password: String = "",
    val profile: String = "default",
    val limitUptime: String = "unlimited",
    val comment: String = "",
    val disabled: Boolean = false,
    val bytesOut: Long = 0,
    val bytesIn: Long = 0
)

data class HotspotActive(
    val id: String = UUID.randomUUID().toString(),
    val user: String,
    val address: String,
    val macAddress: String,
    val uptime: String,
    val bytesIn: Long,
    val bytesOut: Long,
    val keepaliveTimeout: String = ""
)

data class HotspotProfile(
    val name: String,
    val sharedUsers: String = "1",
    val rateLimit: String = "unlimited",
    val keepaliveTimeout: String = "2m"
)

data class UserManagerUser(
    val username: String,
    val password: String = "",
    val profile: String = "default",
    val active: Boolean = true,
    val uptimeUsed: String = "0s",
    val downloadLimit: String = "unlimited",
    val serialNumber: String = "-",
    val comment: String = ""
)

data class UserManagerProfile(
    val name: String,
    val validity: String = "30d",
    val price: Double = 0.0,
    val sharedUsers: Int = 1,
    val downloadLimit: String = "unlimited",
    val uptimeLimit: String = "unlimited",
    val templateName: String = ""
)

data class InterfaceStats(
    val name: String,
    val type: String,
    val rxByte: Long,
    val txByte: Long,
    val rxSpeedKbps: Double,
    val txSpeedKbps: Double,
    val running: Boolean = true
)

data class RouterStats(
    val cpuUsage: Int, // 0-100%
    val totalMemoryBytes: Long,
    val freeMemoryBytes: Long,
    val boardName: String,
    val version: String,
    val uptime: String,
    val activeHotspotUsers: Int,
    val activeUserManagerUsers: Int,
    val totalInterfaces: Int
)

data class CardPrintTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Default_Design",
    val userManagerProfile: String = "",
    val hotspotProfile: String = "",
    val columns: Int = 3,
    val rows: Int = 17,
    val autoFitA4: Boolean = true,
    val horizontalMarginMm: Float = 0f,
    val verticalMarginMm: Float = 0f,
    // Page Note
    val pageNoteEnabled: Boolean = false,
    val pageNoteColorHex: String = "#000000",
    val pageNoteX: Float = 40f,
    val pageNoteY: Float = 6f,
    val pageNoteSize: Float = 10f,
    val pageNoteText: String = "",
    // Page Numbering
    val pageNumberingEnabled: Boolean = false,
    val pageNumberingColorHex: String = "#000000",
    val pageNumberingX: Float = 0f,
    val pageNumberingY: Float = 0f,
    val pageNumberingSize: Float = 10f,
    val pageNumberingText: String = "{page}",
    // Background & Border
    val backgroundEnabled: Boolean = true,
    val backgroundPreset: String = "bg_card_100", // "bg_card_100", "bg_card_200", "bg_card_500", "custom"
    val customBackgroundPath: String = "",
    val borderEnabled: Boolean = true,
    val borderSizeMm: Float = 0.35f,
    val borderColorHex: String = "#000000",
    // User element
    val usernameEnabled: Boolean = true,
    val usernameShowLabel: Boolean = false,
    val usernameLabel: String = "اسم المستخدم",
    val usernameX: Float = 50f, // percentage or dp offset
    val usernameY: Float = 50f,
    val usernameFontSize: Float = 13f,
    val usernameColorHex: String = "#000000",
    val usernameBold: Boolean = true,
    val usernameItalic: Boolean = false,
    // Barcode element
    val barcodeEnabled: Boolean = false,
    val barcodeSize: Float = 18f,
    val barcodeWidth: Float = 18f,
    val barcodeHeight: Float = 18f,
    // Logo element
    val logoEnabled: Boolean = false,
    val logoWidth: Float = 10f,
    val logoHeight: Float = 10f,
    val logoX: Float = 4f,
    val logoY: Float = 8f,
    // Point of Sale element
    val posEnabled: Boolean = false,
    val posText: String = "نقطة البيع: المركز الرئيسي",
    val posSize: Float = 10f,
    val posColorHex: String = "#000000",
    // Batch Number element
    val batchEnabled: Boolean = false,
    val batchText: String = "الدفعة: 1",
    val batchSize: Float = 10f,
    val batchColorHex: String = "#000000",
    // Serial Number element
    val serialEnabled: Boolean = false,
    val serialSize: Float = 10f,
    val serialColorHex: String = "#000000"
)

data class PppoeUser(
    val name: String,
    val password: String = "",
    val service: String = "pppoe",
    val profile: String = "default",
    val localAddress: String = "",
    val remoteAddress: String = "",
    val comment: String = "",
    val disabled: Boolean = false,
    val callerId: String = "",
    val uptime: String = "0s",
    val isActive: Boolean = false
)

data class TelegramBotItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val token: String,
    val chatId: String,
    val isDefault: Boolean = false,
    val isConnected: Boolean = false,
    val botUsername: String = ""
)

data class TelegramNotificationSettings(
    val notifyNewCardSold: Boolean = true,
    val notifyServerReboot: Boolean = true,
    val notifyHighCpu: Boolean = true,
    val notifyNewActiveUser: Boolean = false,
    val notifyDailySummary: Boolean = true
)

data class RouterGenericItem(
    val id: String = "",
    val title: String,
    val subtitle: String = "",
    val status: String = "",
    val extra: String = "",
    val isEnabled: Boolean = true,
    val rawProperties: Map<String, String> = emptyMap()
)

