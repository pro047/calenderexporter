package io.github.pro047.calendarexporter.data

import io.github.pro047.calendarexporter.domain.CalendarEventNormalizer
import io.github.pro047.calendarexporter.model.DeviceCalendar
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import java.time.YearMonth
import java.time.ZoneId

class CalendarRepository(
    private val dataSource: CalendarProviderDataSource,
) {
    fun getCalendars(): List<DeviceCalendar> = dataSource.queryCalendars()

    fun getMonthEvents(
        yearMonth: YearMonth,
        selectedCalendarIds: Set<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<NormalizedCalendarEvent> = dataSource
        .queryMonth(yearMonth, selectedCalendarIds, zoneId)
        .map(CalendarEventNormalizer::normalize)
}
