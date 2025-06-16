package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
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
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.service.wearCommunication.WearCommunicator
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo,
    webSocketClient: WebSocketClient,
    eventsManager: SessionEventsManager,
    private val wearCommunicator: WearCommunicator
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

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

    companion object{
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

    fun getCourseDetail(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val courseDetail = async { courseDetailRepo.getCourseDetail(id) }.await()
            val quizSummary = async { courseDetailRepo.getQuizAnswers() }.await()
            val courseDetailResult = courseDetail.getOrNull()
            val quizSummaryResult = quizSummary.getOrNull()

            if (courseDetail.isFailure || quizSummary.isFailure || courseDetailResult == null || quizSummaryResult == null) {
                _error.value = courseDetail.exceptionOrNull()?.message
                    ?: quizSummary.exceptionOrNull()?.message
                Log.e(TAG, "Error fetching course detail or quiz summary - ${_error.value}")
                return@launch
            }
            _courseDetail.value = courseDetailResult.toCourseDetailUI(quizSummaryResult)
            Log.d(TAG, "Course detail loaded successfully: ${_courseDetail.value}")
            _isLoading.value = false
        }
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
}