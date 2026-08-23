package io.github.pro047.calendarexporter.export

import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CalendarExportFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun format(
        events: List<NormalizedCalendarEvent>,
        format: ExportFormat,
        zoneId: ZoneId,
    ): String = when (format) {
        ExportFormat.CSV -> toCsv(events, zoneId)
        ExportFormat.JSON -> toJson(events, zoneId)
        ExportFormat.TEXT -> toText(events, zoneId)
    }

    fun toCsv(events: List<NormalizedCalendarEvent>, zoneId: ZoneId): String = buildString {
        append('\uFEFF')
        appendLine(
            "calendar_name,title,description,location,start_date,start_time," +
                "end_date,end_time,all_day,timezone,event_id,instance_key",
        )
        events.forEach { event ->
            val fields = csvFields(event, zoneId)
            appendLine(fields.joinToString(",", transform = ::escapeCsv))
        }
    }

    fun toJson(events: List<NormalizedCalendarEvent>, zoneId: ZoneId): String = buildString {
        appendLine("[")
        events.forEachIndexed { index, event ->
            val periodFields = when (val period = event.period) {
                is EventPeriod.AllDay -> listOf(
                    "\"startDate\":${jsonString(period.startDate.toString())}",
                    "\"endDateExclusive\":${jsonString(period.endDateExclusive.toString())}",
                )
                is EventPeriod.Timed -> listOf(
                    "\"start\":${jsonString(period.start.atZone(zoneId).toOffsetDateTime().toString())}",
                    "\"end\":${jsonString(period.end.atZone(zoneId).toOffsetDateTime().toString())}",
                )
            }
            appendLine("  {")
            appendLine("    \"calendarName\":${jsonNullable(event.calendarName)},")
            appendLine("    \"title\":${jsonString(event.title)},")
            appendLine("    \"description\":${jsonNullable(event.description)},")
            appendLine("    \"location\":${jsonNullable(event.location)},")
            appendLine("    ${periodFields[0]},")
            appendLine("    ${periodFields[1]},")
            appendLine("    \"allDay\":${event.period is EventPeriod.AllDay},")
            appendLine("    \"timezone\":${jsonNullable(event.eventTimeZone)},")
            appendLine("    \"eventId\":${event.eventId},")
            appendLine("    \"instanceKey\":${jsonString(event.occurrenceKey)}")
            append("  }")
            if (index != events.lastIndex) append(',')
            appendLine()
        }
        append(']')
    }

    fun toText(events: List<NormalizedCalendarEvent>, zoneId: ZoneId): String = buildString {
        if (events.isEmpty()) {
            append("일정이 없습니다.")
            return@buildString
        }
        events.forEachIndexed { index, event ->
            val periodText = when (val period = event.period) {
                is EventPeriod.AllDay -> "${period.startDate} ~ ${period.endDateExclusive} (종일, 종료일 제외)"
                is EventPeriod.Timed -> {
                    val start = period.start.atZone(zoneId)
                    val end = period.end.atZone(zoneId)
                    "$start ~ $end"
                }
            }
            appendLine("[$periodText] ${event.title}")
            appendLine("일정표: ${event.calendarName.orEmpty()}")
            event.location?.takeUnless(String::isBlank)?.let { appendLine("장소: $it") }
            event.description?.takeUnless(String::isBlank)?.let { appendLine("설명: $it") }
            if (index != events.lastIndex) appendLine()
        }
    }.trimEnd()

    private fun csvFields(event: NormalizedCalendarEvent, zoneId: ZoneId): List<String> {
        val temporal = when (val period = event.period) {
            is EventPeriod.AllDay -> listOf(
                period.startDate.toString(),
                "",
                period.endDateExclusive.toString(),
                "",
                "true",
            )
            is EventPeriod.Timed -> {
                val start = period.start.atZone(zoneId)
                val end = period.end.atZone(zoneId)
                listOf(
                    start.toLocalDate().toString(),
                    start.toLocalTime().format(timeFormatter),
                    end.toLocalDate().toString(),
                    end.toLocalTime().format(timeFormatter),
                    "false",
                )
            }
        }
        return listOf(
            event.calendarName.orEmpty(),
            event.title,
            event.description.orEmpty(),
            event.location.orEmpty(),
        ) + temporal + listOf(
            event.eventTimeZone.orEmpty(),
            event.eventId.toString(),
            event.occurrenceKey,
        )
    }

    private fun escapeCsv(raw: String): String {
        val dangerousPrefix = raw.trimStart().firstOrNull() in setOf('=', '+', '-', '@') ||
            raw.firstOrNull() in setOf('\t', '\r')
        val formulaSafe = if (dangerousPrefix) "'$raw" else raw
        return "\"${formulaSafe.replace("\"", "\"\"")}\""
    }

    private fun jsonNullable(value: String?): String = value?.let(::jsonString) ?: "null"

    private fun jsonString(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u%04x".format(character.code))
                } else {
                    append(character)
                }
            }
        }
        append('"')
    }
}
