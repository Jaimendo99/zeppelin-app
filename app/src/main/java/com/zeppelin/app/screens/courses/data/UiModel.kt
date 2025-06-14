package com.zeppelin.app.screens.courses.data

import android.health.connect.datatypes.units.Percentage

data class CourseCardData(
    val id: Int,
    val subject: String,
    val course: String,
    val progress: String,
    val imageUrl: String
)

data class CourseCardWithProgress(
    val courseId: Int,
    val title: String,
    val startDate: String,
    val description: String,
    val moduleCount: Int,
    val videoCount: Int,
    val textCount: Int,
    val quizCount: Int,
    val lastModule: String,
    val percentage: Float
)