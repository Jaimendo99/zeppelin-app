package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUI
import com.zeppelin.app.screens.courseDetail.data.ModuleListUI
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUi
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUiState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseContent(
    modifier: Modifier = Modifier,
    courseDetailUI: CourseDetailModulesUI,
    quizState: QuizGradesUiState,
    isLoading: Boolean,
    sessionState: WebSocketState,
    onRetryConnection: () -> Unit,
    onLongPressStartAnimation: () -> Unit,
    onButtonPositioned: (position: Offset, size: IntSize) -> Unit,
    onShowGradeDetail: (QuizGradesUi) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onShowAccordion: (ModuleListUI) -> Unit,
    sharedScopes: SharedTransitionScopes,
) {
    with(sharedScopes.sharedTransitionScope) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=50&w=720&auto=format&fit=crop&course_id=${courseDetailUI.id}"
                        ),
                        animatedVisibilityScope = sharedScopes.animatedVisibilityScope,
                        zIndexInOverlay = 0f
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                CourseDetailHeaderImage(
                    modifier = Modifier.matchParentSize(),
                    isLoading = isLoading,
                    courseDetailUI = courseDetailUI
                )
            }

            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(courseDetailUI.id),
                        animatedVisibilityScope = sharedScopes.animatedVisibilityScope,
                        zIndexInOverlay = 1f
                    )
                    .offset(y = (-24).dp)
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 5,
                            topEndPercent = 5
                        )
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(2f)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 75.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            GradesCard(
                                modifier = Modifier
                                    .zIndex(3f)
                                    .padding(16.dp),
                                quizState,
                                onOpenDialog = { onShowGradeDetail(it) },
                                onDismissDialog = onDismissDialog
                            )
                        }

                        item {
                            ModuleAccordion(
                                modifier = Modifier.padding(16.dp),
                                courseDetailModulesUI = courseDetailUI,
                                onModuleClick = onShowAccordion
                            )
                        }

                        item {
                            CourseProgress(
                                modifier = Modifier
                                    .zIndex(3f)
                                    .padding(16.dp),
                                courseDetailUI.progress,
                                isLoading
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .padding(top = 8.dp, bottom = 12.dp), // Padding for the scrim area
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isSessionStarted: Boolean = when (sessionState) {
                            is WebSocketState.Connected -> courseDetailUI.id == sessionState.lastCourseId
                            else -> false
                        }
                        StartSessionButton(
                            onClick = onRetryConnection,
                            onLongPressStartAnimation = onLongPressStartAnimation,
                            isLoading = sessionState is WebSocketState.Connecting,
                            isSessionStarted = isSessionStarted,
                            onPositioned = onButtonPositioned
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseContentOld(
    modifier: Modifier = Modifier,
    courseDetailUI: CourseDetailModulesUI,
    quizState: QuizGradesUiState,
    isLoading: Boolean,
    sessionState: WebSocketState,
    onRetryConnection: () -> Unit,
    onLongPressStartAnimation: () -> Unit,
    onButtonPositioned: (position: Offset, size: IntSize) -> Unit,
    onShowGradeDetail: (QuizGradesUi) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onShowAccordion: (ModuleListUI) -> Unit,
    sharedScopes: SharedTransitionScopes,
) {
    with(sharedScopes.sharedTransitionScope) {
        Column(modifier = modifier) {
            // ... (Header Box and Image remain the same)
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=50&w=720&auto=format&fit=crop&course_id=${courseDetailUI.id}"
                        ),
                        animatedVisibilityScope = sharedScopes.animatedVisibilityScope,
                        zIndexInOverlay = 0f
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                CourseDetailHeaderImage(
                    modifier = Modifier.matchParentSize(),
                    isLoading = isLoading,
                    courseDetailUI = courseDetailUI
                )
            }

            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(courseDetailUI.id),
                        animatedVisibilityScope = sharedScopes.animatedVisibilityScope,
                        zIndexInOverlay = 1f
                    )
                    .offset(y = (-24).dp)
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 5,
                            topEndPercent = 5
                        )
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(2f)
                    .fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Use LazyColumn for the main content area
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            GradesCard(
                                modifier = Modifier
                                    .zIndex(3f)
                                    .padding(16.dp),
                                quizState,
                                onOpenDialog = { onShowGradeDetail(it) },
                                onDismissDialog = onDismissDialog
                            )
                        }

                        // KEY CHANGE: Call ModuleAccordion inside a single item
                        item {
                            ModuleAccordion(
                                modifier = Modifier.padding(16.dp),
                                courseDetailModulesUI = courseDetailUI,
                                onModuleClick = onShowAccordion
                            )
                        }

                        item {
                            CourseProgress(
                                modifier = Modifier
                                    .zIndex(3f)
                                    .padding(16.dp),
                                courseDetailUI.progress,
                                isLoading
                            )
                        }
                    }

                    // ... (StartSessionButton remains the same)
                    val isSessionStarted: Boolean = when (sessionState) {
                        is WebSocketState.Connected -> courseDetailUI.id == sessionState.lastCourseId
                        else -> false
                    }
                    StartSessionButton(
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(bottom = 24.dp),
                        onClick = onRetryConnection,
                        onLongPressStartAnimation = onLongPressStartAnimation,
                        isLoading = sessionState is WebSocketState.Connecting,
                        isSessionStarted = isSessionStarted,
                        onPositioned = onButtonPositioned
                    )
                }
            }
        }
    }
}


@Composable
fun ExpandingCircleOverlay(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    color: Color,
    radius: Float,
    center: Offset
) {
    if (isVisible) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
        }
    }
}