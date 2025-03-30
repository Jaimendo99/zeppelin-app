package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.CardWithTitle
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courseDetail.data.GradeUI


@Composable
fun GradesCard(modifier: Modifier = Modifier, grades: List<GradeUI>, isLoading: Boolean = false) {
    CardWithTitle(modifier, stringResource(R.string.grades)) {
        if (isLoading) {
            for (i in 1..2)
                GradeCardItem(
                    grade = GradeUI("", "", "", ""), isLoading = true,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
        } else
            grades.forEach {
                GradeCardItem(
                    modifier = Modifier
                        .clickable { }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    grade = it,
                )
                if (it != grades.last()) HorizontalDivider()
            }
    }
}

@Composable
fun GradeCardItem(modifier: Modifier = Modifier, grade: GradeUI, isLoading: Boolean = false) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            if (!isLoading) {
                Text(text = grade.dateGraded, style = MaterialTheme.typography.labelSmall)
                Text(text = grade.gradeName, style = MaterialTheme.typography.titleMedium)
            } else {
                LoadingText(
                    length = 20,
                    textStyle = MaterialTheme.typography.labelSmall,
                )
                LoadingText(
                    length = 20,
                    textStyle = MaterialTheme.typography.titleMedium,
                )
            }

        }
        if (!isLoading) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = grade.grade,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LoadingText(
                length = 10,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun GradesCardPreview() {
    GradesCard(
        grades =
        listOf(
            GradeUI("1", "Quiz 1", "10/10", "10-10-2025"),
            GradeUI("2", "Quiz 2", "8/10", "02-10-2025"),
            GradeUI("3", "Quiz 3", "9/10", "03-03-2025"),
        ),
    )
}

