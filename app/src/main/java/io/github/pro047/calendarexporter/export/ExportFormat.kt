package io.github.pro047.calendarexporter.export

enum class ExportFormat(
    val extension: String,
    val mimeType: String,
) {
    CSV("csv", "text/csv"),
    JSON("json", "application/json"),
    TEXT("txt", "text/plain"),
}
