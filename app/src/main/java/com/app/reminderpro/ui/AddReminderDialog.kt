package com.app.reminderpro.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.reminderpro.model.RepeatMode
import com.app.reminderpro.model.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    reminderToEdit: Reminder? = null,
    onDismiss: () -> Unit,
    // VITAL CHANGE: 'end' parameter is now Long? (nullable)
    onSave: (title: String, desc: String, start: Long, end: Long?, repeat: RepeatMode) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var description by remember { mutableStateOf(reminderToEdit?.description ?: "") }

    // --- Start Time State & Logic ---
    val initialStartCalendar = Calendar.getInstance().apply {
        timeInMillis = reminderToEdit?.startTime ?: System.currentTimeMillis()
        // Default to next hour if creating new
        if (reminderToEdit == null) {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    var startCalendar by remember { mutableStateOf(initialStartCalendar) }

    // --- End Time State & Logic ---
    // Use Calendar? for end time to handle null (no end time explicitly set by user)
    // Initialize with reminderToEdit.endTime if it exists, otherwise null.
    val initialEndCalendar: Calendar? = reminderToEdit?.endTime?.let {
        Calendar.getInstance().apply { timeInMillis = it }
    }
    var endCalendar by remember { mutableStateOf(initialEndCalendar) }
    // This flag tracks if the user *wants* an end time.
    // If reminderToEdit.endTime is null, it means no end time was previously set.
    var isEndTimeEnabled by remember { mutableStateOf(reminderToEdit?.endTime != null) }


    var repeatMode by remember { mutableStateOf(reminderToEdit?.repeatMode ?: RepeatMode.ONCE) }

    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    fun showDateTimePicker(
        currentCalendar: Calendar,
        onDateTimeSelected: (Calendar) -> Unit
    ) {
        val tempCalendar = currentCalendar.clone() as Calendar
        DatePickerDialog(
            context,
            { _, year, month, day ->
                tempCalendar.set(Calendar.YEAR, year)
                tempCalendar.set(Calendar.MONTH, month)
                tempCalendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        tempCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        tempCalendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(tempCalendar)
                    },
                    tempCalendar.get(Calendar.HOUR_OF_DAY),
                    tempCalendar.get(Calendar.MINUTE),
                    false // Use 12 or 24 hour format based on system settings
                ).show()
            },
            tempCalendar.get(Calendar.YEAR),
            tempCalendar.get(Calendar.MONTH),
            tempCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) { // Description can be blank
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val finalStartTimeMillis = startCalendar.timeInMillis
                val finalEndTimeMillis: Long? = if (isEndTimeEnabled) {
                    endCalendar?.timeInMillis // Use endCalendar's time if enabled and set
                } else {
                    null // Otherwise, no end time
                }

                if (finalEndTimeMillis != null && finalEndTimeMillis <= finalStartTimeMillis) {
                    Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                onSave(title, description, finalStartTimeMillis, finalEndTimeMillis, repeatMode)
                // onDismiss() // Call onDismiss from the onSave lambda in ReminderListScreen
            }) {
                Text(if (reminderToEdit != null) "Update" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(if (reminderToEdit != null) "Edit Reminder" else "Add Reminder")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Start Time Picker
                Text("Start Time:", style = MaterialTheme.typography.labelMedium)
                Button(
                    onClick = {
                        showDateTimePicker(startCalendar) { updatedCal ->
                            startCalendar = updatedCal
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${dateFormatter.format(startCalendar.time)} at ${timeFormatter.format(startCalendar.time)}")
                }

                // End Time Picker Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable End Time:", style = MaterialTheme.typography.labelMedium)
                    Switch(
                        checked = isEndTimeEnabled,
                        onCheckedChange = {
                            isEndTimeEnabled = it
                            if (it && endCalendar == null) {
                                // If enabling and endCalendar is null, set a default
                                // (e.g., 1 hour after start time)
                                endCalendar = (startCalendar.clone() as Calendar).apply {
                                    add(Calendar.HOUR_OF_DAY, 1)
                                }
                            } else if (!it) {
                                endCalendar = null // Clear end time when disabled
                            }
                        }
                    )
                }

                if (isEndTimeEnabled) {
                    Text("End Time:", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                // Default to 1 hour after start if endCalendar is still null
                                val currentEndForPicker = endCalendar ?: (startCalendar.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }
                                showDateTimePicker(currentEndForPicker) { updatedCal ->
                                    endCalendar = updatedCal
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (endCalendar != null) "${dateFormatter.format(endCalendar!!.time)} at ${timeFormatter.format(endCalendar!!.time)}"
                                else "Pick End Time"
                            )
                        }
                        // Optional: Button to clear the end time if it's enabled
                        if (endCalendar != null) {
                            IconButton(onClick = {
                                endCalendar = null
                                // Optionally, also set isEndTimeEnabled = false here,
                                // or let the user explicitly toggle the switch.
                                // For now, just clears the date, user has to toggle switch to truly disable.
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear End Time")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenuBox(selected = repeatMode, onSelected = { repeatMode = it })
            }
        }
    )
}

@Composable
fun DropdownMenuBox( // This remains the same as your provided code
    selected: RepeatMode,
    onSelected: (RepeatMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Repeat: ${selected.name}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RepeatMode.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
