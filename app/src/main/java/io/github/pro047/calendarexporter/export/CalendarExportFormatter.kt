package io.github.pro047.calendarexporter.export

import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalendarExportFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일(E)", Locale.KOREAN)
    private val timeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

    fun formatMonth(
        month: YearMonth,
        events: List<NormalizedCalendarEvent>,
        zoneId: ZoneId,
    ): String = buildString {
        appendLine("${month.year}년 ${month.monthValue}월 일정")
        if (events.isEmpty()) {
            appendLine()
            append("일정이 없습니다.")
            return@buildString
        }
        appendLine()
        events.forEachIndexed { index, event ->
            val periodText = when (val period = event.period) {
                is EventPeriod.AllDay -> {
                    val endDateInclusive = period.endDateExclusive.minusDays(1)
                    if (period.startDate == endDateInclusive) {
                        "${period.startDate.format(dateFormatter)} 종일"
                    } else {
                        "${period.startDate.format(dateFormatter)} ~ " +
                            "${endDateInclusive.format(dateFormatter)} 종일"
                    }
                }
                is EventPeriod.Timed -> {
                    val start = period.start.atZone(zoneId)
                    val end = period.end.atZone(zoneId)
                    if (start.toLocalDate() == end.toLocalDate()) {
                        "${start.format(dateFormatter)} " +
                            "${start.format(timeFormatter)} ~ ${end.format(timeFormatter)}"
                    } else {
                        "${start.format(dateFormatter)} ${start.format(timeFormatter)} ~ " +
                            "${end.format(dateFormatter)} ${end.format(timeFormatter)}"
                    }
                }
            }
            appendLine(periodText)
            appendLine(event.title)
            event.calendarName?.takeUnless(String::isBlank)?.let { appendLine("일정표: $it") }
            event.location?.takeUnless(String::isBlank)?.let { appendLine("장소: $it") }
            event.description?.takeUnless(String::isBlank)?.let { appendLine("설명: $it") }
            if (index != events.lastIndex) appendLine()
        }
    }.trimEnd()
}
