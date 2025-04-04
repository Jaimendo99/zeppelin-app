package com.zeppelin.app.screens.courseDetail.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot
import kotlin.math.max

fun calculateMaxRadius(
    buttonCenter: Offset,
    layoutSize: IntSize,
    screenWidthPx: Float,
    screenHeightPx: Float
): Float {
    return if (buttonCenter == Offset.Zero || layoutSize == IntSize.Zero) {
        max(screenWidthPx, screenHeightPx)
    } else {
        val tl = hypot(buttonCenter.x, buttonCenter.y)
        val tr = hypot(layoutSize.width - buttonCenter.x, buttonCenter.y)
        val bl = hypot(buttonCenter.x, layoutSize.height - buttonCenter.y)
        val br = hypot(layoutSize.width - buttonCenter.x, layoutSize.height - buttonCenter.y)
        maxOf(tl, tr, bl, br)
    }
}

fun calculateInitialRadius(buttonSize: IntSize): Float {
    return if (buttonSize == IntSize.Zero) 0f else (max(buttonSize.width, buttonSize.height) / 2f)
}

