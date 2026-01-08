package com.ngt.pixplay.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.pow

// Animation specs
val BottomSheetAnimationSpec: AnimationSpec<Dp> = spring(
    dampingRatio = 0.8f,
    stiffness = 300f
)

val BottomSheetSoftAnimationSpec: AnimationSpec<Dp> = spring(
    dampingRatio = 0.75f,
    stiffness = 200f
)

val MiniPlayerHeight = 64.dp

/**
 * Bottom Sheet component for player
 * Based on open source music player implementation
 */
@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    collapsedBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current


    
    // The actual bottom sheet
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            // Use graphicsLayer for offset to ensure hardware acceleration
            .graphicsLayer {
                // Offset from bottom: when collapsed, show collapsedBound height
                // When expanded, show full height
                val offsetFromTop = (state.expandedBound - state.value).toPx().coerceAtLeast(0f)
                translationY = offsetFromTop
            }
            .pointerInput(state) {
                val velocityTracker = VelocityTracker()

                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        velocityTracker.addPointerInputChange(change)
                        state.dispatchRawDelta(dragAmount)
                    },
                    onDragCancel = {
                        velocityTracker.resetTracking()
                        state.snapTo(state.collapsedBound)
                    },
                    onDragEnd = {
                        val velocity = -velocityTracker.calculateVelocity().y
                        velocityTracker.resetTracking()
                        state.performFling(velocity, onDismiss)
                    }
                )
            }
            .clip(RoundedCornerShape(
                topStart = if (!state.isExpanded) 16.dp else 0.dp,
                topEnd = if (!state.isExpanded) 16.dp else 0.dp
            ))

    ) {
        // Back handler when expanded
        if (!state.isCollapsed && !state.isDismissed) {
            BackHandler(onBack = state::collapseSoft)
        }

        // Main expanded content - fades in as sheet expands
        // Use progress > 0.15f to ensure it's hidden during the initial part of the drag
        // This matches the alpha calculation logic: (progress - 0.15f) * 4
        if (state.progress > 0.15f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = ((state.progress - 0.15f) * 4).coerceIn(0f, 1f)
                    },
                content = content
            )
        }

        // Collapsed content (MiniPlayer) - fades out as sheet expands
        if (!state.isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MiniPlayerHeight)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = 1f - (state.progress * 4).coerceAtMost(1f)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = state::expandSoft
                    ),
                content = collapsedContent
            )
        }
    }
}

@Stable
class BottomSheetState(
    draggableState: DraggableState,
    private val coroutineScope: CoroutineScope,
    private val animatable: Animatable<Dp, AnimationVector1D>,
    private val onAnchorChanged: (Int) -> Unit,
    val collapsedBound: Dp,
) : DraggableState by draggableState {
    
    val dismissedBound: Dp
        get() = animatable.lowerBound!!

    val expandedBound: Dp
        get() = animatable.upperBound!!

    val value by animatable.asState()

    val isDismissed by derivedStateOf {
        value == animatable.lowerBound!!
    }

    val isCollapsed by derivedStateOf {
        value == collapsedBound
    }

    val isExpanded by derivedStateOf {
        value == animatable.upperBound
    }

    val progress by derivedStateOf {
        if (animatable.upperBound!! == collapsedBound) {
            0f
        } else {
            1f - (animatable.upperBound!! - animatable.value) / (animatable.upperBound!! - collapsedBound)
        }
    }

    fun collapse(animationSpec: AnimationSpec<Dp>) {
        onAnchorChanged(ANCHOR_COLLAPSED)
        coroutineScope.launch {
            animatable.animateTo(collapsedBound, animationSpec)
        }
    }

    fun expand(animationSpec: AnimationSpec<Dp>) {
        onAnchorChanged(ANCHOR_EXPANDED)
        coroutineScope.launch {
            animatable.animateTo(animatable.upperBound!!, animationSpec)
        }
    }

    private fun collapse() {
        collapse(BottomSheetAnimationSpec)
    }

    private fun expand() {
        expand(BottomSheetAnimationSpec)
    }

    fun collapseSoft() {
        collapse(BottomSheetSoftAnimationSpec)
    }

    fun expandSoft() {
        expand(BottomSheetSoftAnimationSpec)
    }

    fun dismiss() {
        onAnchorChanged(ANCHOR_DISMISSED)
        coroutineScope.launch {
            animatable.animateTo(animatable.lowerBound!!)
        }
    }

    fun snapTo(value: Dp) {
        coroutineScope.launch {
            animatable.snapTo(value)
        }
    }

    fun performFling(velocity: Float, onDismiss: (() -> Unit)?) {
        if (velocity > 250) {
            expand()
        } else if (velocity < -250) {
            if (value < collapsedBound && onDismiss != null) {
                dismiss()
                onDismiss.invoke()
            } else {
                collapse()
            }
        } else {
            val l0 = dismissedBound
            val l1 = (collapsedBound - dismissedBound) / 2
            val l2 = (expandedBound - collapsedBound) / 2
            val l3 = expandedBound

            when (value) {
                in l0..l1 -> {
                    if (onDismiss != null) {
                        dismiss()
                        onDismiss.invoke()
                    } else {
                        collapse()
                    }
                }
                in l1..l2 -> collapse()
                in l2..l3 -> expand()
                else -> Unit
            }
        }
    }
    
    companion object {
        const val ANCHOR_DISMISSED = 0
        const val ANCHOR_COLLAPSED = 1
        const val ANCHOR_EXPANDED = 2
    }
}

@Composable
fun rememberBottomSheetState(
    dismissedBound: Dp = 0.dp,
    expandedBound: Dp,
    collapsedBound: Dp = MiniPlayerHeight,
    initialAnchor: Int = BottomSheetState.ANCHOR_COLLAPSED,
): BottomSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var previousAnchor by rememberSaveable {
        mutableIntStateOf(initialAnchor)
    }
    val animatable = remember {
        Animatable(0.dp, Dp.VectorConverter)
    }

    return remember(dismissedBound, expandedBound, collapsedBound, coroutineScope) {
        val initialValue = when (previousAnchor) {
            BottomSheetState.ANCHOR_EXPANDED -> expandedBound
            BottomSheetState.ANCHOR_COLLAPSED -> collapsedBound
            BottomSheetState.ANCHOR_DISMISSED -> dismissedBound
            else -> collapsedBound
        }

        animatable.updateBounds(dismissedBound.coerceAtMost(expandedBound), expandedBound)
        coroutineScope.launch {
            animatable.animateTo(initialValue, BottomSheetAnimationSpec)
        }

        BottomSheetState(
            draggableState = DraggableState { delta ->
                coroutineScope.launch {
                    animatable.snapTo(animatable.value - with(density) { delta.toDp() })
                }
            },
            onAnchorChanged = { previousAnchor = it },
            coroutineScope = coroutineScope,
            animatable = animatable,
            collapsedBound = collapsedBound
        )
    }
}
