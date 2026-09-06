package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.TelegramBotItem
import com.example.data.model.TelegramNotificationSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TelegramHelper {
    private const val TAG = "TelegramHelper"
    private const val PREFS_NAME = "abo_talal_telegram_prefs"
    private const val KEY_BOTS_JSON = "saved_bots_json"
    private const val KEY_SETTINGS_JSON = "notification_settings_json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Test connection to Telegram Bot API with getMe
     * Returns Pair<Boolean, botUsername or ErrorMessage>
     */
    suspend fun testBot(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) return@withContext Pair(false, "الرجاء إدخال توكن البوت (Bot Token)")
        
        try {
            val url = "https://api.telegram.org/bot$cleanToken/getMe"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                if (json.optBoolean("ok")) {
                    val result = json.getJSONObject("result")
                    val username = result.optString("username", "bot")
                    val firstName = result.optString("first_name", "Telegram Bot")
                    Pair(true, "متصل بنجاح! اسم البوت: @$username ($firstName)")
                } else {
                    val desc = json.optString("description", "التوكن غير صحيح")
                    Pair(false, "فشل الاتصال بالبوت: $desc")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error testing bot token", e)
            Pair(false, "تعذر الاتصال بخوادم تيليجرام: ${e.localizedMessage ?: "خطأ في الشبكة"}")
        }
    }

    /**
     * Fetch recent Chat ID automatically using getUpdates
     */
    suspend fun fetchChatId(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) return@withContext Pair(false, "الرجاء إدخال التوكن أولاً")

        try {
            val url = "https://api.telegram.org/bot$cleanToken/getUpdates?limit=10"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                if (json.optBoolean("ok")) {
                    val resultArray = json.optJSONArray("result") ?: JSONArray()
                    if (resultArray.length() == 0) {
                        return@withContext Pair(false, "لم يتم العثور على رسائل واردة!\n\nيرجى فتح البوت في تيليجرام والضغط على /start أو إرسال أي رسالة، ثم الضغط مجدداً على زر جلب الآي دي.")
                    }

                    // Look from latest to earliest for message or my_chat_member
                    for (i in resultArray.length() - 1 downTo 0) {
                        val update = resultArray.getJSONObject(i)
                        val message = update.optJSONObject("message")
                            ?: update.optJSONObject("my_chat_member")
                            ?: update.optJSONObject("channel_post")

                        val chat = message?.optJSONObject("chat")
                        if (chat != null) {
                            val chatId = chat.optLong("id", 0L)
                            val title = chat.optString("title", chat.optString("first_name", "مستخدم"))
                            if (chatId != 0L) {
                                return@withContext Pair(true, "$chatId")
                            }
                        }
                    }
                    Pair(false, "لم يتم استخراج الآيدي. أرسل رسالة للبوت ثم حاول ثانية.")
                } else {
                    val desc = json.optString("description", "خطأ في التوكن")
                    Pair(false, desc)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chat id", e)
            Pair(false, "خطأ أثناء جلب الـ ID: ${e.localizedMessage}")
        }
    }

    /**
     * Send test or notification message to the Telegram chat
     */
    suspend fun sendMessage(token: String, chatId: String, text: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        val cleanChatId = chatId.trim()
        if (cleanToken.isBlank() || cleanChatId.isBlank()) {
            return@withContext Pair(false, "الرجاء التأكد من تعبئة التوكن ومعرف الشات (Chat ID)")
        }

        try {
            val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
            val jsonPayload = JSONObject().apply {
                put("chat_id", cleanChatId)
                put("text", text)
                put("parse_mode", "HTML")
            }
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                if (json.optBoolean("ok")) {
                    Pair(true, "تم إرسال الرسالة إلى تليجرام بنجاح! 🚀")
                } else {
                    val desc = json.optString("description", "فشل الإرسال")
                    Pair(false, "فشل الإرسال: $desc")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending telegram message", e)
            Pair(false, "خطأ في الاتصال بتيليجرام: ${e.localizedMessage}")
        }
    }

    /**
     * Generate RouterOS MikroTik Script to send alerts directly from Router
     */
    fun generateRouterOsScript(token: String, chatId: String, botName: String): String {
        return """
# ====================================================
# ABO TALAL VIP - سكربت إشعارات تيليجرام للمايكروتك
# البوت: $botName
# ====================================================
:global botToken "$token";
:global chatId "$chatId";

# دالة إرسال التنبيه
:global sendTelegram do={
    :local text [%0];
    :local url ("https://api.telegram.org/bot" . ${'$'}botToken . "/sendMessage?chat_id=" . ${'$'}chatId . "&parse_mode=HTML&text=" . ${'$'}text);
    /tool fetch url=${'$'}url mode=https keep-result=no;
}

# تنبيه تشغيل السيرفر
${'$'}sendTelegram "🔔%0A<b>[ABO TALAL VIP]</b>%0Aتم تشغيل راوتر المايكروتك بنجاح!%0Aالتاريخ: ${'$'}[/system clock get date]%0Aالوقت: ${'$'}[/system clock get time]";
        """.trimIndent()
    }

    /**
     * Open bot in Telegram app via intent
     */
    fun openBotInTelegram(context: Context, botUsername: String) {
        val cleanUser = botUsername.removePrefix("@").trim()
        val uri = if (cleanUser.isNotBlank()) {
            Uri.parse("https://t.me/$cleanUser")
        } else {
            Uri.parse("https://t.me/BotFather")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot open telegram intent", e)
        }
    }

    // Persistence
    fun loadSavedBots(context: Context): List<TelegramBotItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_BOTS_JSON, null) ?: return defaultInitialBots()
        val list = mutableListOf<TelegramBotItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TelegramBotItem(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        token = obj.optString("token"),
                        chatId = obj.optString("chatId"),
                        isDefault = obj.optBoolean("isDefault"),
                        isConnected = obj.optBoolean("isConnected"),
                        botUsername = obj.optString("botUsername")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved bots", e)
            return defaultInitialBots()
        }
        return if (list.isEmpty()) defaultInitialBots() else list
    }

    fun saveBots(context: Context, bots: List<TelegramBotItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        bots.forEach { bot ->
            val obj = JSONObject().apply {
                put("id", bot.id)
                put("name", bot.name)
                put("token", bot.token)
                put("chatId", bot.chatId)
                put("isDefault", bot.isDefault)
                put("isConnected", bot.isConnected)
                put("botUsername", bot.botUsername)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_BOTS_JSON, array.toString()).apply()
    }

    fun loadSettings(context: Context): TelegramNotificationSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SETTINGS_JSON, null) ?: return TelegramNotificationSettings()
        return try {
            val obj = JSONObject(jsonStr)
            TelegramNotificationSettings(
                notifyNewCardSold = obj.optBoolean("notifyNewCardSold", true),
                notifyServerReboot = obj.optBoolean("notifyServerReboot", true),
                notifyHighCpu = obj.optBoolean("notifyHighCpu", true),
                notifyNewActiveUser = obj.optBoolean("notifyNewActiveUser", false),
                notifyDailySummary = obj.optBoolean("notifyDailySummary", true)
            )
        } catch (e: Exception) {
            TelegramNotificationSettings()
        }
    }

    fun saveSettings(context: Context, settings: TelegramNotificationSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val obj = JSONObject().apply {
            put("notifyNewCardSold", settings.notifyNewCardSold)
            put("notifyServerReboot", settings.notifyServerReboot)
            put("notifyHighCpu", settings.notifyHighCpu)
            put("notifyNewActiveUser", settings.notifyNewActiveUser)
            put("notifyDailySummary", settings.notifyDailySummary)
        }
        prefs.edit().putString(KEY_SETTINGS_JSON, obj.toString()).apply()
    }

    private fun defaultInitialBots(): List<TelegramBotItem> {
        return listOf(
            TelegramBotItem(
                id = "bot_default_1",
                name = "بوت الإشعارات الرئيسي",
                token = "",
                chatId = "",
                isDefault = true,
                isConnected = false,
                botUsername = ""
            )
        )
    }
}
