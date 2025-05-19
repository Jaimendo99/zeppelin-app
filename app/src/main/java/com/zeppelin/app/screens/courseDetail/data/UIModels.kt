package com.zeppelin.app.screens.courseDetail.data


data class CourseDetailUI(
    val id: Int = 0,
    val subject: String = "",
    val course: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val grades: List<GradeUI> = listOf(),
    val progress: CourseProgressUI = CourseProgressUI("", 0f, "", 0f)
)

data class CourseProgressUI(
    val contentProgress : String,
    val contentPercentage: Float,
    val testProgress : String,
    val testPercentage: Float
)

data class GradeUI(
    val id: String,
    val gradeName: String,
    val grade: String,
    val dateGraded: String,
)
