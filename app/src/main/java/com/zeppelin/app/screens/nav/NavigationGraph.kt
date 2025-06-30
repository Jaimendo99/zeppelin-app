package com.zeppelin.app.screens.nav

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.NavigationCommand
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
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationCommand: NavigationCommand? = null,
    onNavigationHandled: () -> Unit = {},
) {
    /* ----- dependencies & state ----------------------------------------- */
    val authManager: AuthManager = koinInject()
    val isAuthenticated = authManager
        .observeAuthState()
        .collectAsState(initial = false)

    val loginVM: LoginViewModel = koinViewModel()
    val coursesVM: CourseViewModel = koinViewModel()
    val courseDetailsVM: CourseDetailsViewModel = koinViewModel()

    /* ----- UI ----------------------------------------------------------- */
    SharedTransitionLayout {

        /* ------------ main nav graph ----------------------------------- */
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = if (isAuthenticated.value) "main" else "auth"
        ) {

            /* -------- auth graph -------- */
            navigation(
                route = "auth",
                startDestination = Screens.Login.route
            ) {
                composable(Screens.Login.route) {
                    LoginScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        loginViewModel = loginVM,
                        navController = navController
                    )
                }
            }

            /* -------- main graph -------- */
            navigation(
                route = "main",
                startDestination = Screens.Courses.route
            ) {
                composable(Screens.Courses.route) {
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides
                                SharedTransitionScopes(this, this@SharedTransitionLayout)
                    ) {
                        CoursesScreen(
                            courseViewModel = coursesVM,
                            navController = navController
                        )
                    }
                }

                composable(
                    route = Screens.CourseDetail.route,
                    arguments = listOf(
                        navArgument("id") { type = NavType.StringType }
                    )
                ) { entry ->
                    val id = entry.arguments!!.getString("id")!!
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides
                                SharedTransitionScopes(this, this@SharedTransitionLayout)
                    ) {
                        CourseDetailScreen(
                            id = id,
                            courseViewModel = courseDetailsVM,
                            navController = navController
                        )
                    }
                }

                composable(Screens.WatchLink.route) {
                    PairingScreen(modifier = Modifier.padding(top = 8.dp))
                }

                /* -------- single session screen (no extra graph needed) ------ */
                composable(
                    route = Screens.CourseSession.route,  // courseSession/{sessionId}
                    arguments = listOf(
                        navArgument("sessionId") { type = NavType.StringType }
                    )
                ) { entry ->
                    val sessionId = entry.arguments!!.getString("sessionId")!!
                    CompositionLocalProvider(
                        LocalSharedTransitionScopes provides
                                SharedTransitionScopes(this, this@SharedTransitionLayout)
                    ) {
                        CourseSessionScreen(
                            modifier = Modifier,
                            id = sessionId,
                            navController = navController,
                            courseViewModel = courseDetailsVM
                        )
                    }
                }
            }
        }

        /* ------------ live / deep-link navigation ----------------------- */
        LaunchedEffect(navigationCommand) {
            navigationCommand?.let { cmd ->
                val targetRoute = Screens.CourseSession.build(cmd.id)
                Log.d("NavigationGraph", "Live navigation → $targetRoute")

                if (navController.currentDestination?.route != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(Screens.Courses.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                onNavigationHandled()
            }
        }
    }
}

