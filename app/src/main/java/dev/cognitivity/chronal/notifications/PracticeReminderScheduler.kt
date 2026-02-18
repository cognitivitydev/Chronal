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

package dev.cognitivity.chronal.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.settings.Settings
import java.util.Calendar

object PracticeReminderScheduler {
    private const val NOTIFICATION_ID = 100
    private const val CHANNEL_ID = "practice_reminder"

    fun initialize(context: Context) {
        Log.d("PracticeReminder", "Initializing scheduler")

        createNotificationChannel(context)

        if(Settings.PRACTICE_REMINDER_NOTIFICATION.get()) {
            scheduleDailyReminder(context)
        }
    }

    fun scheduleAt(alarmManager: AlarmManager, millis: Long, pendingIntent: PendingIntent) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
            Log.d("PracticeReminder", "Scheduled exact alarm (legacy version)")
            return
        }
        if(alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
            Log.d("PracticeReminder", "Scheduled exact alarm (permission granted)")
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
            Log.d("PracticeReminder", "Scheduled inexact alarm (no permission)")
        }
    }

    fun scheduleDailyReminder(context: Context) {
        Log.d("PracticeReminder", "Scheduling daily reminder")

        if(!Settings.PRACTICE_REMINDER_NOTIFICATION.get()) {
            cancelReminder(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PracticeReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val timeInMinutes = Settings.PRACTICE_REMINDER_TIME.get()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeInMinutes / 60)
            set(Calendar.MINUTE, timeInMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if(before(Calendar.getInstance())) { // schedule tomorrow if it has already passed
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        Log.d("PracticeReminder", "Scheduling alarm for ${calendar.time}")
        scheduleAt(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    fun scheduleSnooze(context: Context, minutes: Int) {
        Log.d("PracticeReminder", "Snoozing for $minutes minutes")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PracticeReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        scheduleAt(alarmManager, triggerTime, pendingIntent)

        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun cancelReminder(context: Context) {
        Log.d("PracticeReminder", "Cancelling reminder")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PracticeReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)

        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun showNotification(context: Context) {
        Log.d("PracticeReminder", "Attempting to show notification")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        scheduleDailyReminder(context)

        val lastAppOpen = Settings.LAST_OPEN.get()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if(lastAppOpen >= midnight) {
            Log.d("PracticeReminder", "App has already been opened, skipping notification")
            return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle(context.getString(R.string.notification_practice_reminder_title))
            .setContentText(context.getString(R.string.notification_practice_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                context.getString(R.string.notification_practice_reminder_open),
                openPendingIntent
            )

        val snoozeTime = Settings.PRACTICE_REMINDER_SNOOZE.get()
        if(snoozeTime > 0) {
            val snoozeIntent = Intent(context, PracticeReminderReceiver::class.java).apply {
                action = PracticeReminderReceiver.ACTION_SNOOZE
                putExtra(PracticeReminderReceiver.SNOOZE_MINUTES, snoozeTime)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                snoozeTime,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeLabel = when(snoozeTime) {
                10 -> context.getString(R.string.notification_practice_reminder_snooze_10)
                30 -> context.getString(R.string.notification_practice_reminder_snooze_30)
                60 -> context.getString(R.string.notification_practice_reminder_snooze_60)
                90 -> context.getString(R.string.notification_practice_reminder_snooze_90)
                else -> context.getString(R.string.generic_error)
            }

            builder.addAction(0, snoozeLabel, snoozePendingIntent)
        }

        Log.d("PracticeReminder", "Displaying notification")
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.page_settings_practice_reminder)
            val descriptionText = context.getString(R.string.setting_description_practice_reminder_notification)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}



