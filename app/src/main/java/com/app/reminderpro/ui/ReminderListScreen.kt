package com.app.reminderpro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.reminderpro.model.Reminder
import com.app.reminderpro.model.ReminderViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.clipPath // Import clipPath
import androidx.compose.ui.graphics.Path // Import Path
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.unit.DpOffset


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

    // ADD THE FILTER STATE HERE
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Personal", "Work", "Health", "Finance")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    reminderToEdit = null
                    showDialog = true
                },
                containerColor = Color(0xFF8E24AA), // Purple background
                contentColor = Color.White // Optional: sets icon tint
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Remindzy",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8E24AA) // Purple background
                )
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(paddingValues)
        ) {

            //  Category Filter UI - INSERT HERE
            /*Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1A1A1A), // strong dark gray
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedCategory)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }*/
            // 🔄 Replace your current "Category Filter UI" with this:
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Filter by:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF1A1A1A), // Dark gray
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        AssistChip(
                            onClick = {
                                selectedCategory = category
                            },
                            label = {
                                Text(
                                    text = category,
                                    color = if (selectedCategory == category) Color.White else Color(0xFF4A148C)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedCategory == category)
                                    Color(0xFF8E24AA) // Selected = dark purple
                                else
                                    Color(0xFFEDE7F6) // Unselected = light purple
                            )
                        )
                    }
                }
            }


            //  Then your filtered list logic follows
            val filteredReminders = if (selectedCategory == "All") {
                reminders
            } else {
                reminders.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }

            if (filteredReminders.isEmpty()) {
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
                    items(filteredReminders, key = { it.id }) { reminder ->
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

//The below commented lines can be removed if the new @composable function is working
/*
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
*/


// working version with straight orange bar line on the left

@Composable
fun ReminderItem(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    val simpleDateFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()) // ⏰ Example: 4:30 PM
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(reminder) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // ⬇️ Outer Box that allows intrinsic height so left bar can match height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // 🔸 Ensures orange bar can fill full height
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            // 🔶 Orange vertical bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFFFF7043)) // Orange
            )

            // ⋮ Overflow menu
            var menuExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp, end = 2.dp)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.Black, // ✅ Black icon color
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    offset = DpOffset(x = (-8).dp, y = (-10).dp) // 👈 Adjust here
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit(reminder)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete(reminder)
                        }
                    )
                }
            }

            // 📛 Category tag aligned below the 3-dots menu
            if (reminder.category.isNotBlank()) {
                val (bgColor, textColor) = when (reminder.category.lowercase(Locale.getDefault())) {
                    "personal" -> Pair(Color(0xFFFFEDD5), Color(0xFFCD7C5D)) // Light peach + brown text
                    else -> Pair(Color(0xFFFFECB3), Color(0xFFE65100))       // Default fallback
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 45.dp, end = 14.dp) // 👈 adjust as needed to place it under the 3-dots
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reminder.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }


            // 📄 Reminder content
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 85.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A)
                )

                if (reminder.description.isNotBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3C3C3C)
                    )
                }


                val iconColor = Color(0xFF6C6C6C) // Grey

                val formattedStartTime = remember(reminder.startTime) {
                    try {
                        simpleDateFormatter.format(Date(reminder.startTime))
                    } catch (e: Exception) {
                        "Invalid start time"
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Start Time",
                        tint = iconColor,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Starts: $formattedStartTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6C6C6C),
                        //modifier = Modifier.padding(start = 8.dp)
                    )
                }

                reminder.endTime?.let { endTimeValue ->
                    val formattedEndTime = remember(endTimeValue) {
                        try {
                            simpleDateFormatter.format(Date(endTimeValue))
                        } catch (e: Exception) {
                            "Invalid end time"
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "End Time",
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ends:   $formattedEndTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6C6C6C)
                        )
                    }
                }


                Text(
                    text = "Repeat: ${reminder.repeatMode.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}






/*
@Composable
fun ReminderItem(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    val simpleDateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    }
    val cardCornerRadiusDp = 16.dp
    val lineThicknessDp = 6.dp // Thickness of the orange line
    val orangeColor = Color(0xFFFF7043)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(cardCornerRadiusDp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Ensure children can match height
                .drawBehind { // Draw the orange line behind the content
                    val lineThicknessPx = lineThicknessDp.toPx()
                    val cardCornerRadiusPx = cardCornerRadiusDp.toPx()

                    // Path for the orange rounded line segment on the left
                    val linePath = Path().apply {
                        // Start just inside the top-left corner
                        moveTo(0f, cardCornerRadiusPx)
                        // Arc for the top-left corner
                        arcTo(
                            rect = Rect(
                                Offset.Zero,
                                Size(cardCornerRadiusPx * 2, cardCornerRadiusPx * 2)
                            ),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        // Line to the start of the bottom-left corner arc
                        lineTo(lineThicknessPx, size.height - cardCornerRadiusPx)
                        // Arc for the bottom-left corner (drawn in reverse)
                        arcTo(
                            rect = Rect(
                                Offset(0f, size.height - cardCornerRadiusPx * 2),
                                Size(cardCornerRadiusPx * 2, cardCornerRadiusPx * 2)
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        // Close the path to form a fillable shape for the line
                        lineTo(
                            0f,
                            size.height - cardCornerRadiusPx
                        ) // Move back to the outer edge for closing
                        // This part is tricky; let's simplify by drawing a thick rounded stroke instead

                        // Alternative: Draw a rounded rectangle for the line
                        reset() // Clear previous path commands for this simpler approach
                        addRoundRect(
                            RoundRect(
                                left = 0f,
                                top = 0f,
                                right = lineThicknessPx, // Width of the line
                                bottom = size.height,
                                topLeftCornerRadius = CornerRadius(cardCornerRadiusPx),
                                bottomLeftCornerRadius = CornerRadius(cardCornerRadiusPx),
                                topRightCornerRadius = CornerRadius(0f), // Sharp inner corners
                                bottomRightCornerRadius = CornerRadius(0f)  // Sharp inner corners
                            )
                        )
                    }
                    drawPath(
                        path = linePath,
                        color = orangeColor
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer to push content away from where the line is drawn
            // No need for a spacer if drawBehind is used correctly and content padding handles it.

            // Main content
            Row(
                modifier = Modifier
                    // Add padding to the start to account for the line's thickness
                    .padding(
                        start = lineThicknessDp + 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    )
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = reminder.title, style = MaterialTheme.typography.titleMedium)

                    if (reminder.description.isNotBlank()) {
                        Text(text = reminder.description, style = MaterialTheme.typography.bodySmall)
                    }

                    if (reminder.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD1C4E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = reminder.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4A148C)
                            )
                        }
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
}
*/







