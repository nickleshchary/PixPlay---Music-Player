package com.ngt.pixplay.ui.components.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerHost(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Get Material You colors for the shimmer effect
    val shimmerHighlightColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val shimmerBaseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    
    // Create a custom shimmer theme with Material You colors
    val customShimmerTheme = defaultShimmerTheme.copy(
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing,
                delayMillis = 200,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        shaderColors = listOf(
            shimmerBaseColor.copy(alpha = 0.9f),
            shimmerHighlightColor.copy(alpha = 1.0f),
            shimmerBaseColor.copy(alpha = 0.9f),
        ),
    )
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalShimmerTheme provides customShimmerTheme
    ) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            modifier = modifier.shimmer(),
            content = content,
        )
    }
}
