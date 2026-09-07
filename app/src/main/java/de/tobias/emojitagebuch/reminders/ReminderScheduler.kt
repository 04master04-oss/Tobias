package de.tobias.emojitagebuch.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import de.tobias.emojitagebuch.settings.SettingsStore
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Plant die täglichen Erinnerungen über den AlarmManager. Jede Erinnerung wird als
 * einzelner Alarm gesetzt und nach dem Auslösen für den nächsten Tag neu geplant.
 */
object ReminderScheduler {

    const val ACTION_REMINDER = "de.tobias.emojitagebuch.REMINDER"
    const val EXTRA_TYPE = "type"
    const val TYPE_EVENING = "evening"
    const val TYPE_MORNING = "morning"
    const val CHANNEL_ID = "reminders"

    /** Bringt die gesetzten Alarme mit den Einstellungen in Einklang. */
    fun sync(context: Context) {
        val settings = SettingsStore.get(context).state.value
        ensureChannel(context)
        if (settings.eveningEnabled) {
            schedule(context, TYPE_EVENING, settings.eveningHour, settings.eveningMinute)
        } else {
            cancel(context, TYPE_EVENING)
        }
        if (settings.morningEnabled) {
            schedule(context, TYPE_MORNING, settings.morningHour, settings.morningMinute)
        } else {
            cancel(context, TYPE_MORNING)
        }
    }

    private fun schedule(context: Context, type: String, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, minute)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, type)
        alarmManager.cancel(pendingIntent)
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (exactAllowed) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancel(context: Context, type: String) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, type))
    }

    private fun pendingIntent(context: Context, type: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMINDER)
            .putExtra(EXTRA_TYPE, type)
        val requestCode = if (type == TYPE_EVENING) 1 else 2
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tägliche Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Erinnert dich abends an deinen Eintrag und fragt morgens nach, falls gestern fehlt."
        }
        manager.createNotificationChannel(channel)
    }
}
