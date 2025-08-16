package dev.cognitivity.chronal.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomePreset
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.zip.GZIPInputStream

class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val data = intent.data

        setContent {
            MetronomeTheme {
                MainContent(data)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent(data: Uri?) {
        val scope = rememberCoroutineScope()
        val preset = parsePreset(data)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(getString(R.string.import_preset_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { finish() }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if(preset == null) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_attach_file_off_24),
                        contentDescription = getString(R.string.import_preset_malformed),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = getString(R.string.import_preset_malformed),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 32.dp)
                    )

                    val created = preset.timestamp
                    val time = SimpleDateFormat.getDateTimeInstance(2, 2).format(created)
                    Text(
                        text = getString(R.string.presets_created_at, time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                    Button(
                        modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight),
                        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                        onClick = {
                            val settings = ChronalApp.getInstance().settings
                            settings.metronomePresets.value.add(preset)
                            scope.launch {
                                settings.save()
                                val intent = Intent(context, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra("preset", preset.toJson().toString())
                                startActivity(intent)
                                finish()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_download_24),
                            contentDescription = getString(R.string.import_preset_button),
                        )
                        Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
                        Text(context.getString(R.string.import_preset_button),
                            style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 32.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_music_note_24),
                            contentDescription = getString(R.string.presets_bpm, preset.state.bpm),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getString(R.string.presets_bpm, preset.state.bpm),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(32.dp)
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = getString(R.string.presets_rhythm_primary),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            RhythmInfo(preset, true)
                        }

                        val enabled = preset.state.secondaryEnabled
                        Column(
                            modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (enabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = getString(R.string.presets_rhythm_secondary),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            RhythmInfo(preset, false, preset.state.secondaryEnabled)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RhythmInfo(
        preset: MetronomePreset,
        isPrimary: Boolean,
        enabled: Boolean = true
    ) {
        val textColor = if(enabled) {
            if(isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
        } else MaterialTheme.colorScheme.onSurface
        val rhythm = if(isPrimary) preset.primaryRhythm else preset.secondaryRhythm
        val simpleRhythm = if(isPrimary) preset.primarySimpleRhythm else preset.secondarySimpleRhythm
        val isAdvanced = simpleRhythm.timeSignature == 0 to 0
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if(!enabled) {
                Box(
                    modifier = Modifier.height(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = getString(R.string.presets_disabled),
                        tint = textColor,
                        modifier = Modifier.size(64.dp)
                            .align(Alignment.Center)
                    )
                }
                return
            }
            val timeSignature = preset.primaryRhythm.measures[0].timeSig
            Box(modifier = Modifier.weight(1f).height(64.dp)) {
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second, textColor)
                }
            }
            Box(
                modifier = Modifier.fillMaxHeight()
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                val subdivision = if(isAdvanced) rhythm.measures[0].timeSig.second else simpleRhythm.subdivision
                val isTuplet = (subdivision and (subdivision - 1)) != 0
                val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
                val char = MusicFont.Notation.convert(noteValue, false)

                MusicFont.Notation.NoteCentered(
                    note = MusicFont.Notation.entries.find { it.char == char } ?: MusicFont.Notation.N_QUARTER,
                    color = textColor,
                    size = 64.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
                if(isTuplet) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .offset(y = (-16).dp),
                    ) {
                        Box(
                            modifier = Modifier.height(1.dp)
                                .padding(horizontal = 4.dp)
                                .weight(1f)
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
                                .padding(horizontal = 4.dp)
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .background(textColor)
                        )
                    }
                }
            }
        }
    }

    fun parsePreset(data: Uri?): MetronomePreset? {
        if(data == null) return null

        try {
            val content = contentResolver.openInputStream(data)?.bufferedReader()?.use { it.readText() } ?: return null
            val bytes = Base64.decode(content, Base64.DEFAULT)
            val decompressed = GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader().use { it.readText() }
            return MetronomePreset.fromJson(Gson().fromJson(decompressed, JsonObject::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}