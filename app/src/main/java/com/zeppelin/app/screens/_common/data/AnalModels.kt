package com.zeppelin.app.screens._common.data

import com.zeppelin.app.service.distractionDetection.data.DistractionDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportData(
    val userId: String,
    val sessionId: Int?,
    val courseId : Int,
    val type: ReportType? = null,
    val device: String,
    val addedAt:Long,
    val body: ReportBody? = null
)

@Serializable
enum class ReportType() {
    APP_USAGE,
    USER_HEARTRATE,
    USER_PHYSICAL_ACTIVITY,

    PINED_SCREEN, UNPIN_SCREEN,

    WEARABLE_OFF, WEARABLE_ON,

    WEAK_RSSI, STRONG_RSSI,

    WEARABLE_DISCONNECTED, WEARABLE_CONNECTED
}

@Serializable
sealed interface ReportBody

@Serializable
@SerialName("UNPIN_SCREEN")
data class UnPinScreen(
    @SerialName("removed_at") val removedAt: Long
): ReportBody

@Serializable
@SerialName("USER_HEARTRATE")
data class UserHeartRate(
    @SerialName("heartrate_change") val heartRateChange: HeartRateRecord
) : ReportBody {
    @Serializable
    data class HeartRateRecord(
        val value: Int, // Heart rate value
        val mean: Float, // Mean heart rate value
        val count: Int, // Count of heart rate samples
        val time: Long // Timestamp in milliseconds
    )
}

@Serializable
@SerialName("USER_PHYSICAL_ACTIVITY")
data class UserPhysicalActivity(
    @SerialName("detected_at") val detectedAt: Long, // Timestamp in milliseconds
    val speed: Float // Speed in m/s
) : ReportBody


@Serializable
@SerialName("WEARABLE_OFF")
data class WearableOff(
    @SerialName("time") val removedAt: Long // Timestamp in milliseconds
) : ReportBody

@Serializable
@SerialName("WEARABLE_ON")
data class WearableOn(
    @SerialName("time") val addedAt: Long // Timestamp in milliseconds
) : ReportBody



@Serializable
@SerialName("WEAK_RSSI")
data class WeakRssi(
    @SerialName("rssi") val rssi: Int // RSSI value in dBm
) : ReportBody

@Serializable
@SerialName("STRONG_RSSI")
data class StrongRssi(
    @SerialName("rssi") val rssi: Int // RSSI value in dBm
) : ReportBody


@Serializable
@SerialName("WEARABLE_DISCONNECTED")
data class WearableDisconnected(
    @SerialName("time") val disconnectedAt: Long // Timestamp in milliseconds
) : ReportBody

@Serializable
@SerialName("WEARABLE_RECONNECTED")
data class WearableReconnected(
    @SerialName("time") val reconnectedAt: Long // Timestamp in milliseconds
) : ReportBody

@Serializable
@SerialName("APP_USAGE")
data class AppUsageReport(
    @SerialName("records") val records: List<AppUsageRecord>
) : ReportBody {
    @Serializable
    data class AppUsageRecord(
        @SerialName("package_name") val packageName: String,
        @SerialName("total_time_in_foreground") val totalTimeInForeground: Long, // in milliseconds
        @SerialName("usage_interval_start") val usageIntervalStart: Long, // in milliseconds
        @SerialName("usage_interval_end") val usageIntervalEnd: Long, // in milliseconds
        @SerialName("last_time_app_used") val lastTimeAppUsed: Long // in milliseconds
    )
}
fun DistractionDetail.toAppUsageRecord(): AppUsageReport.AppUsageRecord{
    return AppUsageReport.AppUsageRecord(
        packageName = packageName,
        totalTimeInForeground = totalTimeInForeground,
        usageIntervalStart = usageIntervalStart,
        usageIntervalEnd = usageIntervalEnd,
        lastTimeAppUsed = lastTimeAppUsed
    )
}