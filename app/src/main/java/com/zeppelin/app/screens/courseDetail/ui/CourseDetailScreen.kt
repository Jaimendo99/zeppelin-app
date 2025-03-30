package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.zeppelin.app.screens._common.ui.ButtonWithLoader
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.CourseProgressUI
import com.zeppelin.app.screens.courseDetail.data.GradeUI
import com.zeppelin.app.ui.theme.ZeppelinTheme

@Composable
fun CourseDetailScreen(
    modifier: Modifier = Modifier,
    id: String,
    courseViewModel: CourseDetailsViewModel,
    navController: NavController
) {

    val courseDetail = courseViewModel.courseDetail.collectAsState().value
    val loading = courseViewModel.isLoading.collectAsState().value

    LaunchedEffect(Unit) {
        courseViewModel.events.collect { event ->
            navController.navigate(event)
        }
    }

    LaunchedEffect(key1 = id) { courseViewModel.getCourseDetail(id.toInt()) }

    AnimatedContent(targetState = loading,
        transitionSpec = { fadeIn() togetherWith fadeOut() }) { isLoading ->
        if (!isLoading) {
            if (courseDetail != null) CourseScreenLayout(courseDetail)
            else Box(modifier = Modifier.fillMaxSize()) { Text("Error loading course detail") }
        } else CourseScreenLayout(isLoading = true)

    }
}

@Composable
fun CourseScreenLayout(
    courseDetailUI: CourseDetailUI = CourseDetailUI(),
    onStartSessionClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .zIndex(1f)
            ) {
                if (!isLoading) {
                    AsyncImage(
                        model = courseDetailUI.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .blur(radius = 8.dp),
                        contentScale = ContentScale.Crop,
                        clipToBounds = true,
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                CourseDetailHeader(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 40.dp
                    ),
                    title = courseDetailUI.title,
                    description = courseDetailUI.description,
                    course = courseDetailUI.course,
                    isLoading = isLoading
                )
            }

            Box(
                modifier = Modifier
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
                            modifier = Modifier.padding(16.dp),
                            courseDetailUI.grades,
                            isLoading
                        )
                        CourseProgress(
                            modifier = Modifier.padding(16.dp),
                            courseDetailUI.progress,
                            isLoading
                        )
                    }
                    ButtonWithLoader(
                        modifier = Modifier.padding(bottom = 24.dp),
                        isLoading = isLoading,
                        onLongPress = { onStartSessionClick() }
                    ) {
                        Row {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text("Start Session")
                        }
                    }

                }
            }
        }
    }
}


@Composable
@Preview
fun CourseDetailScreenPreview() {
    ZeppelinTheme {
        CourseScreenLayout(
            isLoading = false,
            courseDetailUI = CourseDetailUI(
                id = 1,
                course = "Matemáticas",
                title = "Matrices #1",
                description = "Este curso es sobre el tema 2 del libro donde se habla de las matrices y como hacer opraciones aritmetricas",
                imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?q=80&w=720&auto=format&fit=crop",
                grades = listOf(
                    GradeUI(
                        id = "1",
                        gradeName = "Tarea 1",
                        grade = "10",
                        dateGraded = "12/12/2021"
                    ),
                    GradeUI(
                        id = "6",
                        gradeName = "Tarea 6",
                        grade = "5",
                        dateGraded = "12/12/2021"
                    ),
                    GradeUI(
                        id = "7",
                        gradeName = "Tarea 7",
                        grade = "4",
                        dateGraded = "12/12/2021"
                    ),
                    GradeUI(
                        id = "8",
                        gradeName = "Tarea 8",
                        grade = "3",
                        dateGraded = "12/12/2021"
                    ),
                    GradeUI(
                        id = "9",
                        gradeName = "Tarea 9",
                        grade = "2",
                        dateGraded = "12/12/2021"
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

