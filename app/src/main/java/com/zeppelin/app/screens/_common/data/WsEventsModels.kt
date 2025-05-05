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
    val senderId: String
) : WebSocketEvent

@Serializable
data class PomodoroConfig(
    val workDuration: Int,
    val breakDuration: Int,
    val cycles: Int,
    val startedAt: Long
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
