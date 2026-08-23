package io.github.pro047.calendarexporter.model

sealed interface ExtractionUiState {
    data object Idle : ExtractionUiState
    data object Loading : ExtractionUiState
    data class Success(val events: List<NormalizedCalendarEvent>) : ExtractionUiState
    data class Error(val message: String) : ExtractionUiState
}
