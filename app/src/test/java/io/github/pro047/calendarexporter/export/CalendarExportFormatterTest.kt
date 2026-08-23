package io.github.pro047.calendarexporter.export

import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarExportFormatterTest {
    private val zone = ZoneId.of("Asia/Seoul")

    @Test
    fun `CSV escapes quotes commas newlines and formula prefixes`() {
        val csv = CalendarExportFormatter.toCsv(
            listOf(event(title = "=SUM(1,2)\n\"quoted\"")),
            zone,
        )

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("'=SUM(1,2)"))
        assertTrue(csv.contains("\"\"quoted\"\""))
    }

    @Test
    fun `CSV blocks formula prefix after leading whitespace`() {
        val csv = CalendarExportFormatter.toCsv(listOf(event(title = "  @command")), zone)

        assertTrue(csv.contains("'  @command"))
    }

    @Test
    fun `all day CSV leaves time columns empty`() {
        val csv = CalendarExportFormatter.toCsv(
            listOf(
                event(
                    period = EventPeriod.AllDay(
                        LocalDate.of(2026, 8, 23),
                        LocalDate.of(2026, 8, 24),
                    ),
                ),
            ),
            zone,
        )

        assertTrue(csv.contains("\"2026-08-23\",\"\",\"2026-08-24\",\"\",\"true\""))
    }

    @Test
    fun `JSON escapes control characters and emits no trailing comma`() {
        val json = CalendarExportFormatter.toJson(listOf(event(title = "a\nb")), zone)

        assertTrue(json.contains("a\\nb"))
        assertFalse(json.contains(",\n  }"))
    }

    private fun event(
        title: String = "일정",
        period: EventPeriod = EventPeriod.Timed(
            Instant.parse("2026-08-23T01:00:00Z"),
            Instant.parse("2026-08-23T02:00:00Z"),
        ),
    ) = NormalizedCalendarEvent(
        occurrenceKey = "1:2",
        eventId = 1,
        calendarId = 2,
        calendarName = "개인",
        title = title,
        description = "설명",
        location = "서울",
        period = period,
        eventTimeZone = "Asia/Seoul",
    )
}
