package io.github.pro047.calendarexporter.model

data class NormalizedCalendarEvent(
    val occurrenceKey: String,
    val eventId: Long,
    val calendarId: Long,
    val calendarName: String?,
    val title: String,
    val description: String?,
    val location: String?,
    val period: EventPeriod,
    val eventTimeZone: String?,
)
