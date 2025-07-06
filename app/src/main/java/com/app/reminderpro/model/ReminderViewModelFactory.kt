package com.app.reminderpro.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.reminderpro.alarm.ReminderScheduler

class ReminderViewModelFactory(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler // ✅ add this
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
