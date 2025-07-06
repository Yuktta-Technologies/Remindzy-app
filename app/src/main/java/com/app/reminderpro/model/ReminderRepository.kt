package com.app.reminderpro.model

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()


    suspend fun insert(reminder: Reminder): Long {
        return reminderDao.insert(reminder)
    }


    suspend fun delete(reminder: Reminder) {
        reminderDao.delete(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.update(reminder)
    }
}
