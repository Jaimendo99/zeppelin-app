package com.zeppelin.app.screens.courses.ui

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zeppelin.app.R
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.ui.theme.ZeppelinTheme


@Composable
fun CourseCard(
    courseCardData: CourseCardData,
    enableCLick: Boolean = true,
    onCardClick: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enableCLick
            ) { onCardClick(courseCardData.id) },
        shape = RoundedCornerShape(10.dp),
//        elevation = CardDefaults.cardElevation(),
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
                    .fillMaxWidth()
                    .height(integerResource(R.integer.course_image_height).dp),
                contentScale = ContentScale.Crop
            )
            Row {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = courseCardData.subject, style = MaterialTheme.typography.titleLarge)
                    Text(text = courseCardData.course, style = MaterialTheme.typography.bodyMedium)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CourseCardPreview() {
    ZeppelinTheme {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in 1..3) {
                CourseCard(
                    CourseCardData(
                        1,
                        "Matrices #1",
                        "Matematicas",
                        "14%",
                        "https://cdn.uconnectlabs.com/wp-content/uploads/sites/7/2019/08/math-840x560.jpg?v=56233"
                    )

                ) {}
            }
        }
    }
}
