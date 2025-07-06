package com.app.reminderpro.model

import android.util.Log
//import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.reminderpro.alarm.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    companion object {
        private const val TAG = "ReminderViewModel"
    }

    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

//    var showAddReminder = mutableStateOf(false)

    private val _needsExactAlarmPermission = MutableStateFlow(false)
    val needsExactAlarmPermission: StateFlow<Boolean> = _needsExactAlarmPermission.asStateFlow()

    private val _exactAlarmPermissionOutcome = MutableStateFlow<Boolean?>(null)
    val exactAlarmPermissionOutcome: StateFlow<Boolean?> = _exactAlarmPermissionOutcome.asStateFlow()

    private var pendingReminder: Reminder? = null

    private fun scheduleReminderInternal(reminderToSchedule: Reminder) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to schedule reminder internally: ID ${reminderToSchedule.id}")
            val scheduledSuccessfully = scheduler.schedule(reminderToSchedule)

            if (!scheduledSuccessfully) {
                Log.w(TAG, "Scheduling failed for reminder ID ${reminderToSchedule.id}, exact alarm permission likely needed.")
                pendingReminder = reminderToSchedule
                _needsExactAlarmPermission.value = true
            } else {
                Log.i(TAG, "Reminder ID ${reminderToSchedule.id} scheduled successfully by ReminderScheduler.")
                pendingReminder = null
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
            scheduler.cancel(reminder)
            Log.i(TAG, "Reminder ID ${reminder.id} deleted and alarm cancelled.")
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
            scheduler.cancel(reminder)
            Log.d(TAG, "Old alarm for reminder ID ${reminder.id} cancelled. Attempting to reschedule.")
            scheduleReminderInternal(reminder)
        }
    }

    fun insertReminder(
        title: String,
        desc: String,
        startTime: Long,
        endTime: Long,
        repeatMode: RepeatMode
    ) {
        val newReminder = Reminder(
            title = title,
            description = desc,
            startTime = startTime,
            endTime = endTime,
            repeatMode = repeatMode
        )
        viewModelScope.launch {
            val generatedId = repository.insert(newReminder)
            val reminderWithId = newReminder.copy(id = generatedId.toInt())
            Log.d(TAG, "New reminder created with ID: ${reminderWithId.id}. Proceeding to schedule.")
            scheduleReminderInternal(reminderWithId)
        }
    }

    // THIS FUNCTION IS CORRECTLY DEFINED. IT NEEDS TO BE CALLED BY MainActivity.
    fun onExactAlarmPermissionPromptHandled() {
        Log.d(TAG, "Exact alarm permission prompt has been handled by UI.")
        _needsExactAlarmPermission.value = false
    }

    fun onExactAlarmPermissionGranted() {
        Log.i(TAG, "Exact alarm permission is now GRANTED.")
        _exactAlarmPermissionOutcome.value = true
        pendingReminder?.let {
            Log.i(TAG, "Retrying to schedule pending reminder: ID ${it.id}")
            scheduleReminderInternal(it)
        }
    }

    fun onExactAlarmPermissionDenied() {
        Log.w(TAG, "Exact alarm permission is DENIED.")
        _exactAlarmPermissionOutcome.value = false
        pendingReminder = null
    }

    // THIS FUNCTION IS CORRECTLY DEFINED. IT NEEDS TO BE CALLED BY MainActivity or Composable UI.
    fun clearPermissionOutcome() {
        Log.d(TAG, "Clearing exactAlarmPermissionOutcome by UI request.")
        _exactAlarmPermissionOutcome.value = null
    }
}
