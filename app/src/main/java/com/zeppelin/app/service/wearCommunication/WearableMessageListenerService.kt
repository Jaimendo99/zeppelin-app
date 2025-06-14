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

    private val webSocketClient: WebSocketClient by inject()
    private val eventsManager: SessionEventsManager by inject()
    private val analyticsClient: AnalyticsClient by inject()
    private val authPreferences: AuthPreferences by inject()
    private val liveSessionPref: ILiveSessionPref by inject()
    private val watchLinkRepository: WatchLinkRepository by inject()


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
            val sessionId = liveSessionPref.getSessionIdOnce()
            if (sessionId.isNullOrEmpty()) {
                Log.w(TAG, "Session ID is null or empty, ignoring message: ${messageEvent.path}")
                return@launch
            }
            val genReportData = ReportData(
                userId = authPreferences.getUserIdOnce() ?: "user_not_found",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = sessionId,
                addedAt = System.currentTimeMillis(),
            )
            when (messageEvent.path) {
                PATH_EVENT_OFF_WRIST -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Off-Wrist event received! Payload: $payload")
                    webSocketClient.sendEvent(WearableOffEvent(removedAt = System.currentTimeMillis()))
                    eventsManager.updateOnWristStatus(false)
                    analyticsClient.addReport(
                        genReportData.copy(
                            type = ReportType.WEARABLE_OFF,
                            body = WearableOff(removedAt = System.currentTimeMillis())
                        )
                    )
                }

                PATH_EVENT_ON_WRIST -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "On-Wrist event received! Payload: $payload")
                    webSocketClient.sendEvent(WearableOnEvent(addedAt = System.currentTimeMillis()))
                    eventsManager.updateOnWristStatus(true)
                    analyticsClient.addReport(
                        genReportData.copy(
                            type = ReportType.WEARABLE_ON,
                            body = WearableOn(addedAt = System.currentTimeMillis())
                        )
                    )
                }

                PATH_EVENT_MOVEMENT_DETECTED -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Movement detected event received! Payload: $payload")
                    val speed = payload.toFloatOrNull()
                    val result = analyticsClient.addReport(
                        genReportData.copy(
                            type = ReportType.USER_PHYSICAL_ACTIVITY,
                            body = UserPhysicalActivity(
                                detectedAt = System.currentTimeMillis(),
                                speed = speed ?: 0.0f
                            )
                        )
                    )
                    when (result) {
                        is NetworkResult.Success ->{
                            Log.d(TAG, "Movement detected report added successfully: $result")
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Failed to add UserPhysicalActivity report: ${result.exception?.message}")
                        }
                        else -> {
                            Log.w(TAG, "Unexpected result type: $result")
                        }
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
                        analyticsClient.addReport( genReportData.copy(
                            type = ReportType.USER_HEARTRATE,
                            body = UserHeartRate(
                                heartRateChange = UserHeartRate.HeartRateRecord(
                                    value = heartRate, mean = mean, count = count,
                                    time = System.currentTimeMillis()
                                    )
                                )
                            )
                        )
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
            val genReportData = ReportData(
                userId = authPreferences.getUserIdOnce() ?: "",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = liveSessionPref.getSessionIdOnce() ?: "",
                addedAt = System.currentTimeMillis(),
            )
            if (connectedNodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found.")
                webSocketClient.sendEvent(
                    WearableDisconnectedEvent(disconnectedAt = System.currentTimeMillis())
                )
                analyticsClient.addReport(
                    genReportData.copy(
                        type = ReportType.WEARABLE_DISCONNECTED,
                        body = WearableDisconnected(disconnectedAt = System.currentTimeMillis())
                    )
                )
                watchLinkRepository.saveIsConnectedToWatch(false)

            } else {
                Log.i(TAG, "Connected nodes: ${connectedNodes.size}")
                analyticsClient.addReport(
                    genReportData.copy(
                        type = ReportType.WEARABLE_CONNECTED,
                        body = WearableReconnected(reconnectedAt = System.currentTimeMillis())
                    )
                )
                webSocketClient.sendEvent(
                    WearableReconnectedEvent(reconnectedAt = System.currentTimeMillis())
                )

                watchLinkRepository.saveIsConnectedToWatch(true)
            }
        }
    }


}