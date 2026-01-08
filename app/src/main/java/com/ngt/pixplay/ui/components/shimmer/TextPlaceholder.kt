package com.ngt.pixplay.ui.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
) {
    // Use Material You surface container colors for better theme integration
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainerHigh
    
    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .height(height)
            .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
            .clip(RoundedCornerShape(4.dp))
            .background(placeholderColor)
    )
}
