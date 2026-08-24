package io.github.pro047.calendarexporter.export

import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarExportFormatterTest {
    private val zone = ZoneId.of("Asia/Seoul")

    @Test
    fun `timed event is formatted as readable Korean text`() {
        val text = CalendarExportFormatter.formatMonth(
            YearMonth.of(2026, 8),
            listOf(event(title = "병원 예약")),
            zone,
        )

        assertTrue(text.startsWith("2026년 8월 일정"))
        assertTrue(text.contains("8월 23일(일) 오전 10:00 ~ 오전 11:00"))
        assertTrue(text.contains("병원 예약"))
        assertTrue(text.contains("일정표: 개인"))
        assertTrue(text.contains("장소: 서울"))
        assertTrue(text.contains("설명: 설명"))
    }

    @Test
    fun `single all day event does not expose exclusive end date`() {
        val text = CalendarExportFormatter.formatMonth(
            YearMonth.of(2026, 8),
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

        assertTrue(text.contains("8월 23일(일) 종일"))
        assertTrue(!text.contains("8월 24일"))
    }

    @Test
    fun `multi day all day event shows inclusive date range`() {
        val text = CalendarExportFormatter.formatMonth(
            YearMonth.of(2026, 8),
            listOf(
                event(
                    period = EventPeriod.AllDay(
                        LocalDate.of(2026, 8, 23),
                        LocalDate.of(2026, 8, 26),
                    ),
                ),
            ),
            zone,
        )

        assertTrue(text.contains("8월 23일(일) ~ 8월 25일(화) 종일"))
    }

    @Test
    fun `event crossing midnight includes both dates`() {
        val text = CalendarExportFormatter.formatMonth(
            YearMonth.of(2026, 8),
            listOf(
                event(
                    period = EventPeriod.Timed(
                        Instant.parse("2026-08-23T14:30:00Z"),
                        Instant.parse("2026-08-23T16:30:00Z"),
                    ),
                ),
            ),
            zone,
        )

        assertTrue(text.contains("8월 23일(일) 오후 11:30 ~ 8월 24일(월) 오전 1:30"))
    }

    @Test
    fun `empty month produces a complete plain text message`() {
        val text = CalendarExportFormatter.formatMonth(YearMonth.of(2026, 8), emptyList(), zone)

        assertEquals("2026년 8월 일정\n\n일정이 없습니다.", text)
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
