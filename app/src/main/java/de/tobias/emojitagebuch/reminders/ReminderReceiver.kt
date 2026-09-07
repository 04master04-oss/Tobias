package de.tobias.emojitagebuch.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tobias.emojitagebuch.MainActivity
import de.tobias.emojitagebuch.R
import de.tobias.emojitagebuch.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Wird vom AlarmManager ausgelöst. Zeigt nur dann eine Benachrichtigung, wenn für den
 * betreffenden Tag noch kein Eintrag existiert, und plant den nächsten Alarm.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMINDER) return
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val target = if (type == ReminderScheduler.TYPE_MORNING) LocalDate.now().minusDays(1) else LocalDate.now()
                val existing = AppDatabase.get(appContext).dayEntryDao().getByDate(target.toString())
                if (existing == null) {
                    showNotification(appContext, type, target)
                }
            } finally {
                ReminderScheduler.sync(appContext)
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, type: String, date: LocalDate) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ReminderScheduler.ensureChannel(context)

        val (title, text) = if (type == ReminderScheduler.TYPE_MORNING) {
            val label = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.GERMAN))
            "Wie war gestern?" to "Für $label fehlt noch ein Emoji. Tippe, um es nachzutragen."
        } else {
            "Wie war dein Tag?" to "Setz dein Emoji für heute, bevor der Tag vorbei ist."
        }

        val openIntent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(MainActivity.EXTRA_DATE, date.toString())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            if (type == ReminderScheduler.TYPE_MORNING) 11 else 12,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            manager.notify(if (type == ReminderScheduler.TYPE_MORNING) 101 else 102, notification)
        } catch (e: SecurityException) {
            // Berechtigung für Benachrichtigungen wurde entzogen.
        }
    }
}
