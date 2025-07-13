package com.app.reminderpro

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast // Import Toast for displaying outcome
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
// import androidx.compose.ui.semantics.dismiss // Not used in this snippet
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.app.reminderpro.alarm.ReminderScheduler
import com.app.reminderpro.model.ReminderDatabase
import com.app.reminderpro.model.ReminderRepository
import com.app.reminderpro.model.ReminderViewModel
import com.app.reminderpro.model.ReminderViewModelFactory
import com.app.reminderpro.ui.ReminderListScreen
import com.app.reminderpro.ui.theme.ReminderProTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory(
            ReminderRepository(
                ReminderDatabase.getDatabase(applicationContext).reminderDao()
            ),
            ReminderScheduler(applicationContext)
        )
    }

    private val requestExactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            Log.d("MainActivity", "Returned from exact alarm settings. Checking permission again.")
            checkExactAlarmPermissionAndNotifyViewModel()
        }

    private val requestPostNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("MainActivity", "POST_NOTIFICATIONS permission granted.")
            } else {
                Log.w("MainActivity", "POST_NOTIFICATIONS permission denied.")
                // You might want to show a Toast or dialog here too
                Toast.makeText(this, "Notifications permission denied. You may miss reminders.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestPostNotificationsPermission()

        setContent {
            ReminderProTheme {
                val needsExactAlarmPerm by viewModel.needsExactAlarmPermission.collectAsState()
                val exactAlarmOutcome by viewModel.exactAlarmPermissionOutcome.collectAsState() // Observe this too

                // Effect for prompting user if exact alarm permission is needed
                LaunchedEffect(needsExactAlarmPerm) {
                    if (needsExactAlarmPerm) {
                        Log.d("MainActivity", "ViewModel indicates exact alarm permission is needed.")
                        promptForExactAlarmPermission()
                        // viewModel.onExactAlarmPermissionPromptShown() // OLD NAME
                        viewModel.onExactAlarmPermissionPromptHandled() // <<< CORRECTED: Call this to tell ViewModel prompt was initiated
                    }
                }

                // Effect for showing Toast based on permission outcome
                LaunchedEffect(exactAlarmOutcome) {
                    when (exactAlarmOutcome) {
                        true -> { // Permission Granted
                            Toast.makeText(this@MainActivity, "Exact alarm permission granted!", Toast.LENGTH_SHORT).show()
                            viewModel.clearPermissionOutcome() // <<< ADDED: Clear the outcome
                        }
                        false -> { // Permission Denied
                            Toast.makeText(this@MainActivity, "Exact alarm permission denied. Reminders may not be precise.", Toast.LENGTH_LONG).show()
                            viewModel.clearPermissionOutcome() // <<< ADDED: Clear the outcome
                        }
                        null -> {
                            // Initial state or outcome already cleared, do nothing
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReminderListScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i("MainActivity", "POST_NOTIFICATIONS permission already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.i("MainActivity", "Showing rationale for POST_NOTIFICATIONS.")
                    // Consider showing a dialog here to explain, then launch
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission Needed")
                        .setMessage("This app needs permission to show notifications for your reminders.")
                        .setPositiveButton("Grant") { _, _ ->
                            requestPostNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Later") { dialog, _ ->
                            dialog.dismiss()
                            Toast.makeText(this, "Notifications permission denied. You may miss reminders.", Toast.LENGTH_LONG).show()
                        }
                        .show()
                }
                else -> {
                    Log.i("MainActivity", "Requesting POST_NOTIFICATIONS permission.")
                    requestPostNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun promptForExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required for Precise Reminders")
                    .setMessage("To ensure your reminders trigger exactly on time, this app needs special permission to schedule exact alarms. Please grant this in the app settings.")
                    .setPositiveButton("Go to Settings") { _, _ ->
                        try {
                            Log.i("MainActivity", "Navigating to ACTION_REQUEST_SCHEDULE_EXACT_ALARM settings.")
                            requestExactAlarmPermissionLauncher.launch(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    // Adding package name can sometimes help direct to the correct app settings page
                                    data = Uri.parse("package:$packageName")
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Could not start ACTION_REQUEST_SCHEDULE_EXACT_ALARM", e)
                            try {
                                Log.w("MainActivity", "Fallback: Navigating to generic ACTION_APPLICATION_DETAILS_SETTINGS.")
                                requestExactAlarmPermissionLauncher.launch(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = "package:$packageName".toUri()
                                    }
                                )
                            } catch (e2: Exception) {
                                Log.e("MainActivity", "Could not start ACTION_APPLICATION_DETAILS_SETTINGS", e2)
                                Toast.makeText(this, "Could not open app settings.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Later") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.onExactAlarmPermissionDenied()
                        // The LaunchedEffect observing exactAlarmOutcome will show a Toast
                    }
                    .setCancelable(false)
                    .show()
            } else {
                Log.d("MainActivity", "Exact alarm permission already granted when prompt was triggered (should be rare).")
                viewModel.onExactAlarmPermissionGranted() // This will also trigger the LaunchedEffect for outcome
            }
        } else {
            // On versions before Android S, this specific permission is not an issue at runtime
            // or is handled differently (e.g. granted at install for older APIs).
            // We can consider it effectively granted for the purpose of this flow.
            Log.d("MainActivity", "Not Android S+; considering exact alarm permission effectively granted.")
            viewModel.onExactAlarmPermissionGranted() // Notify ViewModel
        }
    }

    private fun checkExactAlarmPermissionAndNotifyViewModel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Log.i("MainActivity", "Exact alarm permission IS GRANTED after returning from settings.")
                viewModel.onExactAlarmPermissionGranted()
            } else {
                Log.w("MainActivity", "Exact alarm permission IS STILL DENIED after returning from settings.")
                viewModel.onExactAlarmPermissionDenied()
            }
        } else {
            Log.d("MainActivity", "Not Android S+; permission check assumes effectively granted.")
            viewModel.onExactAlarmPermissionGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Checking exact alarm permission.")
        // Re-check permission in onResume. This is important if user changes permission
        // manually in settings and then returns to the app.
        checkExactAlarmPermissionAndNotifyViewModel()
    }
}
