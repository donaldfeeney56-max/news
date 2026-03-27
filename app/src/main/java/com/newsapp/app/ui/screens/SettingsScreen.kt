package com.newsapp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onFeedbackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val autoRefresh by viewModel.autoRefresh.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Dark Theme") },
                supportingContent = { Text("Switch between light and dark mode") },
                leadingContent = { Icon(if (isDarkTheme) Icons.Default.Brightness2 else Icons.Default.Brightness7, null) },
                trailingContent = { Switch(checked = isDarkTheme, onCheckedChange = {
                    SportNewsApp.amplitude.track("Setting Changed", mapOf("setting" to "dark_theme", "value" to it.toString()))
                    viewModel.setDarkTheme(it)
                }) }
            )
            HorizontalDivider()

            // Notifications
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Push Notifications") },
                supportingContent = { Text("Get notified about breaking news") },
                leadingContent = { Icon(Icons.Default.Notifications, null) },
                trailingContent = { Switch(checked = notificationsEnabled, onCheckedChange = {
                    SportNewsApp.amplitude.track("Setting Changed", mapOf("setting" to "notifications", "value" to it.toString()))
                    viewModel.setNotificationsEnabled(it)
                }) }
            )
            HorizontalDivider()

            // Data & Refresh
            Text(
                text = "Data & Refresh",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Auto Refresh") },
                supportingContent = { Text("Automatically refresh news feed") },
                leadingContent = { Icon(Icons.Default.Refresh, null) },
                trailingContent = { Switch(checked = autoRefresh, onCheckedChange = {
                    SportNewsApp.amplitude.track("Setting Changed", mapOf("setting" to "auto_refresh", "value" to it.toString()))
                    viewModel.setAutoRefresh(it)
                }) }
            )
            if (autoRefresh) {
                ListItem(
                    headlineContent = { Text("Refresh Interval") },
                    supportingContent = { Text("Every $refreshInterval minutes") },
                    leadingContent = { Icon(Icons.Default.Schedule, null) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (refreshInterval > 5) viewModel.setRefreshInterval(refreshInterval - 5) }) {
                                Icon(Icons.Default.RemoveCircleOutline, "Decrease")
                            }
                            Text("$refreshInterval min")
                            IconButton(onClick = { if (refreshInterval < 60) viewModel.setRefreshInterval(refreshInterval + 5) }) {
                                Icon(Icons.Default.Add, "Increase")
                            }
                        }
                    }
                )
            }
            HorizontalDivider()

            // Feedback
            Text(
                text = "Support",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Send Feedback") },
                supportingContent = { Text("Report a bug or suggest an improvement") },
                leadingContent = { Icon(Icons.Default.Feedback, null) },
                modifier = Modifier.clickable {
                    SportNewsApp.amplitude.track("Feedback Screen Opened")
                    onFeedbackClick()
                }
            )
            HorizontalDivider()

            // About
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ListItem(
                headlineContent = { Text("SportNews") },
                supportingContent = { Text("Version 1.0.0") },
                leadingContent = { Icon(Icons.Default.Info, null) }
            )
            ListItem(
                headlineContent = { Text("Powered by") },
                supportingContent = { Text("ESPN Public API") },
                leadingContent = { Icon(Icons.Default.Code, null) }
            )
        }
    }
}
