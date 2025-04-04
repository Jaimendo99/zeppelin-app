package com.zeppelin.app.screens.courseSession.data

import kotlinx.coroutines.flow.Flow

interface ICourseSessionRepo {
    fun getCourseSessionPomodoro(sessionId: String): PomodoroSessionRes
}