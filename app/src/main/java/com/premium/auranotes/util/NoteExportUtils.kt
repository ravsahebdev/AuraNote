package com.premium.auranotes.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.premium.auranotes.data.Folder
import com.premium.auranotes.data.Note
import java.io.File
import java.io.FileOutputStream

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val notes: List<Note>,
    val folders: List<Folder>
)

object NoteExportUtils {

    fun shareText(context: Context, note: Note) {
        val text = MarkdownConverter.toPlainText(note)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, note.title.ifBlank { "Aura Note" })
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Aura Note" })
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Note")
        context.startActivity(shareIntent)
    }

    fun shareMarkdown(context: Context, note: Note) {
        val md = MarkdownConverter.toMarkdown(note)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, note.title.ifBlank { "Aura Note" })
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Aura Note" })
            putExtra(Intent.EXTRA_TEXT, md)
            type = "text/markdown"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Markdown")
        context.startActivity(shareIntent)
    }

    fun generateAndSharePdf(context: Context, note: Note) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points
            val pageHeight = 842 // A4 standard height in points
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val margin = 50f
            var yPosition = margin + 30f

            // Draw title
            val title = note.title.ifBlank { "Untitled Note" }
            canvas.drawText(title, margin, yPosition, titlePaint)
            yPosition += 35f

            // Draw divider line
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
            yPosition += 25f

            // Draw body lines
            val plainText = MarkdownConverter.toPlainText(note)
            val lines = plainText.split("\n")

            for (line in lines) {
                if (yPosition > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = margin + 20f
                }

                // Simple wrap
                val maxChars = 75
                if (line.length > maxChars) {
                    val chunks = line.chunked(maxChars)
                    for (chunk in chunks) {
                        canvas.drawText(chunk, margin, yPosition, bodyPaint)
                        yPosition += 18f
                    }
                } else {
                    canvas.drawText(line, margin, yPosition, bodyPaint)
                    yPosition += 18f
                }
            }

            pdfDocument.finishPage(page)

            val safeTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(30)
            val pdfFile = File(context.cacheDir, "$safeTitle.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text share
            shareText(context, note)
        }
    }

    fun exportToJson(notes: List<Note>, folders: List<Folder>): String {
        val backup = BackupData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            notes = notes,
            folders = folders
        )
        return Gson().toJson(backup)
    }

    fun importFromJson(json: String): BackupData? {
        return try {
            val type = object : TypeToken<BackupData>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}
