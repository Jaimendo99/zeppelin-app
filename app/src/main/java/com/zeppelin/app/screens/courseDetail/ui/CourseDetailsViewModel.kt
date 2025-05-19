package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens._common.data.PomodoroState
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.domain.toCourseDetailUI
import com.zeppelin.app.screens.nav.Screens
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo,
    webSocketClient: WebSocketClient,
    eventsManager: SessionEventsManager
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val pomodoroState: StateFlow<PomodoroState> = eventsManager.pomodoroState

    val webSocketState: StateFlow<WebSocketState> = webSocketClient.state

    private val TAG = "CourseDetailViewModel"

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
                Log.e(TAG, "Error fetching course detail or quiz summary")
                return@launch
            }
            _courseDetail.value = courseDetailResult.toCourseDetailUI(quizSummaryResult)
            Log.d(TAG, "Course detail loaded successfully: ${_courseDetail.value}")
            _isLoading.value = false
        }
    }

    fun onDisconnect() {
        viewModelScope.launch {
            courseDetailRepo.disconnectFromSession()
            _events.emit(Screens.Courses.route)
        }
    }

    fun onSessionStartClick(sessionId: Int) {
        Log.d(TAG, "onSessionStartClick called with sessionId: $sessionId")
        if (webSocketState.value is WebSocketState.Connected) {
            viewModelScope.launch {
                _events.emit("courseSession/$sessionId")
            }
        } else {
            Log.w(TAG, "onSessionStartClick called but session not ready or ID is empty.")
        }
    }
}