package com.zeppelin.app.screens._common.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import com.zeppelin.app.screens.nav.Screens

@Composable
fun ZeppelinScaffold(
    viewModel: ScaffoldViewModel,
    navController: NavHostController,
    content: @Composable (PaddingValues, NavHostController) -> Unit
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route
    Log.d("Scaffold", "currentRoute: $currentRoute")

    val state = when (currentRoute) {
        Screens.Courses.route,
        Screens.CourseDetail.route,
        Screens.CourseSession.route -> true
        null -> false
        else -> false
    }

    Scaffold(
        // Set the content padding to account for system bars content
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ZeppelinTopBar(
                screen = currentRoute,
                state = state,
                navController = navController,
                onProfileLongPressed = { viewModel.onProfileLongPressed() })
        },
    ) { innerPadding ->
        val padding = PaddingValues(
            8.dp,
            innerPadding.calculateTopPadding(),
            8.dp,
            innerPadding.calculateBottomPadding()
        )
        content(innerPadding, navController)
    }
}

@Composable
fun ZeppelinTopBar(
    screen: String?,
    state: Boolean,
    navController: NavHostController,
    onProfileLongPressed: () -> Unit
) {

    AnimatedContent(
        targetState =
        state,
        label = "topBarAnimation",
    ) { targetScreen ->
        if (targetScreen) {
            val title = when (screen) {
                Screens.Courses.route -> Screens.Courses.title
                Screens.CourseDetail.route -> Screens.CourseDetail.title
                Screens.CourseSession.route -> Screens.CourseSession.title
                else -> ""
            }
            TopBarBuffer(title, navController, onProfileLongPressed)
        } else {
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBarBuffer(
    title: String,
    navController: NavHostController,
    onProfileLongPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Logo(
                modifier = Modifier
                    .clickable(onClick = {
                        navController.navigate(
                            Screens.Courses.route
                        ) {
                            launchSingleTop = true
                            popUpTo(Screens.Courses.route) { inclusive = true }
                        }
                    })
                    .size(100.dp)
            )
        },
        navigationIcon = {
            if (title != Screens.Courses.title && title != Screens.Login.title && title != Screens.Profile.title) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate(Screens.Profile.route) }) {
                AsyncImage(
                    model = "https://images.ctfassets.net/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=720&h=480&fl=progressive&q=70&fm=jpg",
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(shape = RoundedCornerShape(100)),
                    placeholder = rememberVectorPainter(Icons.Rounded.Face)
                )
            }
        })
}

