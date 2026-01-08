package com.ngt.pixplay.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Lyrics animation styles
 */
enum class LyricsAnimationStyle {
    NONE,
    FADE,
    GLOW,
    SLIDE,
    KARAOKE,
    APPLE
}

/**
 * Animation state holder for lyrics line
 */
data class LyricsAnimationState(
    val fillProgress: Float = 0f,
    val pulseProgress: Float = 0f,
    val glowIntensity: Float = 0f
)

/**
 * Separate animation implementations for each style
 */
object LyricsAnimations {

    // ===== NONE STYLE =====
    /**
     * No animation - simple color change for active line
     */
    fun noneStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign
    ): TextStyle {
        return TextStyle(
            color = if (isCurrentLine) accentColor else accentColor.copy(alpha = 0.4f),
            fontSize = textSize.sp,
            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp
        )
    }

    // ===== FADE STYLE =====
    /**
     * Fade animation - smooth alpha transition
     */
    fun fadeStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        animatedAlpha: Float // 0-1, animated externally
    ): TextStyle {
        val alpha = if (isCurrentLine) animatedAlpha else 0.35f
        return TextStyle(
            color = accentColor.copy(alpha = alpha),
            fontSize = textSize.sp,
            fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp
        )
    }

    // ===== GLOW STYLE =====
    /**
     * Glow animation - pulsing shadow effect with gradient fill (PixPlay style)
     * Note: For full gradient text effect, use glowAnnotatedText() instead
     */
    fun glowStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        fillProgress: Float,  // 0-1, fill animation
        pulseProgress: Float  // 0-1, continuous pulse
    ): TextStyle {
        val pulseEffect = (kotlin.math.sin(pulseProgress * Math.PI.toFloat()) * 0.15f).coerceIn(0f, 0.15f)
        val glowIntensity = if (isCurrentLine) (fillProgress + pulseEffect).coerceIn(0f, 1.2f) else 0f
        
        val shadow = if (isCurrentLine && glowIntensity > 0f) {
            Shadow(
                color = accentColor.copy(alpha = 0.8f * glowIntensity),
                offset = Offset.Zero,
                blurRadius = 28f * (1f + pulseEffect)
            )
        } else null
        
        // Color alpha based on fill progress for active line
        val colorAlpha = if (isCurrentLine) {
            0.5f + (0.5f * fillProgress)
        } else {
            0.4f
        }
        
        return TextStyle(
            color = accentColor.copy(alpha = colorAlpha),
            fontSize = textSize.sp,
            fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = shadow
        )
    }
    
    /**
     * Creates horizontal gradient brush for glow effect (left-to-right fill)
     */
    fun createGlowGradient(
        accentColor: Color,
        fillProgress: Float
    ): Brush {
        return Brush.horizontalGradient(
            0.0f to accentColor.copy(alpha = 0.3f),
            (fillProgress * 0.7f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.9f),
            fillProgress to accentColor,
            (fillProgress + 0.1f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.7f),
            1.0f to accentColor.copy(alpha = if (fillProgress >= 1f) 1f else 0.3f)
        )
    }
    
    /**
     * Calculate bounce scale for glow animation
     */
    fun calculateBounceScale(fillProgress: Float): Float {
        return if (fillProgress < 0.3f) {
            // Gentler rise during fill
            1f + (kotlin.math.sin(fillProgress * 3.33f * Math.PI.toFloat()) * 0.03f)
        } else {
            // Hold at normal scale
            1f
        }
    }

    // ===== SLIDE STYLE =====
    /**
     * Slide animation - horizontal gradient fill effect
     * Returns both TextStyle and optional Brush for gradient text
     */
    fun slideStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        slideProgress: Float // 0-1, slide from left to right
    ): TextStyle {
        // For slide, we use shadow to create the illusion of progressive highlighting
        val shadow = if (isCurrentLine && slideProgress > 0f) {
            Shadow(
                color = accentColor.copy(alpha = 0.4f * slideProgress),
                offset = Offset(2f * slideProgress, 0f),
                blurRadius = 6f * slideProgress
            )
        } else null
        
        return TextStyle(
            color = if (isCurrentLine) {
                accentColor.copy(alpha = 0.5f + (0.5f * slideProgress))
            } else {
                accentColor.copy(alpha = 0.35f)
            },
            fontSize = textSize.sp,
            fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = shadow
        )
    }

    // ===== KARAOKE STYLE =====
    /**
     * Karaoke animation - bold highlight with quick fill
     */
    fun karaokeStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        fillProgress: Float // 0-1, quick fill animation
    ): TextStyle {
        val highlightAlpha = if (isCurrentLine) 0.4f + (0.6f * fillProgress) else 0.3f
        
        val shadow = if (isCurrentLine && fillProgress > 0.3f) {
            Shadow(
                color = accentColor.copy(alpha = 0.3f),
                offset = Offset.Zero,
                blurRadius = 4f
            )
        } else null
        
        return TextStyle(
            color = accentColor.copy(alpha = highlightAlpha),
            fontSize = textSize.sp,
            fontWeight = if (isCurrentLine && fillProgress > 0.5f) FontWeight.Black else FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = shadow
        )
    }

    // ===== APPLE STYLE =====
    /**
     * Apple Music style - smooth progressive highlight with elegant glow
     */
    fun appleStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        fillProgress: Float,  // 0-1, smooth fill
        pulseProgress: Float  // 0-1, subtle breathing
    ): TextStyle {
        // Smooth progress curve (ease in-out)
        val smoothFill = fillProgress * fillProgress * (3f - 2f * fillProgress)
        val breathe = (kotlin.math.sin(pulseProgress * Math.PI.toFloat() * 2f) * 0.05f).coerceIn(0f, 0.05f)
        
        val alpha = if (isCurrentLine) {
            0.55f + (0.45f * smoothFill) + breathe
        } else {
            0.4f
        }
        
        val shadow = if (isCurrentLine && smoothFill > 0.1f) {
            Shadow(
                color = accentColor.copy(alpha = 0.15f + (0.35f * smoothFill)),
                offset = Offset.Zero,
                blurRadius = 8f + (10f * smoothFill)
            )
        } else null
        
        return TextStyle(
            color = accentColor.copy(alpha = alpha),
            fontSize = textSize.sp,
            fontWeight = when {
                !isCurrentLine -> FontWeight.SemiBold
                smoothFill > 0.7f -> FontWeight.ExtraBold
                smoothFill > 0.3f -> FontWeight.Bold
                else -> FontWeight.Medium
            },
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = shadow
        )
    }

    /**
     * Get animation durations for each style
     */
    fun getFillDuration(style: LyricsAnimationStyle): Int {
        return when (style) {
            LyricsAnimationStyle.NONE -> 0
            LyricsAnimationStyle.FADE -> 400
            LyricsAnimationStyle.GLOW -> 1200
            LyricsAnimationStyle.SLIDE -> 1000
            LyricsAnimationStyle.KARAOKE -> 500
            LyricsAnimationStyle.APPLE -> 800
        }
    }

    fun getPulseDuration(style: LyricsAnimationStyle): Int {
        return when (style) {
            LyricsAnimationStyle.NONE -> 0
            LyricsAnimationStyle.FADE -> 0
            LyricsAnimationStyle.GLOW -> 3000
            LyricsAnimationStyle.SLIDE -> 0
            LyricsAnimationStyle.KARAOKE -> 0
            LyricsAnimationStyle.APPLE -> 4000
        }
    }

    fun usesPulse(style: LyricsAnimationStyle): Boolean {
        return style in listOf(LyricsAnimationStyle.GLOW, LyricsAnimationStyle.APPLE)
    }

    fun usesFill(style: LyricsAnimationStyle): Boolean {
        return style != LyricsAnimationStyle.NONE
    }
}
