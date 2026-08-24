package io.github.pro047.calendarexporter

import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.github.pro047.calendarexporter.model.ExtractionUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityLoadingTest {
    @Test
    fun `month navigation stays available while loading`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val renderState = MainActivity::class.java.getDeclaredMethod(
            "renderState",
            ExtractionUiState::class.java,
        ).apply { isAccessible = true }

        renderState.invoke(activity, ExtractionUiState.Loading)

        val previous = activity.findViewById<Button>(R.id.previousMonthButton)
        val next = activity.findViewById<Button>(R.id.nextMonthButton)
        val action = activity.findViewById<Button>(R.id.actionButton)
        val progress = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertTrue(previous.isEnabled)
        assertTrue(next.isEnabled)
        assertFalse(action.isEnabled)
        assertTrue(progress.isShown)

        val monthBeforeClick = activity.findViewById<TextView>(R.id.monthText).text.toString()
        assertTrue(next.performClick())
        val monthAfterClick = activity.findViewById<TextView>(R.id.monthText).text.toString()

        assertNotEquals(monthBeforeClick, monthAfterClick)
        assertTrue(action.isEnabled)
        assertFalse(progress.isShown)
    }
}
