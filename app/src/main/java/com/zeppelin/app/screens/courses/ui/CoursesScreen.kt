package com.zeppelin.app.screens.courses.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun CoursesScreen(modifier: Modifier = Modifier, courseViewModel: CourseViewModel) {
    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = { courseViewModel.onProfileClick() }) {
            Text("Profile Screen")
        }
        Button(onClick = { courseViewModel.onCourseClick(1) }) {
            Text("Course Screen 1")
        }
    }

}