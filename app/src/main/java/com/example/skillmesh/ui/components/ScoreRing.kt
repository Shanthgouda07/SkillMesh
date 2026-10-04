package com.example.skillmesh.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmesh.theme.ScoreAverage
import com.example.skillmesh.theme.ScoreExcellent
import com.example.skillmesh.theme.ScoreGood
import com.example.skillmesh.theme.ScoreLow

@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val curProgress by animateFloatAsState(
        targetValue = if (animationPlayed) score / 100f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ScoreProgress"
    )

    LaunchedEffect(score) {
        animationPlayed = true
    }

    val scoreColor = when {
        score >= 85 -> ScoreExcellent
        score >= 65 -> ScoreGood
        score >= 40 -> ScoreAverage
        else -> ScoreLow
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background track
            drawCircle(
                color = scoreColor.copy(alpha = 0.2f),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Foreground progress arc
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * curProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (size < 50.dp) 11.sp else 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
