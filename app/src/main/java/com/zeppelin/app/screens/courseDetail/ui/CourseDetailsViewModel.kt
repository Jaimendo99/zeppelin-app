package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val webSocketClient: WebSocketClient
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val webSocketState: StateFlow<WebSocketState> = webSocketClient.state
    private val lastCourseId: StateFlow<Int> = webSocketClient.lastCourseId

    private val TAG = "CourseDetailViewModel"

    fun startSession(courseId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "Starting session for course ID: $courseId")
            if (courseId != lastCourseId.value && lastCourseId.value != -1)
               webSocketClient.setConnectionState(WebSocketState.Idle)
            else courseDetailRepo.connectToSession(courseId)
        }
    }

    fun retryConnection(courseId: Int) {
        viewModelScope.launch {
            courseDetailRepo.disconnectFromSession()
            delay(1000)
            courseDetailRepo.connectToSession(courseId )
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