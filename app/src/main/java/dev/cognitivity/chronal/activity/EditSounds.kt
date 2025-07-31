package dev.cognitivity.chronal.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch

// not implemented
class EditSounds: ComponentActivity() {
    private val sounds = listOf(
        R.string.sound_click to (R.raw.click_hi to R.raw.click_lo),
        R.string.sound_sine to (R.raw.sine_hi to R.raw.sine_lo),
        R.string.sound_square to (R.raw.square_hi to R.raw.square_lo),
        R.string.sound_clap to (R.raw.clap_hi to R.raw.clap_lo),
        R.string.sound_bell to (R.raw.bell_hi to R.raw.bell_lo),
        R.string.sound_tambourine to (R.raw.tambourine_hi to R.raw.tambourine_lo),
        R.string.sound_block to (R.raw.block_hi to R.raw.block_lo),
    )

    @SuppressLint("SourceLockedOrientationActivity")
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
        val setting = ChronalApp.getInstance().settings.metronomeSounds
        val soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        var showAttribution by remember { mutableStateOf(false) }

        var selectedSound by remember { mutableStateOf(setting.value) }
        var selection by remember { mutableIntStateOf(0) }

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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selected:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    val highSelection = sounds[selectedSound.first]
                    val highName = getString(highSelection.first)
                    FilledTonalButton(
                        modifier = Modifier.heightIn(ButtonDefaults.MinHeight)
                            .align(Alignment.CenterVertically),
                        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
                        onClick = {
                            val soundId = soundPool.load(this@EditSounds, highSelection.second.first, 1)

                            soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
                                if (status == 0 && sampleId == soundId) {
                                    sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_volume_up_24),
                            contentDescription = getString(R.string.edit_sounds_play_high, highName)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
                        Text(getString(R.string.edit_sounds_high, highName))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val lowSelection = sounds[selectedSound.second]
                    val lowName = getString(lowSelection.first)
                    FilledTonalButton(
                        modifier = Modifier.heightIn(ButtonDefaults.MinHeight)
                            .align(Alignment.CenterVertically),
                        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
                        onClick = {
                            val soundId = soundPool.load(this@EditSounds, lowSelection.second.second, 1)

                            soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
                                if (status == 0 && sampleId == soundId) {
                                    sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_volume_up_24),
                            contentDescription = getString(R.string.edit_sounds_play_low, lowName),
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
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
                            "Sound",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "High",
                            modifier = Modifier.width(48.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Low",
                            modifier = Modifier.width(48.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(sounds.size) { i ->
                    val sound = sounds[i]
                    val name = sound.first
                    val (high, low) = sound.second
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
                        Text(getString(name),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                selection = i
                                val soundId = soundPool.load(this@EditSounds, high, 1)

                                soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
                                    if (status == 0 && sampleId == soundId) {
                                        sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(if(selectedSound.first == i) R.drawable.baseline_volume_up_24 else R.drawable.outline_volume_up_24),
                                contentDescription = getString(R.string.generic_selected),
                                tint = if(selectedSound.first == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                selection = i
                                val soundId = soundPool.load(this@EditSounds, low, 1)

                                soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
                                    if (status == 0 && sampleId == soundId) {
                                        sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(if(selectedSound.second == i) R.drawable.baseline_volume_up_24 else R.drawable.outline_volume_up_24),
                                contentDescription = getString(R.string.generic_selected),
                                tint = if(selectedSound.second == i) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text(getString(sounds[selection].first),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Set as...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                ToggleButton(
                                    checked = selectedSound.first == selection,
                                    onCheckedChange = {
                                        selectedSound = selection to selectedSound.second
                                        setting.value = selectedSound
                                        scope.launch {
                                            ChronalApp.getInstance().settings.save()
                                        }
                                    },
                                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    contentPadding = ButtonDefaults.ContentPadding
                                ) {
                                    if(selectedSound.first == selection) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text(getString(R.string.editor_emphasis_high))
                                }
                                ToggleButton(
                                    checked = selectedSound.second == selection,
                                    onCheckedChange = {
                                        selectedSound = selectedSound.first to selection
                                        setting.value = selectedSound
                                        scope.launch {
                                            ChronalApp.getInstance().settings.save()
                                        }
                                    },
                                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    contentPadding = ButtonDefaults.ContentPadding
                                ) {
                                    if(selectedSound.second == selection) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text(getString(R.string.editor_emphasis_low))
                                }
                            }
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