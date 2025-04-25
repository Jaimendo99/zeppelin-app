package com.zeppelin.app.screens._common.data

import android.util.Log

class SessionEventsManager(
    private val webSocketClient: WebSocketClient
) {
    private val TAG = "WsEventsManager"

    suspend fun observeEvents(){
        webSocketClient.wsEvents.collect { message ->
            when (message) {
                is StatusUpdateMessage -> handleStatusUpdate(message)
                is PomodoroStartMessage -> handlePomodoroStart(message)
                is PomodoroExtendMessage -> handlePomodoroExtend(message)
                is PomodoroPhaseEndMessage -> handlePomodoroPhaseEnd(message)
                is PomodoroSessionEndMessage -> handlePomodoroSessionEnd(message)
                is ClientHelloMessage -> handleClientHello(message)
                is UnknownEvent -> handleUnknownEvent(message)
            }
        }
    }
    private fun handleStatusUpdate(statusUpdate: StatusUpdateMessage) {
        Log.d(TAG, "Status update: $statusUpdate")
        TODO("Not yet implemented $statusUpdate")
    }

    private fun handlePomodoroStart(startMessage: PomodoroStartMessage) {
        Log.d(TAG, "Pomodoro started: $startMessage")
        TODO("Not yet implemented $startMessage")
    }

    private fun handlePomodoroExtend(extendMessage: PomodoroExtendMessage) {
        Log.d(TAG, "Pomodoro extended: $extendMessage")
        TODO("Not yet implemented $extendMessage")
    }

    private fun handlePomodoroPhaseEnd(phaseEndMessage: PomodoroPhaseEndMessage) {
        Log.d(TAG, "Pomodoro phase ended: $phaseEndMessage")
        TODO("Not yet implemented $phaseEndMessage")
    }

    private fun handlePomodoroSessionEnd(sessionEndMessage: PomodoroSessionEndMessage) {
        Log.d(TAG, "Pomodoro session ended: $sessionEndMessage")
        TODO("Not yet implemented $sessionEndMessage")
    }

    private fun handleClientHello(clientHello: ClientHelloMessage) {
        Log.d(TAG, "Client hello: $clientHello")
        TODO("Not yet implemented $clientHello")
    }

    private fun handleUnknownEvent(unknownEvent: UnknownEvent) {
        Log.d(TAG, "Unknown event: $unknownEvent")
        TODO("Not yet implemented $unknownEvent")
    }


}
