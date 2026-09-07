package de.tobias.emojitagebuch.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarme gehen bei Neustart und App-Update verloren und werden hier neu gesetzt. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            -> ReminderScheduler.sync(context.applicationContext)
        }
    }
}
