package com.ngt.pixplay.ui.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

// A fixed width for the grid item placeholder, matching the DiscoverScreen SongCard
private val GridThumbnailSize = 140.dp

@Composable
fun GridItemPlaceholder(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = false,
) {
    // Use Material You surface container colors for better theme integration
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainerHighest
    
    Column(
        modifier = if (fillMaxWidth) {
            modifier.padding(12.dp).fillMaxWidth()
        } else {
            modifier.width(GridThumbnailSize)
        },
    ) {
        Spacer(
            modifier = if (fillMaxWidth) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.height(GridThumbnailSize)
            }.aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(placeholderColor),
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextPlaceholder()

        TextPlaceholder()
    }
}
