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
// Removed unused getValue and setValue imports as `by` delegate handles it.
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.reminderpro.model.Reminder
import com.app.reminderpro.model.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// Import AddReminderDialog if it's shown from here
// e.g., import com.app.reminderpro.ui.AddReminderDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderViewModel // Passed from MainActivity
) {
    val reminders by viewModel.allReminders.collectAsState()
    // State to control the visibility of the Add/Edit Reminder Dialog
    var showDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                reminderToEdit = null // Ensure we are adding, not editing
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
                    contentPadding = PaddingValues(all = 16.dp), // Use 'all' for consistent padding
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
                                viewModel.deleteReminder(it) // ViewModel handles alarm cancellation
                            }
                        )
                    }
                }
            }
        }

        // Show AddReminderDialog when showDialog is true
        if (showDialog) {
            AddReminderDialog( // Assuming AddReminderDialog is imported or in the same package
                reminderToEdit = reminderToEdit,
                onDismiss = { showDialog = false },
                onSave = { title, desc, start, end, repeat ->
                    if (reminderToEdit == null) {
                        // Adding new reminder
                        viewModel.insertReminder(title, desc, start, end, repeat)
                    } else {
                        // Editing existing reminder
                        // Ensure reminderToEdit is not null before accessing, though the if checks this.
                        reminderToEdit?.let { currentReminder ->
                            val updatedReminder = currentReminder.copy(
                                title = title,
                                description = desc,
                                startTime = start,
                                endTime = end,
                                repeatMode = repeat
                            )
                            viewModel.updateReminder(updatedReminder)
                        }
                    }
                    showDialog = false // Dismiss dialog after save/update
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
    // Define a formatter.
    // Example format: "Wed, Jul 4, 2024 at 2:30 PM"
    // Adjust the pattern "EEE, MMM d, yyyy 'at' h:mm a" to your desired format.
    val simpleDateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(reminder) } // Making the whole card clickable for edit
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
                if (reminder.description.isNotBlank()) { // Only show description if not blank
                    Text(text = reminder.description, style = MaterialTheme.typography.bodySmall)
                }

                // Format the startTime Long timestamp into a readable string
                val formattedStartTime = remember(reminder.startTime) {
                    try {
                        simpleDateFormatter.format(Date(reminder.startTime))
                    } catch (e: Exception) {
                        // Fallback or log error if formatting fails
                        "Invalid date" // Or "" or some other placeholder
                    }
                }
                Text(text = "Starts: $formattedStartTime", style = MaterialTheme.typography.bodySmall)
                Text(text = "Repeat: ${reminder.repeatMode.name}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) { // Align icons vertically
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
