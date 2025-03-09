package com.zeppelin.app.screens.courses.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController


class CourseViewModel(private val navController: NavHostController) : ViewModel(){

    fun onProfileClick() {
        // Navigate to profile screen
        navController.navigate("profile")
    }

    fun onCourseClick(courseId: Int) {
        // Navigate to course detail screen
        navController.navigate("courseDetail/$courseId")
    }
}
