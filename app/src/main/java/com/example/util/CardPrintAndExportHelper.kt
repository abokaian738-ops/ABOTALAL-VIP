package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint as AndroidPaint
import android.graphics.Rect as AndroidRect
import android.graphics.RectF as AndroidRectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.UserManagerUser
import com.example.ui.screens.GeneratedBatchRecord
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CardElementConfig(
    val visible: Boolean = true,
    val showTitle: Boolean = false,
    val titleText: String = "",
    val xMm: Float = 4f,
    val yMm: Float = 4f,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val fontSize: Float = 10f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val colorHex: String = "#0F172A",
    val alignment: String = "center",
    val widthMm: Float = 10f,
    val heightMm: Float = 10f
)

data class CardTemplateConfig(
    val templateName: String = "افتراضي",
    val columns: Int = 3,
    val rows: Int = 17,
    val autoFitA4: Boolean = true,
    val horizontalMarginMm: Float = 0.0f,
    val verticalMarginMm: Float = 0.0f,
    val pageMarginMm: Float = 5.0f,
    val cardWidthMm: Float = 65.0f,
    val cardHeightMm: Float = 15.0f,
    val userManagerPackageId: String = "",
    val hotspotPackageId: String = "",
    val backgroundEnabled: Boolean = true,
    val bgPreset: String = "bg_card_100",
    val customBgPath: String? = null,
    val borderEnabled: Boolean = true,
    val borderSizeMm: Float = 0.35f,
    val borderColorHex: String = "#000000",
    val pageNoteEnabled: Boolean = false,
    val pageNoteText: String = "أهلاً بكم في شبكة ABO TALAL",
    val pageNoteX: Float = 40f,
    val pageNoteY: Float = 6f,
    val pageNoteSize: Float = 10f,
    val pageNoteColorHex: String = "#0C5A60",
    val pageNumberingEnabled: Boolean = false,
    val pageNumberingText: String = "{page}",
    val digits: Int = 10,
    val charset: String = "أرقام فقط",
    val moveStep: Float = 0.5f,
    val showUsername: Boolean = true,
    // Independent Elements:
    val usernameConfig: CardElementConfig = CardElementConfig(
        visible = true,
        showTitle = false,
        titleText = "",
        xMm = 4f,
        yMm = 4f,
        xPercent = 0.50f,
        yPercent = 0.42f,
        fontSize = 11f,
        isBold = true,
        colorHex = "#0F172A"
    ),
    val titleConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "ABO TALAL",
        xMm = 4f,
        yMm = 2f,
        fontSize = 8f,
        isBold = true,
        colorHex = "#0C5A60"
    ),
    val passwordConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "",
        xMm = 34f,
        yMm = 4f,
        xPercent = 0.50f,
        yPercent = 0.65f,
        fontSize = 10f,
        isBold = false,
        colorHex = "#0F172A"
    ),
    val priceConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "500 ر.ي",
        xMm = 20f,
        yMm = 10f,
        xPercent = 0.88f,
        yPercent = 0.50f,
        fontSize = 9f,
        isBold = true,
        colorHex = "#0C5A60"
    ),
    val profileConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "الباقة",
        xMm = 25f,
        yMm = 8f,
        xPercent = 0.50f,
        yPercent = 0.82f,
        fontSize = 8f,
        isBold = true,
        colorHex = "#475569"
    ),
    val paymentConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "",
        xMm = 4f,
        yMm = 10f,
        fontSize = 8f,
        isBold = false,
        colorHex = "#64748B"
    ),
    val serialConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "",
        xMm = 34f,
        yMm = 10f,
        xPercent = 0.15f,
        yPercent = 0.90f,
        fontSize = 8f,
        isBold = false,
        colorHex = "#64748B"
    ),
    val posConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "",
        xMm = 4f,
        yMm = 12f,
        fontSize = 8f,
        isBold = false,
        colorHex = "#64748B"
    ),
    val barcodeConfig: CardElementConfig = CardElementConfig(
        visible = false,
        xMm = 44f,
        yMm = 3f,
        widthMm = 18f,
        heightMm = 8f
    ),
    val logoConfig: CardElementConfig = CardElementConfig(
        visible = false,
        xMm = 4f,
        yMm = 8f,
        widthMm = 10f,
        heightMm = 10f
    ),
    val logoUrl: String? = null,
    val validityConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = true,
        titleText = "صلاحية:",
        xPercent = 0.25f,
        yPercent = 0.82f,
        fontSize = 7f,
        isBold = false,
        colorHex = "#475569"
    ),
    val quotaConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = true,
        titleText = "الرصيد:",
        xPercent = 0.75f,
        yPercent = 0.82f,
        fontSize = 7f,
        isBold = false,
        colorHex = "#475569"
    ),
    val qrConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "",
        xPercent = 0.15f,
        yPercent = 0.50f,
        fontSize = 0f,
        isBold = false,
        widthMm = 12f,
        heightMm = 12f
    ),
    val batchConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = true,
        titleText = "الدفعة:",
        xPercent = 0.85f,
        yPercent = 0.90f,
        fontSize = 6.5f,
        isBold = false,
        colorHex = "#64748B"
    )
)

object CardPrintAndExportHelper {

    fun getTemplatesList(context: Context): List<String> {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val defaultList = listOf("افتراضي", "كروت_فئة_100", "كروت_فئة_200", "كروت_فئة_500", "تصميم_مخصص_1")
        val saved = prefs.getStringSet("all_templates_set", null)
        return if (saved.isNullOrEmpty()) {
            defaultList
        } else {
            saved.toList().sorted()
        }
    }

    fun saveTemplatesList(context: Context, list: List<String>) {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("all_templates_set", list.toSet()).apply()
    }

    fun deleteTemplate(context: Context, templateName: String) {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val current = getTemplatesList(context).toMutableList()
        current.remove(templateName)
        saveTemplatesList(context, current)
        // Also clear template specific keys
        val editor = prefs.edit()
        val allKeys = prefs.all.keys.filter { it.startsWith("${templateName}_") }
        allKeys.forEach { editor.remove(it) }
        editor.apply()
    }

    fun loadTemplateConfig(context: Context, templateName: String): CardTemplateConfig {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val p = templateName
        val cardWMm = prefs.getFloat("${p}_card_w_mm", 65.0f)
        val cardHMm = prefs.getFloat("${p}_card_h_mm", 15.0f)

        return CardTemplateConfig(
            templateName = templateName,
            columns = prefs.getInt("${p}_columns", 3),
            rows = prefs.getInt("${p}_rows", 17),
            autoFitA4 = prefs.getBoolean("${p}_auto_fit", true),
            horizontalMarginMm = prefs.getFloat("${p}_h_margin", 0.0f),
            verticalMarginMm = prefs.getFloat("${p}_v_margin", 0.0f),
            pageMarginMm = prefs.getFloat("${p}_page_margin_mm", 5.0f),
            cardWidthMm = cardWMm,
            cardHeightMm = cardHMm,
            userManagerPackageId = prefs.getString("${p}_um_pkg_id", "") ?: "",
            hotspotPackageId = prefs.getString("${p}_hs_pkg_id", "") ?: "",
            backgroundEnabled = prefs.getBoolean("${p}_bg_enabled", true),
            bgPreset = prefs.getString("${p}_bg_preset", "bg_card_100") ?: "bg_card_100",
            customBgPath = prefs.getString("${p}_custom_bg_path", null),
            borderEnabled = prefs.getBoolean("${p}_border_enabled", true),
            borderSizeMm = prefs.getFloat("${p}_border_size", 0.35f),
            borderColorHex = prefs.getString("${p}_border_color", "#000000") ?: "#000000",
            pageNoteEnabled = prefs.getBoolean("${p}_page_note_enabled", false),
            pageNoteText = prefs.getString("${p}_page_note_text", "أهلاً بكم في شبكة ABO TALAL VIP") ?: "أهلاً بكم في شبكة ABO TALAL VIP",
            pageNoteX = prefs.getFloat("${p}_page_note_x", 40f),
            pageNoteY = prefs.getFloat("${p}_page_note_y", 6f),
            pageNoteSize = prefs.getFloat("${p}_page_note_size", 10f),
            pageNoteColorHex = prefs.getString("${p}_page_note_color", "#0C5A60") ?: "#0C5A60",
            pageNumberingEnabled = prefs.getBoolean("${p}_page_num_enabled", false),
            pageNumberingText = prefs.getString("${p}_page_num_text", "{page}") ?: "{page}",
            digits = prefs.getInt("${p}_digits", 10),
            charset = prefs.getString("${p}_charset", "أرقام فقط") ?: "أرقام فقط",
            moveStep = prefs.getFloat("${p}_move_step", 0.5f),
            showUsername = prefs.getBoolean("${p}_show_username", true),
            usernameConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_user_vis", true),
                showTitle = prefs.getBoolean("${p}_user_title_on", false),
                titleText = prefs.getString("${p}_user_title_text", "") ?: "",
                xMm = prefs.getFloat("${p}_user_x_mm", 4f),
                yMm = prefs.getFloat("${p}_user_y_mm", 4f),
                fontSize = prefs.getFloat("${p}_user_size", 11f),
                isBold = prefs.getBoolean("${p}_user_bold", true),
                isItalic = prefs.getBoolean("${p}_user_italic", false),
                colorHex = prefs.getString("${p}_user_color", "#0F172A") ?: "#0F172A"
            ),
            titleConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_title_vis", false),
                showTitle = false,
                titleText = prefs.getString("${p}_title_text", "ABO TALAL VIP") ?: "ABO TALAL VIP",
                xMm = prefs.getFloat("${p}_title_x_mm", 4f),
                yMm = prefs.getFloat("${p}_title_y_mm", 2f),
                fontSize = prefs.getFloat("${p}_title_size", 8f),
                isBold = prefs.getBoolean("${p}_title_bold", true),
                isItalic = prefs.getBoolean("${p}_title_italic", false),
                colorHex = prefs.getString("${p}_title_color", "#0C5A60") ?: "#0C5A60"
            ),
            passwordConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_pass_vis", false),
                showTitle = prefs.getBoolean("${p}_pass_title_on", false),
                titleText = prefs.getString("${p}_pass_title_text", "") ?: "",
                xMm = prefs.getFloat("${p}_pass_x_mm", 34f),
                yMm = prefs.getFloat("${p}_pass_y_mm", 4f),
                fontSize = prefs.getFloat("${p}_pass_size", 10f),
                isBold = prefs.getBoolean("${p}_pass_bold", false),
                isItalic = prefs.getBoolean("${p}_pass_italic", false),
                colorHex = prefs.getString("${p}_pass_color", "#0F172A") ?: "#0F172A"
            ),
            priceConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_price_vis", false),
                showTitle = prefs.getBoolean("${p}_price_title_on", false),
                titleText = prefs.getString("${p}_price_title_text", "500 ر.ي") ?: "500 ر.ي",
                xMm = prefs.getFloat("${p}_price_x_mm", 20f),
                yMm = prefs.getFloat("${p}_price_y_mm", 10f),
                fontSize = prefs.getFloat("${p}_price_size", 9f),
                isBold = prefs.getBoolean("${p}_price_bold", true),
                isItalic = prefs.getBoolean("${p}_price_italic", false),
                colorHex = prefs.getString("${p}_price_color", "#0C5A60") ?: "#0C5A60"
            ),
            profileConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_prof_vis", false),
                showTitle = prefs.getBoolean("${p}_prof_title_on", false),
                titleText = prefs.getString("${p}_prof_title_text", "الباقة") ?: "الباقة",
                xMm = prefs.getFloat("${p}_prof_x_mm", 25f),
                yMm = prefs.getFloat("${p}_prof_y_mm", 8f),
                fontSize = prefs.getFloat("${p}_prof_size", 8f),
                isBold = prefs.getBoolean("${p}_prof_bold", true),
                isItalic = prefs.getBoolean("${p}_prof_italic", false),
                colorHex = prefs.getString("${p}_prof_color", "#475569") ?: "#475569"
            ),
            paymentConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_payment_vis", false),
                showTitle = false,
                titleText = prefs.getString("${p}_payment_text", "") ?: "",
                xMm = prefs.getFloat("${p}_payment_x_mm", 4f),
                yMm = prefs.getFloat("${p}_payment_y_mm", 10f),
                fontSize = prefs.getFloat("${p}_payment_size", 8f),
                isBold = prefs.getBoolean("${p}_payment_bold", false),
                isItalic = prefs.getBoolean("${p}_payment_italic", false),
                colorHex = prefs.getString("${p}_payment_color", "#64748B") ?: "#64748B"
            ),
            serialConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_ser_vis", false),
                showTitle = false,
                titleText = prefs.getString("${p}_ser_title_text", "") ?: "",
                xMm = prefs.getFloat("${p}_ser_x_mm", 34f),
                yMm = prefs.getFloat("${p}_ser_y_mm", 10f),
                fontSize = prefs.getFloat("${p}_ser_size", 8f),
                isBold = prefs.getBoolean("${p}_ser_bold", false),
                isItalic = prefs.getBoolean("${p}_ser_italic", false),
                colorHex = prefs.getString("${p}_ser_color", "#64748B") ?: "#64748B"
            ),
            posConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_pos_vis", false),
                showTitle = false,
                titleText = prefs.getString("${p}_pos_text", "") ?: "",
                xMm = prefs.getFloat("${p}_pos_x_mm", 4f),
                yMm = prefs.getFloat("${p}_pos_y_mm", 12f),
                fontSize = prefs.getFloat("${p}_pos_size", 8f),
                isBold = prefs.getBoolean("${p}_pos_bold", false),
                isItalic = prefs.getBoolean("${p}_pos_italic", false),
                colorHex = prefs.getString("${p}_pos_color", "#64748B") ?: "#64748B"
            ),
            barcodeConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_barcode_vis", false),
                xMm = prefs.getFloat("${p}_barcode_x_mm", 44f),
                yMm = prefs.getFloat("${p}_barcode_y_mm", 3f),
                widthMm = prefs.getFloat("${p}_barcode_w_mm", 18f),
                heightMm = prefs.getFloat("${p}_barcode_h_mm", 8f)
            ),
            logoConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_logo_vis", false),
                xMm = prefs.getFloat("${p}_logo_x_mm", 4f),
                yMm = prefs.getFloat("${p}_logo_y_mm", 8f),
                widthMm = prefs.getFloat("${p}_logo_w_mm", 10f),
                heightMm = prefs.getFloat("${p}_logo_h_mm", 10f)
            ),
            logoUrl = prefs.getString("${p}_logo_url", null)
        )
    }

    fun saveTemplateConfig(context: Context, config: CardTemplateConfig) {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val p = config.templateName
        val templates = getTemplatesList(context).toMutableList()
        if (!templates.contains(config.templateName)) {
            templates.add(config.templateName)
            saveTemplatesList(context, templates)
        }

        prefs.edit()
            .putInt("${p}_columns", config.columns)
            .putInt("${p}_rows", config.rows)
            .putBoolean("${p}_auto_fit", config.autoFitA4)
            .putFloat("${p}_h_margin", config.horizontalMarginMm)
            .putFloat("${p}_v_margin", config.verticalMarginMm)
            .putFloat("${p}_page_margin_mm", config.pageMarginMm)
            .putFloat("${p}_card_w_mm", config.cardWidthMm)
            .putFloat("${p}_card_h_mm", config.cardHeightMm)
            .putString("${p}_um_pkg_id", config.userManagerPackageId)
            .putString("${p}_hs_pkg_id", config.hotspotPackageId)
            .putBoolean("${p}_bg_enabled", config.backgroundEnabled)
            .putString("${p}_bg_preset", config.bgPreset)
            .putString("${p}_custom_bg_path", config.customBgPath)
            .putBoolean("${p}_border_enabled", config.borderEnabled)
            .putFloat("${p}_border_size", config.borderSizeMm)
            .putString("${p}_border_color", config.borderColorHex)
            .putBoolean("${p}_page_note_enabled", config.pageNoteEnabled)
            .putString("${p}_page_note_text", config.pageNoteText)
            .putFloat("${p}_page_note_x", config.pageNoteX)
            .putFloat("${p}_page_note_y", config.pageNoteY)
            .putFloat("${p}_page_note_size", config.pageNoteSize)
            .putString("${p}_page_note_color", config.pageNoteColorHex)
            .putBoolean("${p}_page_num_enabled", config.pageNumberingEnabled)
            .putString("${p}_page_num_text", config.pageNumberingText)
            .putInt("${p}_digits", config.digits)
            .putString("${p}_charset", config.charset)
            .putFloat("${p}_move_step", config.moveStep)
            .putBoolean("${p}_show_username", config.showUsername)
            // Username
            .putBoolean("${p}_user_vis", config.usernameConfig.visible)
            .putBoolean("${p}_user_title_on", config.usernameConfig.showTitle)
            .putString("${p}_user_title_text", config.usernameConfig.titleText)
            .putFloat("${p}_user_x_mm", config.usernameConfig.xMm)
            .putFloat("${p}_user_y_mm", config.usernameConfig.yMm)
            .putFloat("${p}_user_size", config.usernameConfig.fontSize)
            .putBoolean("${p}_user_bold", config.usernameConfig.isBold)
            .putBoolean("${p}_user_italic", config.usernameConfig.isItalic)
            .putString("${p}_user_color", config.usernameConfig.colorHex)
            // Title
            .putBoolean("${p}_title_vis", config.titleConfig.visible)
            .putString("${p}_title_text", config.titleConfig.titleText)
            .putFloat("${p}_title_x_mm", config.titleConfig.xMm)
            .putFloat("${p}_title_y_mm", config.titleConfig.yMm)
            .putFloat("${p}_title_size", config.titleConfig.fontSize)
            .putBoolean("${p}_title_bold", config.titleConfig.isBold)
            .putBoolean("${p}_title_italic", config.titleConfig.isItalic)
            .putString("${p}_title_color", config.titleConfig.colorHex)
            // Password
            .putBoolean("${p}_pass_vis", config.passwordConfig.visible)
            .putBoolean("${p}_pass_title_on", config.passwordConfig.showTitle)
            .putString("${p}_pass_title_text", config.passwordConfig.titleText)
            .putFloat("${p}_pass_x_mm", config.passwordConfig.xMm)
            .putFloat("${p}_pass_y_mm", config.passwordConfig.yMm)
            .putFloat("${p}_pass_size", config.passwordConfig.fontSize)
            .putBoolean("${p}_pass_bold", config.passwordConfig.isBold)
            .putBoolean("${p}_pass_italic", config.passwordConfig.isItalic)
            .putString("${p}_pass_color", config.passwordConfig.colorHex)
            // Price
            .putBoolean("${p}_price_vis", config.priceConfig.visible)
            .putBoolean("${p}_price_title_on", config.priceConfig.showTitle)
            .putString("${p}_price_title_text", config.priceConfig.titleText)
            .putFloat("${p}_price_x_mm", config.priceConfig.xMm)
            .putFloat("${p}_price_y_mm", config.priceConfig.yMm)
            .putFloat("${p}_price_size", config.priceConfig.fontSize)
            .putBoolean("${p}_price_bold", config.priceConfig.isBold)
            .putBoolean("${p}_price_italic", config.priceConfig.isItalic)
            .putString("${p}_price_color", config.priceConfig.colorHex)
            // Profile
            .putBoolean("${p}_prof_vis", config.profileConfig.visible)
            .putBoolean("${p}_prof_title_on", config.profileConfig.showTitle)
            .putString("${p}_prof_title_text", config.profileConfig.titleText)
            .putFloat("${p}_prof_x_mm", config.profileConfig.xMm)
            .putFloat("${p}_prof_y_mm", config.profileConfig.yMm)
            .putFloat("${p}_prof_size", config.profileConfig.fontSize)
            .putBoolean("${p}_prof_bold", config.profileConfig.isBold)
            .putBoolean("${p}_prof_italic", config.profileConfig.isItalic)
            .putString("${p}_prof_color", config.profileConfig.colorHex)
            // Payment
            .putBoolean("${p}_payment_vis", config.paymentConfig.visible)
            .putString("${p}_payment_text", config.paymentConfig.titleText)
            .putFloat("${p}_payment_x_mm", config.paymentConfig.xMm)
            .putFloat("${p}_payment_y_mm", config.paymentConfig.yMm)
            .putFloat("${p}_payment_size", config.paymentConfig.fontSize)
            .putBoolean("${p}_payment_bold", config.paymentConfig.isBold)
            .putBoolean("${p}_payment_italic", config.paymentConfig.isItalic)
            .putString("${p}_payment_color", config.paymentConfig.colorHex)
            // Serial
            .putBoolean("${p}_ser_vis", config.serialConfig.visible)
            .putString("${p}_ser_title_text", config.serialConfig.titleText)
            .putFloat("${p}_ser_x_mm", config.serialConfig.xMm)
            .putFloat("${p}_ser_y_mm", config.serialConfig.yMm)
            .putFloat("${p}_ser_size", config.serialConfig.fontSize)
            .putBoolean("${p}_ser_bold", config.serialConfig.isBold)
            .putBoolean("${p}_ser_italic", config.serialConfig.isItalic)
            .putString("${p}_ser_color", config.serialConfig.colorHex)
            // POS
            .putBoolean("${p}_pos_vis", config.posConfig.visible)
            .putString("${p}_pos_text", config.posConfig.titleText)
            .putFloat("${p}_pos_x_mm", config.posConfig.xMm)
            .putFloat("${p}_pos_y_mm", config.posConfig.yMm)
            .putFloat("${p}_pos_size", config.posConfig.fontSize)
            .putBoolean("${p}_pos_bold", config.posConfig.isBold)
            .putBoolean("${p}_pos_italic", config.posConfig.isItalic)
            .putString("${p}_pos_color", config.posConfig.colorHex)
            // Barcode
            .putBoolean("${p}_barcode_vis", config.barcodeConfig.visible)
            .putFloat("${p}_barcode_x_mm", config.barcodeConfig.xMm)
            .putFloat("${p}_barcode_y_mm", config.barcodeConfig.yMm)
            .putFloat("${p}_barcode_w_mm", config.barcodeConfig.widthMm)
            .putFloat("${p}_barcode_h_mm", config.barcodeConfig.heightMm)
            // Logo
            .putBoolean("${p}_logo_vis", config.logoConfig.visible)
            .putFloat("${p}_logo_x_mm", config.logoConfig.xMm)
            .putFloat("${p}_logo_y_mm", config.logoConfig.yMm)
            .putFloat("${p}_logo_w_mm", config.logoConfig.widthMm)
            .putFloat("${p}_logo_h_mm", config.logoConfig.heightMm)
            .putString("${p}_logo_url", config.logoUrl)
            .apply()

        // Also sync profile link if package specified
        if (config.userManagerPackageId.isNotEmpty()) {
            prefs.edit().putString("${config.userManagerPackageId}_template", config.templateName).apply()
        }
        if (config.hotspotPackageId.isNotEmpty()) {
            prefs.edit().putString("${config.hotspotPackageId}_template", config.templateName).apply()
        }
    }

    /**
     * Generates a preview PDF file in cache directory and returns the File.
     */
    fun generatePdfPreviewFile(
        context: Context,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null,
        users: List<UserManagerUser> = emptyList(),
        batch: GeneratedBatchRecord? = null
    ): File? {
        return try {
            val totalCards = (config.columns * config.rows).coerceAtLeast(1)
            val realCards = if (users.isNotEmpty()) {
                users
            } else {
                (1..totalCards).map { i ->
                    val code = String.format(Locale.US, "%010d", (1000000000L + (i * 388954024L % 9000000000L)))
                    UserManagerUser(
                        username = code,
                        password = String.format(Locale.US, "%04d", 5000 + i),
                        profile = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
                        active = true
                    )
                }
            }

            val curBatch = batch ?: GeneratedBatchRecord(
                batchId = "معاينة",
                profileName = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
                pricePerCard = 100,
                count = realCards.size,
                date = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()),
                prefix = ""
            )

            val bgBitmap = customBgBitmap ?: loadPresetBitmap(context, config.bgPreset, config.customBgPath)

            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            drawHighFidelityCardPage(
                canvas = page.canvas,
                pageIndex = 0,
                totalPages = 1,
                users = realCards,
                config = config,
                bgBitmap = bgBitmap,
                batch = curBatch
            )
            pdfDocument.finishPage(page)

            val cacheFile = File(context.cacheDir, "preview_${config.templateName}.pdf")
            val fos = FileOutputStream(cacheFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()

            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders pages of a PDF file to Bitmaps using Android's native PdfRenderer.
     */
    fun renderPdfFileToBitmaps(pdfFile: File, maxPages: Int = 3): List<Bitmap> {
        val list = mutableListOf<Bitmap>()
        var pfd: android.os.ParcelFileDescriptor? = null
        var renderer: android.graphics.pdf.PdfRenderer? = null
        try {
            pfd = android.os.ParcelFileDescriptor.open(pdfFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = android.graphics.pdf.PdfRenderer(pfd)
            val pages = renderer.pageCount.coerceAtMost(maxPages)
            for (i in 0 until pages) {
                val page = renderer.openPage(i)
                val scale = 2
                val bmp = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                canvas.drawColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                list.add(bmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (ignored: Exception) {}
        }
        return list
    }

    /**
     * Preview card design directly in built-in Android PDF reader
     */
    fun openPdfPreviewWithBuiltInViewer(
        context: Context,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null,
        users: List<UserManagerUser> = emptyList(),
        batch: GeneratedBatchRecord? = null
    ) {
        try {
            val cacheFile = generatePdfPreviewFile(context, config, customBgBitmap, users, batch) ?: return

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "معاينة ملف PDF - ABO TALAL"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح معاينة PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Print cards and save PDF file directly to device Downloads
     */
    fun printAndSavePdfDocument(
        context: Context,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null,
        users: List<UserManagerUser> = emptyList(),
        batch: GeneratedBatchRecord? = null
    ) {
        try {
            val totalCards = (config.columns * config.rows).coerceAtLeast(1)
            val realCards = if (users.isNotEmpty()) {
                users
            } else {
                (1..totalCards).map { i ->
                    val code = (1000000000L + (i * 388954024L % 9000000000L)).toString()
                    UserManagerUser(
                        username = code,
                        password = (5000 + i).toString(),
                        profile = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
                        active = true
                    )
                }
            }

            val curBatch = batch ?: GeneratedBatchRecord(
                batchId = "معاينة",
                profileName = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
                pricePerCard = 100,
                count = realCards.size,
                date = "2026/09/03",
                prefix = ""
            )

            val bgBitmap = customBgBitmap ?: loadPresetBitmap(context, config.bgPreset, config.customBgPath)

            // 1. Save PDF file to Downloads
            try {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val fileName = "ABO_TALAL_${config.templateName}_${System.currentTimeMillis()}.pdf"
                val targetFile = File(downloadsDir, fileName)

                val pdfDocument = android.graphics.pdf.PdfDocument()
                val cardsPerPage = config.columns * config.rows
                val totalPages = Math.ceil(realCards.size.toDouble() / cardsPerPage.toDouble()).toInt().coerceAtLeast(1)

                for (p in 0 until totalPages) {
                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, p + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val startIdx = p * cardsPerPage
                    val endIdx = (startIdx + cardsPerPage).coerceAtMost(realCards.size)
                    val subList = if (startIdx < realCards.size) realCards.subList(startIdx, endIdx) else emptyList()

                    drawHighFidelityCardPage(
                        canvas = page.canvas,
                        pageIndex = p,
                        totalPages = totalPages,
                        users = subList,
                        config = config,
                        bgBitmap = bgBitmap,
                        batch = curBatch
                    )
                    pdfDocument.finishPage(page)
                }

                val fos = FileOutputStream(targetFile)
                pdfDocument.writeTo(fos)
                fos.flush()
                fos.close()
                pdfDocument.close()
                Toast.makeText(context, "تم حفظ ملف PDF في التنزيلات:\n$fileName", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Open native print manager
            previewDesignInSystemPrint(context, config, bgBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل الطباعة: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Preview card design directly in Android native System Print Spooler (PDF Preview)
     */
    fun previewDesignInSystemPrint(
        context: Context,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "خدمة الطباعة غير متوفرة في هذا الجهاز!", Toast.LENGTH_LONG).show()
            return
        }

        val totalCards = config.columns * config.rows
        // Generate realistic sample cards matching the preview
        val sampleCards = (1..totalCards).map { i ->
            val code = (1000000000L + (i * 388954024L % 9000000000L)).toString()
            UserManagerUser(
                username = code,
                password = (5000 + i).toString(),
                profile = if (config.profileConfig.titleText.isNotEmpty()) config.profileConfig.titleText else "باقة VIP",
                active = true,
                uptimeUsed = "0s",
                downloadLimit = "2GB"
            )
        }

        val sampleBatch = GeneratedBatchRecord(
            batchId = "معاينة",
            profileName = "باقة VIP",
            pricePerCard = 100,
            count = totalCards,
            date = "2026/09/03",
            prefix = ""
        )

        val bgBitmap = customBgBitmap ?: loadPresetBitmap(context, config.bgPreset, config.customBgPath)

        val printAdapter = object : PrintDocumentAdapter() {
            private var mPdfDocument: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                mPdfDocument = PrintedPdfDocument(context, newAttributes)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("ABO_TALAL_Preview_${config.templateName}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDoc = mPdfDocument ?: return
                try {
                    val page = pdfDoc.startPage(0)
                    drawHighFidelityCardPage(
                        canvas = page.canvas,
                        pageIndex = 0,
                        totalPages = 1,
                        users = sampleCards,
                        config = config,
                        bgBitmap = bgBitmap,
                        batch = sampleBatch
                    )
                    pdfDoc.finishPage(page)

                    pdfDoc.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onWriteFailed(e.localizedMessage)
                } finally {
                    pdfDoc.close()
                    mPdfDocument = null
                }
            }
        }

        printManager.print("ABO TALAL - معاينة تصميم ${config.templateName}", printAdapter, null)
    }

    /**
     * Direct print of single user card
     */
    fun printSingleCardDirectly(context: Context, user: UserManagerUser) {
        val singleBatch = GeneratedBatchRecord(
            batchId = "CARD_${user.username}",
            profileName = user.profile,
            pricePerCard = 0,
            count = 1,
            date = "",
            prefix = ""
        )
        val cfg = CardTemplateConfig(
            columns = 1,
            rows = 1,
            templateName = "كرت_فردي_${user.username}"
        )
        printVoucherBatchDirectly(
            context = context,
            batch = singleBatch,
            users = listOf(user),
            config = cfg
        )
    }

    /**
     * Direct print of real generated batch
     */
    fun printVoucherBatchDirectly(
        context: Context,
        batch: GeneratedBatchRecord,
        users: List<UserManagerUser>,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "خدمة الطباعة غير متوفرة في هذا الجهاز!", Toast.LENGTH_LONG).show()
            return
        }

        val cardsPerPage = (config.columns * config.rows).coerceAtLeast(1)
        val totalPages = Math.ceil(users.size.toDouble() / cardsPerPage).toInt().coerceAtLeast(1)
        val bgBitmap = customBgBitmap ?: loadPresetBitmap(context, config.bgPreset, config.customBgPath)

        val printAdapter = object : PrintDocumentAdapter() {
            private var mPdfDocument: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                mPdfDocument = PrintedPdfDocument(context, newAttributes)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("ABO_TALAL_TK_${batch.batchId}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDoc = mPdfDocument ?: return
                try {
                    for (pageNumber in 0 until totalPages) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback.onWriteCancelled()
                            return
                        }

                        val page = pdfDoc.startPage(pageNumber)
                        drawHighFidelityCardPage(
                            canvas = page.canvas,
                            pageIndex = pageNumber,
                            totalPages = totalPages,
                            users = users,
                            config = config,
                            bgBitmap = bgBitmap,
                            batch = batch
                        )
                        pdfDoc.finishPage(page)
                    }

                    pdfDoc.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onWriteFailed(e.localizedMessage)
                } finally {
                    pdfDoc.close()
                    mPdfDocument = null
                }
            }
        }

        printManager.print("ABO TALAL - كروت ${batch.batchId}", printAdapter, null)
    }

    /**
     * Save Cards Batch to PDF file and share
     */
    fun saveBatchAsPdf(
        context: Context,
        batch: GeneratedBatchRecord,
        users: List<UserManagerUser>,
        config: CardTemplateConfig,
        customBgBitmap: Bitmap? = null,
        onComplete: (File?) -> Unit
    ) {
        try {
            val cardsPerPage = (config.columns * config.rows).coerceAtLeast(1)
            val totalPages = Math.ceil(users.size.toDouble() / cardsPerPage).toInt().coerceAtLeast(1)
            val bgBitmap = customBgBitmap ?: loadPresetBitmap(context, config.bgPreset, config.customBgPath)

            // Standard ISO A4 @ 72 DPI (595 x 842 points)
            val pdfDoc = PdfDocument()

            for (pageNumber in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber + 1).create()
                val page = pdfDoc.startPage(pageInfo)

                drawHighFidelityCardPage(
                    canvas = page.canvas,
                    pageIndex = pageNumber,
                    totalPages = totalPages,
                    users = users,
                    config = config,
                    bgBitmap = bgBitmap,
                    batch = batch
                )

                pdfDoc.finishPage(page)
            }

            val fileName = "ABO_TALAL_TK_${batch.batchId}_${System.currentTimeMillis()}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = if (downloadsDir != null && downloadsDir.exists()) downloadsDir else context.filesDir
            val pdfFile = File(targetDir, fileName)

            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "كروت ${batch.batchId} - ABO TALAL")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "حفظ أو مشاركة ملف PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "تم حفظ ملف PDF بنجاح (${users.size} كرت)", Toast.LENGTH_LONG).show()
            onComplete(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ في حفظ PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            onComplete(null)
        }
    }

    /**
     * Export to CSV/Excel with UTF-8 BOM
     */
    fun exportBatchToExcel(
        context: Context,
        batch: GeneratedBatchRecord,
        users: List<UserManagerUser>,
        posName: String = "المركز الرئيسي"
    ): File? {
        try {
            val fileName = "ABO_TALAL_TK_${batch.batchId}_${System.currentTimeMillis()}.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write("م,اسم المستخدم,كلمة المرور,الباقة,السعر,نقطة البيع,تاريخ التوليد,الدفعة,الحالة\n")
                    users.forEachIndexed { index, user ->
                        val num = index + 1
                        val uName = sanitizeCsv(user.username)
                        val uPass = sanitizeCsv(user.password)
                        val uProf = sanitizeCsv(user.profile.ifEmpty { batch.profileName })
                        val uPrice = batch.pricePerCard
                        val uPos = sanitizeCsv(posName)
                        val uDate = sanitizeCsv(batch.date)
                        val uBatch = sanitizeCsv(batch.batchId)
                        val uStatus = if (user.active) "نشط" else "غير نشط"
                        writer.write("$num,$uName,$uPass,$uProf,$uPrice,$uPos,$uDate,$uBatch,$uStatus\n")
                    }
                    writer.flush()
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تصدير دفعة كروت: ${batch.batchId}")
                putExtra(Intent.EXTRA_TEXT, "ملف إكسل لكروت الدفعة ${batch.batchId} - نظام ABO TALAL (${users.size} كرت)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة أو فتح في Excel")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "تم تصدير ${users.size} كرت إلى Excel بنجاح!", Toast.LENGTH_LONG).show()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ أثناء التصدير: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    private fun sanitizeCsv(input: String): String {
        var res = input.replace("\"", "\"\"")
        if (res.contains(",") || res.contains("\n") || res.contains("\"")) {
            res = "\"$res\""
        }
        return res
    }

    private fun loadPresetBitmap(context: Context, bgPreset: String, customBgPath: String?): Bitmap? {
        if (bgPreset == "custom" && !customBgPath.isNullOrEmpty()) {
            return try {
                val file = File(customBgPath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(customBgPath)
                } else null
            } catch (_: Exception) {
                null
            }
        }
        val resId = when (bgPreset) {
            "bg_card_200" -> R.drawable.bg_card_200
            "bg_card_500" -> R.drawable.bg_card_500
            "none" -> 0
            else -> R.drawable.bg_card_100
        }
        if (resId == 0) return null
        return try {
            val opts = BitmapFactory.Options().apply {
                inScaled = false
            }
            BitmapFactory.decodeResource(context.resources, resId, opts)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Render an entire A4 page with high fidelity cards and configured elements
     */
    fun drawHighFidelityCardPage(
        canvas: AndroidCanvas,
        pageIndex: Int,
        totalPages: Int,
        users: List<UserManagerUser>,
        config: CardTemplateConfig,
        bgBitmap: Bitmap?,
        batch: GeneratedBatchRecord
    ) {
        val pageWidth = canvas.width.toFloat()
        val pageHeight = canvas.height.toFloat()

        // Page Margins in points
        val marginX = 16f
        val marginY = 18f
        val footerSpace = if (config.pageNumberingEnabled || config.pageNoteEnabled) 20f else 6f

        val printableW = pageWidth - (marginX * 2)
        val printableH = pageHeight - (marginY * 2) - footerSpace

        val cols = config.columns.coerceIn(1, 10)
        val rows = config.rows.coerceIn(1, 30)
        val cardsPerPage = cols * rows

        val hSpacing = config.horizontalMarginMm * 2.83f // Convert mm to pt
        val vSpacing = config.verticalMarginMm * 2.83f

        val totalHSpacing = (cols - 1) * hSpacing
        val totalVSpacing = (rows - 1) * vSpacing

        val cardW = (printableW - totalHSpacing) / cols
        val cardH = (printableH - totalVSpacing) / rows

        val startIndex = pageIndex * cardsPerPage
        val endIndex = Math.min(startIndex + cardsPerPage, users.size)
        var cardIdx = startIndex

        // Border Paint
        val borderPaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = (config.borderSizeMm * 2.83f).coerceAtLeast(0.5f)
            color = parseColorSafe(config.borderColorHex, AndroidColor.BLACK)
        }

        // Draw Cards
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (cardIdx >= endIndex) break
                val user = users[cardIdx]

                val left = marginX + (c * (cardW + hSpacing))
                val top = marginY + (r * (cardH + vSpacing))
                val right = left + cardW
                val bottom = top + cardH
                val cardRect = AndroidRectF(left, top, right, bottom)

                // 1. Draw Background
                if (config.backgroundEnabled) {
                    if (bgBitmap != null) {
                        val bitmapPaint = AndroidPaint().apply {
                            isAntiAlias = true
                            isFilterBitmap = true
                            isDither = true
                        }
                        val src = AndroidRect(0, 0, bgBitmap.width, bgBitmap.height)
                        canvas.drawBitmap(bgBitmap, src, cardRect, bitmapPaint)
                    } else {
                        // Draw default crisp blue/white classic voucher
                        drawDefaultVoucherGraphic(canvas, cardRect, config)
                    }
                } else {
                    // Clean solid white card
                    val whitePaint = AndroidPaint().apply {
                        isAntiAlias = true
                        style = AndroidPaint.Style.FILL
                        color = AndroidColor.WHITE
                    }
                    canvas.drawRoundRect(cardRect, 3f, 3f, whitePaint)
                }

                // 2. Draw Border if enabled
                if (config.borderEnabled) {
                    canvas.drawRoundRect(cardRect, 3f, 3f, borderPaint)
                }

                // 3. Draw All Independent Configured Elements
                drawCardElements(canvas, cardRect, user, batch, config)

                cardIdx++
            }
        }

        // 4. Page Header / Note
        if (config.pageNoteEnabled && config.pageNoteText.isNotEmpty()) {
            val notePaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = config.pageNoteSize.coerceIn(6f, 16f)
                color = parseColorSafe(config.pageNoteColorHex, AndroidColor.parseColor("#0C5A60"))
                isFakeBoldText = true
            }
            val noteY = pageHeight - 8f
            canvas.drawText(config.pageNoteText, pageWidth / 2f, noteY, notePaint)
        }

        // 5. Page Numbering
        if (config.pageNumberingEnabled) {
            val numPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.RIGHT
                textSize = 8f
                color = AndroidColor.parseColor("#64748B")
            }
            val numText = config.pageNumberingText
                .replace("{page}", "${pageIndex + 1}")
                .replace("{total}", "$totalPages")
                .ifEmpty { "${pageIndex + 1}/$totalPages" }
            canvas.drawText(numText, pageWidth - marginX, pageHeight - 8f, numPaint)
        }
    }

    private fun drawCardElements(
        canvas: AndroidCanvas,
        cardRect: AndroidRectF,
        user: UserManagerUser,
        batch: GeneratedBatchRecord,
        config: CardTemplateConfig
    ) {
        val cardW = cardRect.width()
        val cardH = cardRect.height()

        val cardWidthMm = if (config.autoFitA4) {
            val usableW = 210f - (config.pageMarginMm * 2) - (config.horizontalMarginMm * (config.columns - 1))
            (usableW / config.columns.coerceAtLeast(1)).coerceAtLeast(10f)
        } else config.cardWidthMm

        val cardHeightMm = if (config.autoFitA4) {
            val usableH = 297f - (config.pageMarginMm * 2) - (config.verticalMarginMm * (config.rows - 1))
            (usableH / config.rows.coerceAtLeast(1)).coerceAtLeast(10f)
        } else config.cardHeightMm

        val scaleX = cardW / cardWidthMm
        val scaleY = cardH / cardHeightMm

        // Helper to draw text element based on xMm from right and yMm from top
        fun drawTextItem(elem: CardElementConfig, displayText: String) {
            if (!elem.visible || displayText.isEmpty()) return
            val posX = cardRect.right - (elem.xMm * scaleX)
            val posY = cardRect.top + (elem.yMm * scaleY) + (elem.fontSize * 0.85f)

            val paint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.RIGHT
                textSize = elem.fontSize.coerceIn(5f, 26f)
                color = parseColorSafe(elem.colorHex, AndroidColor.parseColor("#0F172A"))
                isFakeBoldText = elem.isBold
            }
            canvas.drawText(displayText, posX, posY, paint)
        }

        // 1. Username / Code (ALWAYS displays real user code, positioned at exact xMm & yMm)
        if (config.usernameConfig.visible) {
            val uCfg = config.usernameConfig
            val posX = cardRect.right - (uCfg.xMm * scaleX)
            val posY = cardRect.top + (uCfg.yMm * scaleY) + (uCfg.fontSize * 0.85f)

            val uPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.RIGHT
                textSize = uCfg.fontSize.coerceIn(6f, 24f)
                color = parseColorSafe(uCfg.colorHex, AndroidColor.parseColor("#0F172A"))
                isFakeBoldText = uCfg.isBold
            }

            if (uCfg.showTitle && uCfg.titleText.isNotEmpty()) {
                val titlePaint = AndroidPaint().apply {
                    isAntiAlias = true
                    textAlign = AndroidPaint.Align.RIGHT
                    textSize = (uCfg.fontSize * 0.65f).coerceIn(5f, 12f)
                    color = parseColorSafe(uCfg.colorHex, AndroidColor.DKGRAY)
                }
                canvas.drawText(uCfg.titleText, posX, posY - (uCfg.fontSize * 0.85f), titlePaint)
            }

            // Real user username code
            canvas.drawText(user.username, posX, posY, uPaint)
        }

        // 2. Title
        if (config.titleConfig.visible) {
            drawTextItem(config.titleConfig, config.titleConfig.titleText)
        }

        // 3. Password
        if (config.passwordConfig.visible) {
            val pText = if (user.password.isNotEmpty()) user.password else user.username
            drawTextItem(config.passwordConfig, pText)
        }

        // 4. Price
        if (config.priceConfig.visible) {
            val prText = if (config.priceConfig.titleText.isNotEmpty()) {
                config.priceConfig.titleText
            } else if (batch.pricePerCard > 0) {
                "${batch.pricePerCard} ر.ي"
            } else {
                "500 ر.ي"
            }
            drawTextItem(config.priceConfig, prText)
        }

        // 5. Profile
        if (config.profileConfig.visible) {
            val pfText = if (config.profileConfig.titleText.isNotEmpty()) {
                config.profileConfig.titleText
            } else if (user.profile.isNotEmpty()) {
                user.profile
            } else {
                batch.profileName
            }
            drawTextItem(config.profileConfig, pfText)
        }

        // 6. Payment / Batch
        if (config.paymentConfig.visible) {
            val payText = if (config.paymentConfig.titleText.isNotEmpty()) {
                config.paymentConfig.titleText
            } else {
                batch.batchId
            }
            drawTextItem(config.paymentConfig, payText)
        }

        // 7. Serial
        if (config.serialConfig.visible) {
            val sText = if (config.serialConfig.titleText.isNotEmpty()) {
                config.serialConfig.titleText
            } else {
                user.username.takeLast(4)
            }
            drawTextItem(config.serialConfig, sText)
        }

        // 8. POS
        if (config.posConfig.visible) {
            val posText = if (config.posConfig.titleText.isNotEmpty()) {
                config.posConfig.titleText
            } else {
                "المركز الرئيسي"
            }
            drawTextItem(config.posConfig, posText)
        }

        // 9. Barcode
        if (config.barcodeConfig.visible) {
            val bCfg = config.barcodeConfig
            val bw = (bCfg.widthMm * scaleX).coerceAtLeast(10f)
            val bh = (bCfg.heightMm * scaleY).coerceAtLeast(6f)
            val bx = cardRect.right - (bCfg.xMm * scaleX) - bw
            val by = cardRect.top + (bCfg.yMm * scaleY)
            val bRect = AndroidRectF(bx, by, bx + bw, by + bh)

            val bgPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                color = AndroidColor.WHITE
            }
            canvas.drawRoundRect(bRect, 2f, 2f, bgPaint)

            val barPaint = AndroidPaint().apply {
                isAntiAlias = true
                color = AndroidColor.BLACK
                strokeWidth = 1.1f
            }
            val lineCount = 10
            val step = bw / (lineCount + 1)
            for (i in 1..lineCount) {
                val lx = bRect.left + (i * step)
                canvas.drawLine(lx, bRect.top + 2f, lx, bRect.bottom - 2f, barPaint)
            }
        }

        // 10. Logo
        if (config.logoConfig.visible) {
            val lCfg = config.logoConfig
            val lw = (lCfg.widthMm * scaleX).coerceAtLeast(8f)
            val lh = (lCfg.heightMm * scaleY).coerceAtLeast(8f)
            val lx = cardRect.right - (lCfg.xMm * scaleX) - lw
            val ly = cardRect.top + (lCfg.yMm * scaleY)
            val lRect = AndroidRectF(lx, ly, lx + lw, ly + lh)

            val logoPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.STROKE
                strokeWidth = 1f
                color = AndroidColor.DKGRAY
            }
            canvas.drawRoundRect(lRect, 2f, 2f, logoPaint)
        }
    }

    private fun drawDefaultVoucherGraphic(
        canvas: AndroidCanvas,
        cardRect: AndroidRectF,
        config: CardTemplateConfig
    ) {
        // Deep Navy Background like Screenshot 1
        val bgPaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
            color = AndroidColor.parseColor("#0C2340") // Deep Luxury Navy
        }
        canvas.drawRoundRect(cardRect, 3f, 3f, bgPaint)

        // Right White Price Pill
        val pillRight = cardRect.right - 3f
        val pillLeft = cardRect.right - (cardRect.width() * 0.22f)
        val pillTop = cardRect.top + 3f
        val pillBottom = cardRect.bottom - 3f
        val pricePill = AndroidRectF(pillLeft, pillTop, pillRight, pillBottom)

        val whitePaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
            color = AndroidColor.WHITE
        }
        canvas.drawRoundRect(pricePill, 8f, 8f, whitePaint)

        // Center White Pin/Username Pill
        val pinLeft = cardRect.left + (cardRect.width() * 0.22f)
        val pinRight = pillLeft - 4f
        val pinTop = cardRect.top + (cardRect.height() * 0.22f)
        val pinBottom = cardRect.top + (cardRect.height() * 0.58f)
        val pinPill = AndroidRectF(pinLeft, pinTop, pinRight, pinBottom)
        canvas.drawRoundRect(pinPill, 6f, 6f, whitePaint)

        // Left Network Icon Area
        val netPaint = AndroidPaint().apply {
            isAntiAlias = true
            textAlign = AndroidPaint.Align.CENTER
            textSize = 5.5f
            color = AndroidColor.WHITE
            isFakeBoldText = true
        }
        val leftCenterX = cardRect.left + (cardRect.width() * 0.11f)
        canvas.drawText("Network", leftCenterX, cardRect.bottom - 5f, netPaint)
    }

    private fun parseColorSafe(hex: String, fallback: Int): Int {
        return try {
            if (hex.startsWith("#")) AndroidColor.parseColor(hex)
            else AndroidColor.parseColor("#$hex")
        } catch (_: Exception) {
            fallback
        }
    }
}
