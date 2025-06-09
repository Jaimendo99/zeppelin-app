package com.zeppelin.app.screens.courseDetail.data

import android.content.Intent
import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
import com.zeppelin.app.service.LiveSessionService
import io.ktor.client.request.get
import io.ktor.http.path

class CourseDetailRepo(
    private val context: android.content.Context,
    private val apiClient: ApiClient,
) : ICourseDetailRepo {
    override suspend fun getCourseDetail(id: Int): Result<CourseDetail> {
        val result = apiClient.getCourseDetail(id)
        return when (result) {
            is NetworkResult.Success<CourseDetail> -> { Result.success(result.data) }
            is NetworkResult.Error<*> -> { Result.failure(Exception(result.errorMessage)) }
            else -> { Result.failure(Exception("Unknown error")) }
        }
    }

    override suspend fun getQuizAnswers(): Result<List<QuizSummary>> {
        val result = apiClient.getQuizAnswers()
        return when (result) {
            is NetworkResult.Success<List<QuizSummary>> -> { Result.success(result.data) }
            is NetworkResult.Error<*> -> { Result.failure(Exception(result.errorMessage)) }
            else -> { Result.failure(Exception("Unknown error")) }
        }
    }

    override suspend fun connectToSession(courseId: Int, retry: Boolean): Result<WebSocketState> {
        try {
            Intent(
                context,
                LiveSessionService::class.java
            ).apply {
                action = LiveSessionService.Action.START.name
                putExtra("courseId", courseId)
                putExtra("retry", retry)
            }.also {
                context.startForegroundService(it)
            }

        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(WebSocketState.Connected(courseId))
    }

    override fun disconnectFromSession() {
        Intent(
            context,
            LiveSessionService::class.java
        ).apply {
            action = LiveSessionService.Action.STOP.name
        }.also {
            context.startForegroundService(it)
        }
    }
}

suspend fun ApiClient.getCourseDetail(id: Int): NetworkResult<CourseDetail, ErrorResponse> {
    return safeApiCall { client.get { url { path("course", "$id") } } }
}
suspend fun ApiClient.getQuizAnswers(): NetworkResult<List<QuizSummary>, ErrorResponse> {
    return safeApiCall { client.get { url { path("quiz", "answers") } } }
}

val imageUrls = listOf(
    "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=80&w=720&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1633493702341-4d04841df53b?q=80&w=720&auto=format&fit=crop",
    "https://plus.unsplash.com/premium_photo-1661430659143-ffbb5ce2b6a7?q=80&w=720&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1561323578-dde5e688b4b7?q=80&w=720&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1564866657311-eefb86a2e568?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1504198458649-3128b932f49e?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
)

val courseDetailList = listOf(
    CourseDetailApi(
        id = 8,
        subject = "Matemáticas",
        course = "Algebra",
        description = "asdasd",
        imageUrl ="" ,
        grades = listOf(
            GradeApi(
                id = "1-1",
                gradeName = "Quiz 1",
                gradeValue = 78,
                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
            ),
            GradeApi(
                id = "1-2",
                gradeName = "Quiz 2",
                gradeValue = 85,
                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
            )
        ),
        progress = CourseProgressApi(
            fullContent = 10,
            viewedContent = 5,
            fullTests = 4,
            passedTests = 2
        )
    ),
//    CourseDetailApi(
//        id = 2,
//        subject = "Math",
//        course = "Calculus",
//        description =
//        "Explore derivatives, integrals, and limits in this comprehensive " +
//                "Calculus course, ideal for both beginners and advanced learners.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=80&w=720&auto=format&fit=crop",
//        grades = listOf(
//            GradeApi(
//                id = "2-1",
//                gradeName = "Quiz 1",
//                gradeValue = 82,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "2-2",
//                gradeName = "Assignment 1",
//                gradeValue = 90,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            ),
//            GradeApi(
//                id = "2-3",
//                gradeName = "Quiz 2",
//                gradeValue = 88,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 3
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 8,
//            fullTests = 4,
//            passedTests = 3
//        )
//    ),
//    CourseDetailApi(
//        id = 3,
//        subject = "Science",
//        course = "Physics",
//        description =
//        "Learn the fundamentals of physics—from classical mechanics to " +
//                "modern theories—by exploring key concepts and experiments.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1633493702341-4d04841df53b?q=80&w=720&auto=format&fit=crop",
//        grades = listOf(
//            GradeApi(
//                id = "3-1",
//                gradeName = "Quiz 1",
//                gradeValue = 70,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "3-2",
//                gradeName = "Assignment 1",
//                gradeValue = 75,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 3,
//            fullTests = 4,
//            passedTests = 1
//        )
//    ),
//    CourseDetailApi(
//        id = 4,
//        subject = "Science",
//        course = "Chemistry",
//        description =
//        "Delve into chemical reactions, periodic trends, and laboratory " +
//                "techniques in our comprehensive Chemistry course.",
//        imageUrl =
//        "https://plus.unsplash.com/premium_photo-1661430659143-ffbb5ce2b6a7?q=80&w=720&auto=format&fit=crop",
//        grades = listOf(
//            GradeApi(
//                id = "4-1",
//                gradeName = "Quiz 1",
//                gradeValue = 95,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "4-2",
//                gradeName = "Assignment 1",
//                gradeValue = 100,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 10,
//            fullTests = 4,
//            passedTests = 4
//        )
//    ),
//    CourseDetailApi(
//        id = 5,
//        subject = "History",
//        course = "World War II",
//        description =
//        "Gain an in-depth understanding of the events, strategies, and global " +
//                "impacts of World War II.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1561323578-dde5e688b4b7?q=80&w=720&auto=format&fit=crop",
//        grades = listOf(
//            GradeApi(
//                id = "5-1",
//                gradeName = "Quiz 1",
//                gradeValue = 65,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "5-2",
//                gradeName = "Assignment 1",
//                gradeValue = 70,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 4,
//            fullTests = 4,
//            passedTests = 1
//        )
//    ),
//    CourseDetailApi(
//        id = 6,
//        subject = "History",
//        course = "Ancient Rome",
//        description =
//        "Explore the fascinating history, politics, and culture of Ancient Rome " +
//                "through engaging lessons and primary sources.",
//        imageUrl =
//        "https://images.unsplash.com/reserve/unsplash_52d3d6f9853e9_1.JPG?q=80&w=3263&auto=format&fit=crop",
//        grades = listOf(
//            GradeApi(
//                id = "6-1",
//                gradeName = "Quiz 1",
//                gradeValue = 88,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "6-2",
//                gradeName = "Assignment 1",
//                gradeValue = 92,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 9,
//            fullTests = 4,
//            passedTests = 4
//        )
//    ),
//    CourseDetailApi(
//        id = 7,
//        subject = "English",
//        course = "Grammar",
//        description =
//        "Improve your language skills by mastering English grammar rules, " +
//                "syntax, and punctuation.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1564866657311-eefb86a2e568?auto=format&fit=crop&w=800&q=80",
//        grades = listOf(
//            GradeApi(
//                id = "7-1",
//                gradeName = "Quiz 1",
//                gradeValue = 60,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "7-2",
//                gradeName = "Assignment 1",
//                gradeValue = 65,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 2,
//            fullTests = 4,
//            passedTests = 0
//        )
//    ),
//    CourseDetailApi(
//        id = 11,
//        subject = "English",
//        course = "Literature",
//        description =
//        "Discover classic and contemporary works, enhancing your " +
//                "analytical and interpretative skills along the way.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80",
//        grades = listOf(
//            GradeApi(
//                id = "8-1",
//                gradeName = "Quiz 1",
//                gradeValue = 75,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "8-2",
//                gradeName = "Assignment 1",
//                gradeValue = 80,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 6,
//            fullTests = 4,
//            passedTests = 2
//        )
//    ),
//    CourseDetailApi(
//        id = 9,
//        subject = "Art",
//        course
//        = "Painting",
//        description =
//        "Develop artistic techniques and explore famous painting styles " +
//                "while getting inspired by art history.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1504198458649-3128b932f49e?auto=format&fit=crop&w=800&q=80",
//        grades = listOf(
//            GradeApi(
//                id = "9-1",
//                gradeName = "Quiz 1",
//                gradeValue = 85,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "9-2",
//                gradeName = "Assignment 1",
//                gradeValue = 88,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 8,
//            fullTests = 4,
//            passedTests = 3
//        )
//    ),
//    CourseDetailApi(
//        id = 10,
//        subject = "Art",
//        course = "Sculpture",
//        description =
//        "Learn about the art of sculpture, exploring both classical " +
//                "techniques and modern innovations in form and material.",
//        imageUrl =
//        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80",
//        grades = listOf(
//            GradeApi(
//                id = "10-1",
//                gradeName = "Quiz 1",
//                gradeValue = 70,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24
//            ),
//            GradeApi(
//                id = "10-2",
//                gradeName = "Assignment 1",
//                gradeValue = 75,
//                dateGraded = System.currentTimeMillis() - 3600L * 1000 * 24 * 2
//            )
//        ),
//        progress = CourseProgressApi(
//            fullContent = 10,
//            viewedContent = 4,
//            fullTests = 4,
//            passedTests = 1
//        )
//    )
)

