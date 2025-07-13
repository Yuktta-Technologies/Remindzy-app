package com.app.reminderpro.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import com.app.reminderpro.model.RepeatMode
import com.app.reminderpro.model.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController


fun showDatePicker(context: Context, calendar: Calendar, onDateSelected: (Calendar) -> Unit) {
    val tempCalendar = calendar.clone() as Calendar
    DatePickerDialog(
        context,
        { _, year, month, day ->
            tempCalendar.set(Calendar.YEAR, year)
            tempCalendar.set(Calendar.MONTH, month)
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            onDateSelected(tempCalendar)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, calendar: Calendar, onTimeSelected: (Calendar) -> Unit) {
    val tempCalendar = calendar.clone() as Calendar
    TimePickerDialog(
        context,
        { _, hour, minute ->
            tempCalendar.set(Calendar.HOUR_OF_DAY, hour)
            tempCalendar.set(Calendar.MINUTE, minute)
            onTimeSelected(tempCalendar)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).show()
}


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    reminderToEdit: Reminder? = null,
    onDismiss: () -> Unit,
    // VITAL CHANGE: 'end' parameter is now Long? (nullable)
    onSave: (title: String, desc: String, start: Long, end: Long?, repeat: RepeatMode, category: String) -> Unit
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
    val categoryOptions = listOf("Personal", "Work", "Health", "Finance", "Others")
    var selectedCategory by remember { mutableStateOf("Personal") }


    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
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


    @OptIn(ExperimentalComposeUiApi::class)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
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

                onSave(title, description, finalStartTimeMillis, finalEndTimeMillis, repeatMode, selectedCategory)
                // onDismiss() // Call onDismiss from the onSave lambda in ReminderListScreen
            },
                    colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9333EA),
                contentColor = Color.White // White text
            ),
            shape = RoundedCornerShape(50) // Capsule shape
            ) {
                Text(if (reminderToEdit != null) "Update" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = Color(0xFF1A1A1A),
                    )
            }
        },
        title = {
            Text(
                text = if (reminderToEdit != null) "Edit Reminder" else "Add Reminder",
                color = Color(0xFF1A1A1A),
                style = MaterialTheme.typography.titleLarge // or any custom text style
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                    ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Color(0xFF9333EA),
                            unfocusedLabelColor = Color(0xFF999999),    // Light grey when not focused
                            focusedTextColor = Color.Black,            // Text color when typing
                            unfocusedTextColor = Color.Black,           // Text color when not focused
                            focusedBorderColor = Color(0xFF9333EA),
                            unfocusedBorderColor = Color.Gray,        // Gray when not focused
                            cursorColor = Color.Black,
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Color(0xFF9333EA),
                            unfocusedLabelColor = Color(0xFF999999),    // Light grey when not focused
                            focusedTextColor = Color.Black,            // Text color when typing
                            unfocusedTextColor = Color.Black,           // Text color when not focused
                            focusedBorderColor = Color(0xFF9333EA),
                            unfocusedBorderColor = Color.Gray,        // Gray when not focused
                            cursorColor = Color.Black,
                        )
                    )

                    // --- Start Date and Time ---
                    Text(
                        "Start Time:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1A1A1A),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 📅 Start Date Button
                        OutlinedButton(
                            onClick = {
                                val tempCal = startCalendar.clone() as Calendar
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val updatedCal = tempCal.apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        startCalendar = updatedCal
                                    },
                                    startCalendar.get(Calendar.YEAR),
                                    startCalendar.get(Calendar.MONTH),
                                    startCalendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = dateFormatter.format(startCalendar.time),
                                color = Color(0xFF1A1A1A)
                            )
                        }

                        // ⏰ Start Time Button
                        OutlinedButton(
                            onClick = {
                                val tempCal = startCalendar.clone() as Calendar
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        val updatedCal = tempCal.apply {
                                            set(Calendar.HOUR_OF_DAY, hour)
                                            set(Calendar.MINUTE, minute)
                                        }
                                        startCalendar = updatedCal
                                    },
                                    startCalendar.get(Calendar.HOUR_OF_DAY),
                                    startCalendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = timeFormatter.format(startCalendar.time),
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }


                    // End Time Picker Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Enable End Time:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1A1A1A),
                        )
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
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF6A1B9A),     // Purple thumb
                                checkedTrackColor = Color(0xFFCE93D8),     // Light purple track
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }

                    if (isEndTimeEnabled) {
                        Text(
                            "End Time:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1A1A1A),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val tempCal = endCalendar
                                            ?: (startCalendar.clone() as Calendar).apply {
                                                add(
                                                    Calendar.HOUR_OF_DAY,
                                                    1
                                                )
                                            }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val updatedCal = tempCal.apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                }
                                                endCalendar = updatedCal
                                            },
                                            tempCal.get(Calendar.YEAR),
                                            tempCal.get(Calendar.MONTH),
                                            tempCal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        if (endCalendar != null) dateFormatter.format(endCalendar!!.time) else "Pick Date",
                                        color = Color(0xFF1A1A1A)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val tempCal = endCalendar
                                            ?: (startCalendar.clone() as Calendar).apply {
                                                add(
                                                    Calendar.HOUR_OF_DAY,
                                                    1
                                                )
                                            }
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val newCal = (tempCal.clone() as Calendar).apply {
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                }
                                                endCalendar = newCal

                                            },
                                            tempCal.get(Calendar.HOUR_OF_DAY),
                                            tempCal.get(Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        if (endCalendar != null) timeFormatter.format(endCalendar!!.time) else "Pick Time",
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                            }

                            // Optional: Button to clear the end time if it's enabled
//                        if (endCalendar != null) {
//                            IconButton(onClick = {
//                                endCalendar = null
//                                // Optionally, also set isEndTimeEnabled = false here,
//                                // or let the user explicitly toggle the switch.
//                                // For now, just clears the date, user has to toggle switch to truly disable.
//                            }) {
//                                Icon(Icons.Filled.Clear, contentDescription = "Clear End Time")
//                            }
//                        }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    //DropdownMenuBox(selected = repeatMode, onSelected = { repeatMode = it })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DropdownMenuBox(
                            label = "Repeat",
                            options = RepeatMode.entries.toList(),
                            selected = repeatMode,
                            onSelected = { repeatMode = it },
                            modifier = Modifier.weight(1f)
                        )

                        DropdownMenuBox(
                            label = "Category",
                            options = categoryOptions,
                            selected = selectedCategory,
                            onSelected = { selectedCategory = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                }
            }
        }
    )
}

//dropdown menu code
/*
@Composable
fun DropdownMenuBox( // This remains the same as your provided code
    selected: RepeatMode,
    onSelected: (RepeatMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                "Repeat: ${selected.name}",
                //style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1A1A1A)
            )
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
*/

@Composable
fun <T> DropdownMenuBox(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "$label: ${selected.toString()}",
                color = Color(0xFF1A1A1A)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
