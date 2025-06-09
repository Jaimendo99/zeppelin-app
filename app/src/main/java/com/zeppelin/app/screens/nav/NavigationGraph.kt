package com.zeppelin.app.screens.nav

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.ui.LoginScreen
import com.zeppelin.app.screens.auth.ui.LoginViewModel
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailScreen
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel
import com.zeppelin.app.screens.courseSession.ui.CourseSessionScreen
import com.zeppelin.app.screens.courses.ui.CourseViewModel
import com.zeppelin.app.screens.courses.ui.CoursesScreen
import com.zeppelin.app.screens.watchLink.ui.PairingScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val authManager: AuthManager = koinInject()
    val isAuthenticated = authManager.observeAuthState().collectAsState(initial = false)


    SharedTransitionLayout {

        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = if (isAuthenticated.value) "main" else "auth",
        ) {
            navigation(startDestination = Screens.Login.route, route = "auth") {
                composable(Screens.Login.route) {

                    LoginScreen(
                        Modifier
                            .fillMaxSize()
                            .imePadding(),
                        koinViewModel<LoginViewModel>(),
                        navController
                    )

                }
            }

            navigation(startDestination = Screens.Courses.route, route = "main") {
                composable(Screens.Courses.route) {
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides SharedTransitionScopes(
                            this, this@SharedTransitionLayout
                        )
                    ) {
                        CoursesScreen(
                            courseViewModel = koinViewModel<CourseViewModel>(),
                            navController = navController,
                        )
                    }
                }
            }


            composable(Screens.CourseDetail.route) { backStackEntry ->
                backStackEntry.arguments?.getString("id")?.let { id ->
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides SharedTransitionScopes(
                            this, this@SharedTransitionLayout
                        )
                    ) {
                        CourseDetailScreen(
                            id = id,
                            courseViewModel = koinViewModel<CourseDetailsViewModel>(),
                            navController = navController,
                        )
                    }
                }
            }

            composable(Screens.CourseSession.route) { backStackEntry ->
                Log.d("CourseSession", "ID: ${backStackEntry.arguments?.getString("id")}")
                backStackEntry.arguments?.getString("sessionId")?.let { id ->
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides SharedTransitionScopes(
                            this, this@SharedTransitionLayout
                        )
                    ) {
                        CourseSessionScreen(
                            modifier = Modifier,
                            id = id,
                            navController = navController,
                            courseViewModel = koinViewModel<CourseDetailsViewModel>(),
                        )
                    }
                }
            }
            composable(Screens.WatchLink.route) {
                PairingScreen(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

