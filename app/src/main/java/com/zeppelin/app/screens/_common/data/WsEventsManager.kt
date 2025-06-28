package com.zeppelin.app.screens._common.data

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

class SessionEventsManager {

    private val context: Context by inject(Context::class.java)

    private val CHECK_INTERVAL_MILLIS = 1000L // Check every second

    private val _pomodoroState = MutableStateFlow(PomodoroState.Initial) // Start with initial state
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()

    private val _pinningUiEventFlow = MutableSharedFlow<PinningUiEvent>(replay = 0)
    val pinningUiEventFlow: SharedFlow<PinningUiEvent> = _pinningUiEventFlow.asSharedFlow()

    private val _pinningManuallyExitedEventFlow = MutableSharedFlow<Unit>(replay = 0)
    val pinningManuallyExitedEventFlow: SharedFlow<Unit> =
        _pinningManuallyExitedEventFlow.asSharedFlow()

    // track if the wearable is on or off the wrist
    private val _isOnWrist = MutableStateFlow<Boolean?>(null)
    val isOnWrist: StateFlow<Boolean?> = _isOnWrist.asStateFlow()


    private var wasPreviouslyInWorkPhase: Boolean = false
    private var timerJob: Job? = null
    private val currentRemainingSeconds = AtomicInteger(0)
    private val TAG = "WsEventsManager"


    val lockTaskModeStatus: Flow<LockTaskModeStatus> = callbackFlow {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        while (isActive) {
            val currentStatus = getCurrentLockTaskModeStatus(activityManager)
            Log.d(TAG, "Lock task mode status: $currentStatus")
            trySend(currentStatus)
            delay(CHECK_INTERVAL_MILLIS)
        }
        awaitClose { }
    }

    fun updateOnWristStatus(isOnWrist: Boolean) {
        Log.d(TAG, "Updating on wrist status: $isOnWrist")
        _isOnWrist.value = isOnWrist
    }

    suspend fun pinScreen(){
        Log.d(TAG, "Updating lock task mode status")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val currentStatus = getCurrentLockTaskModeStatus(activityManager)
        Log.d(TAG, "Current lock task mode status: $currentStatus")
        _pinningUiEventFlow.emit(PinningUiEvent.StartPinning)
    }

    suspend fun unpinScreen() {
        Log.d(TAG, "Unpinning screen")
        _pinningUiEventFlow.emit(PinningUiEvent.StopPinning)
        _pinningManuallyExitedEventFlow.emit(Unit)
    }



    private fun getCurrentLockTaskModeStatus(activityManager: ActivityManager): LockTaskModeStatus {
        return when (activityManager.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_NONE -> LockTaskModeStatus.LOCK_TASK_MODE_NONE
            ActivityManager.LOCK_TASK_MODE_LOCKED -> LockTaskModeStatus.LOCK_TASK_MODE_LOCKED
            ActivityManager.LOCK_TASK_MODE_PINNED -> LockTaskModeStatus.LOCK_TASK_MODE_PINNED
            else -> LockTaskModeStatus.LOCK_TASK_MODE_NONE // Should not happen
        }
    }


    fun handleStatusUpdate(
        statusUpdate: StatusUpdateMessage,
        disconnectFunction: () -> Unit
    ) {
        /*
            * Everytime a connection to the same websocket is established, the server sends a status update
            * Here we check if the web client is connected or not
            * If the web client is not connected, we call the disconnect function
         */

        Log.d(TAG, "Status update: $statusUpdate")
        if (statusUpdate.platforms.web == 0) {
            Log.w(TAG, "Web client disconnected, potentially ending session.")
            _pomodoroState.value = PomodoroState.Initial // Reset state
            if (wasPreviouslyInWorkPhase) {
                CoroutineScope(Dispatchers.Default).launch { _pinningUiEventFlow.emit(PinningUiEvent.StopPinning) }
                wasPreviouslyInWorkPhase = false
            }
            disconnectFunction()
        } else {
            Log.d(TAG, "WebSocket is connected")
        }
    }

    fun handlePomodoroStart(scope: CoroutineScope, startMessage: PomodoroStartMessage) {
        Log.d(TAG, "Pomodoro started: $startMessage")
        stopTimer() // Ensure any previous timer is stopped

        val config = startMessage.config
        val initialState = PomodoroState(
            isRunning = true,
            currentPhase = CurrentPhase.WORK, // Pomodoro always starts with work
            remainingSeconds = calculateRemainingSeconds(
                startMessage.startedAt,
                config.workDuration
            ),
            currentCycle = 1,
            totalCycles = config.cycles,
            workDuration = config.workDuration,
            breakDuration = config.breakDuration,
            timerDisplay = calculateRemainingSeconds(
                startMessage.startedAt,
                config.workDuration
            ).formatTime(),
            timerDigits = calculateRemainingSeconds(
                startMessage.startedAt,
                config.workDuration
            ).toTimerDigits()
        )
        _pomodoroState.value = initialState
        currentRemainingSeconds.set(initialState.remainingSeconds)
        startTimer(scope)

        // Pomodoro always starts with WORK phase
        if (!wasPreviouslyInWorkPhase) {
            scope.launch { _pinningUiEventFlow.emit(PinningUiEvent.StartPinning) }
            wasPreviouslyInWorkPhase = true
        }
    }

    @OptIn(ExperimentalTime::class)
    fun calculateRemainingSeconds(
        startedAt: Long,
        duration: Int
    ): Int {
        val rightNow = { now().toEpochMilliseconds() / 1000 }
        return (duration - (rightNow() - startedAt)).toInt()
    }

    fun handlePomodoroExtend(extendMessage: PomodoroExtendMessage) {
        Log.d(TAG, "Pomodoro extended: $extendMessage")
        if (_pomodoroState.value.isRunning) {
            val newRemaining =
                currentRemainingSeconds.addAndGet(extendMessage.seconds)
            // Update the state immediately for responsiveness
            _pomodoroState.update {
                it.copy(
                    remainingSeconds = newRemaining,
                    timerDisplay = newRemaining.formatTime()
                )
            }
        } else {
            Log.w(TAG, "Received extend event but timer is not running.")
        }
    }

    fun handlePomodoroPhaseEnd(scope: CoroutineScope, phaseEndMessage: PomodoroPhaseEndMessage) {
        Log.d(TAG, "Pomodoro phase ended: $phaseEndMessage")
        stopTimer() // Stop the timer for the completed phase

        val currentState = _pomodoroState.value

        if (phaseEndMessage.isLastCycle && phaseEndMessage.isBreakFinished) {
            Log.d(TAG, "Last cycle break finished according to server.")
            if (wasPreviouslyInWorkPhase) {
                scope.launch { _pinningUiEventFlow.emit(PinningUiEvent.StopPinning) }
                wasPreviouslyInWorkPhase = false
            }
        } else {
            val nextPhase = phaseEndMessage.continueAs
            val nextDuration = if (nextPhase == CurrentPhase.WORK) {
                currentState.workDuration
            } else {
                currentState.breakDuration
            }
            val nextCycle = phaseEndMessage.nextCycle

            val newState = currentState.copy(
                isRunning = true,
                currentPhase = nextPhase,
                remainingSeconds = nextDuration,
                currentCycle = nextCycle,
                timerDisplay = nextDuration.formatTime(),
                timerDigits = nextDuration.toTimerDigits()
            )
            _pomodoroState.value = newState
            currentRemainingSeconds.set(nextDuration)
            startTimer(scope)

            if (nextPhase == CurrentPhase.WORK && !wasPreviouslyInWorkPhase) {
                scope.launch { _pinningUiEventFlow.emit(PinningUiEvent.StartPinning) }
                wasPreviouslyInWorkPhase = true
            } else if (nextPhase != CurrentPhase.WORK && wasPreviouslyInWorkPhase) {
                scope.launch { _pinningUiEventFlow.emit(PinningUiEvent.StopPinning) }
                wasPreviouslyInWorkPhase = false
            }
        }
    }

    fun handlePomodoroSessionEnd(sessionEndMessage: PomodoroSessionEndMessage) {
        Log.d(TAG, "Pomodoro session ended: $sessionEndMessage")
        stopTimer()
        _pomodoroState.value = PomodoroState.Initial // Reset state
        if (wasPreviouslyInWorkPhase) {
            // Assuming SessionEventsManager has a CoroutineScope available, or one is passed in.
            // If not, this launch needs to be handled appropriately.
            // For simplicity, let's assume a default scope for this event,
            // but ideally, it should use the same scope as other operations.
            CoroutineScope(Dispatchers.Default).launch { _pinningUiEventFlow.emit(PinningUiEvent.StopPinning) }
            wasPreviouslyInWorkPhase = false
        }
    }

    fun handleClientHello(clientHello: ClientHelloMessage) {
        Log.d(TAG, "Client hello: $clientHello")
    }

    fun handleUnknownEvent(unknownEvent: UnknownEvent) {
        Log.w(TAG, "Unknown event: ${unknownEvent.rawText}")
    }

    // --- Timer Control ---

    fun startTimer(scope: CoroutineScope) {
        Log.d(TAG, "Starting timer...")
        if (timerJob?.isActive == true) {
            Log.w(TAG, "Timer already running.")
            return
        }

        timerJob = scope.launch(Dispatchers.Default) { // Use Default dispatcher for delays
            Log.d(TAG, "Timer started.")
            try {
                while (isActive && currentRemainingSeconds.get() > 0) {
                    delay(1000) // Wait for 1 second
                    val remaining = currentRemainingSeconds.decrementAndGet()

                    // Ensure we are still active after delay before updating state
                    if (isActive) {
                        _pomodoroState.update {
                            it.copy(
                                remainingSeconds = remaining,
                                timerDisplay = remaining.formatTime(),
                                timerDigits = remaining.toTimerDigits()
                            )
                        }
                    }
                }
                if (isActive) {
                    _pomodoroState.update {
                        it.copy(
                            remainingSeconds = 0,
                            timerDisplay = 0.formatTime(),
                            timerDigits = 0.toTimerDigits(),
                        )
                    }
                    Log.d(TAG, "Timer finished counting down.")
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Timer cancelled.")
                // _pomodoroState.update { it.copy(isRunning = false) }
            } finally {
                Log.d(TAG, "Timer coroutine ended.")
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        // This might conflict if a new state is set immediately after stopping
        // _pomodoroState.update { it.copy(isRunning = false) }
        Log.d(TAG, "Timer stopped.")
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up SessionEventsManager.")
        stopTimer()
        // scope.cancel() // Cancel the scope if it was created solely for this manager
    }
}
