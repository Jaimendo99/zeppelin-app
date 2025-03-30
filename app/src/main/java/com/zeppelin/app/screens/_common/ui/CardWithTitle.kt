package com.zeppelin.app.screens._common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardWithTitle(modifier: Modifier = Modifier , title: String, content : @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.scrim,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
        ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 24.dp),

            )
            HorizontalDivider()
            content()
        }
    }
}