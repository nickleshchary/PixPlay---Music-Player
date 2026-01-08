package com.ngt.pixplay.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val contentAlpha = if (enabled) 1f else 0.38f
    
    ListItem(
        headlineContent = { 
            Text(
                title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            ) 
        },
        supportingContent = { 
            Text(
                subtitle, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            ) 
        },
        leadingContent = {
            FilledTonalIconButton(
                onClick = { }, enabled = false,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = contentAlpha),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = contentAlpha)
                )
            ) { Icon(imageVector = icon, contentDescription = null) }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                thumbContent = if (checked) { 
                    { Icon(Icons.Default.Check, null, Modifier.size(SwitchDefaults.IconSize)) } 
                } else null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) 
        },
        supportingContent = if (subtitle != null) {
            { 
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
        } else null,
        leadingContent = {
            FilledTonalIconButton(
                onClick = { }, enabled = false,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) { Icon(imageVector = icon, contentDescription = null) }
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OptionsBottomSheet(
    title: String,
    options: List<Pair<T, String>>,
    selectedValue: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            options.forEach { (value, label) ->
                ListItem(
                    headlineContent = { 
                        Text(
                            label, 
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedValue == value) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    leadingContent = {
                        RadioButton(
                            selected = selectedValue == value,
                            onClick = { onOptionSelected(value) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    modifier = Modifier
                        .clickable { onOptionSelected(value) }
                        .padding(horizontal = 8.dp),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            }
        }
    }
}
