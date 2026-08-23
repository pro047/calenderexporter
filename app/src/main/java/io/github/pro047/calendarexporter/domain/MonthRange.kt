package io.github.pro047.calendarexporter.domain

import java.time.YearMonth
import java.time.ZoneId

data class MonthRange(
    val startMillis: Long,
    val endExclusiveMillis: Long,
) {
    init {
        require(startMillis < endExclusiveMillis) { "Month range must not be empty" }
    }

    val providerEndInclusiveMillis: Long
        get() = endExclusiveMillis - 1

    fun overlaps(beginMillis: Long, endMillis: Long): Boolean {
        val regularOverlap = beginMillis < endExclusiveMillis && endMillis > startMillis
        val zeroDurationInside = beginMillis == endMillis &&
            beginMillis >= startMillis &&
            beginMillis < endExclusiveMillis
        return regularOverlap || zeroDurationInside
    }

    companion object {
        fun of(yearMonth: YearMonth, zoneId: ZoneId): MonthRange {
            val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endExclusive = yearMonth.plusMonths(1)
                .atDay(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
            return MonthRange(start, endExclusive)
        }
    }
}
