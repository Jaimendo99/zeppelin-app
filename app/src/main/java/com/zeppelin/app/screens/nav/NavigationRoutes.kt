package com.zeppelin.app.screens.nav


sealed class Screens(val route: String, val title: String) {
    data object Login : Screens("login", "Login")
    data object Courses : Screens("courses", "Courses")
    data object Profile : Screens("profile", "Profile")
    data object CourseDetail : Screens("courseDetail/{id}", "Course Detail")
    data object CourseSession : Screens("courseSession/{sessionId}", "Course Session"){
        fun build(sessionId: String): String = "courseSession/$sessionId"
    }
    data object WatchLink : Screens("watchLink", "Watch Link")
}
