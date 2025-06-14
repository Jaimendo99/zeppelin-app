package com.zeppelin.app.screens.courses.data

import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens._common.data.RestClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
import io.ktor.client.request.get
import io.ktor.http.path

class CoursesRepository(
    private val apiClient: ApiClient,
    private val restClient: RestClient,
    private val authPreferences: AuthPreferences
) : ICoursesRepository {


    override suspend fun getCourses(): Result<List<CoursesData>> {
        val result = apiClient.getCourses()
        return when (result) {
            is NetworkResult.Success -> {
                Result.success(result.data.map { it.toDomain() })
            }

            is NetworkResult.Error -> {
                Result.failure(Exception(result.errorBody?.message))
            }

            else -> {
                Result.failure(Exception("Unknown error"))
            }
        }
    }

    override suspend fun getCoursesWithProgress(): NetworkResult<List<CourseWithProgress>, RestClient.ErrorResponse> {
        val userId = authPreferences.getUserIdOnce() ?: return NetworkResult.Error(
            errorBody = RestClient.ErrorResponse(
                code = "NoUserId",
                message = "User ID not found in preferences"
            )
        )
        return restClient.getCourses(userId)
    }

    /*
    return listOf(
        CoursesData(
            8,
            "Matemáticas",
            "Algebra",
            0.3f,
            "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=80&w=720&auto=format&fit=crop"
        ),
        CoursesData(
            2,
            "Sociales",
            "WWII",
            0.75f,
            "https://images.unsplash.com/photo-1561323578-dde5e688b4b7?q=80&w=720&auto=format&fit=crop"
        ),
//            CoursesData(
//                3,
//                "Science",
//                "Physics",
//                0.25f,
//                "https://images.unsplash.com/photo-1633493702341-4d04841df53b?q=80&w=720&auto=format&fit=crop"
//            ),
//            CoursesData(
//                4,
//                "Science",
//                "Chemistry",
//                1f,
//                "https://plus.unsplash.com/premium_photo-1661430659143-ffbb5ce2b6a7?q=80&w=720&auto=format&fit=crop"
//            ),
//            CoursesData(
//                5,
//                "History",
//                "World War II",
//                0.35f,
//                "https://images.unsplash.com/photo-1561323578-dde5e688b4b7?q=80&w=720&auto=format&fit=crop"
//            ),
//            CoursesData(
//                6,
//                "History",
//                "Ancient Rome",
//                0.9f,
//                "https://images.unsplash.com/reserve/unsplash_52d3d6f9853e9_1.JPG?q=80&w=3263&auto=format&fit=crop"
//            ),
//            CoursesData(
//                7,
//                "English",
//                "Grammar",
//                0.2f,
//                "https://images.unsplash.com/photo-1564866657311-eefb86a2e568?auto=format&fit=crop&w=800&q=80"
//            ),
//            CoursesData(
//                11,
//                "English",
//                "Literature",
//                0.6f,
//                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80"
//            ),
//            CoursesData(
//                9,
//                "Art",
//                "Painting",
//                0.8f,
//                "https://images.unsplash.com/photo-1504198458649-3128b932f49e?auto=format&fit=crop&w=800&q=80"
//            ),
//            CoursesData(
//                10,
//                "Art",
//                "Sculpture",
//                0.42f,
//                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
//            )
    )
}
     */
    private fun Course.toDomain(): CoursesData {
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
        return CoursesData(
            id = this.id,
            subject = this.modulesSummary.lastModuleName,
            course = this.title,
            progress = this.modulesSummary.lastModuleIndex.toFloat() / this.modulesSummary.moduleSummary,
            imageUrl = imageUrls.random()
        )
    }
}

suspend fun RestClient.getCourses(userId: String): NetworkResult<List<CourseWithProgress>, RestClient.ErrorResponse> {
    return safeApiCall {
        client.get {
            url {
                path("course_list_student")
                parameters.append("user_id", "eq.$userId")
            }
        }
    }
}

suspend fun ApiClient.getCourses(): NetworkResult<List<Course>, ErrorResponse> {
    return safeApiCall { client.get { url { path("v2", "courses", "student") } } }
}

