package com.zeppelin.app.screens.courseDetail.data


data class CourseDetailApi(
    val id: Int,
    val course: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val grades: List<GradeApi>,
    val progress: CourseProgressApi,
)

data class CourseProgressApi(
    val fullContent: Int,
    val viewedContent: Int,
    val fullTests: Int,
    val passedTests: Int,
)

data class GradeApi(
    val id: String,
    val gradeName: String,
    val gradeValue: Int,
    val dateGraded: Long,
)