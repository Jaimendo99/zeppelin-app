package com.zeppelin.app.service.wearCommunication

import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
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