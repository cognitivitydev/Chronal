package dev.cognitivity.chronal.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.MetronomePreset
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.ui.theme.AquaGlanceTheme

class ClockWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val presetJson = context.getSharedPreferences("chronalWidgets", Context.MODE_PRIVATE)
            .getString("preset_${id.hashCode()}", null)

        provideContent {
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
                else AquaGlanceTheme.colors
            ) {
                WidgetContent(presetJson, context)
            }
        }
    }

    @Composable
    private fun WidgetContent(presetJson: String?, context: Context) {
        val preset = if(presetJson != null) MetronomePreset.fromJson(Gson().fromJson(presetJson, JsonObject::class.java)) else null
        val onClick = actionStartActivity<MainActivity>(
            parameters = actionParametersOf(
                ActionParameters.Key<String>("preset") to (preset?.toJson()?.toString() ?: ""),
            )
        )
        val bpm = preset?.state?.bpm ?: (if(ChronalApp.getInstance().isInitialized()) ChronalApp.getInstance().metronome.bpm else 120)

        val size = LocalSize.current
        val squareSize = minOf(size.width, size.height)
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_cookie_12_sided),
                contentDescription = null,
                modifier = GlanceModifier.size(squareSize)
                    .appWidgetBackground()
                    .clickable(onClick = onClick, rippleOverride = R.drawable.widget_empty_clickable),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.widgetBackground)
            )
            Box(
                modifier = GlanceModifier.size(squareSize * 0.8f).cornerRadius(squareSize * 0.8f / 2f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.widget_clock),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.secondaryContainer)
                )
                Image(
                    provider = ImageProvider(R.drawable.widget_clock),
                    contentDescription = null,
                    modifier = GlanceModifier.size(squareSize * 0.7f),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.tertiaryContainer)
                )
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$bpm",
                        modifier = GlanceModifier.padding(end = 2.dp),
                        style = TextStyle(
                            fontSize = 28.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Text(
                        text = context.getString(R.string.metronome_bpm),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}