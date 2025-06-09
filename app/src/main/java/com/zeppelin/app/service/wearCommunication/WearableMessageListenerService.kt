package com.zeppelin.app.service.wearCommunication

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.ReportData
import com.zeppelin.app.screens._common.data.ReportType
import com.zeppelin.app.screens._common.data.SessionEventsManager
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

        // Ensure these paths match what the watch sends
        val PATH_EVENT_OFF_WRIST = WearOsPaths.EventOffWrist.path
        val PATH_EVENT_ON_WRIST = WearOsPaths.EventOnWrist.path
        val PATH_EVENT_MOVEMENT_DETECTED = WearOsPaths.EventMovementDetected.path
        private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        serviceScope.launch {
            val genReportData = ReportData(
                userId = authPreferences.getUserIdOnce() ?: "",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = liveSessionPref.getSessionIdOnce() ?: "",
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
                            data = WearableOff(removedAt = System.currentTimeMillis())
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
                            data = WearableOn(addedAt = System.currentTimeMillis())
                        )
                    )
                }

                PATH_EVENT_MOVEMENT_DETECTED -> {
                    val payload = String(messageEvent.data)
                    Log.i(TAG, "Movement detected event received! Payload: $payload")
                    val speed = payload.toFloatOrNull()
                    analyticsClient.addReport(
                        genReportData.copy(
                            type = ReportType.USER_PHYSICAL_ACTIVITY,
                            data = UserPhysicalActivity(
                                detectedAt = System.currentTimeMillis(),
                                speed = speed ?: 0.0f
                            )
                        )
                    )
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
                        data = WearableDisconnected(disconnectedAt = System.currentTimeMillis())
                    )
                )
                watchLinkRepository.saveIsConnectedToWatch(false)

            } else {
                Log.i(TAG, "Connected nodes: ${connectedNodes.size}")
                analyticsClient.addReport(
                    genReportData.copy(
                        type = ReportType.WEARABLE_CONNECTED,
                        data = WearableReconnected(reconnectedAt = System.currentTimeMillis())
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