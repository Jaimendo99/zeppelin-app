package com.zeppelin.app.screens.courses.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.R
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.screens.courses.data.CourseCardWithProgress
import com.zeppelin.app.ui.theme.ZeppelinTheme
import com.zeppelin.app.ui.theme.bodyFontFamily


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseCard(
    modifier: Modifier = Modifier,
    courseCardData: CourseCardData,
    enableCLick: Boolean = true,
    onCardClick: (Int) -> Unit
) {
    val transScope = LocalSharedTransitionScopes.current

    with(transScope.sharedTransitionScope) {
        ElevatedCard(
            modifier = modifier

                .fillMaxWidth()
                .clickable(
                    enabled = enableCLick
                ) { onCardClick(courseCardData.id) },
            shape = RoundedCornerShape(10.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.scrim,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {
            Column {
                AsyncImage(
                    courseCardData.imageUrl+"&course_id=${courseCardData.id}",
                    contentDescription = null,
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(courseCardData.imageUrl),
                            animatedVisibilityScope = transScope.animatedVisibilityScope,
                        )
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .fillMaxWidth()
                        .height(integerResource(R.integer.course_image_height).dp),
                    contentScale = ContentScale.Crop
                )
                Box {
                    Row(
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(courseCardData.id),
                                animatedVisibilityScope = transScope.animatedVisibilityScope,
                                zIndexInOverlay = 1f
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("course/${courseCardData.id}/${courseCardData.course}"),
                                    animatedVisibilityScope = transScope.animatedVisibilityScope,
                                ),
                                text = courseCardData.course,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("subject/${courseCardData.id}/${courseCardData.subject}"),
                                    animatedVisibilityScope = transScope.animatedVisibilityScope,
                                ),
                                text = courseCardData.subject,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = courseCardData.progress,
                                style = TextStyle(
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseCardWithProgress(
    modifier: Modifier = Modifier,
    courseCardData: CourseCardWithProgress,
    enableCLick: Boolean = true,
    onCardClick: (Int) -> Unit,
) {
    val transScope = LocalSharedTransitionScopes.current
    with(transScope.sharedTransitionScope) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enableCLick
                ) { onCardClick(courseCardData.courseId) },
            shape = RoundedCornerShape(10.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.scrim,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {

            Column {
                AsyncImage(
                    "https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=50&w=720&auto=format&fit=crop&course_id=${courseCardData.courseId}",
                    contentDescription = null,
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState("https://images.unsplash.com/photo-1561089489-f13d5e730d72?q=50&w=720&auto=format&fit=crop&course_id=${courseCardData.courseId}"),
                            animatedVisibilityScope = transScope.animatedVisibilityScope,
                        )
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .fillMaxWidth()
                        .height(integerResource(R.integer.course_image_height).dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(courseCardData.courseId),
                                    animatedVisibilityScope = transScope.animatedVisibilityScope,
                                    zIndexInOverlay = 1f
                                ),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.Top)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    modifier = Modifier.sharedElement(
                                        state = rememberSharedContentState(
                                            "course/${courseCardData.courseId}/${courseCardData.title}"
                                        ),
                                        animatedVisibilityScope = transScope.animatedVisibilityScope,
                                    ),
                                    text = courseCardData.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    modifier = Modifier.sharedElement(
                                        state = rememberSharedContentState(
                                            "subject/${courseCardData.courseId}/${courseCardData.lastModule}"
                                        ),
                                        animatedVisibilityScope = transScope.animatedVisibilityScope,
                                    ),
                                    text = courseCardData.lastModule,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Box(
                                modifier = Modifier
//                                .align(Alignment.CenterVertically)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ){
                                    Icon(
                                       modifier= modifier.height(14.dp)
                                           .padding(end=2.dp)
                                           .offset(y = (-0.7).dp)
                                           ,
                                        painter=painterResource( R.drawable.outline_calendar_month_24), contentDescription = null
                                        , tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                Text(
                                    text = courseCardData.startDate,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            CourseStatItem(count = courseCardData.moduleCount, type = "modules")
                            CourseStatItem(count = courseCardData.videoCount, type = "videos")
                            CourseStatItem(count = courseCardData.quizCount, type = "quizzes")
                            CourseStatItem(count = courseCardData.textCount, type = "texts")
                        }
                        CourseProgressBar(modifier = Modifier, progress = courseCardData.percentage)
                    }
                }
            }

        }
    }
}

@Composable
fun CourseStatItem(
    modifier: Modifier = Modifier,
    count: Int,
    type: String,
) {
    val ( trailingIcon , tint ) = when (type) {
        "modules" -> painterResource(R.drawable.rounded_stacks_24) to MaterialTheme.colorScheme.primaryContainer
        "videos" -> painterResource(R.drawable.rounded_play_circle_24) to MaterialTheme.colorScheme.secondary
        "quizzes" -> painterResource(R.drawable.baseline_quiz_24) to MaterialTheme.colorScheme.secondary
        "texts" -> painterResource(R.drawable.rounded_docs_24) to MaterialTheme.colorScheme.secondary
        else -> painterResource(R.drawable.ic_fg_dark) to MaterialTheme.colorScheme.secondaryContainer
    }

    Row {
        Icon(
            painter = trailingIcon,
            contentDescription = null,
            modifier = Modifier
                .height(16.dp)
                .align(Alignment.CenterVertically)
            ,
            tint = tint
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
            modifier = modifier
                .align(Alignment.CenterVertically)
        )

    }
}



@Composable
fun CourseProgressBar(
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    trackColor : Color = MaterialTheme.colorScheme.secondaryContainer,
    progressColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    LinearProgressIndicator(modifier = modifier
        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp )
        .fillMaxWidth()
        .height(8.dp)
        , progress = { progress }, gapSize = 0.dp,
        trackColor = trackColor,
        color = progressColor,

    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CourseCardPreview1() {
    ZeppelinTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true, label = "PreviewScope") {
                CompositionLocalProvider(
                    LocalSharedTransitionScopes provides SharedTransitionScopes(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 1..3) {
                            CourseCardWithProgress (
                                courseCardData =
                                    CourseCardWithProgress(
                                        courseId = i,
                                        title = "Matrices #$i",
                                        startDate = "2023-10-01",
                                        description = "Learn about matrices and their applications.",
                                        lastModule = "Introduction to Matrices",
                                        moduleCount = 5,
                                        videoCount = 10,
                                        textCount = 3,
                                        quizCount = 2,
                                        percentage = (i * 0.1f),
                                    )
                                   ,
                                onCardClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preview Fix ---

/**
 * Define a data class (or use the actual one from your project if it exists)
 * that holds the scopes provided by LocalSharedTransitionScopes.
 * The names `sharedTransitionScope` and `animatedVisibilityScope` must match
 * how they are accessed in `CourseCard` (e.g., `transScope.sharedTransitionScope`).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun CourseCardPreview() {
    ZeppelinTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true, label = "PreviewScope") {
                CompositionLocalProvider(
                    LocalSharedTransitionScopes provides SharedTransitionScopes(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 1..3) {
                            CourseCard(
                                courseCardData =
                                    CourseCardData(
                                        i,
                                        "Matrices #$i",
                                        "Matematicas",
                                        "14%",
                                        "https://cdn.uconnectlabs.com/wp-content/uploads/sites/7/2019/08/math-840x560.jpg?v=56233"
                                    ),
                                onCardClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}