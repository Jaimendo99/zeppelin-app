package com.zeppelin.app.screens.courses.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courses.data.CourseCardWithProgress
import com.zeppelin.app.ui.theme.ZeppelinTheme


@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    courseViewModel: CourseViewModel,
    navController: NavController,
) {
    val courses by courseViewModel.courseWithProgress.collectAsState()
    val loading = courseViewModel.loading.collectAsState()
    val enableClick = courseViewModel.enableClick.collectAsState()

    LaunchedEffect(Unit) {
        courseViewModel.events.collect { event ->
            navController.navigate(event)
        }
    }
    AnimatedContent(loading.value, label = "loadingAnimation",
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { targetLoading ->
        when (targetLoading) {
            true -> LoadingCards(modifier = modifier.padding(horizontal = 8.dp))
            false -> CourseList(
                courses = courses,
                onCourseClick = { id ->
                    courseViewModel.onCourseClick(id)
                },
                enableClick = enableClick.value,
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            )
        }
    }

}


// --- CourseList ---
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseList(
    courses: List<CourseCardWithProgress>,
    onCourseClick: (Int) -> Unit,
    enableClick: Boolean,
    modifier: Modifier = Modifier
) {
        LazyColumn(
            modifier = modifier.imePadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(courses.size) { index ->
                CourseCardWithProgress(
                    courseCardData = courses[index], enableCLick = enableClick
                ) { id ->
                    onCourseClick(id)
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
}

@Composable
fun LoadingCards(modifier: Modifier = Modifier, len: Int = 4) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        for (i in 1..len) {
            CourseCardWithProgressLoading()
        }
    }
}

@Composable
fun CourseCardLoading() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.scrim,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .fillMaxWidth()
                    .height(integerResource(R.integer.course_image_height).dp),
            ) {
            }
            Row {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    LoadingText(20, MaterialTheme.typography.titleLarge)
                    LoadingText(20, MaterialTheme.typography.bodyMedium)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    LoadingText(10, TextStyle(fontSize = 16.sp))
                }
            }
        }
    }
}

@Composable
fun CourseCardWithProgressLoading(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.scrim,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .fillMaxWidth()
                    .height(integerResource(R.integer.course_image_height).dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    LoadingText(
                        30,
                         MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LoadingText(
                         20,
                         MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier.align(Alignment.Top)
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.outline_calendar_month_24
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .height(14.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    LoadingText(
                        25,
                        MaterialTheme.typography.bodySmall
                    )
                }
            }

            // stats placeholders
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CourseStatItemLoading(type = "modules")
                CourseStatItemLoading(type = "videos")
                CourseStatItemLoading(type = "quizzes")
                CourseStatItemLoading(type = "texts")
            }
            CourseProgressBar(trackColor = Color.Gray.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun CourseStatItemLoading(
    modifier: Modifier = Modifier,
    type: String,
) {
    val trailingIcon = when (type) {
        "modules" -> painterResource(R.drawable.rounded_stacks_24)
        "videos" -> painterResource(R.drawable.rounded_play_circle_24)
        "quizzes" -> painterResource(R.drawable.baseline_quiz_24)
        "texts" -> painterResource(R.drawable.rounded_docs_24)
        else -> painterResource(R.drawable.ic_fg_dark)
    }

    Row {
        Icon(
            painter = trailingIcon,
            contentDescription = null,
            modifier = Modifier
                .height(16.dp)
                .align(Alignment.CenterVertically),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.padding(2.dp))
        LoadingText(4, MaterialTheme.typography.labelLarge,
            modifier = modifier
                .align(Alignment.CenterVertically)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoadingCardPreview() {
    ZeppelinTheme {
        LoadingCards()
    }
}
