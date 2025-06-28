package com.zeppelin.app.service.wearCommunication

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.ReportData
import com.zeppelin.app.screens._common.data.ReportType
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.UserHeartRate
import com.zeppelin.app.screens._common.data.UserPhysicalActivity
import com.zeppelin.app.screens._common.data.WearableDisconnected
import com.zeppelin.app.screens._common.data.WearableDisconnectedEvent
import com.zeppelin.app.screens._common.data.WearableOff
import com.zeppelin.app.screens._common.data.WearableOffEvent
import com.zeppelin.app.screens._common.data.WearableOn
import com.zeppelin.app.screens._common.data.WearableOnEvent
import com.zeppelin.app.screens._common.data.WearableReconnected
import com.zeppelin.app.screens._common.data.WearableReconnectedEvent
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import com.zeppelin.app.service.ILiveSessionPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WearableMessageListenerService : WearableListenerService() {

    private val eventsManager: SessionEventsManager by inject()
    private val watchLinkRepository: WatchLinkRepository by inject()
    private val watchMetricsRepository: IWatchMetricsRepository by inject()

    companion object {
        private const val TAG = "WearMessageListener"

        private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        // Ensure these paths match what the watch sends
        val PATH_EVENT_OFF_WRIST = WearOsPaths.EventOffWrist.path
        val PATH_EVENT_ON_WRIST = WearOsPaths.EventOnWrist.path
        val PATH_EVENT_MOVEMENT_DETECTED = WearOsPaths.EventMovementDetected.path
        val PATH_DATA_HEART_RATE_SUMMARY = WearOsPaths.DataHeartRateSummary.path
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        serviceScope.launch {
            when (messageEvent.path) {
                PATH_EVENT_OFF_WRIST -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Off-Wrist event received! Payload: $payload")
                    watchMetricsRepository.emitOffWrist(true)
                    eventsManager.updateOnWristStatus(false)
                }

                PATH_EVENT_ON_WRIST -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "On-Wrist event received! Payload: $payload")
                    watchMetricsRepository.emitOffWrist(false)
                    eventsManager.updateOnWristStatus(true)
                }

                PATH_EVENT_MOVEMENT_DETECTED -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Movement detected event received! Payload: $payload")
                    val speed = payload.toFloatOrNull()
                        if (speed == null) { Log.w(TAG, "Invalid speed value received: $payload")
                        } else {
                            watchMetricsRepository.emitMovementDetected(speed)
                            Log.d(TAG, "Movement detected with speed: $speed m/s")
                    }
                }

                PATH_DATA_HEART_RATE_SUMMARY -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Heart rate summary received! Payload: $payload")
                    val parts = payload.split(",")
                    if (parts.size == 3) {
                        val heartRate = parts[0].toIntOrNull() ?: 0
                        val count = parts[1].toIntOrNull() ?: 0
                        val mean = parts[2].toFloatOrNull() ?: 0.0f
                        Log.d( TAG, "Heart Rate Summary - Rate: $heartRate, Count: $count, Mean: $mean" )
                        watchMetricsRepository.emitHeartRate(heartRate)
                    } else {
                        Log.w(TAG, "Invalid heart rate summary format: $payload")
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown message path: ${messageEvent.path}")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearableMessageListenerService onCreate") // Very first line
    }

    override fun onConnectedNodes(connectedNodes: MutableList<Node>) {
        super.onConnectedNodes(connectedNodes)
        Log.d(TAG, "Connected nodes: ${connectedNodes.joinToString { it.displayName }}")
        serviceScope.launch {
            if (connectedNodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found.")
                watchLinkRepository.saveIsConnectedToWatch(false)

            } else {
                Log.i(TAG, "Connected nodes: ${connectedNodes.size}")
                watchLinkRepository.saveIsConnectedToWatch(true)
            }
        }
    }


}