package io.github.pro047.calendarexporter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.YearMonth
import java.time.ZoneId

class MonthRangeTest {
    @Test
    fun `leap year February has 29 days in UTC`() {
        val range = MonthRange.of(YearMonth.of(2024, 2), ZoneId.of("UTC"))

        assertEquals(29, Duration.ofMillis(range.endExclusiveMillis - range.startMillis).toDays())
    }

    @Test
    fun `December range rolls into next year`() {
        val range = MonthRange.of(YearMonth.of(2026, 12), ZoneId.of("Asia/Seoul"))
        val expectedEnd = YearMonth.of(2027, 1).atDay(1)
            .atStartOfDay(ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedEnd, range.endExclusiveMillis)
    }

    @Test
    fun `DST month uses calendar boundaries instead of fixed day duration`() {
        val range = MonthRange.of(YearMonth.of(2026, 3), ZoneId.of("America/New_York"))

        assertEquals(31L * 24 - 1, Duration.ofMillis(
            range.endExclusiveMillis - range.startMillis,
        ).toHours())
    }

    @Test
    fun `event ending at start does not overlap half open range`() {
        val range = MonthRange(1_000, 2_000)

        assertFalse(range.overlaps(beginMillis = 500, endMillis = 1_000))
        assertTrue(range.overlaps(beginMillis = 500, endMillis = 1_001))
    }

    @Test
    fun `zero duration event inside range is included`() {
        val range = MonthRange(1_000, 2_000)

        assertTrue(range.overlaps(beginMillis = 1_500, endMillis = 1_500))
        assertFalse(range.overlaps(beginMillis = 2_000, endMillis = 2_000))
    }
}
