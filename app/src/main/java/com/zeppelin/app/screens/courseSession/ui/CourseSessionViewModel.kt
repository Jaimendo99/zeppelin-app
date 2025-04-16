package com.zeppelin.app.screens.courseSession.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.courseSession.data.ICourseSessionRepo
import com.zeppelin.app.screens.courseSession.data.PomodoroSessionRes
import com.zeppelin.app.screens.courseSession.data.PomodoroSessionUI
import com.zeppelin.app.screens.courseSession.domain.toPomodoroSessionUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseSessionViewModel(
    private val courseSessionRepo: ICourseSessionRepo
) : ViewModel() {

    private val _pomodoroSession = MutableStateFlow<PomodoroSessionUI?>(null)
    val pomodoroSession: StateFlow<PomodoroSessionUI?> = _pomodoroSession

    init {
        getPomodoroSession("123")
    }

    fun getPomodoroSession(sessionId: String) {
        viewModelScope.launch {
            courseSessionRepo.getCourseSessionPomodoro(sessionId).collect {
                _pomodoroSession.value = it.toPomodoroSessionUI()
            }
        }
    }
}