package io.github.pro047.calendarexporter.domain

import io.github.pro047.calendarexporter.model.CalendarEventInstance
import io.github.pro047.calendarexporter.model.EventPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CalendarEventNormalizerTest {
    @Test
    fun `all day event is normalized as UTC date range`() {
        val event = event(
            begin = Instant.parse("2026-08-23T00:00:00Z").toEpochMilli(),
            end = Instant.parse("2026-08-24T00:00:00Z").toEpochMilli(),
            allDay = true,
        )

        val period = CalendarEventNormalizer.normalize(event).period

        assertTrue(period is EventPeriod.AllDay)
        period as EventPeriod.AllDay
        assertEquals(LocalDate.of(2026, 8, 23), period.startDate)
        assertEquals(LocalDate.of(2026, 8, 24), period.endDateExclusive)
    }

    @Test
    fun `blank title receives fallback`() {
        val normalized = CalendarEventNormalizer.normalize(event(title = "  "))

        assertEquals("(제목 없음)", normalized.title)
    }

    private fun event(
        begin: Long = 1_000,
        end: Long = 2_000,
        allDay: Boolean = false,
        title: String? = "일정",
    ) = CalendarEventInstance(
        eventId = 7,
        calendarId = 3,
        calendarName = "개인",
        title = title,
        description = null,
        location = null,
        beginMillis = begin,
        endMillis = end,
        isAllDay = allDay,
        eventTimeZone = "UTC",
        status = 1,
    )
}
