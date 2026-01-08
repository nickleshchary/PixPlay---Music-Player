package com.ngt.pixplay.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.saket.squiggles.SquigglySlider

enum class SliderStyle {
    DEFAULT,
    SQUIGGLY,
    SLIM
}

@Composable
fun PlayerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    style: SliderStyle,
    isPlaying: Boolean,
    colors: SliderColors,
    modifier: Modifier = Modifier
) {
    when (style) {
        SliderStyle.DEFAULT -> DefaultPlayerSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = colors,
            modifier = modifier
        )
        SliderStyle.SQUIGGLY -> SquigglyPlayerSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            isPlaying = isPlaying,
            colors = colors,
            modifier = modifier
        )
        SliderStyle.SLIM -> SlimPlayerSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = colors,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultPlayerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colors: SliderColors,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        colors = colors,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquigglyPlayerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    isPlaying: Boolean,
    colors: SliderColors,
    modifier: Modifier = Modifier
) {
    SquigglySlider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        colors = colors,
        squigglesSpec = SquigglySlider.SquigglesSpec(
            amplitude = if (isPlaying) (4.dp).coerceAtLeast(2.dp) else 0.dp,
            strokeWidth = 5.5.dp,
            wavelength = 36.dp,
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlimPlayerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colors: SliderColors,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
        track = { sliderState ->
            PlayerSliderTrack(
                sliderState = sliderState,
                colors = colors,
                trackHeight = 6.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}
