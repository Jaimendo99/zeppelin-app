package com.zeppelin.app.service.distractionDetection.data


data class DistractionDetail(
    val packageName: String,
    val totalTimeInForeground: Long,
    val usageIntervalStart: Long,
    val usageIntervalEnd: Long,
    val lastTimeAppUsed: Long
)