package com.app.reminderpro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.reminderpro.model.Reminder
import com.app.reminderpro.model.ReminderViewModel
// Make sure RepeatMode is available if AddReminderDialog from the other file needs it directly,
// though it's passed as a parameter so direct import here might not be strictly needed for this file.
// import com.app.reminderpro.model.RepeatMode

// Explicit import for clarity, though often not needed if in the same package.
// import com.app.reminderpro.ui.AddReminderDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class) // Keep if Scaffold, FAB, TopAppBar use experimental APIs
@Composable
fun ReminderListScreen(
    viewModel: ReminderViewModel
) {
    val reminders by viewModel.allReminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                reminderToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        },
        topBar = {
            TopAppBar(title = { Text("Your Reminders") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reminders yet. Add one!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onEdit = {
                                reminderToEdit = it
                                showDialog = true
                            },
                            onDelete = {
                                viewModel.deleteReminder(it)
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            // This will now correctly call the AddReminderDialog from your AddReminderDialog.kt file
            AddReminderDialog(
                reminderToEdit = reminderToEdit,
                onDismiss = { showDialog = false },
                // The onSave lambda matches the signature required by the updated AddReminderDialog
                // (title: String, desc: String, start: Long, end: Long?, repeat: RepeatMode)
                onSave = { title, description, startTime, endTime, repeatMode ->
                    if (reminderToEdit == null) {
                        viewModel.insertReminder(title, description, startTime, endTime, repeatMode)
                    } else {
                        reminderToEdit?.let { currentReminder ->
                            val updatedReminder = currentReminder.copy(
                                title = title,
                                description = description,
                                startTime = startTime,
                                endTime = endTime,
                                repeatMode = repeatMode
                            )
                            viewModel.updateReminder(updatedReminder)
                        }
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    val simpleDateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(reminder) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.title, style = MaterialTheme.typography.titleMedium)
                if (reminder.description.isNotBlank()) {
                    Text(text = reminder.description, style = MaterialTheme.typography.bodySmall)
                }

                val formattedStartTime = remember(reminder.startTime) {
                    try {
                        simpleDateFormatter.format(Date(reminder.startTime))
                    } catch (e: Exception) {
                        "Invalid start date"
                    }
                }
                Text(text = "Starts: $formattedStartTime", style = MaterialTheme.typography.bodySmall)

                reminder.endTime?.let { endTimeValue ->
                    val formattedEndTime = remember(endTimeValue) {
                        try {
                            simpleDateFormatter.format(Date(endTimeValue))
                        } catch (e: Exception) {
                            "Invalid end date"
                        }
                    }
                    Text(text = "Ends: $formattedEndTime", style = MaterialTheme.typography.bodySmall)
                }

                Text(text = "Repeat: ${reminder.repeatMode.name}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEdit(reminder) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Reminder")
                }
                IconButton(onClick = { onDelete(reminder) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Reminder")
                }
            }
        }
    }
}

// Ensure the DUMMY AddReminderDialog that was previously here IS DELETED.
// The actual AddReminderDialog should reside in its own file (AddReminderDialog.kt)
// and have the updated implementation we worked on.

