package io.github.pro047.calendarexporter.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.CalendarContract
import io.github.pro047.calendarexporter.model.CalendarEventInstance
import io.github.pro047.calendarexporter.model.DeviceCalendar
import io.github.pro047.calendarexporter.domain.MonthRange
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarProviderDataSource(
    private val contentResolver: ContentResolver,
) {
    fun queryCalendars(): List<DeviceCalendar> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.VISIBLE,
        )

        return buildList {
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                )
                val accountNameIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Calendars.ACCOUNT_NAME,
                )
                val accountTypeIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Calendars.ACCOUNT_TYPE,
                )
                val colorIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Calendars.CALENDAR_COLOR,
                )
                val visibleIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Calendars.VISIBLE,
                )

                while (cursor.moveToNext()) {
                    add(
                        DeviceCalendar(
                            id = cursor.getLong(idIndex),
                            displayName = cursor.getString(nameIndex).orEmpty(),
                            accountName = cursor.getString(accountNameIndex),
                            accountType = cursor.getString(accountTypeIndex),
                            color = if (cursor.isNull(colorIndex)) null else cursor.getInt(colorIndex),
                            isVisible = cursor.getInt(visibleIndex) == 1,
                        ),
                    )
                }
            }
        }
    }

    fun queryDay(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<CalendarEventInstance> {
        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endExclusiveMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return queryRange(
            startMillis = startMillis,
            endExclusiveMillis = endExclusiveMillis,
            selectedCalendarIds = null,
        )
    }

    fun queryMonth(
        yearMonth: YearMonth,
        selectedCalendarIds: Set<Long>? = null,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<CalendarEventInstance> {
        if (selectedCalendarIds != null && selectedCalendarIds.isEmpty()) return emptyList()

        val range = MonthRange.of(yearMonth, zoneId)
        return queryRange(
            startMillis = range.startMillis,
            endExclusiveMillis = range.endExclusiveMillis,
            selectedCalendarIds = selectedCalendarIds,
        )
    }

    private fun queryRange(
        startMillis: Long,
        endExclusiveMillis: Long,
        selectedCalendarIds: Set<Long>?,
    ): List<CalendarEventInstance> {
        require(startMillis < endExclusiveMillis) { "Query range must not be empty" }

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_TIMEZONE,
            CalendarContract.Instances.STATUS,
        )

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also { builder ->
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endExclusiveMillis - 1)
        }.build()

        val selection = selectedCalendarIds?.joinToString(
            prefix = "${CalendarContract.Instances.CALENDAR_ID} IN (",
            postfix = ")",
            separator = ",",
        ) { "?" }
        val selectionArgs = selectedCalendarIds?.map(Long::toString)?.toTypedArray()

        return buildList {
            contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Instances.BEGIN} ASC, ${CalendarContract.Instances.END} DESC",
            )?.use { cursor ->
                val eventIdIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val calendarIdIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Instances.CALENDAR_ID,
                )
                val calendarNameIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
                )
                val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val descriptionIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Instances.DESCRIPTION,
                )
                val locationIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Instances.EVENT_LOCATION,
                )
                val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                val timeZoneIndex = cursor.getColumnIndexOrThrow(
                    CalendarContract.Instances.EVENT_TIMEZONE,
                )
                val statusIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.STATUS)

                while (cursor.moveToNext()) {
                    val begin = cursor.getLong(beginIndex)
                    val end = cursor.getLong(endIndex)
                    val overlapsDay = begin < endExclusiveMillis && end > startMillis
                    val zeroDurationInsideDay = begin == end &&
                        begin >= startMillis &&
                        begin < endExclusiveMillis

                    if (!overlapsDay && !zeroDurationInsideDay) continue
                    val status = cursor.getInt(statusIndex)
                    if (status == CalendarContract.Events.STATUS_CANCELED) continue

                    add(
                        CalendarEventInstance(
                            eventId = cursor.getLong(eventIdIndex),
                            calendarId = cursor.getLong(calendarIdIndex),
                            calendarName = cursor.getString(calendarNameIndex),
                            title = cursor.getString(titleIndex),
                            description = cursor.getString(descriptionIndex),
                            location = cursor.getString(locationIndex),
                            beginMillis = begin,
                            endMillis = end,
                            isAllDay = cursor.getInt(allDayIndex) == 1,
                            eventTimeZone = cursor.getString(timeZoneIndex),
                            status = status,
                        ),
                    )
                }
            }
        }
    }
}
