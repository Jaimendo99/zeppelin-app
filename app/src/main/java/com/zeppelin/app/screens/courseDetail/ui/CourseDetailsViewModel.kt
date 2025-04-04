package com.zeppelin.app.screens.courseDetail.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.data.SessionState
import com.zeppelin.app.screens.courseDetail.domain.toCourseDetailUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val TAG = "CourseDetailViewModel"

    fun connectToSessionWithRetry(
        courseId: Int,
        maxRetries: Int = 2,
        initialDelayMillis: Long = 1000L,
        maxDelayMillis: Long = 16000L, // Cap the delay
        factor: Double = 2.0
    ) {
        viewModelScope.launch {
            _sessionState.update {
                it.copy(isSessionLoading = true, isSessionStarted = false, sessionId = "")
            }

            var currentDelay = initialDelayMillis
            for (attempt in 1..maxRetries) {
                courseDetailRepo.connectToSession(courseId)
                    .onSuccess { sessionId ->
                        Log.d(TAG, "Connection successful on attempt $attempt")
                        _sessionState.update {
                            it.copy(
                                sessionId = sessionId,
                                isSessionStarted = true,
                                isSessionLoading = false,
                            )
                        }
                        return@launch
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Connection attempt $attempt failed: ${error.message}")
                        _sessionState.update { it.copy(isSessionStarted = false) }


                        if (attempt == maxRetries) {
                            _sessionState.update { it.copy(isSessionLoading = false) }
                            return@launch
                        }
                        delay(currentDelay)
                        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                    }
            }
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
        val currentSessionId = sessionState.value.sessionId
        if (currentSessionId.isNotEmpty() && sessionState.value.isSessionStarted) {
            viewModelScope.launch {
                _events.emit("courseSession/$currentSessionId")
            }
        } else {
            Log.w(TAG, "onSessionStartClick called but session not ready or ID is empty.")
        }
    }
}