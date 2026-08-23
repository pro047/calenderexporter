package io.github.pro047.calendarexporter.model

data class DeviceCalendar(
    val id: Long,
    val displayName: String,
    val accountName: String?,
    val accountType: String?,
    val color: Int?,
    val isVisible: Boolean,
)
