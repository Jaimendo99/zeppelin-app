package com.zeppelin.app.service.wearCommunication

import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.zeppelin.app.screens._common.data.CurrentPhase
import com.zeppelin.app.screens._common.data.PomodoroState
import kotlinx.coroutines.tasks.await

class WearCommunicator(
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient,
) {
    companion object {
        private const val TAG = "WearCommunicator"
        val COMMAND_START_MONITORING = WearOsPaths.CommandStartMonitoring.path
        val COMMAND_STOP_MONITORING = WearOsPaths.CommandStopMonitoring.path
    }

    suspend fun sendStartMonitoringCommand() {
        sendMessageToNearbyNodes(COMMAND_START_MONITORING, "start_monitoring".toByteArray())
    }

    suspend fun sendStopMonitoringCommand() {
        sendMessageToNearbyNodes(COMMAND_STOP_MONITORING, "stop_monitoring".toByteArray())
    }

    suspend fun sendLiveSessionConnected(){
        sendMessageToNearbyNodes(WearOsPaths.CommandLiveSessionConnected.path, "live_session_connected".toByteArray())
    }

    suspend fun sendPomodoroInfo(pomodoroState : PomodoroState) {

        Log.d(TAG, "Sending pomodoro info for current phase: $pomodoroState")
        when(pomodoroState.currentPhase){
            CurrentPhase.WORK -> {
                val timerPercentage = if (pomodoroState.workDuration > 0) {
                    (pomodoroState.workDuration - pomodoroState.remainingSeconds).div(pomodoroState.workDuration.toFloat())
                } else  {
                    0.0f
                }

                val payload = "$timerPercentage,${pomodoroState.timerDisplay}"
                Log.d(TAG, "${pomodoroState.workDuration} -> Sending pomodoro work phase with payload: $payload")
                sendMessageToNearbyNodes(WearOsPaths.CommandWorkPhase.path, payload.toByteArray())}

            CurrentPhase.BREAK -> {
                val timerPercentage = if (pomodoroState.breakDuration > 0) {
                    (pomodoroState.breakDuration - pomodoroState.remainingSeconds).div(pomodoroState.breakDuration.toFloat())
                } else 0f
                val payload = "$timerPercentage,${pomodoroState.timerDisplay}"
                Log.d(TAG, "Sending pomodoro break phase with payload: $payload")
                sendMessageToNearbyNodes(WearOsPaths.CommandBreakPhase.path, payload.toByteArray())}
            CurrentPhase.NONE -> {
                Log.w(TAG, "No current phase set, not sending pomodoro info")
                return
            }
        }
    }



    private suspend fun sendMessageToNearbyNodes(path: String, payload: ByteArray) {
        try {
            val nodes = nodeClient.connectedNodes.await().toList()

            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found to send message: $path")
                return
            }

            nodes.forEach { node ->
                try {
                    Log.d(TAG, "Message '$path' sent successfully to ${node.displayName}")
                    messageClient.sendMessage(node.id, path, payload).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send message '$path' to node: ${node.displayName}", e)
                }
            }
        } catch (e: Exception) {
            // Catch exception from getting the connected nodes list
            Log.e(TAG, "Error getting connected nodes for message: $path", e)
        }
    }
}