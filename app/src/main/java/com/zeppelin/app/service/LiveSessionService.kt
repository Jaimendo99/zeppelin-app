package com.zeppelin.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zeppelin.app.MainActivity
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.AppUsageReport
import com.zeppelin.app.screens._common.data.ClientHelloMessage
import com.zeppelin.app.screens._common.data.CurrentPhase
import com.zeppelin.app.screens._common.data.PomodoroExtendMessage
import com.zeppelin.app.screens._common.data.PomodoroPausedMessage
import com.zeppelin.app.screens._common.data.PomodoroPhaseEndMessage
import com.zeppelin.app.screens._common.data.PomodoroRestartMessage
import com.zeppelin.app.screens._common.data.PomodoroSessionEndMessage
import com.zeppelin.app.screens._common.data.PomodoroStartMessage
import com.zeppelin.app.screens._common.data.ReportData
import com.zeppelin.app.screens._common.data.ReportType
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.StatusUpdateMessage
import com.zeppelin.app.screens._common.data.UnknownEvent
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens._common.data.toAppUsageRecord
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.service.distractionDetection.DistractionDetectionManager
import com.zeppelin.app.service.wearCommunication.IWatchMetricsRepository
import com.zeppelin.app.service.wearCommunication.WatchProximityMonitor
import com.zeppelin.app.service.wearCommunication.WearCommunicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.UUID

class LiveSessionService : Service() {

    private val webSocketClient by inject<WebSocketClient>()
    private val eventsManager by inject<SessionEventsManager>()
    private val watchProximityMonitor by inject<WatchProximityMonitor>() // Inject it
    private val analyticsClient by inject<AnalyticsClient>()
    private val authPreferences by inject<AuthPreferences>()
    private val liveSessionPref by inject<ILiveSessionPref>()
    private val distractionDetectionManager: DistractionDetectionManager by inject()
    private val wearCommunicator: WearCommunicator by inject()
    private val watchMetricsRepository: IWatchMetricsRepository by inject()
    private val eventsAndMetricsOutHandler: EventsAndMetricsOutHandler by inject()


    private var phaseCount = 0
    private var courseId: Int = -1
    private val _currentPhase = MutableStateFlow(CurrentPhase.NONE)

    private lateinit var notificationManager: NotificationManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentCourseId: Int = -1
    private val SERVICE_UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")

    private var sessionStartTime: Long? = null

    companion object {
        private const val TAG = "LiveSessionService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "live_session_channel"
        const val EXTRA_DESTINATION_ROUTE = "com.zeppelin.app.DESTINATION_ROUTE"
        const val EXTRA_COURSE_ID = "com.zeppelin.app.COURSE_ID"
    }

    private val btAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bleScanner: BluetoothLeScanner? = null
    private var isScanning = false

    private val DEBUG_WIDE_SCAN = false

    private val singleFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()

    private val scanSettings =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setReportDelay(0)
            .setLegacy(true)
            .build()

    private val scanCb =
        object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(ct: Int, result: ScanResult) {
                watchMetricsRepository.emitRSSI(result.rssi)
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onBatchScanResults(results: List<ScanResult>) {
                for (r in results) {
                    Log.d(TAG, "batch: ${r.device.name} - ${r.device.address}  rssi=${r.rssi}")
                    watchMetricsRepository.emitRSSI(r.rssi)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "onScanFailed: $errorCode")
            }
        }

    @SuppressLint("MissingPermission")
    private fun startRssiMonitoring() {
        if (isScanning) {
            Log.w(TAG, "Already scanning, skip")
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Cannot start scan, no BLUETOOTH_SCAN permission")
            return
        }
        val adapter = btAdapter
        if (adapter == null || !adapter.isEnabled) {
            Log.e(TAG, "BT adapter null or disabled")
            return
        }
        bleScanner = adapter.bluetoothLeScanner ?: run {
                    Log.e(TAG, "No BluetoothLeScanner")
                    return
                }

        val filters = if (DEBUG_WIDE_SCAN) null else listOf(singleFilter)
        Log.d(TAG, "Starting BLE scan; filter=${filters?.let { "$SERVICE_UUID" } ?: "NONE"}")
        bleScanner!!.startScan(filters, scanSettings, scanCb)
        isScanning = true
    }

    @SuppressLint("MissingPermission")
    private fun stopRssiMonitoring() {
        if (!isScanning) return
        bleScanner?.stopScan(scanCb)
        Log.d(TAG, "Stopped BLE scan")
        isScanning = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        observeWebSocketState()
        observeIncomingEvents()
        observePomodoroState()
        observePomodoroPhase()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START.name -> {
                Log.d(TAG, "Starting service...")
                courseId = intent.getIntExtra("courseId", -1)
                val retry = intent.getBooleanExtra("retry", false)
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Starting session...", currentCourseId).build()
                )
                serviceScope.launch {
                    liveSessionPref.saveCurrentCourseId(courseId)
                    webSocketClient.connect(courseId, retry)
                }
                eventsAndMetricsOutHandler.start()
                startRssiMonitoring()
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
            liveSessionPref.clearCourseId(courseId)
            liveSessionPref.clearSessionId()
            sendDistractionReport()
            stopRssiMonitoring()
            wearCommunicator.sendStopMonitoringCommand()
            watchProximityMonitor.stopMonitoring()
            webSocketClient.disconnect()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            serviceScope.cancel()
            eventsAndMetricsOutHandler.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            liveSessionPref.clearSessionId()
            liveSessionPref.clearCourseId(courseId)
            sendDistractionReport()
            wearCommunicator.sendStopMonitoringCommand()

            eventsAndMetricsOutHandler.stop()
            stopRssiMonitoring()
            watchProximityMonitor.stopMonitoring() // Ensure it's stopped here too
            serviceScope.cancel()
        }
    }

    private suspend fun sendDistractionReport() {
        val report =
            sessionStartTime?.let { startTime ->
                distractionDetectionManager.getDetailedDistractionReport(
                    startTime,
                    System.currentTimeMillis()
                )
            }
        report?.let {
            analyticsClient.addReport(
                ReportData(
                    userId = authPreferences.getUserIdOnce() ?: "",
                    device = Build.MODEL + " (${Build.MANUFACTURER})",
                    sessionId = liveSessionPref.getSessionIdOnce(),
                    addedAt = System.currentTimeMillis(),
                    type = ReportType.APP_USAGE,
                    courseId = currentCourseId,
                    body = AppUsageReport(report.map { it.toAppUsageRecord() }),
                )
            )
        }
    }

    private fun observeWebSocketState() {
        val title = "Live Session for Course ID"
        serviceScope.launch {
            webSocketClient.state.collect { state ->
                val (notiTitle, text) =
                    when (state) {
                        WebSocketState.Idle -> "Live Session" to "Idle"
                        WebSocketState.Connecting -> {
                            Log.d(TAG, "WebSocket connecting")
                            "Live Session" to "Connecting..."
                        }

                        is WebSocketState.Connected -> {
                            Log.d(TAG, "WebSocket connected ${state.lastCourseId}")
                            wearCommunicator.sendLiveSessionConnected()
                            currentCourseId = state.lastCourseId
                            "$title $currentCourseId" to "Connected"
                        }

                        WebSocketState.Disconnected -> {
                            Log.d(TAG, "WebSocket disconnected")
                            "$title $currentCourseId" to "Disconnected"
                        }

                        is WebSocketState.Error ->
                            "$title $currentCourseId" to "Error: ${state.message}"
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
                    wearCommunicator.sendPomodoroInfo(state)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(
                            state.timerDisplay,
                            currentCourseId,
                            "Live Session for Course ID: $currentCourseId",
                            colored = state.isRunning,
                            phase = state.currentPhase
                        )
                            .build()
                    )
                }
                Log.d(TAG, "Pomodoro state updated: $state")
                _currentPhase.value = state.currentPhase
            }
        }
    }

    private fun observePomodoroPhase() {
        serviceScope.launch {
            _currentPhase.collect { state ->
                handlePomodoroPhase(state)
            }
        }
    }

    private suspend fun handlePomodoroPhase(state: CurrentPhase) {
        when (state) {
            CurrentPhase.WORK -> {
                Log.d(TAG, "Pomodoro work phase started")
                if (currentCourseId >= 0) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                        .apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                            putExtra(EXTRA_DESTINATION_ROUTE, Screens.CourseSession.route)
                            putExtra(EXTRA_COURSE_ID, currentCourseId.toString())
                        }
                    startActivity(intent)
                    eventsManager.pinScreen()
                    Log.d(
                        TAG,
                        "Launched intent to navigate to CourseSession $currentCourseId"
                    )
                }
            }

            CurrentPhase.BREAK -> {
                eventsManager.unpinScreen()
                Log.d(TAG, "Pomodoro break phase started")
            }

            CurrentPhase.NONE -> {
                eventsManager.unpinScreen()
                Log.d(TAG, "Pomodoro session ended or not running")
            }
        }
        phaseCount++
    }

    private fun observeIncomingEvents() {
        serviceScope.launch {
            webSocketClient.wsEvents.collect { event ->
                if (webSocketClient.state.value is WebSocketState.Connected) {
                    Log.d(TAG, "Received event: $event")
                    when (event) {
                        is ClientHelloMessage -> eventsManager.handleClientHello(event)
                        is PomodoroExtendMessage -> eventsManager.handlePomodoroExtend(event)
                        is PomodoroPhaseEndMessage -> {
                            eventsManager.handlePomodoroPhaseEnd(serviceScope, event)
                        }

                        is PomodoroSessionEndMessage -> {
                            wearCommunicator.sendStopMonitoringCommand()
                            eventsManager.handlePomodoroSessionEnd(event)
                        }

                        is PomodoroStartMessage -> {
                            Log.d(TAG, "Received PomodoroStartMessage: $event")
                            sessionStartTime = System.currentTimeMillis()
                            liveSessionPref.saveSessionId(event.sessionId)
                            eventsManager.handlePomodoroStart(serviceScope, event)
                        }

                        is PomodoroPausedMessage -> {
                            Log.d(TAG, "Received PomodoroPausedMessage: $event")
                            eventsManager.handlePomodoroPaused(event)
                        }

                        is PomodoroRestartMessage -> {
                            Log.d(TAG, "Received PomodoroRestartMessage: $event")
                            eventsManager.handlePomodoroResumed(serviceScope, event)
                        }

                        is StatusUpdateMessage ->
                            eventsManager.handleStatusUpdate(event) { onStop() }

                        is UnknownEvent -> eventsManager.handleUnknownEvent(event)
                        else -> {
                            Log.d(TAG, "Unknown event: $event")
                        }
                    }
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

        val notiColor =
            when (phase) {
                CurrentPhase.WORK -> resources.getColor(R.color.primaryContainerDark, null)
                CurrentPhase.BREAK -> resources.getColor(R.color.secondaryContainerDark, null)
                else -> resources.getColor(R.color.primaryContainerDark, null)
            }

        val contentIntent =
            Intent(this, MainActivity::class.java).apply {
                putExtra("courseId", courseId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent =
            PendingIntent.getActivity(this, 0, contentIntent, pendingIntentFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fg_dark)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
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
    //

    enum class Action {
        START,
        STOP
    }
}
