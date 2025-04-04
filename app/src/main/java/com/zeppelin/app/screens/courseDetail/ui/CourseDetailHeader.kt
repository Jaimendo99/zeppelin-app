package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseDetailHeader(modifier: Modifier = Modifier , course: String, description: String, subject: String, id:Int, isLoading : Boolean = false) {
    val transScope = LocalSharedTransitionScopes.current
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        with(transScope.sharedTransitionScope) {
            Column(modifier = modifier) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isLoading)
                        Text(
                            modifier = Modifier
                                .sharedElement(
                                    state = rememberSharedContentState("course/$id/$course"),
                                    animatedVisibilityScope = transScope.animatedVisibilityScope
                                )
                                .weight(1f),
                            text = course,
                            style = MaterialTheme.typography.displaySmall
                        )
                    else Box(modifier = Modifier.weight(1f)) {
                        LoadingText(
                            length = 20,
                            textStyle = MaterialTheme.typography.displaySmall
                        )
                    }

                    if (!isLoading) Text(
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState("subject/$id/$subject"),
                            animatedVisibilityScope = transScope.animatedVisibilityScope
                        ),
                        text = subject, style = MaterialTheme.typography.bodyMedium)
                    else LoadingText(
                        length = 10,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (!isLoading) Text(text = description)
                else for (i in 1..3) LoadingText(
                    length = 500,
                    textStyle = LocalTextStyle.current
                )
            }
        }
    }
}

@Composable
fun CourseDetailHeaderImage(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    courseDetailUI: CourseDetailUI = CourseDetailUI(),
){
    AsyncImage(
        model = courseDetailUI.imageUrl,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .blur(radius = 8.dp),
        contentScale = ContentScale.Crop,
        clipToBounds = true,
    )

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.4f))
    )

    CourseDetailHeader(
        modifier = Modifier.padding(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 40.dp
        ),
        course = courseDetailUI.course,
        description = courseDetailUI.description,
        subject = courseDetailUI.subject,
        id = courseDetailUI.id,
        isLoading = isLoading
    )
}

@Composable
@Preview
fun CourseDetailHeaderPreview() {
    CourseDetailHeader(
        course = "Matrices #1",
        description = "Este curso es sobre el tema 2 del libro donde se habla de las matrices y como hacer opraciones aritmetricas",
        subject = "Matemáticas",
        isLoading = true,
        id =1
    )

}