package com.zeppelin.app.screens.courseDetail.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.security.auth.Subject


data class CourseDetailApi(
    val id: Int,
    val course: String,
    val subject: String,
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


@Serializable
data class CourseDetail(
    val id: Int,
    val title: String,
    @SerialName("start_date") val startDate: String,
    val description: String,
    val teacher: Teacher,
    val modules: List<Module>
)

@Serializable
data class Teacher(
    @SerialName("user_id") val userId: String,
    val name: String,
    val lastname: String,
    val email: String
)

@Serializable
data class Module(
    @SerialName("module_id") val moduleId: Int,
    @SerialName("module_name") val moduleName: String,
    @SerialName("module_index") val moduleIndex: Int,
    val content: List<ContentItem>
)

@Serializable
data class ContentItem(
    @SerialName("content_id") val contentId: String,
    @SerialName("content_type_id") val contentTypeId: Int,
    val title: String,
    val description: String,
    val url: String,
    @SerialName("section_index") val sectionIndex: Int
)

@Serializable
data class QuizSummary(
    @SerialName("content_id") val contentID: String,
    @SerialName("total_points")val totalPoints: Int,
    @SerialName("total_grade") val totalGrade: Float,
    @SerialName("last_quiz_time") val lastQuizTime:Long
)

enum class ContentType{
    VIDEO, TEXT, QUIZ,
}