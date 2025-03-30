package com.zeppelin.app.screens.courses.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zeppelin.app.LocalSharedTransitionScopes
import com.zeppelin.app.R
import com.zeppelin.app.SharedTransitionScopes
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.ui.theme.ZeppelinTheme


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
                    courseCardData.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(courseCardData.imageUrl),
                            animatedVisibilityScope = transScope.animatedVisibilityScope,
                        )
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .fillMaxWidth()
                        .height(integerResource(R.integer.course_image_height).dp),
                    contentScale = ContentScale.Crop
                )
                Box {
                    Row (modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(courseCardData.id),
                            animatedVisibilityScope = transScope.animatedVisibilityScope,
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