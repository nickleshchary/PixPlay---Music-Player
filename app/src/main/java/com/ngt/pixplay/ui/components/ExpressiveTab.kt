package com.ngt.pixplay.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Material You expressive tab with morphing animations
 * Selected tabs have circular/pill containers, unselected have subtle rounded corners
 */
@Composable
fun ExpressiveTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animate corner radius: 50% (circular/pill) when selected, 8dp when unselected
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 100.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 380f
        ),
        label = "corner radius"
    )
    
    // Animate horizontal padding
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 12.dp,
        animationSpec = tween(durationMillis = 300),
        label = "horizontal padding"
    )
    
    // Animate vertical padding
    val verticalPadding by animateDpAsState(
        targetValue = if (selected) 10.dp else 8.dp,
        animationSpec = tween(durationMillis = 300),
        label = "vertical padding"
    )
    
    Box(
        modifier = modifier
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = true,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp) // Slight gap between tabs
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    color = if (selected) selectedContainerColor else unselectedContainerColor
                )
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) selectedContentColor else unselectedContentColor
            )
        }
    }
}

/**
 * Row of expressive tabs with morphing animations
 */
@Composable
fun ExpressiveTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tabText ->
            ExpressiveTab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = tabText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
