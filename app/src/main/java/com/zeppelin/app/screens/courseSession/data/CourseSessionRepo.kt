package com.zeppelin.app.screens.courseSession.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CourseSessionRepo:ICourseSessionRepo {
    override fun getCourseSessionPomodoro(sessionId: String): Flow<PomodoroSessionRes> {
        val startTime = System.currentTimeMillis() - 1000 * 60
        val duration = 1000 * 60 * 5
        return flowOf(PomodoroSessionRes(startTime, duration.toLong()))
    }
}