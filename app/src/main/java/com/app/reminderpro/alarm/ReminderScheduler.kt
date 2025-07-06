package com.app.reminderpro.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.app.reminderpro.model.Reminder
import com.app.reminderpro.model.RepeatMode

object AlarmReceiverConstants {
    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_DESCRIPTION = "description"
}

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "ReminderScheduler"
    }

    fun schedule(reminder: Reminder): Boolean {
        Log.d(TAG, "Attempting to schedule reminder: ${reminder.id}, Mode: ${reminder.repeatMode}")

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(AlarmReceiverConstants.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlarmReceiverConstants.EXTRA_TITLE, reminder.title)
            putExtra(AlarmReceiverConstants.EXTRA_DESCRIPTION, reminder.description)
        }

        // Assuming minSdk is high enough (e.g., 23+ or 24+ based on previous Lint)
        // that SDK_INT checks for M are redundant.
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            pendingIntentFlags
        )

        when (reminder.repeatMode) {
            RepeatMode.ONCE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        Log.i(TAG, "Exact alarm permission granted. Scheduling for ONCE.")
                        setExactAlarmInternal(reminder.startTime, pendingIntent)
                        return true
                    } else {
                        Log.w(TAG, "Exact alarm permission NOT granted for ONCE. User action required.")
                        Toast.makeText(context, "Exact alarm permission needed. Please grant in settings.", Toast.LENGTH_LONG).show()
                        return false
                    }
                } else {
                    Log.i(TAG, "Below Android S. Scheduling exact alarm for ONCE.")
                    setExactAlarmInternal(reminder.startTime, pendingIntent)
                    return true
                }
            }
            RepeatMode.DAILY, RepeatMode.WEEKLY, RepeatMode.MONTHLY -> {
                val interval = when (reminder.repeatMode) {
                    RepeatMode.DAILY -> AlarmManager.INTERVAL_DAY
                    RepeatMode.WEEKLY -> AlarmManager.INTERVAL_DAY * 7
                    RepeatMode.MONTHLY -> AlarmManager.INTERVAL_DAY * 30 // Approximate
                    // This else path is critical. If a new RepeatMode is added and not handled above,
                    // or if ONCE (or another mode not intended for repeating) somehow reached here,
                    // this will prevent scheduling with an invalid interval.
                    else -> {
                        Log.e(TAG, "Unexpected repeat mode for interval calculation: ${reminder.repeatMode}. Cannot schedule repeating alarm.")
                        return false // Immediately stop if interval cannot be determined for a repeating type
                    }
                }

                // Based on Lint feedback, the 'interval' is guaranteed to be > 0
                // for DAILY, WEEKLY, MONTHLY due to the structure of the 'when' statement above.
                // The 'else' case in the 'when' statement now directly returns 'false',
                // preventing scheduling with a 0 or negative interval from an unexpected mode.
                // Therefore, an explicit check for 'interval == 0L' here is redundant.

                Log.i(TAG, "Scheduling repeating alarm for mode: ${reminder.repeatMode} with interval $interval")
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminder.startTime,
                    interval, // This is now guaranteed to be > 0 for DAILY, WEEKLY, MONTHLY
                    pendingIntent
                )
                return true
            }
            // If RepeatMode enum could have other values not covered by ONCE, DAILY, WEEKLY, MONTHLY,
            // an explicit 'else' here in the outer 'when' would catch them.
            // else -> {
            //     Log.w(TAG, "Unhandled repeat mode in schedule(): ${reminder.repeatMode}")
            //     return false
            // }
        }
        // This line should ideally be unreachable if all paths in the 'when' statement
        // explicitly return a Boolean. Consider adding an 'else' to the main 'when'
        // to handle any unknown RepeatMode states explicitly and return false.
        // For example, if RepeatMode was nullable or could have more states.
        // If RepeatMode is a sealed class or enum with all cases covered, this is fine.
        // Log.wtf(TAG, "Reached end of schedule method without returning, mode: ${reminder.repeatMode}")
        // return false
    }

    private fun setExactAlarmInternal(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.i(TAG, "setExactAndAllowWhileIdle called successfully.")
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException: ${se.message}. This may indicate SCHEDULE_EXACT_ALARM permission is missing or revoked.", se)
            Toast.makeText(context, "Could not set exact alarm. Please check permissions.", Toast.LENGTH_LONG).show()
            // Optionally, re-throw or handle more explicitly if needed
        } catch (e: Exception) {
            Log.e(TAG, "Exception setting exact alarm: ${e.message}", e)
            Toast.makeText(context, "Error setting alarm.", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancel(reminder: Reminder) {
        Log.d(TAG, "Cancelling reminder: ${reminder.id}")
        val intent = Intent(context, ReminderAlarmReceiver::class.java)

        // Assuming minSdk is high enough
        val pendingIntentFlags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            pendingIntentFlags
        )

        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Also cancel the PendingIntent itself
                Log.i(TAG, "Alarm and PendingIntent cancelled for reminder: ${reminder.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling alarm for reminder ${reminder.id}", e)
            }
        } else {
            Log.w(TAG, "PendingIntent for reminder ${reminder.id} not found to cancel. It might have already been cancelled or never scheduled.")
        }
    }
}
