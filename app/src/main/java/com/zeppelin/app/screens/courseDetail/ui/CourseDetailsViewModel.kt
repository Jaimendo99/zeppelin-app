package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.domain.toCourseDetailUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo,
    webSocketClient: WebSocketClient,
    private val sessionEventsManager: SessionEventsManager
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val webSocketState: StateFlow<WebSocketState> = webSocketClient.state

    private val TAG = "CourseDetailViewModel"

    init {
        viewModelScope.launch {
            sessionEventsManager.observeEvents({ courseDetailRepo.disconnectFromSession() })
        }
        viewModelScope.launch {
            webSocketClient.state.collect { state ->
                when (state) {
                    is WebSocketState.Connected -> { Log.d(TAG, "WebSocket connected ${state.lastCourseId}") }
                    is WebSocketState.Disconnected -> { Log.d(TAG, "WebSocket disconnected") }
                    is WebSocketState.Error -> { Log.d(TAG, "WebSocket error: ${state.message}") }
                    WebSocketState.Connecting -> { Log.d(TAG, "WebSocket connecting") }
                    WebSocketState.Idle -> { Log.d(TAG, "WebSocket idle") }
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
            try {
                courseDetailRepo.getCourseDetail(id)?.let {
                    _courseDetail.value = it.toCourseDetailUI()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSessionStartClick() {
        val currentSessionId = webSocketState.value
        if (webSocketState.value is WebSocketState.Connected) {
            viewModelScope.launch {
                _events.emit("courseSession/$currentSessionId")
            }
        } else {
            Log.w(TAG, "onSessionStartClick called but session not ready or ID is empty.")
        }
    }
}