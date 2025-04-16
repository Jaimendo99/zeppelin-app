package com.zeppelin.app.screens.courseSession.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel

@Composable
fun CourseSessionScreen(modifier: Modifier = Modifier, sessionId: String?, courseViewModel: CourseSessionViewModel) {
    // Course session screen
    val pomodoroSession = courseViewModel.pomodoroSession.collectAsState().value
    Column {
        Text("Course Session Screen with sessionId: $sessionId")
        Row {
            pomodoroSession?.let { pomodoroSession ->
                Text("minutes: ${pomodoroSession.minutes}")
                Text("seconds: ${pomodoroSession.seconds}")
            }
        }
    }
}

