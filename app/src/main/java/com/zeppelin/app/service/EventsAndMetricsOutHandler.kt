package com.zeppelin.app.service

import android.util.Log
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.LockTaskModeStatus
import com.zeppelin.app.screens._common.data.LockTaskOnEvent
import com.zeppelin.app.screens._common.data.LockTaskRemovedEvent
import com.zeppelin.app.screens._common.data.ReportData
import com.zeppelin.app.screens._common.data.ReportType
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.StrongRssiEvent
import com.zeppelin.app.screens._common.data.UnPinScreen
import com.zeppelin.app.screens._common.data.UserHeartRate
import com.zeppelin.app.screens._common.data.UserPhysicalActivity
import com.zeppelin.app.screens._common.data.WeakRssi
import com.zeppelin.app.screens._common.data.WeakRssiEvent
import com.zeppelin.app.screens._common.data.WearableDisconnected
import com.zeppelin.app.screens._common.data.WearableDisconnectedEvent
import com.zeppelin.app.screens._common.data.WearableOff
import com.zeppelin.app.screens._common.data.WearableOffEvent
import com.zeppelin.app.screens._common.data.WearableOnEvent
import com.zeppelin.app.screens._common.data.WearableReconnectedEvent
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import com.zeppelin.app.service.wearCommunication.IWatchMetricsRepository
import com.zeppelin.app.utils.windowed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EventsAndMetricsOutHandler(
    private val sessionEventsManager: SessionEventsManager,
    private val watchMetricsRepository: IWatchMetricsRepository,
    private val watchLinkRepository: WatchLinkRepository,

    private val webSocketClient: WebSocketClient,
    private val analyticsClient: AnalyticsClient,
    private val liveSessionPref: ILiveSessionPref,
    private val authPreferences: AuthPreferences
) {
    companion object {
        private const val TAG = "EventsAndMetricsOutHandle"
        private const val RSSI_WINDOW_SIZE = 5
        private const val HEART_RATE_WINDOW_SIZE = 10
        private const val MOVEMENT_WINDOW_SIZE = 10
    }

    private var currentScope: CoroutineScope? = null

    fun start() {
        Log.d(TAG, "EventsAndMetricsOutHandler started")
        currentScope?.cancel()
        currentScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        Log.d(TAG, "EventsAndMetricsOutHandler scope created for this run.")

        handleLockTaskModeStatus()
        handleWatchLinkStatus()
        handleHeartRate()
        handleMovement()
        handleOffWrist()
        handleRssi()
    }

    private fun handleLockTaskModeStatus() {
        Log.d(TAG, "Handling LockTaskModeStatus changes")
        currentScope?.launch {
            var lastLockStatus: LockTaskModeStatus? = null
            sessionEventsManager.lockTaskModeStatus.distinctUntilChanged().collect { status ->
                Log.d(TAG, "LockTaskModeStatus changed: $status, last was: $lastLockStatus")
                getReportData()?.let { reportData ->
                    when (status) {
                        LockTaskModeStatus.LOCK_TASK_MODE_NONE -> {
                            if (lastLockStatus != LockTaskModeStatus.LOCK_TASK_MODE_NONE) {
                                webSocketClient.sendEvent(
                                    LockTaskRemovedEvent(
                                        System.currentTimeMillis()
                                    )
                                )
                                analyticsClient.addReport(
                                    reportData.copy(
                                        type = ReportType.UNPIN_SCREEN,
                                        body = UnPinScreen(System.currentTimeMillis())
                                    )
                                )
                            }
                            lastLockStatus = status
                        }

                        LockTaskModeStatus.LOCK_TASK_MODE_PINNED -> {
                            if (lastLockStatus != LockTaskModeStatus.LOCK_TASK_MODE_PINNED) {
                                webSocketClient.sendEvent(
                                    LockTaskOnEvent(
                                        System.currentTimeMillis()
                                    )
                                )
                            }
                            lastLockStatus = status
                        }

                        else -> {
                            Log.d(TAG, "Unknown LockTaskModeStatus: $status")
                        }
                    }
                }
            }
        } ?: Log.e(TAG, "handleLockTaskModeStatus: currentScope is null, cannot launch coroutine.")
    }

    private fun handleWatchLinkStatus() {
        Log.d(TAG, "Handling WatchLink connection status")
        currentScope?.launch {
            watchLinkRepository.isConnectedToWatch.distinctUntilChanged().collect { isConnected ->
                Log.d(TAG, "Watch connection status changed: $isConnected")
                getReportData()?.let { reportData ->
                    if (isConnected) {
                        webSocketClient.sendEvent(
                            WearableReconnectedEvent(
                                System.currentTimeMillis()
                            )
                        )
                    } else {
                        webSocketClient.sendEvent(
                            WearableDisconnectedEvent(
                                System.currentTimeMillis()
                            )
                        )
                        analyticsClient.addReport(
                            reportData.copy(
                                type = ReportType.WEARABLE_DISCONNECTED,
                                body = WearableDisconnected(
                                    System.currentTimeMillis()
                                )
                            )
                        )
                    }
                }
            }
        } ?: Log.e(TAG, "handleWatchLinkStatus: currentScope is null, cannot launch coroutine.")
    }

    private fun handleHeartRate() {
        Log.d(TAG, "Handling heart rate data")
        currentScope?.launch {
            watchMetricsRepository.hearRate.filterNotNull()
                .windowed(HEART_RATE_WINDOW_SIZE)
                .collect { heartRateList ->
                    Log.d(TAG, "Heart rate data received: $heartRateList")
                    getReportData()?.let { reportData ->
                        analyticsClient.addReport(
                            reportData.copy(
                                type = ReportType.USER_HEARTRATE,
                                body = UserHeartRate(
                                    UserHeartRate.HeartRateRecord(
                                        value = heartRateList[0],
                                        count = heartRateList.size,
                                        mean = heartRateList.average().toFloat(),
                                        time = System.currentTimeMillis()
                                    )
                                )
                            )
                        )
                    }
                }
        } ?: Log.e(TAG, "handleHeartRate: currentScope is null, cannot launch coroutine.")
    }

    private fun handleMovement() {
        Log.d(TAG, "Handling movement data")
        currentScope?.launch {
            watchMetricsRepository.movementDetected.filterNotNull()
                .windowed(MOVEMENT_WINDOW_SIZE)
                .collect { movementList ->
                    Log.d(TAG, "Movement detected: $movementList")
                    getReportData()?.let { reportData ->
                        val movement = movementList.average()
                        analyticsClient.addReport(
                            reportData.copy(
                                type = ReportType.USER_PHYSICAL_ACTIVITY,
                                body = UserPhysicalActivity(
                                    detectedAt = System.currentTimeMillis(),
                                    speed = movement.toFloat()
                                )
                            )
                        )
                    }
                }
        } ?: Log.e(TAG, "handleMovement: currentScope is null, cannot launch coroutine.")
    }

    private fun handleOffWrist() {
        Log.d(TAG, "Handling on/off wrist status")
        currentScope?.launch {
            var lastOnWrist: Boolean = true
            sessionEventsManager.isOnWrist.filterNotNull().collect { isOnWrist ->
                Log.d(TAG, "On-wrist status changed: $isOnWrist, last was: $lastOnWrist")
                getReportData()?.let { reportData ->
                    if (lastOnWrist && !isOnWrist) {
                        webSocketClient.sendEvent(
                            WearableOffEvent(System.currentTimeMillis())
                        )
                        analyticsClient.addReport(
                            reportData.copy(
                                type = ReportType.WEARABLE_OFF,
                                body = WearableOff(System.currentTimeMillis())
                            )
                        )
                        lastOnWrist = false
                    } else if (!lastOnWrist && isOnWrist) {
                        webSocketClient.sendEvent(
                            WearableOnEvent(
                                System.currentTimeMillis()
                            )
                        )
                        lastOnWrist = true
                    }
                }
            }
        } ?: Log.e(TAG, "handleOffWrist: currentScope is null, cannot launch coroutine.")
    }

    private fun handleRssi() {
        Log.d(TAG, "Handling RSSI data")
        currentScope?.launch {
            var wasLastWeak: Boolean? = false
            watchMetricsRepository.rssi.filterNotNull().windowed(RSSI_WINDOW_SIZE)
                .collect { rssiList ->
                    val rssi = rssiList.average().toInt()
                    Log.d(TAG, "RSSI value received: $rssi, last was weak: $wasLastWeak")
                    getReportData()?.let { reportData ->
                        if (wasLastWeak == false && rssi < -90) {
                            Log.d(TAG, "Weak RSSI detected: $rssi")
                            webSocketClient.sendEvent(WeakRssiEvent(rssi))
                            analyticsClient.addReport(
                                reportData.copy(
                                    type = ReportType.WEAK_RSSI,
                                    body = WeakRssi(rssi)
                                )
                            )
                            wasLastWeak = true
                        } else if (wasLastWeak == true && rssi >= -90) {
                            webSocketClient.sendEvent(StrongRssiEvent(rssi))
                            wasLastWeak = false
                        }
                    }
                }
        } ?: Log.e(TAG, "handleRssi: currentScope is null, cannot launch coroutine.")
    }

    fun stop() {
        Log.d(TAG, "Stopping EventsAndMetricsOutHandler")
        currentScope?.cancel()
        currentScope = null
        Log.d(TAG, "EventsAndMetricsOutHandler scope cancelled and cleared.")
    }

    private suspend fun getReportData(): ReportData? {
        val sessionId = liveSessionPref.getSessionIdOnce()
        if (sessionId == null || sessionId == 0) {
            Log.d(TAG, "No session ID found")
            return null
        }
        Log.d(TAG, "Session ID found: $sessionId")

        return ReportData(
            userId = authPreferences.getUserIdOnce() ?: "user_not_found",
            device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
            sessionId = sessionId,
            addedAt = System.currentTimeMillis(),
            courseId = liveSessionPref.getCourseIdOnce() ?: -1
        )
    }
}