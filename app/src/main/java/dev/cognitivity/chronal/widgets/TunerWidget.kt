package dev.cognitivity.chronal.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.services.TunerWidgetService
import dev.cognitivity.chronal.ui.theme.AquaGlanceTheme
import dev.cognitivity.chronal.ui.tuner.windows.frequencyToNote
import dev.cognitivity.chronal.ui.tuner.windows.toDisplayNote
import dev.cognitivity.chronal.ui.tuner.windows.transposeFrequency
import kotlin.math.abs


class TunerWidget : GlanceAppWidget() {
    val hzKey = floatPreferencesKey("tuner_hz")
    val bitmapKey = stringPreferencesKey("tuner_bitmap")

    companion object {
        private val smallMode = DpSize(150.dp, 150.dp)
        private val smallWideMode = DpSize(300.dp, 150.dp)
        private val wideMode = DpSize(350.dp, 300.dp)
        private val tallMode = DpSize(150.dp, 300.dp)
    }


    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(smallMode, smallWideMode, wideMode, tallMode),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
                else AquaGlanceTheme.colors
            ) {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val size = LocalSize.current

        val prefs = currentState<Preferences>()
        val hz = prefs[hzKey] ?: 0f

        val base64 = prefs[bitmapKey]
        val bitmap = base64?.let {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        Scaffold(
            titleBar = {
                TitleBar(
                    startIcon = ImageProvider(R.drawable.baseline_music_note_24),
                    iconColor = GlanceTheme.colors.onPrimaryContainer,
//                    title = "${size.width}x${size.height}",
                    title = context.getString(R.string.widget_tuner_title),
                    textColor = GlanceTheme.colors.onSurface
                )
            },
            modifier = GlanceModifier.clickable {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ChronalApp.getInstance().startForegroundService(
                        Intent(context, TunerWidgetService::class.java)
                    )
                } else {
                    ChronalApp.getInstance().startService(
                        Intent(context, TunerWidgetService::class.java)
                    )
                }
            },
            horizontalPadding = 12.dp
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    when(size) {
                        smallMode -> {
                            SmallUI(context, hz)
                        }
                        smallWideMode -> {
                            SmallWideUI(context, hz, bitmap)
                        }
                        wideMode -> {
                            WideUI(context, hz, bitmap)
                        }
                        tallMode -> {
                            TallUI(context, hz, bitmap)
                        }
                    }
                }

                Text(
                    text = context.getString(R.string.widget_tuner_start),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.padding(4.dp)
                )
            }
        }
    }

    @Composable
    fun SmallUI(context: Context, hz: Float) {
        Row(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            NoteNames(context, hz,
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            )
            Spacer(modifier = GlanceModifier.size(8.dp))
            CentsDisplay(horizontal = false, hz,
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            )
        }
    }

    @Composable
    fun SmallWideUI(context: Context, hz: Float, bitmap: Bitmap?) {
        Row(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            NoteNames(context, hz,
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            )
            Spacer(modifier = GlanceModifier.size(8.dp))
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            ) {
                TunerGraph(context,
                    bitmap = bitmap,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(24.dp)
                )
                Spacer(modifier = GlanceModifier.size(8.dp))
                CentsDisplay(horizontal = true, hz,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                )
            }
        }
    }

    @Composable
    fun WideUI(context: Context, hz: Float, bitmap: Bitmap?) {
        Row(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            ) {
                NoteNames(context, hz,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                )
                Spacer(modifier = GlanceModifier.size(8.dp))
                CentsDisplay(horizontal = false, hz,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                )
            }
            Spacer(modifier = GlanceModifier.size(8.dp))
            TunerGraph(context,
                bitmap = bitmap,
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
                    .background(GlanceTheme.colors.surface)
                    .cornerRadius(24.dp)
            )
        }
    }

    @Composable
    fun TallUI(context: Context, hz: Float, bitmap: Bitmap?) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
            ) {
                NoteNames(context, hz,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                )
                Spacer(modifier = GlanceModifier.size(4.dp))
                CentsDisplay(horizontal = false, hz,
                    modifier = GlanceModifier.fillMaxSize()
                        .defaultWeight()
                )
            }
            Spacer(modifier = GlanceModifier.size(8.dp))
            TunerGraph(context,
                bitmap = bitmap,
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
                    .background(GlanceTheme.colors.surface)
                    .cornerRadius(24.dp)
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NoteNames(context: Context, hz: Float, modifier: GlanceModifier = GlanceModifier) {

        @Composable
        fun ColumnScope.TuningInstrument(name: String, hz: Float, color: ColorProvider, colorContainer: ColorProvider, onColorContainer: ColorProvider) {
            val note = frequencyToNote(hz).first

            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .defaultWeight()
                    .background(colorContainer)
                    .cornerRadius(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = color
                    )
                )
                Text(
                    text = toDisplayNote(note),
                    style = TextStyle(
                        fontSize = 32.sp,
                        color = onColorContainer,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        val transpose = ChronalApp.getInstance().settings.transposeNotes.value
        val instrument = ChronalApp.getInstance().settings.primaryInstrument.value
        Row(
            modifier = modifier
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TuningInstrument(context.getString(R.string.tuner_concert_pitch), hz,
                    color = GlanceTheme.colors.onSecondaryContainer,
                    colorContainer = GlanceTheme.colors.secondaryContainer,
                    onColorContainer = GlanceTheme.colors.onSecondaryContainer
                )
                if(transpose) {
                    val transposedHz = transposeFrequency(hz, instrument.transposition)
                    Spacer(modifier = GlanceModifier.size(8.dp))
                    TuningInstrument(instrument.name, transposedHz,
                        color = GlanceTheme.colors.onPrimaryContainer,
                        colorContainer = GlanceTheme.colors.primaryContainer,
                        onColorContainer = GlanceTheme.colors.onPrimaryContainer
                    )
                }
            }
        }
    }

    @Composable
    fun CentsDisplay(horizontal: Boolean, hz: Float, modifier: GlanceModifier = GlanceModifier) {
        val cents = frequencyToNote(hz).second

        @Composable
        fun CentsText(textColor1: ColorProvider, textColor2: ColorProvider) {
            val text: String = if(cents.isNaN()) ""
            else if(cents.toInt() > 0) "♯"
            else if(cents.toInt() < 0) "♭"
            else "♮"

            Text(
                text = (if(cents.toInt() < 0) "" else "+") + cents.toInt().toString(),
                style = TextStyle(
                    fontSize = 42.sp,
                    color = textColor1
                )
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 48.sp,
                    color = textColor2,
                ),
            )
        }

        val containerColor = if (cents.isNaN()) GlanceTheme.colors.surface
            else if (abs(cents) >= 40) GlanceTheme.colors.surfaceVariant
            else if (abs(cents) >= 30) GlanceTheme.colors.secondaryContainer
            else if (abs(cents) >= 20) GlanceTheme.colors.tertiaryContainer
            else if (abs(cents) >= 5) GlanceTheme.colors.primaryContainer
            else GlanceTheme.colors.onSurfaceVariant

        val textColor1 = if (cents.isNaN()) GlanceTheme.colors.surface
            else if (abs(cents) >= 40) GlanceTheme.colors.onSurface
            else if (abs(cents) >= 30) GlanceTheme.colors.onSecondaryContainer
            else if (abs(cents) >= 20) GlanceTheme.colors.onTertiaryContainer
            else if (abs(cents) >= 5) GlanceTheme.colors.onPrimaryContainer
            else GlanceTheme.colors.surface

        val textColor2 = if (cents.isNaN()) GlanceTheme.colors.surface
            else if (abs(cents) >= 40) GlanceTheme.colors.onSurfaceVariant
            else if (abs(cents) >= 30) GlanceTheme.colors.secondary
            else if (abs(cents) >= 20) GlanceTheme.colors.tertiary
            else if (abs(cents) >= 5) GlanceTheme.colors.primary
            else GlanceTheme.colors.inverseOnSurface

        if(horizontal) {
            Row(
                modifier = modifier
                    .background(containerColor)
                    .cornerRadius(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CentsText(textColor1, textColor2)
            }
        } else {
            Column(
                modifier = modifier
                    .background(containerColor)
                    .cornerRadius(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CentsText(textColor1, textColor2)
            }
        }
    }

    @Composable
    fun TunerGraph(context: Context, bitmap: Bitmap?, modifier: GlanceModifier = GlanceModifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if(bitmap == null) {
                Text(
                    text = context.getString(R.string.widget_tuner_graph_unavailable),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            } else {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }
}