package io.github.pro047.calendarexporter

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppLayoutTest {
    private val application: Application = RuntimeEnvironment.getApplication()
    private lateinit var root: android.view.View

    @Before
    fun setUp() {
        val context = ContextThemeWrapper(application, R.style.AppTheme)
        root = LayoutInflater.from(context).inflate(R.layout.activity_main, null)
    }

    @Test
    fun `layout leads with plain text sharing and keeps file sharing secondary`() {
        val exportContainer = root.findViewById<LinearLayout>(R.id.exportContainer)
        val plainTextButton = exportContainer.getChildAt(0) as Button
        val fileButton = exportContainer.getChildAt(1) as Button

        assertEquals(R.id.exportPlainTextButton, plainTextButton.id)
        assertEquals("카톡·문자로 보내기", plainTextButton.text.toString())
        assertEquals(R.id.exportTextFileButton, fileButton.id)
        assertEquals("파일로 보내기", fileButton.text.toString())
    }

    @Test
    fun `primary controls and preview meet readability targets`() {
        val density = application.resources.displayMetrics.density
        val plainTextButton = root.findViewById<Button>(R.id.exportPlainTextButton)
        val preview = root.findViewById<TextView>(R.id.resultText)

        assertTrue(plainTextButton.minHeight >= (56 * density).toInt())
        assertTrue(preview.textSize / density >= 16f)
        assertEquals("일정을 불러오면 여기에 내용이 표시됩니다.", preview.text.toString())
    }

    @Test
    fun `calendar choices use a vertical readable list`() {
        val calendarContainer = root.findViewById<LinearLayout>(R.id.calendarContainer)
        assertEquals(LinearLayout.VERTICAL, calendarContainer.orientation)
        assertEquals("한 달 일정 보내기", root.findViewById<TextView>(R.id.screenTitle).text.toString())
    }
}
