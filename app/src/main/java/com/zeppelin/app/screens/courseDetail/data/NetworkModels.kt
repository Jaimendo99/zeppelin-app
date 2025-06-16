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



@Serializable
data class CourseDetailWithModules(
    @SerialName("user_id") val userId: String,
    @SerialName("course_id") val courseId: Int,
    @SerialName("course_info") val courseInfo: CourseInfo,
    val modules: List<ModuleWithContents>
){
    @Serializable
    data class CourseInfo(
        val title: String,
        @SerialName("qr_code") val qrCode: String,
        @SerialName("course_id") val courseId: Int,
        @SerialName("start_date") val startDate: String,
        @SerialName("teacher_id") val teacherId: String,
        val description: String
    )

    @Serializable
    data class ModuleWithContents(
        val module: String,
        val contents: List<ContentItemInfo>,
        @SerialName("module_index") val moduleIndex: Int,
        @SerialName("course_content_id") val courseContentId: Int
    )

    @Serializable
    data class ContentItemInfo(
        val url: String? = null,
        val title: String? = null,
        val status: ContentStatus,
        @SerialName("is_active") val isActive: Boolean? = null,
        @SerialName("content_id") val contentId: String? = null,
        val description: String? = null,
        @SerialName("content_type") val contentType: ContentType? = null,
        @SerialName("section_index") val sectionIndex: Int? = null
    )

    enum class ContentStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    enum class ContentType {
        VIDEO, TEXT, QUIZ
    }
}

@Serializable
data class QuizAnswer(
    @SerialName("quiz_answer_id") val quizAnswerId: Int,
    @SerialName("content_id") val contentId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("grade") val grade: Float,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("total_points") val totalPoints: Int,
    @SerialName("quiz_title") val quizTitle: String,
    @SerialName("quiz_description") val quizDescription: String,
    @SerialName("course_content_id") val courseContentId: Int,
    @SerialName("course_id") val courseId: Int,
    @SerialName("module") val module: String,
    @SerialName("module_index") val moduleIndex: Int,
    @SerialName("course_title") val courseTitle: String,
    @SerialName("course_description") val courseDescription: String,
    @SerialName("teacher_id") val teacherId: String,
    @SerialName("total_quizzes") val totalQuizzes: Int,
    @SerialName("needs_review") val needsReview: Boolean
)