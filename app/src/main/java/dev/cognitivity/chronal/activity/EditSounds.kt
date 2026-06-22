/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

package dev.cognitivity.chronal.activity

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.sound.Sound
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.settings.Setting
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch

class EditSounds: BaseActivity() {
    private data class SoundOption(
        val packId: String,
        val label: String,
        val highRes: Int,
        val lowRes: Int,
    )

    private val sounds = SoundPack.builtins().mapNotNull { pack ->
        val label = pack.name
        val high = (pack.getSound(1) as? Sound.Resource)?.resId
        val low = (pack.getSound(0) as? Sound.Resource)?.resId
        if(high != null && low != null) {
            SoundOption(pack.id, label, high, low)
        } else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        val scope = rememberCoroutineScope()
        val soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        DisposableEffect(Unit) {
            onDispose {
                soundPool.release()
            }
        }

        var showAttribution by remember { mutableStateOf(false) }

        var selectedPackId by remember {
            mutableStateOf(ChronalApp.getInstance().metronome.tracks[0].soundPack.id)
        }
        var selection by remember { mutableIntStateOf(0) }

        fun playResource(resId: Int) {
            val soundId = soundPool.load(this@EditSounds, resId, 1)
            soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
                if (status == 0 && sampleId == soundId) {
                    sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(getString(R.string.edit_sounds_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = getString(R.string.generic_back))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAttribution = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = getString(R.string.generic_info)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text("Selected:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))

                    val selectedSound = sounds.firstOrNull { it.packId == selectedPackId } ?: sounds.first()
                    val highName = selectedSound.label
                    FilledTonalButton(
                        modifier = Modifier.heightIn(ButtonDefaults.MinHeight)
                            .align(Alignment.CenterVertically),
                        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
                        onClick = {
                            playResource(selectedSound.highRes)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_volume_up_24),
                            contentDescription = getString(R.string.edit_sounds_play_high, highName)
                        )
                        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
                        Text(getString(R.string.edit_sounds_high, highName))
                    }
                    Spacer(Modifier.width(8.dp))
                    val lowName = selectedSound.label
                    FilledTonalButton(
                        modifier = Modifier.heightIn(ButtonDefaults.MinHeight)
                            .align(Alignment.CenterVertically),
                        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
                        onClick = {
                            playResource(selectedSound.lowRes)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_volume_up_24),
                            contentDescription = getString(R.string.edit_sounds_play_low, lowName),
                        )
                        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
                        Text(getString(R.string.edit_sounds_low, lowName))
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = getString(R.string.edit_sounds_header_sound),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = getString(R.string.edit_sounds_header_high),
                            modifier = Modifier.width(48.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getString(R.string.edit_sounds_header_low),
                            modifier = Modifier.width(48.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(sounds.size) { i ->
                    val sound = sounds[i]
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if(selection == i) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface)
                            .clickable {
                                selection = if(selection == i) -1 else i
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sound.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                selection = i
                                playResource(sound.highRes)
                            }
                        ) {
                            Icon(
                                painter = painterResource(if(selectedPackId == sound.packId) R.drawable.baseline_volume_up_24 else R.drawable.outline_volume_up_24),
                                contentDescription = getString(R.string.generic_selected),
                                tint = if(selectedPackId == sound.packId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                selection = i
                                playResource(sound.lowRes)
                            }
                        ) {
                            Icon(
                                painter = painterResource(if(selectedPackId == sound.packId) R.drawable.baseline_volume_up_24 else R.drawable.outline_volume_up_24),
                                contentDescription = getString(R.string.generic_selected),
                                tint = if(selectedPackId == sound.packId) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }

            if(selection != -1) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sounds[selection].label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        FilledTonalButton(onClick = {
                            val selected = sounds[selection].packId
                            selectedPackId = selected
                            val pack = SoundPack.byId(selected) ?: SoundPack.default()
                            val metronome = ChronalApp.getInstance().metronome
                            metronome.tracks.forEach { track ->
                                track.soundPack = pack
                            }

                            val config = Settings.METRONOME_CONFIG.get()
                            Settings.METRONOME_CONFIG.set(
                                config.copy(tracks = config.tracks.map { it.copy(soundPackId = pack.id) })
                            )
                            scope.launch {
                                Setting.saveAll()
                            }
                        }) {
                            Text(getString(R.string.generic_confirm))
                        }
                    }
                }
            }

            if(showAttribution) {
                AlertDialog(
                    onDismissRequest = { showAttribution = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = getString(R.string.edit_sounds_attribution)
                        )
                    },
                    title = { Text(getString(R.string.edit_sounds_attribution_title)) },
                    text = {
                        Column {
                            Text(getString(R.string.edit_sounds_attribution_text))
                            Row {
                                TextButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, "http://www.ludwigmueller.net/en/".toUri())
                                    startActivity(intent)
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_open_in_new_24),
                                        contentDescription = getString(R.string.generic_website)
                                    )
                                    Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
                                    Text(getString(R.string.generic_website))
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, "https://stash.reaper.fm/v/40824/Metronomes.zip".toUri())
                                    startActivity(intent)
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_download_24),
                                        contentDescription = getString(R.string.generic_download),
                                    )
                                    Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
                                    Text(getString(R.string.generic_download))
                                }
                            }

                            Row(
                                modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, "https://creativecommons.org/publicdomain/zero/1.0/".toUri())
                                        startActivity(intent)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_counter_0_24),
                                    contentDescription = getString(R.string.edit_sounds_cczero_short),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(getString(R.string.edit_sounds_cczero_long))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAttribution = false }) {
                            Text(getString(R.string.generic_okay))
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}