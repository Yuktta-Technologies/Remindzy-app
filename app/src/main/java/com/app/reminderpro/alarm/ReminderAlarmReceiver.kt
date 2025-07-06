package com.app.reminderpro.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

object AlarmConstants {
    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_DESCRIPTION = "description"
}

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive CALLED! Action: ${intent.action}")

        val reminderId = intent.getIntExtra(AlarmConstants.EXTRA_REMINDER_ID, -1)
        val title = intent.getStringExtra(AlarmConstants.EXTRA_TITLE) ?: "Reminder"
        val desc = intent.getStringExtra(AlarmConstants.EXTRA_DESCRIPTION) ?: "Don't forget!"

        Log.i(TAG, "Received alarm for ID: $reminderId, Title: '$title', Description: '$desc'")

        // Call NotificationUtils with the correct number of arguments
        // Using context.applicationContext is still a good practice for operations
        // initiated from a BroadcastReceiver that might outlive the receiver's scope,
        // such as posting a notification.
        NotificationUtils.showReminderNotification(context.applicationContext, title, desc)
    }
}
