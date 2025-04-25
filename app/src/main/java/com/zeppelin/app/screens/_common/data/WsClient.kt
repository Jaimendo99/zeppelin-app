package com.zeppelin.app.screens._common.data

import android.util.Log
import com.zeppelin.app.screens.auth.data.IAuthPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException

class WebSocketClient(
    private val authPreferences: IAuthPreferences
) {
    private val TAG = "WebSocketClient"
    private var session: DefaultClientWebSocketSession? = null

    private val host = "api.focused.uno"
    private val path = "/ws"
    private val platform = "mobile"

    private val _state = MutableStateFlow<WebSocketState>(WebSocketState.Idle)
    val state: StateFlow<WebSocketState> = _state.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>()
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private val _wsEvents = MutableSharedFlow<WebSocketEvent>()
    val wsEvents: SharedFlow<WebSocketEvent> = _wsEvents.asSharedFlow()

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 20_000
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v(TAG, message)
                }
            }
        }
    }


    suspend fun connect(courseId: Int, retry: Boolean = false) {
        Log.d(TAG, "Connecting to course $courseId")
        if (courseId < 1) {
            Log.d(TAG, "Invalid course ID")
            return
        }
        when (val currentState = _state.value) {
            is WebSocketState.Connected -> {
                if (currentState.lastCourseId == courseId) {
                    Log.d(TAG, "Already connected to course $courseId")
                    return
                }
                if (retry) {
                    Log.d(
                        TAG,
                        "Already connected to a different course, reconnecting to course $courseId"
                    )
                    disconnect()
                    connectWithRetry(courseId) { connectToSession(it) }
                } else {
                    Log.d(TAG, "Already connected to a different course, not reconnecting")
                }
            }

            WebSocketState.Connecting -> {
                Log.d(TAG, "Already connecting to a course")
                return
            }

            WebSocketState.Disconnected, WebSocketState.Idle, is WebSocketState.Error -> {
                Log.d(TAG, "Disconnected from previous course, connecting to course $courseId")
                connectWithRetry(courseId) { connectToSession(it) }
            }
        }
    }

    private suspend fun connectToSession(courseId: Int): Result<Unit> {
        _state.value = WebSocketState.Connecting
        authPreferences.getAuthTokenOnce().let { token ->
            if (token.isNullOrEmpty()) return@let
            try {
                session = client.webSocketSession {
                    url {
                        host = this@WebSocketClient.host
                        port = 443
                        protocol = URLProtocol.WSS
                        method = HttpMethod.Get
                        path(path)
                        parameter("token", token)
                        parameter("platform", platform)
                        parameter("courseId", courseId)
                    }
                }
                _state.value = WebSocketState.Connected(courseId)
            } catch (e: Exception) {
                _state.value = WebSocketState.Error("Error connecting to WebSocket", e)
                Log.e(TAG, "Error connecting to WebSocket", e)
                return Result.failure(e)
            }
            session?.let { listenIncomingMessages(it) }
        }
        return Result.success(Unit)
    }

    private suspend fun connectWithRetry(
        courseId: Int = -1,
        maxRetries: Int = 2,
        initialDelayMillis: Long = 1000L,
        maxDelayMillis: Long = 16000L,
        factor: Double = 2.0,
        connectFunction: suspend (Int) -> Result<Unit>
    ) {
        _state.value = WebSocketState.Connecting
        var currentDelay = initialDelayMillis
        for (attempt in 1..maxRetries) {
            connectFunction(courseId)
                .onSuccess { _ ->
                    _state.update { WebSocketState.Connected(courseId) }
                    Log.d(TAG, "Connection successful on attempt $attempt")
                    return
                }
                .onFailure { error ->
                    _state.value = WebSocketState.Error("Connection attempt $attempt failed", error)
                    Log.e(TAG, "Connection attempt $attempt failed: ${error.message}")

                    if (attempt == maxRetries) {
                        Log.e(TAG, "Max retries reached. Giving up.")
                        _state.value =
                            WebSocketState.Error("Max retries reached. Giving up.", error)
                        return@onFailure
                    }
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                }
        }

    }


    private suspend fun listenIncomingMessages(session: DefaultClientWebSocketSession) {
        try {
            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> receiveTextMessage(frame)
                    is Frame.Binary -> Log.d(TAG, "Received binary data")
                    is Frame.Close -> receiveFrameClose(frame)
                    else -> Log.d(TAG, "Received other frame: $frame")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while receiving messages", e)
            _state.value = WebSocketState.Error("Error while receiving", e)
        } finally {
            _state.value = WebSocketState.Disconnected
        }
    }

    suspend fun sendEvent(event: WebSocketEvent) {
        session?.let {
            try {
                it.sendSerialized(event)
                Log.d(TAG, "Sent event: $event")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending event", e)
                _state.value = WebSocketState.Error("Error sending event", e)
            }
        } ?: run {
            Log.e(TAG, "Cannot send event, not connected")
            _state.value = WebSocketState.Error("Cannot send event, not connected", null)
        }
    }

    suspend fun sendMessage(message: String) {
        session?.let {
            try {
                it.send(Frame.Text(message))
                Log.d(TAG, "Sent: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _state.value = WebSocketState.Error("Error sending message", e)
            }
        } ?: run {
            Log.e(TAG, "Cannot send message, not connected")
            _state.value = WebSocketState.Error("Cannot send message, not connected", null)
        }
    }

    suspend fun disconnect() {
        session?.let {
            try {
                it.close(CloseReason(CloseReason.Codes.NORMAL, "Client closed connection"))
                Log.d(TAG, "Disconnected")
                _state.value = WebSocketState.Disconnected
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
                _state.value = WebSocketState.Error("Error disconnecting", e)
            } finally {
                session = null
            }
        }
    }



    private suspend fun receiveTextMessage(frame: Frame.Text) {
        val text = frame.readText()
        Log.d(TAG, "Received text: $text")
        val event = try {
            AppJson.decodeFromString(
                PolymorphicSerializer(WebSocketEvent::class),
                text
            )
        } catch (e: SerializationException) {
            Log.w(TAG, "Failed to deserialize text into known WebSocketEvent: ${e.message}. Treating as UnknownEvent.")
            UnknownEvent(rawText = text)

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error processing text message: $text", e)
            UnknownEvent(rawText = "Error during processing: $text")
        }
        _incomingMessages.emit(text)
        Log.d(TAG, "Processed event: $event")
        _wsEvents.emit(event)
    }

    private fun receiveFrameClose(frame: Frame.Close) {
        val reason = frame.readReason()
        Log.d(TAG, "Connection closed: $reason")
        _state.value = WebSocketState.Disconnected
    }

    fun setConnectionState(state: WebSocketState) {
        _state.value = state
    }

}

sealed class WebSocketState {
    data object Idle : WebSocketState()
    data object Connecting : WebSocketState()
    data class Connected(val lastCourseId: Int) : WebSocketState()
    data class Error(val message: String, val cause: Throwable? = null) : WebSocketState()
    data object Disconnected : WebSocketState()
}