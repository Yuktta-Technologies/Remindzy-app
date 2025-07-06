package com.app.reminderpro.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.reminderpro.model.RepeatMode
import com.app.reminderpro.model.Reminder
import java.util.*

@Composable
fun AddReminderDialog(
    reminderToEdit: Reminder? = null, // ✅ Optional reminder to edit
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, start: Long, end: Long, repeat: RepeatMode) -> Unit
) {
    val context = LocalContext.current

    // ✅ Pre-fill values if editing, else use defaults
    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var description by remember { mutableStateOf(reminderToEdit?.description ?: "") }
    var startTimeMillis by remember { mutableLongStateOf(reminderToEdit?.startTime ?: System.currentTimeMillis()) }
    var endTimeMillis by remember { mutableLongStateOf(
        reminderToEdit?.endTime ?: (System.currentTimeMillis() + 3600000)
    ) }

    var repeatMode by remember { mutableStateOf(reminderToEdit?.repeatMode ?: RepeatMode.ONCE) }

    fun pickDateTime(initial: Long, onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initial }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute)
                        onDateTimeSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    onSave(title, description, startTimeMillis, endTimeMillis, repeatMode)
                } else {
                    Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    pickDateTime(startTimeMillis) { startTimeMillis = it }
                }) {
                    Text("Pick Start Time")
                }
                Button(onClick = {
                    pickDateTime(endTimeMillis) { endTimeMillis = it }
                }) {
                    Text("Pick End Time")
                }
                DropdownMenuBox(repeatMode) {
                    repeatMode = it
                }
            }
        }
    )
}

@Composable
fun DropdownMenuBox(
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
