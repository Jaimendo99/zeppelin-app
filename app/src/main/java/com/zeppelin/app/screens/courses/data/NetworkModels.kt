package com.zeppelin.app.screens.courses.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class CoursesData(
    val id: Int,
    val subject: String,
    val course: String,
    val progress: Float,
    val imageUrl: String
)

@Serializable
data class Course(
    val id: Int,
    val title: String,
    @SerialName("start_date") val startDate: String, // Consider parsing to a date object if needed
    val description: String,
    val teacher: Teacher,
    @SerialName("modules_summary") val  modulesSummary: ModuleSummary
)

@Serializable
data class Teacher(
    @SerialName("user_id") val userId: String,
    val name: String,
    val lastname: String,
    val email: String
)

@Serializable
data class ModuleSummary(
    @SerialName("num_modules") val moduleSummary: Int,
    @SerialName("last_module_index") val lastModuleIndex: Int,
    @SerialName("last_module_name") val lastModuleName: String
)


@Serializable
data class CourseWithProgress(
    @SerialName("course_id") val courseId : Int,
    @SerialName("start_date") val startDate: String,
    @SerialName("qr_code") val qrCode: String,
    @SerialName("last_module") val lastModule: String,
    @SerialName("module_count") val moduleCount: Int,
    @SerialName("video_count") val videoCount: Int,
    @SerialName("text_count") val textCount: Int,
    @SerialName("quiz_count") val quizCount: Int,
    @SerialName("completion_percentage") val percentage: Float,
    val description: String,
    val title: String,
)