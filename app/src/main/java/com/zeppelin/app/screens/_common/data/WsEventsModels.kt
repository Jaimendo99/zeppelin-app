package com.zeppelin.app.screens._common.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface WebSocketEvent


@Serializable
@SerialName("status_update")
data class StatusUpdateMessage(
    val connections: Int,
    val platforms: Platforms
) : WebSocketEvent

@Serializable
data class Platforms(
    val web: Int,
    val mobile: Int
)


@Serializable
@SerialName("pomodoro_start")
data class PomodoroStartMessage(
    val config: PomodoroConfig,
    val startedAt: Long,
    @SerialName("session_id") val sessionId: Int,
    val senderId: String
) : WebSocketEvent

@Serializable
data class PomodoroConfig(
    val workDuration: Int,
    val breakDuration: Int,
    val cycles: Int,
)

@Serializable
@SerialName("pomodoro_extend")
data class PomodoroExtendMessage(
    val seconds: Int,
    val senderId: String
) : WebSocketEvent

@Serializable
@SerialName("pomodoro_phase_end")
data class PomodoroPhaseEndMessage(
    val phase: CurrentPhase, // "work" or "break"
    val nextCycle: Int,
    val isBreakFinished: Boolean,
    val isLastCycle: Boolean,
    val continueAs: CurrentPhase // "work" or "break"
) : WebSocketEvent

@Serializable
@SerialName("pomodoro_session_end")
data class PomodoroSessionEndMessage(
    val senderId: String
) : WebSocketEvent

@Serializable
@SerialName("client_hello") // Example type name
data class ClientHelloMessage(
    val clientId: String
) : WebSocketEvent


@Serializable
@SerialName("unknown")
data class UnknownEvent(
    val rawText: String
) : WebSocketEvent

@Serializable
@SerialName("lock_task_removed")
data class LockTaskRemovedEvent(
    @SerialName("remove_at") val removedAt: Long,
) : WebSocketEvent


@Serializable
@SerialName("lock_task_on")
data class LockTaskOnEvent(
    @SerialName("on_at") val onAt: Long,
) : WebSocketEvent




@Serializable
@SerialName("WEARABLE_OFF")
data class WearableOffEvent(
    @SerialName("time") val removedAt: Long,
) : WebSocketEvent

@Serializable
@SerialName("WEARABLE_ON")
data class WearableOnEvent(
    @SerialName("time") val addedAt: Long,
) : WebSocketEvent


@Serializable
@SerialName("WEAK_RSSI")
data class WeakRssiEvent(
    @SerialName("rssi") val rssi: Int,
) : WebSocketEvent

@Serializable
@SerialName("STRONG_RSSI")
data class StrongRssiEvent(
    @SerialName("rssi") val rssi: Int,
) : WebSocketEvent


@Serializable
@SerialName("WEARABLE_DISCONNECTED")
data class WearableDisconnectedEvent(
    @SerialName("time") val disconnectedAt: Long,
) : WebSocketEvent

@Serializable
@SerialName("WEARABLE_RECONNECTED")
data class WearableReconnectedEvent(
    @SerialName("time") val reconnectedAt: Long,
) : WebSocketEvent


enum class LockTaskModeStatus() {
    LOCK_TASK_MODE_NONE, //    Constant Value: 0 (0x00000000)
    LOCK_TASK_MODE_LOCKED, //    Constant Value: 1 (0x00000001)
    LOCK_TASK_MODE_PINNED, //    Constant Value: 2 (0x00000002)
}

// --- Serializers Module for Polymorphism ---

val AppJsonModule = SerializersModule {
    polymorphic(WebSocketEvent::class) {
        subclass(StatusUpdateMessage::class)
        subclass(PomodoroStartMessage::class)
        subclass(PomodoroExtendMessage::class)
        subclass(PomodoroPhaseEndMessage::class)
        subclass(PomodoroSessionEndMessage::class)
        subclass(ClientHelloMessage::class)
        subclass(UnknownEvent::class)
        subclass(LockTaskRemovedEvent::class)
        subclass(WearableOffEvent::class)
        subclass(WearableOnEvent::class)
        subclass(WearableDisconnectedEvent::class)
        subclass(WearableReconnectedEvent::class)
        subclass(WeakRssiEvent::class)
        subclass(StrongRssiEvent::class)
        subclass(LockTaskOnEvent::class)
    }
}


data class PomodoroState(
    val isRunning: Boolean = false,
    val currentPhase: CurrentPhase = CurrentPhase.NONE,  //"work", "break", or null if inactive
    val remainingSeconds: Int = 0,
    val currentCycle: Int = 0,
    val totalCycles: Int = 0,
    val workDuration: Int = 0,
    val breakDuration: Int = 0,
    val timerDisplay: String = "00:00", // Formatted string for easy UI display
    val timerDigits: TimerDigits = TimerDigits(),

    ) {
    companion object {
        val Initial = PomodoroState() // Represents the state before starting
    }
}

data class TimerDigits(
    val minone: Char = '0',
    val mintwo: Char = '0',
    val secone: Char = '0',
    val sectwo: Char = '0',
)

enum class CurrentPhase {
    @SerialName("work") // Map "work" JSON to WORK enum value
    WORK,

    @SerialName("break") // Map "break" JSON to BREAK enum value
    BREAK,

    @SerialName("none") // Map "none" JSON to NONE enum value
    NONE
}

fun Int.formatTime(format: String = "%02d:%02d"): String {
    val minutes = this / 60
    val seconds = this % 60
    return format.format(minutes, seconds)
}

fun Long.formatTime(format: String = "%02d:%02d"): String {
    val minutes = this / 60
    val seconds = this % 60
    return format.format(minutes, seconds)
}

fun Int.toTimerDigits(): TimerDigits {
    val totalSeconds = this.coerceAtLeast(0) // Ensure non-negative
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    // Clamp minutes to a maximum of 99 for display purposes
    val clampedMinutes = minutes.coerceAtMost(99)

    val minOneDigit = (clampedMinutes / 10).toString().single()
    val minTwoDigit = (clampedMinutes % 10).toString().single()
    val secOneDigit = (seconds / 10).toString().single()
    val secTwoDigit = (seconds % 10).toString().single()

    return TimerDigits(minOneDigit, minTwoDigit, secOneDigit, secTwoDigit)

}


val AppJson = Json {
    ignoreUnknownKeys = true
    serializersModule = AppJsonModule
    classDiscriminator = "type"
}
