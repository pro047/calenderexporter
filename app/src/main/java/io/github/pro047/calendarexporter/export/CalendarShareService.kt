package io.github.pro047.calendarexporter.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.github.pro047.calendarexporter.R
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.io.File
import java.time.YearMonth
import java.time.ZoneId

class CalendarShareService(
    private val context: Context,
    private val fileUriProvider: (File) -> Uri = { file ->
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    },
    private val activityStarter: (Intent) -> Unit = context::startActivity,
) {
    fun shareTextFile(
        month: YearMonth,
        events: List<NormalizedCalendarEvent>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(directory, "calendar-$month.txt")
        file.writeText(CalendarExportFormatter.formatMonth(month, events, zoneId), Charsets.UTF_8)
        val uri = fileUriProvider(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activityStarter(
            Intent.createChooser(intent, context.getString(R.string.share_file_chooser_title)),
        )
    }

    fun sharePlainText(
        month: YearMonth,
        events: List<NormalizedCalendarEvent>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.share_subject, month.year, month.monthValue),
            )
            putExtra(Intent.EXTRA_TEXT, CalendarExportFormatter.formatMonth(month, events, zoneId))
        }
        activityStarter(
            Intent.createChooser(intent, context.getString(R.string.share_text_chooser_title)),
        )
    }
}
