package com.zeppelin.app.screens._common.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import com.zeppelin.app.R
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.ui.theme.ZeppelinTheme

@Composable
fun ZeppelinScaffold(
    viewModel: ScaffoldViewModel,
    navController: NavHostController,
    content: @Composable (PaddingValues, NavHostController) -> Unit
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route
    Log.d("Scaffold", "currentRoute: $currentRoute")


    val connected by viewModel.isWatchLinked.collectAsState(false)

    val state = when (currentRoute) {
        Screens.Courses.route,
        Screens.CourseDetail.route,
        Screens.CourseSession.route -> true

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
                connected = connected,
                onParingClick = { viewModel.onParingClicked(navController) })
        },
        floatingActionButton = {
                WatchStatus(
                    connected = connected,
                    onParingClick = { viewModel.onParingClicked(navController) }
                )
        }

    ) { innerPadding ->
        PaddingValues(
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
    connected: Boolean,
    onParingClick: () -> Unit
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
            TopBarBuffer(title, navController, connected, onParingClick)
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
    connected: Boolean = false,
    onParingClick: () -> Unit = {}

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
            IconButton(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .size(32.dp)
                ,
                onClick = {}) {
                AsyncImage(
                    model = "https://images.ctfasseis.net/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=720&h=480&fl=progressive&q=70&fm=jpg",
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(shape = RoundedCornerShape(100)),
                    placeholder = rememberVectorPainter(Icons.Rounded.Face),
                    error = rememberVectorPainter(Icons.Rounded.Face)
                )
            }
        }

    )
}

@Composable
fun WatchStatus(
    modifier: Modifier = Modifier,
    connected: Boolean = false,
    onParingClick: () -> Unit = {}
) {
    IconButton(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        onClick = {
             if (!connected) onParingClick()
             else Log.d("WatchStatus", "Watch is already connected"); onParingClick()
        }
    ) {
        if (connected) {
            Icon(
                painterResource(R.drawable.baseline_watch_24), null,
                tint = MaterialTheme.colorScheme.secondaryContainer
            )

        } else {
            Icon(
                painterResource(R.drawable.baseline_watch_off_24), null,
                tint = MaterialTheme.colorScheme.secondaryContainer
            )

        }
    }
}

@Composable
@Preview
fun WatchStatusPreview() {
    ZeppelinTheme {
        WatchStatus(connected = true, onParingClick = {})
    }
}

@Composable
@Preview
fun WatchStatusPreviewFalse() {
    ZeppelinTheme {
        WatchStatus()
    }
}


