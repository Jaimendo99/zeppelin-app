package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CourseDetailScreen(modifier: Modifier = Modifier, id:String, courseViewModel: CourseDetailsViewModel) {
    Row(modifier = modifier) {
        Button(onClick = { courseViewModel.startSession() }) {
            Text("Start Session for course $id")
        }
    }
}
