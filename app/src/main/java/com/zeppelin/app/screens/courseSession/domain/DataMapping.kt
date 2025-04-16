package com.zeppelin.app.screens.courseSession.domain

import com.zeppelin.app.screens.courseSession.data.PomodoroSessionRes
import com.zeppelin.app.screens.courseSession.data.PomodoroSessionUI


fun PomodoroSessionRes.toPomodoroSessionUI(): PomodoroSessionUI {
    val timerTime =  startTime + duration - System.currentTimeMillis()
    val minutes = timerTime / 1000 / 60
    val seconds = timerTime / 1000 % 60
    return PomodoroSessionUI(minutes, seconds)
}