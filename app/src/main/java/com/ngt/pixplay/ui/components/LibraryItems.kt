package com.ngt.pixplay.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Constants from PixPlay
private val ListItemHeight = 72.dp
private val GridThumbnailHeight = 128.dp
private val ListThumbnailSize = 56.dp
private val ThumbnailCornerRadius = 12.dp

@Composable
fun LibraryCategoryListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(ListItemHeight)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(ListThumbnailSize)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), // More minimal/subtle
                    shape = RoundedCornerShape(ThumbnailCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp) // Increased from 28.dp
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium, // Changed from bodyLarge + Bold for better hierarchy
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryCategoryGridItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .width(GridThumbnailHeight)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), // More minimal/subtle
                    shape = RoundedCornerShape(ThumbnailCornerRadius)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.fillMaxSize(0.5f) // Dynamic size: 50% of container size (approx 64dp for 128dp box)
            )
        }

        Spacer(modifier = Modifier.height(12.dp)) // More breathing room

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium, // Changed from bodyLarge + Bold
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().basicMarquee()
        )

        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

enum class LibraryViewMode {
    LIST,
    GRID
}

@Composable
fun ViewToggle(
    viewMode: LibraryViewMode,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (viewMode == LibraryViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
            contentDescription = "Toggle View"
        )
    }
}
