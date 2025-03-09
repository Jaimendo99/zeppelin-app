package com.zeppelin.app.screens._common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.zeppelin.app.R

@Composable
fun Logo(modifier: Modifier = Modifier, title: String = "Zeppelin") {
    Row {
        if (title.isNotBlank() and (title != "Zeppelin")) {
            Text(
                text = title,
                modifier = modifier
            )
        }
        Image(
            modifier = modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
        )
    }
}