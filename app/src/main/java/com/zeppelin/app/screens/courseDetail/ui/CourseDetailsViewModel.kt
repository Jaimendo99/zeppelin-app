package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens._common.data.PomodoroState
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUIState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.data.ModuleListUI
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUi
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUiState
import com.zeppelin.app.screens.courseDetail.domain.toCourseDetailUI
import com.zeppelin.app.screens.courseDetail.domain.toUI
import com.zeppelin.app.screens.courseSession.data.MetricListItem
import com.zeppelin.app.screens.courseSession.data.WatchMetricData
import com.zeppelin.app.screens.courseSession.data.WatchMetricLists
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.service.wearCommunication.IWatchMetricsRepository
import com.zeppelin.app.service.wearCommunication.WearCommunicator
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo,
    webSocketClient: WebSocketClient,
    eventsManager: SessionEventsManager,
    private val wearCommunicator: WearCommunicator,
    val metricsRepository: IWatchMetricsRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

       private val _courseInfo = MutableStateFlow(CourseDetailModulesUIState())
    val courseInfo: StateFlow<CourseDetailModulesUIState> = _courseInfo

    private val _quizGrades = MutableStateFlow(QuizGradesUiState())
    val quizGrades: StateFlow<QuizGradesUiState> = _quizGrades

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val pomodoroState: StateFlow<PomodoroState> = eventsManager.pomodoroState

    val webSocketState: StateFlow<WebSocketState> = webSocketClient.state

    val watchOff: StateFlow<Boolean?> = eventsManager.isOnWrist


    private val _allMetricsHistory = MutableStateFlow(WatchMetricLists())
    val allMetricsHistory: StateFlow<WatchMetricLists> = _allMetricsHistory.asStateFlow()


    companion object{
        private const val MAX_LIST_SIZE = 200
        private const val TAG = "CourseDetailViewModel"
    }

    init {
        viewModelScope.launch {
            webSocketClient.state.collect { state ->
                when (state) {
                    is WebSocketState.Connected -> Log.d(TAG, "WebSocket connected ${state.lastCourseId}")
                    is WebSocketState.Disconnected -> Log.d(TAG, "WebSocket disconnected")
                    is WebSocketState.Error -> Log.d(TAG, "WebSocket error: ${state.message}")
                    WebSocketState.Connecting -> Log.d(TAG, "WebSocket connecting")
                    WebSocketState.Idle -> Log.d(TAG, "WebSocket idle")
                }
            }
        }
        collectAllMetrics()
    }

    fun startSession(courseId: Int, retry: Boolean = false) {
        viewModelScope.launch {
            Log.d(TAG, "Starting session for course ID: $courseId")
            courseDetailRepo.connectToSession(courseId, retry)
        }
    }

    private suspend fun loadQuizGrades(courseId: Int) {
            _quizGrades.value = QuizGradesUiState(isLoading = true)
            val quizReq = courseDetailRepo.getQuizAttempts(courseId)

        Log.d(TAG, "Loading quiz attempts for course ID: $courseId")

            _quizGrades.value = when (val result = quizReq) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Quiz attempts loaded successfully")
                    QuizGradesUiState(
                        isLoading = false,
                        quizGrades = result.data.map { it.toUI() }
                    )
                }
                is NetworkResult.Error<*> -> {
                    Log.e(TAG, "Error loading quiz attempts: ${result.exception}")
                    QuizGradesUiState( isLoading = false,
                        error = result.exception?.message ?: "Unknown error"
                    )
                }
                NetworkResult.Idle -> QuizGradesUiState(isLoading = false, error = "Idle state, no data available")
                NetworkResult.Loading -> QuizGradesUiState(isLoading = true)
        }
    }

    private suspend fun loadCourseInfo(courseId: Int) {
        _courseInfo.value = CourseDetailModulesUIState(isLoading = true)
        val courseReq = courseDetailRepo.getCourseInfo(courseId)
        _courseInfo.value = when (courseReq) {
            is NetworkResult.Success -> {
                Log.d(TAG, "Course info loaded successfully")
                CourseDetailModulesUIState(
                    isLoading = false,
                    courseDetailModulesUI = courseReq.data.toUI()
                )
            }
            is NetworkResult.Error<*> -> {
                Log.e(TAG, "Error loading course info: ${courseReq.exception}")
                CourseDetailModulesUIState(
                    isLoading = false,
                    error = courseReq.exception?.message ?: "Unknown error"
                )
            }
            NetworkResult.Idle -> CourseDetailModulesUIState(isLoading = false, error = "Idle state, no data available")
            NetworkResult.Loading -> CourseDetailModulesUIState(isLoading = true)
        }
    }

    fun loadCourseDetails(courseId: Int) {
        Log.d(TAG, "Loading course details for course ID: $courseId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val courseInfoJob = async { loadCourseInfo(courseId) }
                val quizGradesJob = async { loadQuizGrades(courseId) }
                courseInfoJob.await()
                quizGradesJob.await()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e(TAG, "Error loading course details: ${_error.value}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onShowGradeDetail(quizGradesUi: QuizGradesUi) {
        _quizGrades.value = _quizGrades.value.copy(
            showDetails = true,
        )
    }

    fun onDismissDialog() {
        _quizGrades.value = _quizGrades.value.copy(
            showDetails = false,
        )
    }


    fun onDisconnect() {
        viewModelScope.launch {
            wearCommunicator.sendStopMonitoringCommand()
            courseDetailRepo.disconnectFromSession()
            _events.emit(Screens.Courses.route)
        }
    }

    fun onSessionStartClick(sessionId: Int) {
        Log.d(TAG, "onSessionStartClick called with sessionId: $sessionId")
        if (webSocketState.value is WebSocketState.Connected) {
            viewModelScope.launch {
                wearCommunicator.sendStartMonitoringCommand()
                _events.emit("courseSession/$sessionId")
                collectAllMetrics()
            }
        } else {
            Log.w(TAG, "onSessionStartClick called but session not ready or ID is empty.")
        }
    }

    fun onToggleAccordion(moduleIndex: Int, moduleName:String) {
        // 1) Flip showContent on the matching module
        _courseInfo.update { state ->
            val updatedModules = state.courseDetailModulesUI.modules.map { module ->
                if (module.moduleIndex == moduleIndex && module.moduleName == moduleName) {
                    module.copy(showContent = !module.showContent)
                } else {
                    module
                }
            }
            // 2) Return a brand-new CourseDetailModulesUIState
            state.copy(
                courseDetailModulesUI = state.courseDetailModulesUI.copy(
                    modules = updatedModules
                )
            )
        }

        val newShow = _courseInfo.value.courseDetailModulesUI.modules
            .first { it.moduleIndex == moduleIndex }
            .showContent
        Log.d(TAG, "accordion toggled: show -> $newShow")
    }


    fun collectHeartRate() {
        viewModelScope.launch {
            metricsRepository.hearRate.collect { heartRate ->
                Log.d(TAG, "heartRate: $heartRate")
                heartRate?.let{
                    val newItem = MetricListItem(it, System.currentTimeMillis())
                    _allMetricsHistory.update { currentLists ->
                        val updatedList = (currentLists.heartRate + newItem).takeLast(MAX_LIST_SIZE)
                        currentLists.copy(heartRate = updatedList)
                    }
                }
            }
        }
    }

    fun collectMovementIntensity() {
        viewModelScope.launch {
            metricsRepository.movementDetected.collect { movementIntensity ->
                Log.d(TAG, "movementIntensity: $movementIntensity")
                movementIntensity?.let {
                    val newItem = MetricListItem(it, System.currentTimeMillis())
                    _allMetricsHistory.update { currentLists ->
                        val updatedList = (currentLists.movementIntensity + newItem).takeLast(MAX_LIST_SIZE)
                        currentLists.copy(movementIntensity = updatedList)
                    }
                }
            }
        }
    }

    fun collectRssi() {
        viewModelScope.launch {
            metricsRepository.rssi.collect { rssi ->
                Log.d(TAG, "rssi: $rssi")
                rssi?.let {
                    val newItem = MetricListItem(it, System.currentTimeMillis())
                    _allMetricsHistory.update { currentLists ->
                        val updatedList = (currentLists.rssi + newItem).takeLast(MAX_LIST_SIZE)
                        currentLists.copy(rssi = updatedList)
                    }
                }
            }
        }
    }


    private fun collectAllMetrics() {
        Log.d(TAG, "collectAllMetrics called")
        collectHeartRate()
        collectMovementIntensity()
        collectRssi()
    }

}