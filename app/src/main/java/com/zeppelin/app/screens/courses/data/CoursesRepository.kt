package com.zeppelin.app.screens.courses.data

import kotlinx.coroutines.delay

class CoursesRepository : ICoursesRepository {
    override suspend fun getCourses(): List<CoursesData> {
        delay(500)
        return listOf(
            CoursesData(
                1,
                "Math",
                "Algebra",
                0.5f,
                "https://images.unsplash.com/photo-1509228468518-180dd4864904?q=80&w=720&auto=format&fit=crop"
            ),
            CoursesData(
                2,
                "Math",
                "Calculus",
                0.75f,
                "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=80&w=720&auto=format&fit=crop"
            ),
            CoursesData(
                3,
                "Science",
                "Physics",
                0.25f,
                "https://images.unsplash.com/photo-1633493702341-4d04841df53b?q=80&w=720&auto=format&fit=crop"
            ),
            CoursesData(
                4,
                "Science",
                "Chemistry",
                1f,
                "https://plus.unsplash.com/premium_photo-1661430659143-ffbb5ce2b6a7?q=80&w=720&auto=format&fit=crop"
            ),
            CoursesData(
                5,
                "History",
                "World War II",
                0.35f,
                "https://images.unsplash.com/photo-1561323578-dde5e688b4b7?q=80&w=720&auto=format&fit=crop"
            ),
            CoursesData(
                6,
                "History",
                "Ancient Rome",
                0.9f,
                "https://images.unsplash.com/reserve/unsplash_52d3d6f9853e9_1.JPG?q=80&w=3263&auto=format&fit=crop"
            ),
            CoursesData(
                7,
                "English",
                "Grammar",
                0.2f,
                "https://images.unsplash.com/photo-1564866657311-eefb86a2e568?auto=format&fit=crop&w=800&q=80"
            ),
            CoursesData(
                8,
                "English",
                "Literature",
                0.6f,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80"
            ),
            CoursesData(
                9,
                "Art",
                "Painting",
                0.8f,
                "https://images.unsplash.com/photo-1504198458649-3128b932f49e?auto=format&fit=crop&w=800&q=80"
            ),
            CoursesData(
                10,
                "Art",
                "Sculpture",
                0.42f,
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
            )
        )
    }

}