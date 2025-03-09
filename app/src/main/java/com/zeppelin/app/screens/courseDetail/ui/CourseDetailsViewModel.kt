package com.zeppelin.app.screens.courseDetail.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController

class CourseDetailsViewModel(private val navController: NavHostController) :ViewModel() {
    fun startSession() {
        // Start session
        navController.navigate("courseSession")
    }
}