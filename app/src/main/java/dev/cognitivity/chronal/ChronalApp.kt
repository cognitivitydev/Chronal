package dev.cognitivity.chronal

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class ChronalApp : Application() {
    lateinit var metronome: Metronome
    lateinit var metronomeSecondary: Metronome
    lateinit var settings: SettingsManager
    var tuner: Tuner? = null
    var widgetGraph: Bitmap? = null
    val developmentBuild = true

    override fun onCreate() {
        super.onCreate()
        application = this

        CoroutineScope(Dispatchers.Default).launch {
            settings = SettingsManager(applicationContext)
            settings.load()

            val state = settings.metronomeState.value

            metronome = Metronome(Rhythm.deserialize(settings.metronomeRhythm.value))
            metronome.bpm = state.bpm
            metronome.beatValue = state.beatValuePrimary
            metronomeSecondary = Metronome(Rhythm.deserialize(settings.metronomeRhythmSecondary.value))
            metronomeSecondary.bpm = state.bpm
            metronomeSecondary.beatValue = state.beatValueSecondary
            metronomeSecondary.active = state.secondaryEnabled
        }
    }

    fun isInitialized(): Boolean {
        return ::metronome.isInitialized && ::settings.isInitialized
    }

    companion object {
        lateinit var application: Application

        fun getInstance(): ChronalApp {
            return application as ChronalApp
        }

        val context: Context
            get() = application.applicationContext

    }
}

fun Double.floor(decimals: Int = 2): Double = floor(this * 10.0.pow(decimals)) / 10.0.pow(decimals)
fun Float.floor(decimals: Int = 2): Float = floor(this * 10f.pow(decimals)) / 10f.pow(decimals)

fun Double.ceil(decimals: Int = 2): Double = ceil(this * 10.0.pow(decimals)) / 10.0.pow(decimals)
fun Float.ceil(decimals: Int = 2): Float = ceil(this * 10f.pow(decimals)) / 10f.pow(decimals)

fun Double.round(decimals: Int = 2) = (this * 10.0.pow(decimals)).roundToInt() / 10.0.pow(decimals)
fun Float.round(decimals: Int = 2) = (this * 10f.pow(decimals)).roundToInt() / 10f.pow(decimals)

fun Dp.toPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.value,
        ChronalApp.context.resources.displayMetrics
    )
}

fun Float.pxToDp(): Dp {
    return ceil(this / ChronalApp.context.resources.displayMetrics.density).dp
}

@Composable
fun Dp.toSp(): TextUnit {
    return with(LocalDensity.current) { this@toSp.toSp() }
}

@Composable
fun TextUnit.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

