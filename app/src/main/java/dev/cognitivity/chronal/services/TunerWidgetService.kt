package dev.cognitivity.chronal.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.os.IBinder
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.ui.tuner.removeOutliers
import dev.cognitivity.chronal.widgets.TunerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class TunerWidgetService : Service() {
    val hzKey = floatPreferencesKey("tuner_hz")
    val bitmapKey = stringPreferencesKey("tuner_bitmap")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ChronalApp.getInstance().tuner != null) {
            ChronalApp.getInstance().tuner!!.stop()

            CoroutineScope(Dispatchers.IO).launch {
                val context = applicationContext
                val glanceManager = GlanceAppWidgetManager(context)

                val glanceIds = glanceManager.getGlanceIds(TunerWidget::class.java)
                for (glanceId in glanceIds) {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[hzKey] = 0f
                            this[bitmapKey] = ""
                        }
                    }
                    TunerWidget().update(context, glanceId)
                }
            }

            stopSelf()

            return START_NOT_STICKY
        }

        startForeground(1, createNotification())

        val tuner = Tuner()

        CoroutineScope(Dispatchers.IO).launch {
            val context = applicationContext
            val glanceManager = GlanceAppWidgetManager(context)

            while(ChronalApp.getInstance().tuner != null) {
                val glanceIds = glanceManager.getGlanceIds(TunerWidget::class.java)

                val hz = tuner.hz

                val bitmap = drawPitchGraphBitmap(tuner.history.toList().takeLast(50)) // half of history
                val output = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                val base64 = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)

                for (glanceId in glanceIds) {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[hzKey] = hz
                            this[bitmapKey] = base64
                        }
                    }
                    TunerWidget().update(context, glanceId)
                }

                delay(50)
            }
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("tuner_widget_service", "Tuner widget", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, "tuner_widget_service")
            .setContentTitle(getString(R.string.widget_tuner_notification_title))
            .setContentText(getString(R.string.widget_tuner_notification_text))
            .setSmallIcon(R.drawable.baseline_graphic_eq_24)
            .build()
    }

    fun drawPitchGraphBitmap(history: List<Pair<Long, Float>>, width: Int = 250, height: Int = 250): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val filteredHistory = removeOutliers(
            history.toList().filter { System.currentTimeMillis() - it.first < 10_000 }
        )

        if (filteredHistory.isEmpty()) return bitmap

        val minFreq = filteredHistory.minOf { it.second }
        val maxFreq = filteredHistory.maxOf { it.second }
        val yRange = maxFreq - minFreq

        val paint = Paint().apply {
            color = Color.White.toArgb()
            strokeWidth = 4f
            isAntiAlias = true
        }

        var lastPoint: PointF? = null
        var lastTime: Long? = null

        val firstTime = filteredHistory.first().first
        val lastTimeStamp = filteredHistory.last().first
        val timeRange = (lastTimeStamp - firstTime).coerceAtLeast(1)

        for ((timestamp, freq) in filteredHistory) {
            val x = ((timestamp - firstTime).toFloat() / timeRange) * width
            val y = height - ((freq - minFreq) / yRange) * height
            val currentPoint = PointF(x, y)

            if (lastPoint != null && lastTime != null) {
                val timeDiff = timestamp - lastTime
                if (timeDiff <= 250) {
                    canvas.drawLine(
                        lastPoint.x, lastPoint.y,
                        currentPoint.x, currentPoint.y,
                        paint
                    )
                }
            }

            lastPoint = currentPoint
            lastTime = timestamp
        }

        return bitmap
    }

    override fun onBind(intent: Intent?): IBinder? = null
}