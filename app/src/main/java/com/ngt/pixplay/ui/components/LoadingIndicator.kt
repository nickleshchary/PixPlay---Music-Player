package com.ngt.pixplay.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.InfiniteAnimationPolicy
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterial3ExpressiveApi
@Composable
fun LoadingIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.DeterminateIndicatorPolygons,
) =
    LoadingIndicatorImpl(
        progress = progress,
        modifier = modifier,
        containerColor = Color.Unspecified,
        indicatorColor = color,
        containerShape = LoadingIndicatorDefaults.containerShape,
        indicatorPolygons = polygons,
    )

@ExperimentalMaterial3ExpressiveApi
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons,
) =
    LoadingIndicatorImpl(
        modifier = modifier,
        containerColor = Color.Unspecified,
        indicatorColor = color,
        containerShape = LoadingIndicatorDefaults.containerShape,
        indicatorPolygons = polygons,
    )

@ExperimentalMaterial3ExpressiveApi
@Composable
fun ContainedLoadingIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    containerColor: Color = LoadingIndicatorDefaults.containedContainerColor,
    indicatorColor: Color = LoadingIndicatorDefaults.containedIndicatorColor,
    containerShape: Shape = LoadingIndicatorDefaults.containerShape,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.DeterminateIndicatorPolygons,
) =
    LoadingIndicatorImpl(
        progress = progress,
        modifier = modifier,
        containerColor = containerColor,
        indicatorColor = indicatorColor,
        containerShape = containerShape,
        indicatorPolygons = polygons,
    )

@ExperimentalMaterial3ExpressiveApi
@Composable
fun ContainedLoadingIndicator(
    modifier: Modifier = Modifier,
    containerColor: Color = LoadingIndicatorDefaults.containedContainerColor,
    indicatorColor: Color = LoadingIndicatorDefaults.containedIndicatorColor,
    containerShape: Shape = LoadingIndicatorDefaults.containerShape,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons,
) =
    LoadingIndicatorImpl(
        modifier = modifier,
        containerColor = containerColor,
        indicatorColor = indicatorColor,
        containerShape = containerShape,
        indicatorPolygons = polygons,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingIndicatorImpl(
    progress: () -> Float,
    modifier: Modifier,
    containerColor: Color,
    indicatorColor: Color,
    containerShape: Shape,
    indicatorPolygons: List<RoundedPolygon>,
) {
    require(indicatorPolygons.size > 1) {
        "indicatorPolygons should have, at least, two RoundedPolygons"
    }
    // Fixed: fastCoerceIn -> coerceIn
    val coercedProgress = { progress().coerceIn(0f, 1f) }
    val path = remember { Path() }
    val scaleMatrix = remember { Matrix() }
    val morphSequence =
        remember(indicatorPolygons) {
            morphSequence(polygons = indicatorPolygons, circularSequence = false)
        }
    val morphScaleFactor =
        remember(morphSequence) {
            calculateScaleFactor(indicatorPolygons) * LoadingIndicatorDefaults.ActiveIndicatorScale
        }
    Box(
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(
                            coercedProgress().takeUnless { it.isNaN() } ?: 0f,
                            0f..1f,
                        )
                }
                .size(
                    width = LoadingIndicatorDefaults.ContainerWidth,
                    height = LoadingIndicatorDefaults.ContainerHeight,
                )
                .fillMaxSize()
                .clip(containerShape)
                .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier =
                Modifier.aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
                    .drawWithContent {
                        val progressValue = coercedProgress()
                        val activeMorphIndex =
                            (morphSequence.size * progressValue)
                                .toInt()
                                .coerceAtMost(morphSequence.size - 1)
                        val adjustedProgressValue =
                            if (progressValue == 1f && activeMorphIndex == morphSequence.size - 1) {
                                1f
                            } else {
                                (progressValue * morphSequence.size) % 1f
                            }

                        val rotation = -progressValue * 180
                        rotate(rotation) {
                            drawPath(
                                path =
                                    processPath(
                                        path =
                                            morphSequence[activeMorphIndex].toPath(
                                                progress = adjustedProgressValue,
                                                path = path,
                                                startAngle = 0,
                                            ),
                                        size = size,
                                        scaleFactor = morphScaleFactor,
                                        scaleMatrix = scaleMatrix,
                                    ),
                                color = indicatorColor,
                                style = Fill,
                            )
                        }
                    }
        )
    }
}

@ExperimentalMaterial3ExpressiveApi
@Composable
private fun LoadingIndicatorImpl(
    modifier: Modifier,
    containerColor: Color,
    indicatorColor: Color,
    containerShape: Shape,
    indicatorPolygons: List<RoundedPolygon>,
) {
    require(indicatorPolygons.size > 1) {
        "indicatorPolygons should have, at least, two RoundedPolygons"
    }
    val morphSequence =
        remember(indicatorPolygons) {
            morphSequence(polygons = indicatorPolygons, circularSequence = true)
        }
    val shapesScaleFactor =
        remember(indicatorPolygons) {
            calculateScaleFactor(indicatorPolygons) * LoadingIndicatorDefaults.ActiveIndicatorScale
        }
    val morphProgress = remember { Animatable(0f) }
    var morphRotationTargetAngle by remember { mutableFloatStateOf(QuarterRotation) }
    val globalRotation = remember { Animatable(0f) }
    var currentMorphIndex by remember(indicatorPolygons) { mutableIntStateOf(0) }
    
    LaunchedEffect(indicatorPolygons) {
        val morphAnimationBlock = {
            launch {
                val morphAnimationSpec =
                    spring(dampingRatio = 0.6f, stiffness = 200f, visibilityThreshold = 0.1f)
                while (true) {
                    val deferred = async {
                        val animationResult =
                            morphProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = morphAnimationSpec,
                            )
                        if (animationResult.endReason == AnimationEndReason.Finished) {
                            currentMorphIndex = (currentMorphIndex + 1) % morphSequence.size
                            morphProgress.snapTo(0f)
                            morphRotationTargetAngle =
                                (morphRotationTargetAngle + QuarterRotation) % FullRotation
                        }
                    }
                    delay(MorphIntervalMillis)
                    deferred.await()
                }
            }
        }

        val rotationAnimationBlock = {
            launch {
                globalRotation.animateTo(
                    targetValue = FullRotation,
                    animationSpec =
                        infiniteRepeatable(
                            tween(GlobalRotationDurationMillis, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                )
            }
        }

        when (val policy = coroutineContext[InfiniteAnimationPolicy]) {
            null -> {
                morphAnimationBlock()
                rotationAnimationBlock()
            }
            else ->
                policy.onInfiniteOperation {
                    morphAnimationBlock()
                    rotationAnimationBlock()
                }
        }
    }

    val path = remember { Path() }
    val scaleMatrix = remember { Matrix() }
    Box(
        modifier =
            modifier
                .progressSemantics()
                .size(
                    width = LoadingIndicatorDefaults.ContainerWidth,
                    height = LoadingIndicatorDefaults.ContainerHeight,
                )
                .fillMaxSize()
                .clip(containerShape)
                .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier =
                Modifier.aspectRatio(1f, matchHeightConstraintsFirst = true).drawWithContent {
                    val progress = morphProgress.value
                    rotate(progress * 90 + morphRotationTargetAngle + globalRotation.value) {
                        drawPath(
                            path =
                                processPath(
                                    path =
                                        morphSequence[currentMorphIndex].toPath(
                                            progress = progress,
                                            path = path,
                                            startAngle = 0,
                                        ),
                                    size = size,
                                    scaleFactor = shapesScaleFactor,
                                    scaleMatrix = scaleMatrix,
                                ),
                            color = indicatorColor,
                            style = Fill,
                        )
                    }
                }
        )
    }
}

@ExperimentalMaterial3ExpressiveApi
object LoadingIndicatorDefaults {
    val ContainerWidth: Dp = 48.0.dp
    val ContainerHeight: Dp = 48.0.dp
    val IndicatorSize = 38.0.dp

    val containerShape: Shape
        @Composable get() = CircleShape // Use CircleShape instead of ShapeKeyTokens.CornerFull

    val indicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary // Use MaterialTheme directly

    val containedIndicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer

    val containedContainerColor: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    val IndeterminateIndicatorPolygons =
        listOf(
            MaterialShapes.SoftBurst,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Pentagon,
            MaterialShapes.Pill,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Oval,
        )

    val DeterminateIndicatorPolygons =
        listOf(
            MaterialShapes.Circle.transformed(Matrix().apply { rotateZ(360f / 20) }),
            MaterialShapes.SoftBurst,
        )

    internal val ActiveIndicatorScale =
        IndicatorSize.value / min(ContainerWidth.value, ContainerHeight.value)
}

// Helper functions (formerly in separate file or part of LoadingIndicator.kt)

private fun morphSequence(polygons: List<RoundedPolygon>, circularSequence: Boolean): List<Morph> {
    return buildList {
        for (i in polygons.indices) {
            if (i + 1 < polygons.size) {
                add(Morph(polygons[i].normalized(), polygons[i + 1].normalized()))
            } else if (circularSequence) {
                add(Morph(polygons[i].normalized(), polygons[0].normalized()))
            }
        }
    }
}

private fun calculateScaleFactor(indicatorPolygons: List<RoundedPolygon>): Float {
    var scaleFactor = 1f
    val bounds = FloatArray(size = 4)
    val maxBounds = FloatArray(size = 4)
    indicatorPolygons.fastForEach { polygon ->
        polygon.calculateBounds(bounds)
        polygon.calculateMaxBounds(maxBounds)
        val scaleX = bounds.width() / maxBounds.width()
        val scaleY = bounds.height() / maxBounds.height()
        scaleFactor = min(scaleFactor, max(scaleX, scaleY))
    }
    return scaleFactor
}

private fun FloatArray.width(): Float {
    return this[2] - this[0]
}

private fun FloatArray.height(): Float {
    return this[3] - this[1]
}

private fun processPath(
    path: Path,
    size: Size,
    scaleFactor: Float,
    scaleMatrix: Matrix = Matrix(),
): Path {
    scaleMatrix.reset()
    scaleMatrix.apply { scale(x = size.width * scaleFactor, y = size.height * scaleFactor) }
    path.transform(scaleMatrix)
    path.translate(size.center - path.getBounds().center)
    return path
}

private const val GlobalRotationDurationMillis = 4666
private const val MorphIntervalMillis = 650L
private const val FullRotation = 360f
private const val QuarterRotation = FullRotation / 4f
