/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.ui.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.metronome.MetronomeTrack

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackItem(
    track: MetronomeTrack,
    index: Int,
    topRounded: Boolean,
    bottomRounded: Boolean,
    onCheckedChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val enabled = track.enabled
    val shape = RoundedCornerShape(
        topStart = if (topRounded) 12.dp else 6.dp,
        topEnd = if (topRounded) 12.dp else 6.dp,
        bottomStart = if (bottomRounded) 12.dp else 6.dp,
        bottomEnd = if (bottomRounded) 12.dp else 6.dp
    )

    val animatedFontWeight by animateIntAsState(
        targetValue = if(enabled) 900 else 300,
        animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
        label = "animatedFontWeight"
    )
    val animatedFontColor by animateColorAsState(
        targetValue = if(enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
    )

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                onClick()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = track.name,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = animatedFontColor,
            fontWeight = FontWeight(animatedFontWeight),
            modifier = Modifier.weight(1f)
                .padding(horizontal = 16.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(32.dp)
        )
        Box(
            modifier = Modifier.width(1.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        val palette = track.color.getPalette()
        Switch(
            checked = enabled,
            onCheckedChange = {
                onCheckedChanged(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.onColor,
                checkedTrackColor = palette.color,
            ),
            modifier = Modifier.padding(start = 12.dp, end = 16.dp)
        )
    }
}