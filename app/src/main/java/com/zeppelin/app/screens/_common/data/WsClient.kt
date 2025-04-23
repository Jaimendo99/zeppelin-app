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
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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

    private val _lastCourseId = MutableStateFlow(-1)
    val lastCourseId: StateFlow<Int> = _lastCourseId.asStateFlow()

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 20_000
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                ignoreUnknownKeys = true
            })
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

    suspend fun connect(courseId: Int) {
        if (courseId == -1) {
            Log.e(TAG, "Invalid course ID")
            _state.value = WebSocketState.Error("Invalid course ID")
            return
        }else if (courseId == _lastCourseId.value) {
            _state.value = WebSocketState.Connected("Already connected to course $courseId")
            Log.d(TAG, "Already connected to course $courseId")
            return
        } else if (courseId != _lastCourseId.value && _lastCourseId.value != -1) {
            _state.value = WebSocketState.Disconnected
            Log.d(TAG, "Connecting to course $courseId")
            return
        }
        connectWithRetry(courseId){ connectToSession(it) }
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
                _lastCourseId.value = courseId
                _state.value = WebSocketState.Connected("Connected")
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
        maxDelayMillis: Long = 16000L, // Cap the delay
        factor: Double = 2.0,
        connectFunction: suspend (Int) -> Result<Unit>
    ) {
        _state.value = WebSocketState.Connecting
        var currentDelay = initialDelayMillis
        for (attempt in 1..maxRetries) {
            connectFunction(courseId)
                .onSuccess { _ ->
                    _state.value = WebSocketState.Connected("Connected to course $courseId")
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


    private fun listenIncomingMessages(session: DefaultClientWebSocketSession) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (frame in session.incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            Log.d(TAG, "Received: $text")
                            _incomingMessages.emit(text)
                        }

                        is Frame.Binary -> {
                            Log.d(TAG, "Received binary data")
                        }

                        is Frame.Close -> {
                            val reason = frame.readReason()
                            Log.d(TAG, "Connection closed: $reason")
                            _state.value = WebSocketState.Disconnected
                            break
                        }

                        else -> {
                            Log.d(TAG, "Received other frame: $frame")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while receiving messages", e)
                _state.value = WebSocketState.Error("Error while receiving", e)
            } finally {
                _state.value = WebSocketState.Disconnected
            }
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
                _lastCourseId.value = -1
                _state.value = WebSocketState.Disconnected
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
                _state.value = WebSocketState.Error("Error disconnecting", e)
            } finally {
                session = null
            }
        }
    }

    fun setConnectionState(state: WebSocketState) {
        _state.value = state
    }

}

sealed class WebSocketState {
    data object Idle : WebSocketState()
    data object Connecting : WebSocketState()
    data class Connected(val initialStatus: String) : WebSocketState() // Pass initial status
    data class Error(val message: String, val cause: Throwable? = null) : WebSocketState()
    data object Disconnected : WebSocketState()
}