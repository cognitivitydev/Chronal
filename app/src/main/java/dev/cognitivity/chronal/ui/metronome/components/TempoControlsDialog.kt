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

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel

@Composable
fun TempoControlsDialog(
    metronome: Metronome,
    viewModel: MetronomeViewModel,
    onDismissRequest: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismissRequest) {
                    Text(context.getString(R.string.generic_close))
                }
            }
        },
        onDismissRequest = {
            resetTapper(viewModel)
            onDismissRequest()
        },
        modifier = Modifier,
        title = {
            Text(context.getString(R.string.metronome_tempo_controls))
        },
        text = {
            TempoControlsDialogContent(scrollState, metronome, viewModel)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )
}

@Composable
fun TempoControlsDialogContent(scrollState: ScrollState, metronome: Metronome, viewModel: MetronomeViewModel) {
    var inputTab by remember { mutableStateOf(0) }

    val scrollable = scrollState.maxValue != 0 && scrollState.maxValue != Int.MAX_VALUE
    Column {
        if(scrollable) HorizontalDivider()

        NavHost(
            navController = rememberNavController(),
            startDestination = "input",
            modifier = Modifier.verticalScroll(scrollState)
                .padding(vertical = if(scrollable) 4.dp else 0.dp)
        ) {
            composable("input") {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InputPage(metronome, viewModel, inputTab,
                        onTabChanged = {
                            inputTab = it
                            resetTapper(viewModel)
                        }
                    )
                }
            }
            composable("modifiers") {
                // TODO
            }
        }

        if(scrollable) HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InputPage(metronome: Metronome, viewModel: MetronomeViewModel, inputTab: Int, onTabChanged: (Int) -> Unit) {
    val navController = rememberNavController()
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
            .fillMaxWidth()
            .widthIn(min = 128.dp)
            .padding(bottom = 4.dp)
    ) {
        toggleableItem(
            checked = inputTab == 0,
            onCheckedChange = {
                onTabChanged(0)
                navController.navigate("manual")
            },
            label = context.getString(R.string.metronome_input_manual),
            icon = {
                Icon(
                    painter = painterResource(id = if (inputTab == 0) R.drawable.baseline_keyboard_24 else R.drawable.outline_keyboard_24),
                    contentDescription = null
                )
            },
            weight = 1f
        )
        toggleableItem(
            checked = inputTab == 1,
            onCheckedChange = {
                onTabChanged(1)
                navController.navigate("tap")
            },
            label = context.getString(R.string.metronome_input_tap),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_music_note_24),
                    contentDescription = null
                )
            },
            weight = 1f
        )
    }

    val fastEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    NavHost(
        navController = navController,
        startDestination = "manual",
        enterTransition = { fadeIn(animationSpec = fastEffectsSpec) },
        exitTransition = { fadeOut(animationSpec = fastEffectsSpec) },
    ) {
        composable("manual") {
            ManualTab(metronome, viewModel)
        }
        composable("tap") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TapTab(viewModel)
            }
        }
    }
}

private fun resetTapper(viewModel: MetronomeViewModel) {
    viewModel.setLastTapTime(0)
    viewModel.setIntervals(emptyList())
}