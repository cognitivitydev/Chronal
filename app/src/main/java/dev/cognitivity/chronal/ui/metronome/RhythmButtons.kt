/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal.ui.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmPrimary
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmSecondary

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RhythmButtons(navController: NavController, modifier: Modifier = Modifier) {
    val metronome = ChronalApp.getInstance().metronome
    val secondaryEnabled = metronome.getTrack(1).enabled
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val simpleRhythmPrimary = ChronalApp.getInstance().settings.metronomeSimpleRhythm.value
    val parsedRhythmPrimary = metronome.getTrack(0).getRhythm()
    val isAdvancedPrimary = simpleRhythmPrimary == SimpleRhythm(0 to 0, 0, 0)

    val primaryTimeSignature = if(isAdvancedPrimary) {
        parsedRhythmPrimary.measures[0].timeSig
    } else {
        simpleRhythmPrimary.timeSignature
    }
    val primarySubdivision = if(isAdvancedPrimary || simpleRhythmPrimary.subdivision == 0) { // advanced or auto
        primaryTimeSignature.second
    } else {
        simpleRhythmPrimary.subdivision
    }

    val simpleRhythmSecondary = ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value
    val parsedRhythmSecondary = metronome.getTrack(1).getRhythm()
    val isAdvancedSecondary = simpleRhythmSecondary == SimpleRhythm(0 to 0, 0, 0)

    val secondaryTimeSignature = if(isAdvancedSecondary) {
        parsedRhythmSecondary.measures[0].timeSig
    } else {
        simpleRhythmSecondary.timeSignature
    }
    val secondarySubdivision = if(isAdvancedSecondary || simpleRhythmSecondary.subdivision == 0) {
        secondaryTimeSignature.second
    } else {
        simpleRhythmSecondary.subdivision
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .weight(1f)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                showRhythmPrimary = true
            }
        ) {
            DrawContent(primaryTimeSignature, primarySubdivision, MaterialTheme.colorScheme.onPrimaryContainer)
        }
        val secondaryBackground by animateColorAsState(
            targetValue = if(currentRoute == "conductor") MaterialTheme.colorScheme.surfaceContainerLow
                else if(secondaryEnabled) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "secondaryBackground"
        )
        val secondaryText by animateColorAsState(
            targetValue = if(currentRoute == "conductor") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                else if(secondaryEnabled) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "secondaryText"
        )
        Box(
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .weight(1f)
                .align(Alignment.CenterVertically)
                .clip(RoundedCornerShape(16.dp))
                .background(secondaryBackground)
                .clickable {
                    showRhythmSecondary = true
                }
        ) {
            DrawContent(secondaryTimeSignature, secondarySubdivision, secondaryText)
        }
    }
}

@Composable
fun DrawContent(timeSignature: Pair<Int, Int>, subdivision: Int, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(Modifier.fillMaxHeight(0.75f)) {
            MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second, textColor)
        }
        Box(
            modifier = Modifier.fillMaxHeight()
                .width(IntrinsicSize.Min)
                .align(Alignment.CenterVertically)
        ) {
            val isTuplet = (subdivision and (subdivision - 1)) != 0
            val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
            val char = MusicFont.Notation.convert(noteValue, false)

            MusicFont.Notation.NoteCentered(
                note = MusicFont.Notation.entries.find { it.char == char } ?: MusicFont.Notation.N_QUARTER,
                color = textColor,
                size = 52.dp,
                modifier = Modifier.align(Alignment.Center)
            )
            if(isTuplet) {
                Row(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.height(1.dp)
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .defaultMinSize(minWidth = 16.dp)
                            .align(Alignment.CenterVertically)
                            .background(textColor)
                    )
                    Text(
                        text = "3",
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(4.dp)
                    )
                    Box(
                        modifier = Modifier.height(1.dp)
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .defaultMinSize(minWidth = 16.dp)
                            .align(Alignment.CenterVertically)
                            .background(textColor)
                    )
                }
            }
        }
    }
}
