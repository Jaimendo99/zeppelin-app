package com.zeppelin.app.screens.courseDetail.ui


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUIState
import com.zeppelin.app.screens.courseDetail.data.ModuleListUI
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUi
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUiState
import com.zeppelin.app.screens.courseDetail.domain.calculateInitialRadius
import com.zeppelin.app.screens.courseDetail.domain.calculateMaxRadius


@Composable
fun CourseDetailScreen(
    modifier: Modifier = Modifier,
    id: String,
    courseViewModel: CourseDetailsViewModel,
    navController: NavController,
) {
    val courseDetailUI by courseViewModel.courseInfo.collectAsState()
    val quizGradesUiState by courseViewModel.quizGrades.collectAsState()
    val sessionState by courseViewModel.webSocketState.collectAsState()

    LaunchedEffect(Unit) {
        courseViewModel.events.collect { event ->
            navController.navigate(event) {
            }
        }
    }
    LaunchedEffect(key1 = "connection/$id") { courseViewModel.startSession(id.toInt(), false) }
    LaunchedEffect(key1 = "data/$id") { courseViewModel.loadCourseDetails(id.toInt()) }


    val currentScopes = LocalSharedTransitionScopes.current
    CompositionLocalProvider(LocalSharedTransitionScopes provides currentScopes) {
        CourseScreenLayout(
            modifier = modifier,
            courseDetailUI = courseDetailUI,
            onSessionStartNavigation = { courseViewModel.onSessionStartClick(id.toInt()) },
            sessionState = sessionState,
            onRetryConnection = { courseViewModel.startSession(id.toInt(), true) },
            quizGradesUiState = quizGradesUiState,
            onShowGradeDetail = { courseViewModel.onShowGradeDetail(it) },
            onDismissDialog = { courseViewModel.onDismissDialog() },
            onShowAccordion = { courseViewModel.onToggleAccordion(it.moduleIndex, it.moduleName) }
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CourseScreenLayout(
    modifier: Modifier = Modifier,
    courseDetailUI: CourseDetailModulesUIState = CourseDetailModulesUIState(),
    quizGradesUiState: QuizGradesUiState,
    onSessionStartNavigation: () -> Unit = {},
    sessionState: WebSocketState,
    onShowGradeDetail: (QuizGradesUi) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onShowAccordion: (ModuleListUI) -> Unit,
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
        remember(
            sessionState is WebSocketState.Connecting,
            sessionState is WebSocketState.Connected
        ) {
            {
                if (!(sessionState is WebSocketState.Connecting) && sessionState is WebSocketState.Connected) {
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
        AnimatedContent(courseDetailUI.isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { isLoading ->

            CourseContent(
                courseDetailUI = courseDetailUI.courseDetailModulesUI,
                isLoading = isLoading,
                sessionState = sessionState,
                onRetryConnection = onRetryConnection,
                onLongPressStartAnimation = startAnimationLambda,
                onButtonPositioned = buttonPositionedLambda,
                sharedScopes = transScope,
                onShowGradeDetail = onShowGradeDetail,
                onDismissDialog = onDismissDialog,
                quizState = quizGradesUiState,
                onShowAccordion = onShowAccordion
            )
        }

        ExpandingCircleOverlay(
            isVisible = showOverlay,
            color = animationColor,
            radius = currentRadius,
            center = buttonCenter
        )
    }
}


