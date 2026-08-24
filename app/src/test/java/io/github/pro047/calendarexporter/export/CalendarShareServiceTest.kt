package io.github.pro047.calendarexporter.export

import android.app.Application
import android.content.Intent
import android.net.Uri
import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarShareServiceTest {
    private val application: Application = RuntimeEnvironment.getApplication()
    private lateinit var service: CalendarShareService
    private var startedChooser: Intent? = null
    private val month = YearMonth.of(2026, 8)
    private val events = listOf(event())
    private val zone = ZoneId.of("Asia/Seoul")

    @Before
    fun setUp() {
        startedChooser = null
        service = CalendarShareService(
            context = application,
            fileUriProvider = { file ->
                Uri.parse("content://io.github.pro047.calendarexporter.fileprovider/${file.name}")
            },
            activityStarter = { intent -> startedChooser = intent },
        )
    }

    @Test
    fun `plain text export sends message body without file attachment`() {
        service.sharePlainText(month, events, zone)

        val intent = startedShareIntent()
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertTrue(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().contains("병원 예약"))
        assertEquals("2026년 8월 일정", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertNull(intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
    }

    @Test
    fun `text file export sends txt uri without message body`() {
        service.shareTextFile(month, events, zone)

        val intent = startedShareIntent()
        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertTrue(uri.toString().endsWith("calendar-2026-08.txt"))
        assertNull(intent.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertTrue(application.cacheDir.resolve("exports/calendar-2026-08.txt").isFile)
        assertFalse(application.cacheDir.resolve("exports/calendar-2026-08.csv").exists())
        assertFalse(application.cacheDir.resolve("exports/calendar-2026-08.json").exists())
    }

    private fun startedShareIntent(): Intent {
        val chooser = startedChooser ?: error("Chooser was not started")
        return chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
            ?: error("Share intent was not wrapped in a chooser")
    }

    private fun event() = NormalizedCalendarEvent(
        occurrenceKey = "1:2",
        eventId = 1,
        calendarId = 2,
        calendarName = "개인",
        title = "병원 예약",
        description = "정기 검진",
        location = "서울",
        period = EventPeriod.Timed(
            Instant.parse("2026-08-23T01:00:00Z"),
            Instant.parse("2026-08-23T02:00:00Z"),
        ),
        eventTimeZone = "Asia/Seoul",
    )
}
