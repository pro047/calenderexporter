package io.github.pro047.calendarexporter.model

import java.time.Instant
import java.time.LocalDate

sealed interface EventPeriod {
    data class Timed(
        val start: Instant,
        val end: Instant,
    ) : EventPeriod

    data class AllDay(
        val startDate: LocalDate,
        val endDateExclusive: LocalDate,
    ) : EventPeriod
}
