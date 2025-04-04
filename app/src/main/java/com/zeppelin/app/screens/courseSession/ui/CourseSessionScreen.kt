package com.zeppelin.app.screens.courseSession.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CourseSessionScreen(modifier: Modifier = Modifier, sessionId: String?) {
    // Course session screen
    Row(modifier = modifier.fillMaxSize()) {
        Text("Course Session Screen")
    }
}

