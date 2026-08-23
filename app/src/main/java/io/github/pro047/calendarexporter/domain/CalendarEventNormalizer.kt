package io.github.pro047.calendarexporter.domain

import io.github.pro047.calendarexporter.model.CalendarEventInstance
import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.time.Instant
import java.time.ZoneOffset

object CalendarEventNormalizer {
    fun normalize(event: CalendarEventInstance): NormalizedCalendarEvent {
        val start = Instant.ofEpochMilli(event.beginMillis)
        val end = Instant.ofEpochMilli(event.endMillis)
        val period = if (event.isAllDay) {
            EventPeriod.AllDay(
                startDate = start.atZone(ZoneOffset.UTC).toLocalDate(),
                endDateExclusive = end.atZone(ZoneOffset.UTC).toLocalDate(),
            )
        } else {
            EventPeriod.Timed(start = start, end = end)
        }

        return NormalizedCalendarEvent(
            occurrenceKey = event.occurrenceKey,
            eventId = event.eventId,
            calendarId = event.calendarId,
            calendarName = event.calendarName,
            title = event.title?.takeUnless(String::isBlank) ?: "(제목 없음)",
            description = event.description,
            location = event.location,
            period = period,
            eventTimeZone = event.eventTimeZone,
        )
    }
}
