package com.ngt.pixplay.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Sleep Timer Bottom Sheet component.
 * Allows users to set a sleep timer with a slider (5-120 minutes)
 * or choose to pause at the end of the current song.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onStartTimer: (minutes: Int) -> Unit,
    onEndOfSong: () -> Unit,
    initialValue: Float = 30f
) {
    if (!isVisible) return
    
    var sleepTimerValue by remember { mutableFloatStateOf(initialValue) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sleep Timer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "${sleepTimerValue.toInt()} minutes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Slider(
                value = sleepTimerValue,
                onValueChange = { sleepTimerValue = it },
                valueRange = 5f..120f,
                steps = (120 - 5) / 5 - 1,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5 min", style = MaterialTheme.typography.labelSmall)
                Text("120 min", style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // End of Song option
            OutlinedButton(
                onClick = {
                    onEndOfSong()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("End of Current Song")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        onStartTimer(sleepTimerValue.toInt())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start Timer")
                }
            }
        }
    }
}
