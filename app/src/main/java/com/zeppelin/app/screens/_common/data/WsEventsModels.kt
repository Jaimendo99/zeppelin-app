package com.zeppelin.app.screens._common.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.json.Json

@Serializable
sealed interface WebSocketEvent {
    val type: String
}


@Serializable
@SerialName("status_update")
data class StatusUpdateMessage(
    override val type: String = "status_update",
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
    override val type: String = "pomodoro_start",
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
    override val type: String = "pomodoro_extend",
    val seconds: Int,
    val senderId: String
) : WebSocketEvent

@Serializable
@SerialName("pomodoro_phase_end")
data class PomodoroPhaseEndMessage(
    override val type: String = "pomodoro_phase_end",
    val phase: String, // "work" or "break"
    val nextCycle: Int,
    val isBreakFinished: Boolean,
    val isLastCycle: Boolean,
    val continueAs: String // "work" or "break"
) : WebSocketEvent

@Serializable
@SerialName("pomodoro_session_end")
data class PomodoroSessionEndMessage(
    override val type: String = "pomodoro_session_end",
    val senderId: String
) : WebSocketEvent

@Serializable
@SerialName("client_hello") // Example type name
data class ClientHelloMessage(
    override val type: String = "client_hello",
    val clientId: String
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
    }
}


val AppJson = Json {
    ignoreUnknownKeys = true // Good practice for API evolution
    serializersModule = AppJsonModule
}
