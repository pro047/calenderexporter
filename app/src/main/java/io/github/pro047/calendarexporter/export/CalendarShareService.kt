package io.github.pro047.calendarexporter.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.pro047.calendarexporter.R
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.io.File
import java.time.YearMonth
import java.time.ZoneId

class CalendarShareService(private val context: Context) {
    fun share(
        month: YearMonth,
        events: List<NormalizedCalendarEvent>,
        format: ExportFormat,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(directory, "calendar-$month.${format.extension}")
        file.writeText(
            CalendarExportFormatter.format(events, format, zoneId),
            Charsets.UTF_8,
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_chooser_title)),
        )
    }
}
