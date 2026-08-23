package io.github.pro047.calendarexporter.model

data class CalendarEventInstance(
    val eventId: Long,
    val calendarId: Long,
    val calendarName: String?,
    val title: String?,
    val description: String?,
    val location: String?,
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    val eventTimeZone: String?,
    val status: Int,
) {
    val occurrenceKey: String = "$eventId:$beginMillis"
}
