package com.zeppelin.app.screens._common.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun TextWithLoader(
    text: String,
    size: Int,
    style : TextStyle,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingText(size, style, modifier = modifier)
    } else {
        Text(text = text, modifier = modifier, style = style)
    }
}