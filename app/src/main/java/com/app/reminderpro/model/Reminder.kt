package com.app.reminderpro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long?,
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val category: String = "Personal" // Add this line with default value
)
