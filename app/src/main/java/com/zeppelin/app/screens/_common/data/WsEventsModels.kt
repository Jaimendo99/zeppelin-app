package com.zeppelin.app.screens._common.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.json.Json

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
    val phase: String, // "work" or "break"
    val nextCycle: Int,
    val isBreakFinished: Boolean,
    val isLastCycle: Boolean,
    val continueAs: String // "work" or "break"
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


val AppJson = Json {
    ignoreUnknownKeys = true
    serializersModule = AppJsonModule
    classDiscriminator = "type"
}
