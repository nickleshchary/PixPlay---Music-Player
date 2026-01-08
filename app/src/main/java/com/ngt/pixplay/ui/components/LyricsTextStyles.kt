package com.ngt.pixplay.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Provides text styles for lyrics display with various effects.
 * Supports glow effects, animations, and Material You color adoption.
 */
object LyricsTextStyles {
    
    /**
     * Creates styled TextStyle for current (active) lyric line with glow effect.
     */
    fun activeLineStyle(
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        glowIntensity: Float = 1f,
        glowEnabled: Boolean = true
    ): TextStyle {
        val shadow = if (glowEnabled && glowIntensity > 0f) {
            Shadow(
                color = accentColor.copy(alpha = 0.6f * glowIntensity),
                offset = Offset.Zero,
                blurRadius = 16f * glowIntensity
            )
        } else null
        
        return TextStyle(
            color = accentColor,
            fontSize = textSize.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = shadow
        )
    }
    
    /**
     * Creates styled TextStyle for inactive lyric lines.
     */
    fun inactiveLineStyle(
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        alpha: Float = 0.5f
    ): TextStyle {
        return TextStyle(
            color = accentColor.copy(alpha = alpha),
            fontSize = textSize.sp,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp
        )
    }
    
    /**
     * Creates appropriate text style based on whether the line is current or not.
     */
    fun getLineStyle(
        isCurrentLine: Boolean,
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        glowEnabled: Boolean = true,
        glowIntensity: Float = 1f
    ): TextStyle {
        return if (isCurrentLine) {
            activeLineStyle(
                accentColor = accentColor,
                textSize = textSize,
                lineSpacing = lineSpacing,
                textAlign = textAlign,
                glowIntensity = glowIntensity,
                glowEnabled = glowEnabled
            )
        } else {
            inactiveLineStyle(
                accentColor = accentColor,
                textSize = textSize,
                lineSpacing = lineSpacing,
                textAlign = textAlign
            )
        }
    }
    
    /**
     * Creates glow-enhanced active line style with pulsing effect.
     * @param fillProgress Progress of the fill animation (0-1)
     * @param pulseProgress Progress of the pulse animation (0-1)
     */
    fun glowingActiveLineStyle(
        accentColor: Color,
        textSize: Float,
        lineSpacing: Float,
        textAlign: TextAlign,
        fillProgress: Float,
        pulseProgress: Float
    ): TextStyle {
        val pulseEffect = (kotlin.math.sin(pulseProgress * Math.PI.toFloat()) * 0.15f).coerceIn(0f, 0.15f)
        val glowIntensity = (fillProgress + pulseEffect).coerceIn(0f, 1.2f)
        
        return TextStyle(
            color = accentColor,
            fontSize = textSize.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = textAlign,
            lineHeight = (textSize * lineSpacing).sp,
            shadow = Shadow(
                color = accentColor.copy(alpha = 0.4f * glowIntensity),
                offset = Offset.Zero,
                blurRadius = 12f + (8f * glowIntensity)
            )
        )
    }
    
    /**
     * Creates a horizontal gradient brush for slide/karaoke effect.
     */
    fun createFillGradient(
        accentColor: Color,
        fillProgress: Float
    ): Brush {
        return Brush.horizontalGradient(
            0.0f to accentColor.copy(alpha = 0.3f),
            (fillProgress * 0.7f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.9f),
            fillProgress to accentColor,
            (fillProgress + 0.1f).coerceIn(0f, 1f) to accentColor.copy(alpha = 0.7f),
            1.0f to accentColor.copy(alpha = 0.35f)
        )
    }
    
    /**
     * Creates Apple Music style word span with progressive highlight.
     * @param isWordActive Whether this word is currently being sung
     * @param hasWordPassed Whether this word has already been sung
     * @param isActiveLine Whether this line is the current line
     * @param smoothProgress Progress of word animation (0-1)
     */
    fun appleWordStyle(
        accentColor: Color,
        isWordActive: Boolean,
        hasWordPassed: Boolean,
        isActiveLine: Boolean,
        smoothProgress: Float
    ): SpanStyle {
        val wordAlpha = when {
            !isActiveLine -> 0.55f
            hasWordPassed -> 1f
            isWordActive -> 0.55f + (0.45f * smoothProgress)
            else -> 0.35f
        }
        
        val wordWeight = when {
            !isActiveLine -> FontWeight.SemiBold
            hasWordPassed -> FontWeight.Bold
            isWordActive -> FontWeight.ExtraBold
            else -> FontWeight.Normal
        }
        
        val wordShadow = when {
            isWordActive -> Shadow(
                color = accentColor.copy(alpha = 0.15f + (0.35f * smoothProgress)),
                offset = Offset.Zero,
                blurRadius = 8f + (10f * smoothProgress)
            )
            hasWordPassed && isActiveLine -> Shadow(
                color = accentColor.copy(alpha = 0.25f),
                offset = Offset.Zero,
                blurRadius = 10f
            )
            else -> null
        }
        
        return SpanStyle(
            color = accentColor.copy(alpha = wordAlpha),
            fontWeight = wordWeight,
            shadow = wordShadow
        )
    }
    
    /**
     * Creates Glow style word span.
     */
    fun glowWordStyle(
        accentColor: Color,
        isWordActive: Boolean,
        hasWordPassed: Boolean,
        isActiveLine: Boolean,
        fillProgress: Float
    ): SpanStyle {
        val glowIntensity = fillProgress * fillProgress
        val brightness = 0.45f + (0.55f * fillProgress)
        
        val wordColor = when {
            !isActiveLine -> accentColor.copy(alpha = 0.5f)
            isWordActive || hasWordPassed -> accentColor.copy(alpha = brightness)
            else -> accentColor.copy(alpha = 0.35f)
        }
        
        val wordWeight = when {
            !isActiveLine -> FontWeight.Bold
            isWordActive -> FontWeight.ExtraBold
            hasWordPassed -> FontWeight.Bold
            else -> FontWeight.Medium
        }
        
        val wordShadow = when {
            isWordActive && glowIntensity > 0.05f -> Shadow(
                color = accentColor.copy(alpha = 0.5f + (0.3f * glowIntensity)),
                offset = Offset.Zero,
                blurRadius = 16f + (12f * glowIntensity)
            )
            hasWordPassed -> Shadow(
                color = accentColor.copy(alpha = 0.25f),
                offset = Offset.Zero,
                blurRadius = 8f
            )
            else -> null
        }
        
        return SpanStyle(
            color = wordColor,
            fontWeight = wordWeight,
            shadow = wordShadow
        )
    }
    
    /**
     * Smooth progress calculation using smoothstep function.
     */
    fun smoothProgress(linear: Float): Float {
        return linear * linear * (3f - 2f * linear)
    }
}
