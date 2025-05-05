package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens._common.data.PomodoroState
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseContent(
    modifier: Modifier = Modifier,
    courseDetailUI: CourseDetailUI,
    isLoading: Boolean,
    sessionState: WebSocketState,
    onRetryConnection: () -> Unit,
    onLongPressStartAnimation: () -> Unit,
    onButtonPositioned: (position: Offset, size: IntSize) -> Unit,
    sharedScopes: SharedTransitionScopes,
) {
    with(sharedScopes.sharedTransitionScope) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(courseDetailUI.imageUrl),
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
                    .clip(RoundedCornerShape(topStartPercent = 5, topEndPercent = 5))
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(2f)
                    .fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        GradesCard(
                            modifier = Modifier
                                .zIndex(3f)
                                .padding(16.dp),
                            courseDetailUI.grades,
                            isLoading
                        )
                        CourseProgress(
                            modifier = Modifier
                                .zIndex(3f)
                                .padding(16.dp),
                            courseDetailUI.progress,
                            isLoading
                        )
                    }
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