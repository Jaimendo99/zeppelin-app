package com.zeppelin.app.screens.courseSession.ui

import androidx.lifecycle.ViewModel
import com.zeppelin.app.screens._common.data.PomodoroState
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseSession.data.ICourseSessionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.android.inject

class CourseSessionViewModel(
    private val courseSessionRepo: ICourseSessionRepo,
    eventsManager: SessionEventsManager
) : ViewModel() {

    private val _courseDetail = MutableStateFlow(CourseDetailUI())
    val courseDetail: StateFlow<CourseDetailUI> = _courseDetail

    val pomodoroState: StateFlow<PomodoroState> = eventsManager.pomodoroState

}