package com.zeppelin.app.screens.courseDetail.ui


import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens._common.ui.ButtonWithLoader
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.CourseProgressUI
import com.zeppelin.app.screens.courseDetail.data.GradeUI
import com.zeppelin.app.screens.courseDetail.data.SessionState
import com.zeppelin.app.screens.courseDetail.domain.calculateInitialRadius
import com.zeppelin.app.screens.courseDetail.domain.calculateMaxRadius
import com.zeppelin.app.ui.theme.ZeppelinTheme
import kotlin.math.hypot
import kotlin.math.max


@Composable
fun CourseDetailScreen(
    modifier: Modifier = Modifier,
    id: String,
    courseViewModel: CourseDetailsViewModel,
    navController: NavController,
) {
    val courseDetail = courseViewModel.courseDetail.collectAsState().value
    val loading = courseViewModel.isLoading.collectAsState().value
    val sessionState = courseViewModel.sessionState.collectAsState().value

    LaunchedEffect(Unit) {
        courseViewModel.events.collect { event ->
            navController.navigate(event) {
            }
        }
    }

    LaunchedEffect(key1 = "connection/$id") { courseViewModel.connectToSessionWithRetry(id.toInt()) }
    LaunchedEffect(key1 = "data/$id") { courseViewModel.getCourseDetail(id.toInt()) }

    AnimatedContent(
        targetState = loading,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "CourseDetailLoading"
    ) { isLoading ->
        val currentScopes = LocalSharedTransitionScopes.current
        CompositionLocalProvider(LocalSharedTransitionScopes provides currentScopes) {
            if (!isLoading) {
                if (courseDetail != null) {
                    CourseScreenLayout(
                        modifier = modifier,
                        courseDetailUI = courseDetail,
                        onSessionStartNavigation = { courseViewModel.onSessionStartClick() },
                        isLoading = false,
                        sessionState = sessionState,
                        onRetryConnection = { courseViewModel.connectToSessionWithRetry(id.toInt()) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading course detail")
                    }
                }
            } else {
                CourseScreenLayout(
                    modifier = modifier,
                    isLoading = true,
                    courseDetailUI = CourseDetailUI(), // Example empty data
                    sessionState = SessionState(),
                )
            }
        }
    }
}







@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CourseScreenLayout(
    modifier: Modifier = Modifier,
    courseDetailUI: CourseDetailUI = CourseDetailUI(),
    onSessionStartNavigation: () -> Unit = {},
    isLoading: Boolean = false,
    sessionState: SessionState,
    onRetryConnection: () -> Unit = {},
) {
    val transScope = LocalSharedTransitionScopes.current
    val animationColor = MaterialTheme.colorScheme.primary

    var isAnimating by remember { mutableStateOf(false) }
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    var layoutSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val maxRadius = remember(buttonCenter, layoutSize) {
        calculateMaxRadius(buttonCenter, layoutSize, screenWidthPx, screenHeightPx)
    }

    val initialRadius = remember(buttonSize) {
        calculateInitialRadius(buttonSize)
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        finishedListener = { finalValue ->
            if (finalValue == 1f) {
                onSessionStartNavigation()
            }
        },
        label = "CircleExpandProgress"
    )

    val currentRadius = initialRadius + (maxRadius - initialRadius) * animationProgress
    val showOverlay = isAnimating || animationProgress > 0f

    val startAnimationLambda =
        remember(sessionState.isSessionLoading, sessionState.isSessionStarted) {
            {
                if (!sessionState.isSessionLoading && sessionState.isSessionStarted) {
                    isAnimating = true
                }
            }
        }

    val buttonPositionedLambda = remember {
        { position: Offset, size: IntSize ->
            buttonCenter = Offset(
                position.x + size.width / 2f,
                position.y + size.height / 2f
            )
            buttonSize = size
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                layoutSize = layoutCoordinates.size
            }
    ) {
        CourseContent(
            courseDetailUI = courseDetailUI,
            isLoading = isLoading,
            sessionState = sessionState,
            onRetryConnection = onRetryConnection,
            onLongPressStartAnimation = startAnimationLambda,
            onButtonPositioned = buttonPositionedLambda,
            stsps = transScope,
        )

        ExpandingCircleOverlay(
            isVisible = showOverlay,
            color = animationColor,
            radius = currentRadius,
            center = buttonCenter
        )
    }
}





@Composable
@Preview
fun CourseDetailScreenPreview() {
    ZeppelinTheme {
        val date = "12/12/2021"
        CourseScreenLayout(
            sessionState = SessionState(),
            isLoading = false,
            courseDetailUI = CourseDetailUI(
                id = 1,
                subject = "Matemáticas",
                course = "Matrices #1",
                description = "Este curso es sobre el tema 2 del libro donde se habla de las matrices y como hacer opraciones aritmetricas",
                imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?q=80&w=720&auto=format&fit=crop",
                grades = listOf(
                    GradeUI(
                        id = "1",
                        gradeName = "Tarea 1",
                        grade = "10",
                        dateGraded = date
                    ),
                    GradeUI(
                        id = "6",
                        gradeName = "Tarea 6",
                        grade = "5",
                        dateGraded = date
                    ),
                    GradeUI(
                        id = "7",
                        gradeName = "Tarea 7",
                        grade = "4",
                        dateGraded = date
                    ),
                    GradeUI(
                        id = "8",
                        gradeName = "Tarea 8",
                        grade = "3",
                        dateGraded = date
                    ),
                    GradeUI(
                        id = "9",
                        gradeName = "Tarea 9",
                        grade = "2",
                        dateGraded = date
                    )
                ),
                progress = CourseProgressUI(
                    contentProgress = "10/10",
                    contentPercentage = 1f,
                    testProgress = "5/10",
                    testPercentage = 0.5f
                )
            ),
        )
    }
}

