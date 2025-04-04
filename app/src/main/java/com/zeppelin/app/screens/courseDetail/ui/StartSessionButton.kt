package com.zeppelin.app.screens.courseDetail.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.zeppelin.app.screens._common.ui.ButtonWithLoader

@Composable
fun StartSessionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongPressStartAnimation: () -> Unit,
    isLoading: Boolean = false,
    isSessionStarted: Boolean = false,
    onPositioned: (position: Offset, size: IntSize) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.positionInRoot()
                val size = layoutCoordinates.size
                onPositioned(position, size)
            }
    ) {
        ButtonWithLoader(
            modifier = Modifier.zIndex(1f),
            isLoading = isLoading,
            onLongPress = {
                if (!isLoading && isSessionStarted) onLongPressStartAnimation()
            },
            onClick = {
                if (!isLoading && !isSessionStarted) {
                    onClick()
                } else if (!isLoading) {
                    Toast.makeText(context, "Long press to enter session", Toast.LENGTH_SHORT)
                        .show()
                }
            },
        ) {
            if (isSessionStarted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("Start Session")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Retry connection")
                }
            }
        }
    }
}