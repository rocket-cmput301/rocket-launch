package com.example.rl_v1.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.theme.Cream
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen

@Composable
fun SwipeUpPanel(
    title: String,
    subtitle: String,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier,
    swipeThreshold: Dp = 48.dp,
    // align: Modifier.Companion.(Any) -> Unit,
   // size: Unit.(Float, Float) -> Unit,
    //background: Unit.(Any, Any) -> Modifier
) {
    val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }
    val currentOnSwipeUp by rememberUpdatedState(onSwipeUp)
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .background(Cream, shape)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                    onDragEnd = {
                        // Negative = finger moved up
                        if (dragTotal < -thresholdPx) currentOnSwipeUp()
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f }
                )
            }
            // Tap also works, which keeps it usable with TalkBack and for people who don't swipe
            .clickable(onClickLabel = "Show campsites", onClick = onSwipeUp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Drag handle
        Box(
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)

                .background(GreyGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = GreyGreen
        )
    }
}