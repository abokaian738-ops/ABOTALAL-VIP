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

data class CardElementConfig(
    val visible: Boolean = true,
    val showTitle: Boolean = false,
    val titleText: String = "",
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val fontSize: Float = 10f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val colorHex: String = "#0F172A",
    val widthMm: Float = 10f,
    val heightMm: Float = 10f
)

data class CardTemplateConfig(
    val templateName: String = "افتراضي",
    val columns: Int = 3,
    val rows: Int = 10,
    val autoFitA4: Boolean = true,
    val horizontalMarginMm: Float = 1.0f,
    val verticalMarginMm: Float = 1.0f,
    val backgroundEnabled: Boolean = true,
    val bgPreset: String = "bg_card_100",
    val customBgPath: String? = null,
    val borderEnabled: Boolean = true,
    val borderSizeMm: Float = 0.35f,
    val borderColorHex: String = "#000000",
    val pageNoteEnabled: Boolean = true,
    val pageNoteText: String = "أهلاً بكم في شبكة ABO TALAL VIP",
    val pageNoteX: Float = 50f,
    val pageNoteY: Float = 98f,
    val pageNoteSize: Float = 9f,
    val pageNoteColorHex: String = "#0C5A60",
    val pageNumberingEnabled: Boolean = true,
    val pageNumberingText: String = "{page}",
    // Independent Elements:
    val usernameConfig: CardElementConfig = CardElementConfig(
        visible = true,
        showTitle = true,
        titleText = "رمز الدخول ↓",
        xPercent = 0.50f,
        yPercent = 0.42f,
        fontSize = 12f,
        isBold = true,
        colorHex = "#0F172A"
    ),
    val passwordConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = true,
        titleText = "كلمة المرور:",
        xPercent = 0.50f,
        yPercent = 0.65f,
        fontSize = 9.5f,
        isBold = true,
        colorHex = "#0F172A"
    ),
    val priceConfig: CardElementConfig = CardElementConfig(
        visible = true,
        showTitle = true,
        titleText = "ريال",
        xPercent = 0.88f,
        yPercent = 0.50f,
        fontSize = 13f,
        isBold = true,
        colorHex = "#0C5A60"
    ),
    val profileConfig: CardElementConfig = CardElementConfig(
        visible = true,
        showTitle = false,
        titleText = "الباقة",
        xPercent = 0.50f,
        yPercent = 0.82f,
        fontSize = 8f,
        isBold = true,
        colorHex = "#475569"
    ),
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
    val serialConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = true,
        titleText = "تسلسلي:",
        xPercent = 0.15f,
        yPercent = 0.90f,
        fontSize = 6.5f,
        isBold = false,
        colorHex = "#64748B"
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
    ),
    val posConfig: CardElementConfig = CardElementConfig(
        visible = false,
        showTitle = false,
        titleText = "نقطة البيع: المركز الرئيسي",
        xPercent = 0.50f,
        yPercent = 0.95f,
        fontSize = 6.5f,
        isBold = false,
        colorHex = "#64748B"
    )
)

object CardPrintAndExportHelper {

    fun loadTemplateConfig(context: Context, templateName: String): CardTemplateConfig {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val p = templateName
        return CardTemplateConfig(
            templateName = templateName,
            columns = prefs.getInt("${p}_columns", 3),
            rows = prefs.getInt("${p}_rows", 10),
            autoFitA4 = prefs.getBoolean("${p}_auto_fit", true),
            horizontalMarginMm = prefs.getFloat("${p}_h_margin", 1.0f),
            verticalMarginMm = prefs.getFloat("${p}_v_margin", 1.0f),
            backgroundEnabled = prefs.getBoolean("${p}_bg_enabled", true),
            bgPreset = prefs.getString("${p}_bg_preset", "bg_card_100") ?: "bg_card_100",
            customBgPath = prefs.getString("${p}_custom_bg_path", null),
            borderEnabled = prefs.getBoolean("${p}_border_enabled", true),
            borderSizeMm = prefs.getFloat("${p}_border_size", 0.35f),
            borderColorHex = prefs.getString("${p}_border_color", "#000000") ?: "#000000",
            pageNoteEnabled = prefs.getBoolean("${p}_page_note_enabled", true),
            pageNoteText = prefs.getString("${p}_page_note_text", "أهلاً بكم في شبكة ABO TALAL VIP") ?: "أهلاً بكم في شبكة ABO TALAL VIP",
            pageNoteSize = prefs.getFloat("${p}_page_note_size", 9f),
            pageNoteColorHex = prefs.getString("${p}_page_note_color", "#0C5A60") ?: "#0C5A60",
            pageNumberingEnabled = prefs.getBoolean("${p}_page_num_enabled", true),
            pageNumberingText = prefs.getString("${p}_page_num_text", "{page}") ?: "{page}",
            usernameConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_user_vis", true),
                showTitle = prefs.getBoolean("${p}_user_title_on", true),
                titleText = prefs.getString("${p}_user_title_text", "رمز الدخول ↓") ?: "رمز الدخول ↓",
                xPercent = prefs.getFloat("${p}_user_x", 0.50f),
                yPercent = prefs.getFloat("${p}_user_y", 0.42f),
                fontSize = prefs.getFloat("${p}_user_size", 12f),
                isBold = prefs.getBoolean("${p}_user_bold", true),
                colorHex = prefs.getString("${p}_user_color", "#0F172A") ?: "#0F172A"
            ),
            passwordConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_pass_vis", false),
                showTitle = prefs.getBoolean("${p}_pass_title_on", true),
                titleText = prefs.getString("${p}_pass_title_text", "كلمة المرور:") ?: "كلمة المرور:",
                xPercent = prefs.getFloat("${p}_pass_x", 0.50f),
                yPercent = prefs.getFloat("${p}_pass_y", 0.65f),
                fontSize = prefs.getFloat("${p}_pass_size", 9.5f),
                isBold = prefs.getBoolean("${p}_pass_bold", true),
                colorHex = prefs.getString("${p}_pass_color", "#0F172A") ?: "#0F172A"
            ),
            priceConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_price_vis", true),
                showTitle = prefs.getBoolean("${p}_price_title_on", true),
                titleText = prefs.getString("${p}_price_title_text", "ريال") ?: "ريال",
                xPercent = prefs.getFloat("${p}_price_x", 0.88f),
                yPercent = prefs.getFloat("${p}_price_y", 0.50f),
                fontSize = prefs.getFloat("${p}_price_size", 13f),
                isBold = prefs.getBoolean("${p}_price_bold", true),
                colorHex = prefs.getString("${p}_price_color", "#0C5A60") ?: "#0C5A60"
            ),
            profileConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_prof_vis", true),
                showTitle = prefs.getBoolean("${p}_prof_title_on", false),
                titleText = prefs.getString("${p}_prof_title_text", "الباقة") ?: "الباقة",
                xPercent = prefs.getFloat("${p}_prof_x", 0.50f),
                yPercent = prefs.getFloat("${p}_prof_y", 0.82f),
                fontSize = prefs.getFloat("${p}_prof_size", 8f),
                isBold = prefs.getBoolean("${p}_prof_bold", true),
                colorHex = prefs.getString("${p}_prof_color", "#475569") ?: "#475569"
            ),
            validityConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_val_vis", false),
                showTitle = prefs.getBoolean("${p}_val_title_on", true),
                titleText = prefs.getString("${p}_val_title_text", "صلاحية:") ?: "صلاحية:",
                xPercent = prefs.getFloat("${p}_val_x", 0.25f),
                yPercent = prefs.getFloat("${p}_val_y", 0.82f),
                fontSize = prefs.getFloat("${p}_val_size", 7f),
                colorHex = prefs.getString("${p}_val_color", "#475569") ?: "#475569"
            ),
            quotaConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_quota_vis", false),
                showTitle = prefs.getBoolean("${p}_quota_title_on", true),
                titleText = prefs.getString("${p}_quota_title_text", "الرصيد:") ?: "الرصيد:",
                xPercent = prefs.getFloat("${p}_quota_x", 0.75f),
                yPercent = prefs.getFloat("${p}_quota_y", 0.82f),
                fontSize = prefs.getFloat("${p}_quota_size", 7f),
                colorHex = prefs.getString("${p}_quota_color", "#475569") ?: "#475569"
            ),
            qrConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_qr_vis", false),
                xPercent = prefs.getFloat("${p}_qr_x", 0.15f),
                yPercent = prefs.getFloat("${p}_qr_y", 0.50f),
                widthMm = prefs.getFloat("${p}_qr_w", 12f),
                heightMm = prefs.getFloat("${p}_qr_h", 12f)
            ),
            serialConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_ser_vis", false),
                showTitle = prefs.getBoolean("${p}_ser_title_on", true),
                titleText = prefs.getString("${p}_ser_title_text", "تسلسلي:") ?: "تسلسلي:",
                xPercent = prefs.getFloat("${p}_ser_x", 0.15f),
                yPercent = prefs.getFloat("${p}_ser_y", 0.90f),
                fontSize = prefs.getFloat("${p}_ser_size", 6.5f),
                colorHex = prefs.getString("${p}_ser_color", "#64748B") ?: "#64748B"
            ),
            batchConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_batch_vis", false),
                showTitle = prefs.getBoolean("${p}_batch_title_on", true),
                titleText = prefs.getString("${p}_batch_title_text", "الدفعة:") ?: "الدفعة:",
                xPercent = prefs.getFloat("${p}_batch_x", 0.85f),
                yPercent = prefs.getFloat("${p}_batch_y", 0.90f),
                fontSize = prefs.getFloat("${p}_batch_size", 6.5f),
                colorHex = prefs.getString("${p}_batch_color", "#64748B") ?: "#64748B"
            ),
            posConfig = CardElementConfig(
                visible = prefs.getBoolean("${p}_pos_vis", false),
                titleText = prefs.getString("${p}_pos_text", "نقطة البيع: المركز الرئيسي") ?: "نقطة البيع: المركز الرئيسي",
                xPercent = prefs.getFloat("${p}_pos_x", 0.50f),
                yPercent = prefs.getFloat("${p}_pos_y", 0.95f),
                fontSize = prefs.getFloat("${p}_pos_size", 6.5f),
                colorHex = prefs.getString("${p}_pos_color", "#64748B") ?: "#64748B"
            )
        )
    }

    fun saveTemplateConfig(context: Context, config: CardTemplateConfig) {
        val prefs = context.getSharedPreferences("card_templates_prefs", Context.MODE_PRIVATE)
        val p = config.templateName
        prefs.edit()
            .putInt("${p}_columns", config.columns)
            .putInt("${p}_rows", config.rows)
            .putBoolean("${p}_auto_fit", config.autoFitA4)
            .putFloat("${p}_h_margin", config.horizontalMarginMm)
            .putFloat("${p}_v_margin", config.verticalMarginMm)
            .putBoolean("${p}_bg_enabled", config.backgroundEnabled)
            .putString("${p}_bg_preset", config.bgPreset)
            .putString("${p}_custom_bg_path", config.customBgPath)
            .putBoolean("${p}_border_enabled", config.borderEnabled)
            .putFloat("${p}_border_size", config.borderSizeMm)
            .putString("${p}_border_color", config.borderColorHex)
            .putBoolean("${p}_page_note_enabled", config.pageNoteEnabled)
            .putString("${p}_page_note_text", config.pageNoteText)
            .putFloat("${p}_page_note_size", config.pageNoteSize)
            .putString("${p}_page_note_color", config.pageNoteColorHex)
            .putBoolean("${p}_page_num_enabled", config.pageNumberingEnabled)
            .putString("${p}_page_num_text", config.pageNumberingText)
            // Username
            .putBoolean("${p}_user_vis", config.usernameConfig.visible)
            .putBoolean("${p}_user_title_on", config.usernameConfig.showTitle)
            .putString("${p}_user_title_text", config.usernameConfig.titleText)
            .putFloat("${p}_user_x", config.usernameConfig.xPercent)
            .putFloat("${p}_user_y", config.usernameConfig.yPercent)
            .putFloat("${p}_user_size", config.usernameConfig.fontSize)
            .putBoolean("${p}_user_bold", config.usernameConfig.isBold)
            .putString("${p}_user_color", config.usernameConfig.colorHex)
            // Password
            .putBoolean("${p}_pass_vis", config.passwordConfig.visible)
            .putBoolean("${p}_pass_title_on", config.passwordConfig.showTitle)
            .putString("${p}_pass_title_text", config.passwordConfig.titleText)
            .putFloat("${p}_pass_x", config.passwordConfig.xPercent)
            .putFloat("${p}_pass_y", config.passwordConfig.yPercent)
            .putFloat("${p}_pass_size", config.passwordConfig.fontSize)
            .putBoolean("${p}_pass_bold", config.passwordConfig.isBold)
            .putString("${p}_pass_color", config.passwordConfig.colorHex)
            // Price
            .putBoolean("${p}_price_vis", config.priceConfig.visible)
            .putBoolean("${p}_price_title_on", config.priceConfig.showTitle)
            .putString("${p}_price_title_text", config.priceConfig.titleText)
            .putFloat("${p}_price_x", config.priceConfig.xPercent)
            .putFloat("${p}_price_y", config.priceConfig.yPercent)
            .putFloat("${p}_price_size", config.priceConfig.fontSize)
            .putBoolean("${p}_price_bold", config.priceConfig.isBold)
            .putString("${p}_price_color", config.priceConfig.colorHex)
            // Profile
            .putBoolean("${p}_prof_vis", config.profileConfig.visible)
            .putBoolean("${p}_prof_title_on", config.profileConfig.showTitle)
            .putString("${p}_prof_title_text", config.profileConfig.titleText)
            .putFloat("${p}_prof_x", config.profileConfig.xPercent)
            .putFloat("${p}_prof_y", config.profileConfig.yPercent)
            .putFloat("${p}_prof_size", config.profileConfig.fontSize)
            .putBoolean("${p}_prof_bold", config.profileConfig.isBold)
            .putString("${p}_prof_color", config.profileConfig.colorHex)
            // Validity
            .putBoolean("${p}_val_vis", config.validityConfig.visible)
            .putBoolean("${p}_val_title_on", config.validityConfig.showTitle)
            .putString("${p}_val_title_text", config.validityConfig.titleText)
            .putFloat("${p}_val_x", config.validityConfig.xPercent)
            .putFloat("${p}_val_y", config.validityConfig.yPercent)
            .putFloat("${p}_val_size", config.validityConfig.fontSize)
            .putString("${p}_val_color", config.validityConfig.colorHex)
            // Quota
            .putBoolean("${p}_quota_vis", config.quotaConfig.visible)
            .putBoolean("${p}_quota_title_on", config.quotaConfig.showTitle)
            .putString("${p}_quota_title_text", config.quotaConfig.titleText)
            .putFloat("${p}_quota_x", config.quotaConfig.xPercent)
            .putFloat("${p}_quota_y", config.quotaConfig.yPercent)
            .putFloat("${p}_quota_size", config.quotaConfig.fontSize)
            .putString("${p}_quota_color", config.quotaConfig.colorHex)
            // QR
            .putBoolean("${p}_qr_vis", config.qrConfig.visible)
            .putFloat("${p}_qr_x", config.qrConfig.xPercent)
            .putFloat("${p}_qr_y", config.qrConfig.yPercent)
            .putFloat("${p}_qr_w", config.qrConfig.widthMm)
            .putFloat("${p}_qr_h", config.qrConfig.heightMm)
            // Serial
            .putBoolean("${p}_ser_vis", config.serialConfig.visible)
            .putBoolean("${p}_ser_title_on", config.serialConfig.showTitle)
            .putString("${p}_ser_title_text", config.serialConfig.titleText)
            .putFloat("${p}_ser_x", config.serialConfig.xPercent)
            .putFloat("${p}_ser_y", config.serialConfig.yPercent)
            .putFloat("${p}_ser_size", config.serialConfig.fontSize)
            .putString("${p}_ser_color", config.serialConfig.colorHex)
            // Batch
            .putBoolean("${p}_batch_vis", config.batchConfig.visible)
            .putBoolean("${p}_batch_title_on", config.batchConfig.showTitle)
            .putString("${p}_batch_title_text", config.batchConfig.titleText)
            .putFloat("${p}_batch_x", config.batchConfig.xPercent)
            .putFloat("${p}_batch_y", config.batchConfig.yPercent)
            .putFloat("${p}_batch_size", config.batchConfig.fontSize)
            .putString("${p}_batch_color", config.batchConfig.colorHex)
            // POS
            .putBoolean("${p}_pos_vis", config.posConfig.visible)
            .putString("${p}_pos_text", config.posConfig.titleText)
            .putFloat("${p}_pos_x", config.posConfig.xPercent)
            .putFloat("${p}_pos_y", config.posConfig.yPercent)
            .putFloat("${p}_pos_size", config.posConfig.fontSize)
            .putString("${p}_pos_color", config.posConfig.colorHex)
            .apply()
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

        printManager.print("ABO TALAL VIP - معاينة تصميم ${config.templateName}", printAdapter, null)
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

        printManager.print("ABO TALAL VIP - كروت ${batch.batchId}", printAdapter, null)
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
                putExtra(Intent.EXTRA_SUBJECT, "كروت ${batch.batchId} - ABO TALAL VIP")
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
                putExtra(Intent.EXTRA_TEXT, "ملف إكسل لكروت الدفعة ${batch.batchId} - نظام ABO TALAL VIP (${users.size} كرت)")
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

        // 1. Username / Code
        if (config.usernameConfig.visible) {
            val uCfg = config.usernameConfig
            val posX = cardRect.left + (cardW * uCfg.xPercent)
            val posY = cardRect.top + (cardH * uCfg.yPercent)

            val uPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = uCfg.fontSize.coerceIn(6f, 22f)
                color = parseColorSafe(uCfg.colorHex, AndroidColor.parseColor("#0F172A"))
                isFakeBoldText = uCfg.isBold
            }

            if (uCfg.showTitle && uCfg.titleText.isNotEmpty()) {
                val titlePaint = AndroidPaint().apply {
                    isAntiAlias = true
                    textAlign = AndroidPaint.Align.CENTER
                    textSize = (uCfg.fontSize * 0.65f).coerceIn(5f, 12f)
                    color = parseColorSafe(uCfg.colorHex, AndroidColor.DKGRAY)
                }
                canvas.drawText(uCfg.titleText, posX, posY - (uCfg.fontSize * 0.8f), titlePaint)
            }

            canvas.drawText(user.username, posX, posY, uPaint)
        }

        // 2. Password (if enabled)
        if (config.passwordConfig.visible) {
            val pCfg = config.passwordConfig
            val posX = cardRect.left + (cardW * pCfg.xPercent)
            val posY = cardRect.top + (cardH * pCfg.yPercent)

            val pPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = pCfg.fontSize.coerceIn(6f, 18f)
                color = parseColorSafe(pCfg.colorHex, AndroidColor.parseColor("#0F172A"))
                isFakeBoldText = pCfg.isBold
            }

            val passText = if (user.password.isNotEmpty()) user.password else user.username
            val displayText = if (pCfg.showTitle && pCfg.titleText.isNotEmpty()) "${pCfg.titleText} $passText" else passText
            canvas.drawText(displayText, posX, posY, pPaint)
        }

        // 3. Price / الفئة
        if (config.priceConfig.visible) {
            val prCfg = config.priceConfig
            val posX = cardRect.left + (cardW * prCfg.xPercent)
            val posY = cardRect.top + (cardH * prCfg.yPercent)

            val prPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = prCfg.fontSize.coerceIn(7f, 24f)
                color = parseColorSafe(prCfg.colorHex, AndroidColor.parseColor("#0C5A60"))
                isFakeBoldText = prCfg.isBold
            }

            val priceValue = if (batch.pricePerCard > 0) "${batch.pricePerCard}" else "100"
            canvas.drawText(priceValue, posX, posY, prPaint)

            if (prCfg.showTitle && prCfg.titleText.isNotEmpty()) {
                val currPaint = AndroidPaint().apply {
                    isAntiAlias = true
                    textAlign = AndroidPaint.Align.CENTER
                    textSize = (prCfg.fontSize * 0.55f).coerceIn(5f, 12f)
                    color = parseColorSafe(prCfg.colorHex, AndroidColor.parseColor("#0C5A60"))
                    isFakeBoldText = true
                }
                canvas.drawText(prCfg.titleText, posX, posY + (prCfg.fontSize * 0.65f), currPaint)
            }
        }

        // 4. Profile Name
        if (config.profileConfig.visible) {
            val pfCfg = config.profileConfig
            val posX = cardRect.left + (cardW * pfCfg.xPercent)
            val posY = cardRect.top + (cardH * pfCfg.yPercent)

            val pfPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = pfCfg.fontSize.coerceIn(5f, 14f)
                color = parseColorSafe(pfCfg.colorHex, AndroidColor.parseColor("#475569"))
                isFakeBoldText = pfCfg.isBold
            }

            val pText = if (user.profile.isNotEmpty()) user.profile else batch.profileName
            val fullText = if (pfCfg.showTitle && pfCfg.titleText.isNotEmpty()) "${pfCfg.titleText}: $pText" else pText
            canvas.drawText(fullText, posX, posY, pfPaint)
        }

        // 5. Validity
        if (config.validityConfig.visible) {
            val vCfg = config.validityConfig
            val posX = cardRect.left + (cardW * vCfg.xPercent)
            val posY = cardRect.top + (cardH * vCfg.yPercent)

            val vPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = vCfg.fontSize.coerceIn(5f, 12f)
                color = parseColorSafe(vCfg.colorHex, AndroidColor.parseColor("#475569"))
                isFakeBoldText = vCfg.isBold
            }
            val text = if (vCfg.showTitle && vCfg.titleText.isNotEmpty()) "${vCfg.titleText} 7 أيام" else "7 أيام"
            canvas.drawText(text, posX, posY, vPaint)
        }

        // 6. Quota
        if (config.quotaConfig.visible) {
            val qCfg = config.quotaConfig
            val posX = cardRect.left + (cardW * qCfg.xPercent)
            val posY = cardRect.top + (cardH * qCfg.yPercent)

            val qPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = qCfg.fontSize.coerceIn(5f, 12f)
                color = parseColorSafe(qCfg.colorHex, AndroidColor.parseColor("#475569"))
                isFakeBoldText = qCfg.isBold
            }
            val text = if (qCfg.showTitle && qCfg.titleText.isNotEmpty()) "${qCfg.titleText} 2GB" else "2GB"
            canvas.drawText(text, posX, posY, qPaint)
        }

        // 7. QR / Barcode (ONLY if enabled by user!)
        if (config.qrConfig.visible) {
            val qrCfg = config.qrConfig
            val posX = cardRect.left + (cardW * qrCfg.xPercent)
            val posY = cardRect.top + (cardH * qrCfg.yPercent)
            val sizePt = (qrCfg.widthMm * 2.83f).coerceIn(15f, 50f)

            val qrRect = AndroidRectF(posX - (sizePt / 2f), posY - (sizePt / 2f), posX + (sizePt / 2f), posY + (sizePt / 2f))
            val qrBgPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                color = AndroidColor.WHITE
            }
            canvas.drawRoundRect(qrRect, 2f, 2f, qrBgPaint)

            // Draw clean barcode lines
            val barPaint = AndroidPaint().apply {
                isAntiAlias = true
                color = AndroidColor.BLACK
                strokeWidth = 1.2f
            }
            val lineCount = 8
            val step = sizePt / (lineCount + 1)
            for (i in 1..lineCount) {
                val lx = qrRect.left + (i * step)
                canvas.drawLine(lx, qrRect.top + 2f, lx, qrRect.bottom - 2f, barPaint)
            }
        }

        // 8. Serial Number
        if (config.serialConfig.visible) {
            val sCfg = config.serialConfig
            val posX = cardRect.left + (cardW * sCfg.xPercent)
            val posY = cardRect.top + (cardH * sCfg.yPercent)

            val sPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = sCfg.fontSize.coerceIn(4.5f, 10f)
                color = parseColorSafe(sCfg.colorHex, AndroidColor.parseColor("#64748B"))
            }
            val sText = if (sCfg.showTitle && sCfg.titleText.isNotEmpty()) "${sCfg.titleText} #1024" else "#1024"
            canvas.drawText(sText, posX, posY, sPaint)
        }

        // 9. Batch ID
        if (config.batchConfig.visible) {
            val bCfg = config.batchConfig
            val posX = cardRect.left + (cardW * bCfg.xPercent)
            val posY = cardRect.top + (cardH * bCfg.yPercent)

            val bPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = bCfg.fontSize.coerceIn(4.5f, 10f)
                color = parseColorSafe(bCfg.colorHex, AndroidColor.parseColor("#64748B"))
            }
            val bText = if (bCfg.showTitle && bCfg.titleText.isNotEmpty()) "${bCfg.titleText} ${batch.batchId}" else batch.batchId
            canvas.drawText(bText, posX, posY, bPaint)
        }

        // 10. Point of Sale (POS)
        if (config.posConfig.visible && config.posConfig.titleText.isNotEmpty()) {
            val pCfg = config.posConfig
            val posX = cardRect.left + (cardW * pCfg.xPercent)
            val posY = cardRect.top + (cardH * pCfg.yPercent)

            val posPaint = AndroidPaint().apply {
                isAntiAlias = true
                textAlign = AndroidPaint.Align.CENTER
                textSize = pCfg.fontSize.coerceIn(4.5f, 10f)
                color = parseColorSafe(pCfg.colorHex, AndroidColor.parseColor("#64748B"))
            }
            canvas.drawText(config.posConfig.titleText, posX, posY, posPaint)
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
