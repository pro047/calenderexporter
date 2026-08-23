package io.github.pro047.calendarexporter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.github.pro047.calendarexporter.data.CalendarProviderDataSource
import io.github.pro047.calendarexporter.data.CalendarRepository
import io.github.pro047.calendarexporter.export.CalendarShareService
import io.github.pro047.calendarexporter.export.ExportFormat
import io.github.pro047.calendarexporter.model.DeviceCalendar
import io.github.pro047.calendarexporter.model.EventPeriod
import io.github.pro047.calendarexporter.model.ExtractionUiState
import io.github.pro047.calendarexporter.model.NormalizedCalendarEvent
import androidx.core.net.toUri
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val calendarChecks = linkedMapOf<Long, CheckBox>()
    private lateinit var repository: CalendarRepository
    private lateinit var actionButton: Button
    private lateinit var previousMonthButton: Button
    private lateinit var nextMonthButton: Button
    private lateinit var monthText: TextView
    private lateinit var calendarContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var summaryText: TextView
    private lateinit var resultText: TextView
    private lateinit var exportContainer: LinearLayout
    private lateinit var shareService: CalendarShareService
    private var selectedMonth: YearMonth = YearMonth.now()
    private var calendarsLoaded = false
    private var loadedEvents: List<NormalizedCalendarEvent> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = CalendarRepository(CalendarProviderDataSource(contentResolver))
        shareService = CalendarShareService(this)
        bindViews()
        updateMonthText()
        previousMonthButton.setOnClickListener { changeMonth(-1) }
        nextMonthButton.setOnClickListener { changeMonth(1) }
        actionButton.setOnClickListener { ensurePermissionAndLoad() }
        findViewById<Button>(R.id.exportCsvButton).setOnClickListener {
            shareEvents(ExportFormat.CSV)
        }
        findViewById<Button>(R.id.exportJsonButton).setOnClickListener {
            shareEvents(ExportFormat.JSON)
        }
        findViewById<Button>(R.id.exportTextButton).setOnClickListener {
            shareEvents(ExportFormat.TEXT)
        }
        renderPermissionState()
    }

    override fun onResume() {
        super.onResume()
        renderPermissionState()
        if (hasCalendarPermission()) {
            if (!calendarsLoaded) loadCalendars()
        } else {
            calendarsLoaded = false
            calendarChecks.clear()
            calendarContainer.removeAllViews()
            renderState(ExtractionUiState.Idle)
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    @Deprecated("Used for the dependency-free permission flow")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != READ_CALENDAR_REQUEST) return
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            renderPermissionState()
            loadCalendars(loadEventsAfterward = true)
        } else {
            renderState(ExtractionUiState.Error(getString(R.string.permission_denied)))
            showPermissionDeniedDialog()
        }
    }

    private fun bindViews() {
        actionButton = findViewById(R.id.actionButton)
        previousMonthButton = findViewById(R.id.previousMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        monthText = findViewById(R.id.monthText)
        calendarContainer = findViewById(R.id.calendarContainer)
        progressBar = findViewById(R.id.progressBar)
        summaryText = findViewById(R.id.summaryText)
        resultText = findViewById(R.id.resultText)
        exportContainer = findViewById(R.id.exportContainer)
    }

    private fun changeMonth(delta: Long) {
        selectedMonth = selectedMonth.plusMonths(delta)
        updateMonthText()
        summaryText.text = ""
        resultText.text = ""
        loadedEvents = emptyList()
        exportContainer.visibility = View.GONE
    }

    private fun updateMonthText() {
        monthText.text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월"))
    }

    private fun ensurePermissionAndLoad() {
        if (hasCalendarPermission()) {
            if (calendarsLoaded) loadSelectedMonth() else loadCalendars(loadEventsAfterward = true)
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
            AlertDialog.Builder(this)
                .setMessage(R.string.permission_reason)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.continue_label) { _, _ -> requestCalendarPermission() }
                .show()
        } else {
            requestCalendarPermission()
        }
    }

    private fun requestCalendarPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), READ_CALENDAR_REQUEST)
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
            .setMessage(R.string.permission_denied_help)
            .setNegativeButton(R.string.close, null)

        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
            builder.setPositiveButton(R.string.open_phone_settings) { _, _ ->
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:$packageName".toUri(),
                    ),
                )
            }
        }
        builder.show()
    }

    private fun hasCalendarPermission(): Boolean =
        checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    private fun renderPermissionState() {
        actionButton.text = if (hasCalendarPermission()) {
            getString(R.string.load_calendar)
        } else {
            getString(R.string.grant_permission)
        }
    }

    private fun loadCalendars(loadEventsAfterward: Boolean = false) {
        renderState(ExtractionUiState.Loading)
        executor.execute {
            val result = runCatching(repository::getCalendars)
            runOnUiThread {
                result.onSuccess { calendars ->
                    calendarsLoaded = true
                    renderCalendars(calendars)
                    renderState(ExtractionUiState.Idle)
                    if (loadEventsAfterward && calendars.isNotEmpty()) loadSelectedMonth()
                }.onFailure { error ->
                    renderState(ExtractionUiState.Error(readableError(error)))
                }
            }
        }
    }

    private fun renderCalendars(calendars: List<DeviceCalendar>) {
        calendarContainer.removeAllViews()
        calendarChecks.clear()
        if (calendars.isEmpty()) {
            calendarContainer.addView(TextView(this).apply { text = getString(R.string.no_calendars) })
            return
        }
        calendars.forEach { calendar ->
            val checkBox = CheckBox(this).apply {
                text = calendar.displayName.ifBlank { getString(R.string.unnamed_calendar) }
                isChecked = calendar.isVisible
                contentDescription = getString(
                    R.string.calendar_choice_description,
                    calendar.displayName,
                )
            }
            calendarChecks[calendar.id] = checkBox
            calendarContainer.addView(checkBox)
        }
    }

    private fun loadSelectedMonth() {
        val selectedIds = calendarChecks.filterValues(CheckBox::isChecked).keys.toSet()
        if (selectedIds.isEmpty()) {
            renderState(ExtractionUiState.Error(getString(R.string.select_calendar_error)))
            return
        }
        renderState(ExtractionUiState.Loading)
        executor.execute {
            val result = runCatching { repository.getMonthEvents(selectedMonth, selectedIds) }
            runOnUiThread {
                result.onSuccess { renderState(ExtractionUiState.Success(it)) }
                    .onFailure { renderState(ExtractionUiState.Error(readableError(it))) }
            }
        }
    }

    private fun renderState(state: ExtractionUiState) {
        val loading = state is ExtractionUiState.Loading
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        actionButton.isEnabled = !loading
        previousMonthButton.isEnabled = !loading
        nextMonthButton.isEnabled = !loading
        if (loading) {
            loadedEvents = emptyList()
            exportContainer.visibility = View.GONE
        }
        when (state) {
            ExtractionUiState.Idle -> {
                summaryText.text = ""
                resultText.text = ""
                exportContainer.visibility = View.GONE
            }
            ExtractionUiState.Loading -> summaryText.text = getString(R.string.provider_loading)
            is ExtractionUiState.Error -> {
                loadedEvents = emptyList()
                summaryText.text = getString(R.string.load_error_title)
                resultText.text = state.message
                exportContainer.visibility = View.GONE
            }
            is ExtractionUiState.Success -> {
                loadedEvents = state.events
                summaryText.text = getString(
                    R.string.event_count,
                    selectedMonth.year,
                    selectedMonth.monthValue,
                    state.events.size,
                )
                resultText.text = formatPreview(state.events)
                exportContainer.visibility = if (state.events.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun shareEvents(format: ExportFormat) {
        if (loadedEvents.isEmpty()) return
        runCatching { shareService.share(selectedMonth, loadedEvents, format) }
            .onFailure {
                renderState(
                    ExtractionUiState.Error(
                        getString(R.string.share_error, readableError(it)),
                    ),
                )
            }
    }

    private fun formatPreview(events: List<NormalizedCalendarEvent>): String {
        if (events.isEmpty()) return getString(R.string.no_events)
        val zone = ZoneId.systemDefault()
        val dateTimeFormat = DateTimeFormatter.ofPattern("M월 d일 HH:mm")
        return buildString {
            events.forEach { event ->
                val periodText = when (val period = event.period) {
                    is EventPeriod.AllDay -> if (
                        period.endDateExclusive == period.startDate.plusDays(1)
                    ) {
                        "${period.startDate.monthValue}월 ${period.startDate.dayOfMonth}일 종일"
                    } else {
                        "${period.startDate} ~ ${period.endDateExclusive} (종일)"
                    }
                    is EventPeriod.Timed -> {
                        val start = period.start.atZone(zone)
                        val end = period.end.atZone(zone)
                        "${dateTimeFormat.format(start)} ~ ${dateTimeFormat.format(end)}"
                    }
                }
                appendLine("[$periodText] ${event.title}")
                appendLine("  ${event.calendarName.orEmpty()}${event.location?.let { " · $it" }.orEmpty()}")
            }
        }.trimEnd()
    }

    private fun readableError(error: Throwable): String = when (error) {
        is SecurityException -> getString(R.string.permission_missing)
        else -> "${error.javaClass.simpleName}: ${error.message.orEmpty()}"
    }

    private companion object {
        const val READ_CALENDAR_REQUEST = 1001
    }
}
