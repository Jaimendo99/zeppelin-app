package com.zeppelin.app.screens.courses.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.ui.theme.ZeppelinTheme


@Composable
fun CoursesScreen(
    courseViewModel: CourseViewModel,
    navController: NavController
) {
    val courses = courseViewModel.courses.collectAsState()
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
            true -> LoadingCards(modifier = Modifier.padding(horizontal = 8.dp))
            false -> CourseList(
                courses = courses.value,
                onCourseClick = { id ->
                    courseViewModel.onCourseClick(id)
                },
                enableClick = enableClick.value,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            )
        }
    }

}


// --- CourseList ---
@Composable
fun CourseList(
    courses: List<CourseCardData>,
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
            CourseCard(
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
            CourseCardLoading()
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





@Preview(showBackground = true)
@Composable
fun LoadingCardPreview() {
    ZeppelinTheme {
        LoadingCards()
    }
}
