package com.zeppelin.app.screens.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.ui.LoginScreen
import com.zeppelin.app.screens.auth.ui.LoginViewModel
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailScreen
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel
import com.zeppelin.app.screens.courseSession.ui.CourseSessionScreen
import com.zeppelin.app.screens.courses.ui.CourseViewModel
import com.zeppelin.app.screens.courses.ui.CoursesScreen
import com.zeppelin.app.screens.profile.ui.ProfileScreen
import com.zeppelin.app.screens.profile.ui.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val authManager: AuthManager = koinInject()
    val isAuthenticated = authManager.observeAuthState().collectAsState(initial = false)

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (isAuthenticated.value) "main" else "auth",
    ) {
        // Auth navigation graph
        navigation(startDestination = Screens.Login.route, route = "auth") {
            composable(Screens.Login.route) {
                LoginScreen(
                    loginViewModel = koinViewModel<LoginViewModel>(),
                    navController = navController
                )
            }
        }

        // Main app navigation graph (authenticated routes)
        navigation(startDestination = Screens.Courses.route, route = "main") {
            composable(Screens.Courses.route) {
                CoursesScreen(courseViewModel = CourseViewModel(navController))
            }

            composable(Screens.CourseDetail.route) { backStackEntry ->
                backStackEntry.arguments?.getString("id")?.let { id ->
                    CourseDetailScreen(
                        id = id, courseViewModel = CourseDetailsViewModel(navController)
                    )
                }
            }

            composable(Screens.Profile.route) {
                ProfileScreen(profileViewModel = ProfileViewModel())
            }
            composable(Screens.CourseSession.route) {
                CourseSessionScreen()
            }
        }
    }
}
