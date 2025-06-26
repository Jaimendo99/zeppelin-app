package com.zeppelin.app.screens.courseSession.data

data class PomodoroSessionUI(
    val minutes: Long,
    val seconds: Long,
)

data class WatchMetricData(
    val timestamp: Long,
    val heartRate: Int?,
    val movementIntensity: Float?,
    val rssi: Int?
)

data class WatchMetricLists(
    val heartRate: List<MetricListItem> = emptyList(),
    val movementIntensity: List<MetricListItem> = emptyList(),
    val rssi: List<MetricListItem> = emptyList(),
)

data class MetricListItem(
    val value: Any,
    val timestamp: Long
)