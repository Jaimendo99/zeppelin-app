package com.zeppelin.app.screens._common.data

import android.util.Log

class SessionEventsManager(
    private val webSocketClient: WebSocketClient
) {
    private val TAG = "WsEventsManager"

    suspend fun observeEvents(disconnectFunction: () -> Unit){
        webSocketClient.wsEvents.collect { message ->
            when (message) {
                is StatusUpdateMessage -> handleStatusUpdate(message, disconnectFunction)
                is PomodoroStartMessage -> handlePomodoroStart(message)
                is PomodoroExtendMessage -> handlePomodoroExtend(message)
                is PomodoroPhaseEndMessage -> handlePomodoroPhaseEnd(message)
                is PomodoroSessionEndMessage -> handlePomodoroSessionEnd(message)
                is ClientHelloMessage -> handleClientHello(message)
                is UnknownEvent -> handleUnknownEvent(message)
            }
        }
    }
    private suspend fun handleStatusUpdate(statusUpdate: StatusUpdateMessage, disconnectFunction: () -> Unit) {
        Log.d(TAG, "Status update: $statusUpdate")
        if (statusUpdate.platforms.web == 0) {
            disconnectFunction()
        } else {
            Log.d(TAG, "WebSocket is connected")
        }
    }

    private fun handlePomodoroStart(startMessage: PomodoroStartMessage) {
        Log.d(TAG, "Pomodoro started: $startMessage")
        // TODO: Handle pomodoro start event
    }

    private fun handlePomodoroExtend(extendMessage: PomodoroExtendMessage) {
        Log.d(TAG, "Pomodoro extended: $extendMessage")
        // TODO: Handle pomodoro extend event
    }

    private fun handlePomodoroPhaseEnd(phaseEndMessage: PomodoroPhaseEndMessage) {
        Log.d(TAG, "Pomodoro phase ended: $phaseEndMessage")
        // TODO: Handle pomodoro phase end event
    }

    private fun handlePomodoroSessionEnd(sessionEndMessage: PomodoroSessionEndMessage) {
        Log.d(TAG, "Pomodoro session ended: $sessionEndMessage")
        // TODO: Handle pomodoro session end event
    }

    private fun handleClientHello(clientHello: ClientHelloMessage) {
        Log.d(TAG, "Client hello: $clientHello")
        // TODO: Handle client hello event
    }

    private fun handleUnknownEvent(unknownEvent: UnknownEvent) {
        Log.d(TAG, "Unknown event: $unknownEvent")
        // TODO: Handle unknown event
    }


}
