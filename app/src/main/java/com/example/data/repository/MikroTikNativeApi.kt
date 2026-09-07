package com.example.data.repository

import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

/**
 * Native MikroTik RouterOS Binary API Client (Port 8728 / 8729)
 * Supports ROS v6 and v7 authentication and sentence command execution.
 */
class MikroTikNativeApi(
    val host: String,
    val port: Int = 8728,
    private val connectTimeoutMs: Int = 12000,
    private val commandTimeoutMs: Int = 25000,
    private val useSsl: Boolean = false
) {
    private var socket: Socket? = null
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null

    private val socketLock = Any()
    private var savedUsername: String = ""
    private var savedPassword: String = ""

    companion object {
        private const val TAG = "MikroTikNativeApi"

        fun testSocket(host: String, port: Int, timeoutMs: Int = 5000): Pair<Boolean, String> {
            return try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), timeoutMs)
                s.close()
                Pair(true, "✅ الاتصال ناجح ومكتمل!\n\nتم الوصول إلى الراوتر ($host) على المنفذ ($port) بنجاح، ومنفذ الخدمة جاهز لاستقبال الأوامر.")
            } catch (e: Exception) {
                Log.w(TAG, "Test socket failed to $host:$port", e)
                val rawMsg = e.localizedMessage ?: ""

                // Diagnose alternative common ports on same host to help the user
                var openAltPort: Int? = null
                val altPorts = listOf(8728, 80, 8291, 8729).filter { it != port }
                for (alt in altPorts) {
                    try {
                        val testSock = Socket()
                        testSock.connect(InetSocketAddress(host, alt), 600)
                        testSock.close()
                        openAltPort = alt
                        break
                    } catch (_: Exception) {}
                }

                val altHint = when (openAltPort) {
                    80 -> "\n\n💡 ملاحظة تشخيصية:\nعنوان الآي بي ($host) متصل وجهاز الراوتر يعمل، والمنفذ 80 (الويب) مفتوح! ولكن منفذ الـ API ($port) مغلق.\nلتفعيله: افتح WinBox -> ثم IP -> Services -> واضغط على (api) واختر Enable (✔️)."
                    8291 -> "\n\n💡 ملاحظة تشخيصية:\nالراوتر متصل ومنفذ WinBox (8291) مفتوح! لكن منفذ الـ API ($port) لم يتم تفعيله في قائمة IP -> Services."
                    8729 -> "\n\n💡 ملاحظة تشخيصية:\nالراوتر يعمل ومنفذ API المشفر (8729) مفتوح! يرجى تجربة المنفذ 8729 مع تفعيل خيار SSL."
                    8728 -> "\n\n💡 ملاحظة تشخيصية:\nمنفذ الـ API الافتراضي (8728) مفتوح على الراوتر! يرجى استخدام المنفذ 8728 بدلاً من $port."
                    else -> ""
                }

                val friendlyMsg = when {
                    rawMsg.contains("ECONNREFUSED", ignoreCase = true) || rawMsg.contains("refused", ignoreCase = true) -> {
                        "⚠️ تم رفض الاتصال من الراوتر على المنفذ ($port).\n\n" +
                        "💡 السبب والحل:\n" +
                        "• الآي بي ($host) صحيح، لكن خدمة API معطلة في نظام الراوتر أو تعمل على منفذ مختلف.\n\n" +
                        "خطوات التفعيل عبر WinBox:\n" +
                        "1. افتح قائمة IP ثم Services\n" +
                        "2. اضغط على خدمة (api) واضغط زر تفعيل (Enable ✔️)\n" +
                        "3. تأكد أن المنفذ المحدد هناك هو $port." +
                        altHint
                    }
                    rawMsg.contains("ETIMEDOUT", ignoreCase = true) || rawMsg.contains("timed out", ignoreCase = true) -> {
                        "⚠️ انتهت مهلة الاتصال بالراوتر ($host:$port).\n\n" +
                        "💡 الأسباب المحتملة والحل:\n" +
                        "• تأكد أن الهاتف متصل بنفس شبكة الواي فاي للراوتر.\n" +
                        "• تأكد من صحة الآي بي ($host) وأنه ضمن نفس مجال شبكتك."
                    }
                    rawMsg.contains("EHOSTUNREACH", ignoreCase = true) || rawMsg.contains("No route to host", ignoreCase = true) -> {
                        "⚠️ تعذر الوصول إلى عنوان الراوتر ($host).\n\n" +
                        "💡 السبب:\n" +
                        "الهاتف غير متصل بشبكة الراوتر، أو تم إدخال آي بي خاطئ. يرجى الاتصال بشبكة الواي فاي للراوتر والمحاولة مجدداً."
                    }
                    else -> {
                        "⚠️ تعذر الاتصال بـ $host:$port\n\nالسبب: ${e.localizedMessage ?: "المنفذ مغلق أو الشبكة غير متاحة"}\n\n💡 تأكد من تشغيل خدمة api في قائمة IP -> Services في المايكروتيك." + altHint
                    }
                }
                Pair(false, friendlyMsg)
            }
        }
    }

    fun isConnected(): Boolean {
        synchronized(socketLock) {
            val s = socket
            return s != null && s.isConnected && !s.isClosed && !s.isInputShutdown && !s.isOutputShutdown
        }
    }

    fun connect(): Boolean {
        synchronized(socketLock) {
            return try {
                val s = if (useSsl) {
                    SSLSocketFactory.getDefault().createSocket()
                } else {
                    Socket()
                }
                s.tcpNoDelay = true
                s.keepAlive = true
                s.soTimeout = commandTimeoutMs
                s.connect(InetSocketAddress(host, port), connectTimeoutMs)
                socket = s
                inStream = s.getInputStream()
                outStream = s.getOutputStream()
                Log.d(TAG, "Socket connected to $host:$port (tcpNoDelay=true, keepAlive=true)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect socket to $host:$port", e)
                closeInternal()
                false
            }
        }
    }

    fun ensureConnected(): Boolean {
        synchronized(socketLock) {
            if (isConnected()) return true
            if (savedUsername.isBlank()) return false
            Log.w(TAG, "Socket disconnected, attempting auto-reconnect to $host:$port...")
            closeInternal()
            if (connect()) {
                val (ok, msg) = performLogin(savedUsername, savedPassword)
                if (ok) {
                    Log.i(TAG, "Auto-reconnect & re-login succeeded!")
                    return true
                } else {
                    Log.e(TAG, "Auto-reconnect login failed: $msg")
                }
            }
            return false
        }
    }

    fun login(user: String, pass: String): Pair<Boolean, String> {
        synchronized(socketLock) {
            savedUsername = user
            savedPassword = pass
            if (!isConnected()) {
                if (!connect()) {
                    return Pair(false, "تعذر إنشاء اتصال Socket بالراوتر على المنفذ $port. تأكد من فتح خدمة api في الراوتر والاتصال بنفس شبكة الراوتر.")
                }
            }
            return performLogin(user, pass)
        }
    }

    private fun performLogin(user: String, pass: String): Pair<Boolean, String> {
        try {
            // First attempt: Modern ROS v6.43+ and v7 direct login
            writeSentence(listOf("/login", "=name=$user", "=password=$pass"))
            val response = readSentence()

            val status = response.firstOrNull() ?: ""
            if (status == "!done") {
                // Check if challenge response was requested (Older ROS v6)
                val ret = response.firstOrNull { it.startsWith("=ret=") }
                if (ret != null) {
                    val challengeHex = ret.removePrefix("=ret=")
                    val challengeHash = md5Response(challengeHex, pass)
                    writeSentence(listOf("/login", "=name=$user", "=response=00$challengeHash"))
                    val resp2 = readSentence()
                    if (resp2.firstOrNull() == "!done") {
                        return Pair(true, "تم تسجيل الدخول بنجاح")
                    } else {
                        val trap = resp2.firstOrNull { it.startsWith("=message=") }?.removePrefix("=message=")
                        return Pair(false, trap ?: "اسم المستخدم أو كلمة المرور غير صحيحة")
                    }
                }
                return Pair(true, "تم تسجيل الدخول بنجاح")
            } else if (status.startsWith("!trap")) {
                val trap = response.firstOrNull { it.startsWith("=message=") }?.removePrefix("=message=")
                return Pair(false, trap ?: "اسم المستخدم أو كلمة المرور غير صحيحة")
            }

            return Pair(true, "تم تسجيل الدخول بنجاح")
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            closeInternal()
            return Pair(false, "خطأ أثناء محاولة تسجيل الدخول: ${e.localizedMessage ?: "انتهت المهلة"}")
        }
    }

    fun execute(command: String, params: Map<String, String>): List<Map<String, String>> {
        val arr = params.map { "${it.key}=${it.value}" }.toTypedArray()
        return execute(command, *arr)
    }

    fun execute(command: String, vararg params: String): List<Map<String, String>> {
        val results = mutableListOf<Map<String, String>>()
        synchronized(socketLock) {
            if (!isConnected()) {
                if (!ensureConnected()) {
                    return results
                }
            }

            try {
                val words = mutableListOf(command)
                for (p in params) {
                    words.add("=$p")
                }
                writeSentence(words)

                var currentRecord = mutableMapOf<String, String>()
                while (true) {
                    val reply = readSentence()
                    if (reply.isEmpty()) break
                    val replyType = reply.firstOrNull() ?: ""

                    if (replyType == "!re") {
                        currentRecord = mutableMapOf()
                        for (word in reply.drop(1)) {
                            if (word.startsWith("=")) {
                                val parts = word.substring(1).split("=", limit = 2)
                                if (parts.size == 2) {
                                    currentRecord[parts[0]] = parts[1]
                                }
                            }
                        }
                        results.add(currentRecord)
                    } else if (replyType == "!done") {
                        val doneMap = mutableMapOf<String, String>()
                        for (word in reply.drop(1)) {
                            if (word.startsWith("=")) {
                                val parts = word.substring(1).split("=", limit = 2)
                                if (parts.size == 2) {
                                    doneMap[parts[0]] = parts[1]
                                }
                            }
                        }
                        if (doneMap.isNotEmpty() && results.isEmpty()) {
                            results.add(doneMap)
                        }
                        break
                    } else if (replyType == "!trap") {
                        Log.w(TAG, "Command trap response for $command: $reply")
                        break
                    } else if (replyType == "!fatal") {
                        Log.e(TAG, "Command fatal response for $command: $reply")
                        closeInternal()
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Execute command error for $command", e)
                // Socket error: close internal socket to trigger reconnect next time
                closeInternal()
            }
            return results
        }
    }

    fun ping(): Boolean {
        return try {
            val res = execute("/system/identity/print")
            res.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun reboot(): Boolean {
        return try {
            execute("/system/reboot")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shutdown(): Boolean {
        return try {
            execute("/system/shutdown")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        synchronized(socketLock) {
            savedUsername = ""
            savedPassword = ""
            closeInternal()
        }
    }

    private fun closeInternal() {
        try {
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        inStream = null
        outStream = null
    }

    private fun writeSentence(words: List<String>) {
        val out = outStream ?: return
        for (w in words) {
            writeWord(out, w)
        }
        out.write(0) // end of sentence
        out.flush()
    }

    private fun writeWord(out: OutputStream, word: String) {
        val bytes = word.toByteArray(Charsets.UTF_8)
        val len = bytes.size
        when {
            len < 0x80 -> {
                out.write(len)
            }
            len < 0x4000 -> {
                out.write((len shr 8) or 0x80)
                out.write(len and 0xFF)
            }
            len < 0x200000 -> {
                out.write((len shr 16) or 0xC0)
                out.write((len shr 8) and 0xFF)
                out.write(len and 0xFF)
            }
            else -> {
                out.write((len shr 24) or 0xE0)
                out.write((len shr 16) and 0xFF)
                out.write((len shr 8) and 0xFF)
                out.write(len and 0xFF)
            }
        }
        out.write(bytes)
    }

    private fun readSentence(): List<String> {
        val list = mutableListOf<String>()
        val input = inStream ?: return list
        while (true) {
            val word = readWord(input) ?: break
            if (word.isEmpty()) {
                break
            }
            list.add(word)
        }
        return list
    }

    private fun readWord(input: InputStream): String? {
        val firstByte = input.read()
        if (firstByte == -1) return null
        if (firstByte == 0) return ""

        val len = when {
            (firstByte and 0x80) == 0 -> firstByte
            (firstByte and 0xC0) == 0x80 -> {
                val b2 = input.read()
                ((firstByte and 0x3F) shl 8) or b2
            }
            (firstByte and 0xE0) == 0xC0 -> {
                val b2 = input.read()
                val b3 = input.read()
                ((firstByte and 0x1F) shl 16) or (b2 shl 8) or b3
            }
            (firstByte and 0xF0) == 0xE0 -> {
                val b2 = input.read()
                val b3 = input.read()
                val b4 = input.read()
                ((firstByte and 0x0F) shl 24) or (b2 shl 16) or (b3 shl 8) or b4
            }
            else -> return ""
        }

        val buffer = ByteArray(len)
        var offset = 0
        while (offset < len) {
            val r = input.read(buffer, offset, len - offset)
            if (r == -1) break
            offset += r
        }
        return String(buffer, 0, offset, Charsets.UTF_8)
    }

    private fun md5Response(challengeHex: String, pass: String): String {
        return try {
            val challengeBytes = hexStringToByteArray(challengeHex)
            val md = MessageDigest.getInstance("MD5")
            md.update(0.toByte())
            md.update(pass.toByteArray(Charsets.UTF_8))
            md.update(challengeBytes)
            val digest = md.digest()
            bytesToHex(digest)
        } catch (e: Exception) {
            ""
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}
