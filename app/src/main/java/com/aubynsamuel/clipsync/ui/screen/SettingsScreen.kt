package com.aubynsamuel.clipsync.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aubynsamuel.clipsync.core.Essentials.isServiceBound
import com.aubynsamuel.clipsync.core.Essentials.toggleAutoCopy
import com.aubynsamuel.clipsync.ui.component.AppInfoCard
import com.aubynsamuel.clipsync.ui.component.SettingItem
import com.aubynsamuel.clipsync.ui.component.StatusBarColor
import com.aubynsamuel.clipsync.ui.viewModel.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val autoCopy by settingsViewModel.autoCopy.collectAsStateWithLifecycle()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

    LaunchedEffect(autoCopy) {
        delay(300)
        if (isServiceBound) {
            toggleAutoCopy(autoCopy)
        }
    }

    StatusBarColor(!isDarkMode)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = Color.Transparent,
                    ),
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimaryContainer
                    )
                },
                actions = {
                    TextButton(onClick = {
                        showResetDialog = true
                    }) {
                        Text(
                            "Reset", color = colorScheme.onPrimaryContainer,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button",
                        modifier = Modifier
                            .clickable(onClick = { navController.popBackStack() })
                            .padding(horizontal = 8.dp)
                            .size(30.dp),
                        tint = colorScheme.onPrimaryContainer,
                    )
                }
            )
        }) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Settings") },
                    text = { Text("Are you sure you want to reset all settings to default?") },
                    confirmButton = {
                        TextButton(onClick = {
                            settingsViewModel.resetSettings()
                            showResetDialog = false
                        }) {
                            Text("Reset", color = colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            AppInfoCard()

            SettingItem(
                "Auto Copy", "Automatically copy received text", Icons.Default.ContentCopy,
                actionButton = {
                    Switch(
                        checked = autoCopy,
                        onCheckedChange = {
                            settingsViewModel.toggleAutoCopy()
                        }
                    )
                },
                pressAction = { settingsViewModel.toggleAutoCopy() }
            )

            SettingItem(
                "Dark Theme",
                "Toggle between dark and light theme",
                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                actionButton = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.switchTheme() },
                    )
                },
                pressAction = { settingsViewModel.switchTheme() }
            )

            SettingItem(
                "Support",
                "Get help and view FAQs",
                Icons.Default.Support,
                pressAction = { navController.navigate("SupportScreen") }
            )
        }
    }
}