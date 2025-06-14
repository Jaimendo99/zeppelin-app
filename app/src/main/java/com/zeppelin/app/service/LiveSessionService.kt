package com.zeppelin.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zeppelin.app.MainActivity
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.AppUsageReport
import com.zeppelin.app.screens._common.data.ClientHelloMessage
import com.zeppelin.app.screens._common.data.CurrentPhase
import com.zeppelin.app.screens._common.data.LockTaskModeStatus
import com.zeppelin.app.screens._common.data.LockTaskRemovedEvent
import com.zeppelin.app.screens._common.data.PomodoroExtendMessage
import com.zeppelin.app.screens._common.data.PomodoroPhaseEndMessage
import com.zeppelin.app.screens._common.data.PomodoroSessionEndMessage
import com.zeppelin.app.screens._common.data.PomodoroStartMessage
import com.zeppelin.app.screens._common.data.ReportData
import com.zeppelin.app.screens._common.data.ReportType
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.StatusUpdateMessage
import com.zeppelin.app.screens._common.data.StrongRssi
import com.zeppelin.app.screens._common.data.StrongRssiEvent
import com.zeppelin.app.screens._common.data.UnPinScreen
import com.zeppelin.app.screens._common.data.UnknownEvent
import com.zeppelin.app.screens._common.data.WeakRssi
import com.zeppelin.app.screens._common.data.WeakRssiEvent
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens._common.data.toAppUsageRecord
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import com.zeppelin.app.service.distractionDetection.DistractionDetectionManager
import com.zeppelin.app.service.wearCommunication.WatchProximityMonitor
import com.zeppelin.app.service.wearCommunication.WearCommunicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LiveSessionService : Service() {

    private val webSocketClient by inject<WebSocketClient>()
    private val eventsManager by inject<SessionEventsManager>()
    private val watchProximityMonitor by inject<WatchProximityMonitor>() // Inject it
    private val analyticsClient by inject<AnalyticsClient>()
    private val authPreferences by inject<AuthPreferences>()
    private val liveSessionPref by inject<ILiveSessionPref>()
    private val watchLinkRepository: WatchLinkRepository by inject()
    private val distractionDetectionManager: DistractionDetectionManager by inject()
    private val wearCommunicator: WearCommunicator by inject()


    private lateinit var notificationManager: NotificationManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentCourseId: Int = -1

    private var sessionStartTime: Long? = null

    companion object {
        private const val TAG = "LiveSessionService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "live_session_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        observeWebSocketState()
        observeIncomingEvents()
        observePomodoroState()
        observeSessionEvents()
        observeWatchRssi()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            Action.START.name -> {
                Log.d(TAG, "Starting service...")
                val courseId = intent.getIntExtra("courseId", -1)
                val retry = intent.getBooleanExtra("retry", false)
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Starting session...", currentCourseId).build()
                )
                serviceScope.launch {
                    webSocketClient.connect(courseId, retry)

                    val watchAddress = watchLinkRepository.watchAddress.firstOrNull()
                    if (watchAddress != null) {
                        Log.d(TAG, "Starting watch proximity monitoring for address: $watchAddress")
                        watchProximityMonitor.startMonitoring(watchAddress)
                    } else {
                        Log.w(TAG, "No paired watch MAC address to start proximity monitoring.")
                    }
                }
            }

            Action.STOP.name -> {
                Log.d(TAG, "Stopping service...")
                onStop()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun onStop() {
        serviceScope.launch {
            sendDistractionReport()
            wearCommunicator.sendStopMonitoringCommand()
            watchProximityMonitor.stopMonitoring()
            webSocketClient.disconnect()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            sendDistractionReport()
            wearCommunicator.sendStopMonitoringCommand()
        }
        watchProximityMonitor.stopMonitoring() // Ensure it's stopped here too
        serviceScope.cancel()
    }

    private suspend fun sendDistractionReport() {
        val report = sessionStartTime?.let { startTime ->
            distractionDetectionManager.getDetailedDistractionReport(
                startTime,
                System.currentTimeMillis()
            )
        }
        report?.let {
            analyticsClient.addReport(
                ReportData(
                userId = authPreferences.getUserIdOnce() ?: "",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = liveSessionPref.getSessionIdOnce() ?: "",
                addedAt = System.currentTimeMillis(),
                type = ReportType.APP_USAGE,
                body = AppUsageReport(report.map { it.toAppUsageRecord() })
            ))
        }
    }

    private fun observeWebSocketState() {
        val title = "Live Session for Course ID"
        serviceScope.launch {
            webSocketClient.state.collect { state ->
                val (notiTitle, text) = when (state) {
                    WebSocketState.Idle -> "Live Session" to "Idle"
                    WebSocketState.Connecting -> {
                        Log.d(TAG, "WebSocket connecting")
                        "Live Session" to "Connecting..."
                    }

                    is WebSocketState.Connected -> {
                        Log.d(TAG, "WebSocket connected ${state.lastCourseId}")
                        currentCourseId = state.lastCourseId
                        "$title $currentCourseId" to "Connected"
                    }

                    WebSocketState.Disconnected -> {
                        Log.d(TAG, "WebSocket disconnected")
                        "$title $currentCourseId" to "Disconnected"
                    }

                    is WebSocketState.Error -> "$title $currentCourseId" to "Error: ${state.message}"
                }

                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification(text, currentCourseId, notiTitle, false).build()
                )
            }
        }
    }

    private fun observePomodoroState() {
        serviceScope.launch {
            eventsManager.pomodoroState.collect { state ->
                if (webSocketClient.state.value is WebSocketState.Connected) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(
                            state.timerDisplay,
                            currentCourseId,
                            "Live Session for Course ID: $currentCourseId",
                            colored = state.isRunning,
                            phase = state.currentPhase
                        ).build()
                    )
                }
            }
        }
    }

    fun observeSessionEvents() {
        serviceScope.launch {
            val genReportData = ReportData(
                userId = authPreferences.getUserIdOnce() ?: "",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = liveSessionPref.getSessionIdOnce() ?: "",
                addedAt = System.currentTimeMillis(),
            )
            eventsManager.lockTaskModeStatus.distinctUntilChanged().collect {
                when (it) {
                    LockTaskModeStatus.LOCK_TASK_MODE_PINNED -> {
                        Log.d(TAG, "Screen pinning enabled")
                    }

                    LockTaskModeStatus.LOCK_TASK_MODE_NONE -> {
                        webSocketClient.sendEvent(
                            LockTaskRemovedEvent(removedAt = System.currentTimeMillis())
                        )
                        analyticsClient.addReport(
                            genReportData.copy(
                                type = ReportType.UNPIN_SCREEN,
                                body = UnPinScreen(removedAt = System.currentTimeMillis())
                            )
                        )
                        Log.d(TAG, "Screen pinning disabled")
                    }

                    LockTaskModeStatus.LOCK_TASK_MODE_LOCKED -> {
                        Log.d(TAG, "Screen pinning locked")
                    }
                }
            }
        }
    }

    private fun observeIncomingEvents() {
        serviceScope.launch {
            webSocketClient.wsEvents.collect { event ->
                if (webSocketClient.state.value is WebSocketState.Connected) {
                    when (event) {
                        is ClientHelloMessage -> eventsManager.handleClientHello(event)
                        is PomodoroExtendMessage -> eventsManager.handlePomodoroExtend(event)
                        is PomodoroPhaseEndMessage -> eventsManager.handlePomodoroPhaseEnd(
                            serviceScope,
                            event
                        )

                        is PomodoroSessionEndMessage -> eventsManager.handlePomodoroSessionEnd(event)
                        is PomodoroStartMessage -> {
                            sessionStartTime = System.currentTimeMillis()
                            eventsManager.handlePomodoroStart(serviceScope, event)
                        }

                        is StatusUpdateMessage -> eventsManager.handleStatusUpdate(event) { onStop() }
                        is UnknownEvent -> eventsManager.handleUnknownEvent(event)
                        else -> {
                            Log.d(TAG, "Unknown event: $event")
                        }
                    }
                }
            }
        }
    }

    private fun observeWatchRssi() {
        serviceScope.launch {
            val genReportData = ReportData(
                userId = authPreferences.getUserIdOnce() ?: "",
                device = android.os.Build.MODEL + " (${android.os.Build.MANUFACTURER})",
                sessionId = liveSessionPref.getSessionIdOnce() ?: "",
                addedAt = System.currentTimeMillis(),
            )
            watchProximityMonitor.currentRssi.collect { rssiValue ->
                Log.i(TAG, "Watch RSSI updated: $rssiValue dBm")
                if (rssiValue != null) {
                    val rssiThreshold = -85 // Example: -85 dBm
                    if (rssiValue < rssiThreshold) {
                        Log.w(TAG, "Watch RSSI ($rssiValue) is below threshold ($rssiThreshold)")
                        webSocketClient.sendEvent(WeakRssiEvent(rssiValue))
                        analyticsClient.addReport(
                            genReportData.copy(
                                type = ReportType.WEAK_RSSI,
                                body = WeakRssi(rssi = rssiValue)
                            )
                        )
                    } else {
                        Log.i(TAG, "Watch RSSI ($rssiValue) is above threshold ($rssiThreshold)")
                        webSocketClient.sendEvent(StrongRssiEvent(rssiValue))
                        analyticsClient.addReport(
                            genReportData.copy(
                                type = ReportType.STRONG_RSSI,
                                body = StrongRssi(rssi = rssiValue)
                            )
                        )
                    }
                } else {
                    Log.w(TAG, "Watch RSSI is null (disconnected or error reading).")
                }
            }
        }
    }

    private fun buildNotification(
        contentText: String,
        courseId: Int,
        contentTitle: String = "Live Session for Course ID: $courseId",
        colored: Boolean = true,
        phase: CurrentPhase = CurrentPhase.WORK
    ): NotificationCompat.Builder {

        val notiColor = when (phase) {
            CurrentPhase.WORK -> resources.getColor(R.color.primaryContainerDark, null)
            CurrentPhase.BREAK -> resources.getColor(R.color.secondaryContainerDark, null)
            else -> resources.getColor(R.color.primaryContainerDark, null)
        }

        // clicking takes you back to MainActivity with the courseId in extras
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("courseId", courseId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fg_dark)
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, R.drawable.logo)
            )
            .setContentIntent(contentPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(contentTitle)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setColorized(colored)
            .setColor(notiColor)
    }

    enum class Action {
        START, STOP
    }
}
