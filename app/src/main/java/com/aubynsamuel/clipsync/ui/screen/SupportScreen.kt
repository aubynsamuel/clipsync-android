package com.aubynsamuel.clipsync.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aubynsamuel.clipsync.R.string
import com.aubynsamuel.clipsync.ui.component.SettingItem
import com.aubynsamuel.clipsync.ui.navigation.safePopBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavHostController,
) {
    val uriHandler = LocalUriHandler.current

    val supportUrl = stringResource(string.support_url)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = Color.Transparent,
                    ),
                title = {
                    Text(
                        text = "Support",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SettingItem(
                        title = "Sharing failed?",
                        subTitle = "Ensure both devices are paired and Bluetooth is " +
                                "enabled on both devices. Make sure the other device is " +
                                "listening",
                        icon = Icons.Default.Error,
                    )

                    SettingItem(
                        title = "Auto-copy not working?",
                        subTitle = "Check if auto-copy is enabled in settings",
                        icon = Icons.Default.ContentCopy,
                    )

                    SettingItem(
                        title = "Battery optimization",
                        subTitle = "Disable battery optimization for ClipSync to prevent " +
                                "background service issues",
                        icon = Icons.Default.BatteryChargingFull,
                    )
                }
            }

            // Contact Section
            Text(
                text = "Contact Support",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SettingItem(
                        title = "Report a Bug",
                        subTitle = "Submit bug reports and technical issues",
                        icon = Icons.Default.BugReport,
                        pressAction = {
                            uriHandler.openUri(supportUrl)
                        }
                    )

                    SettingItem(
                        title = "Feature Request",
                        subTitle = "Suggest new features or improvements",
                        icon = Icons.AutoMirrored.Filled.Help,
                        pressAction = {
                            uriHandler.openUri(supportUrl)
                        }
                    )

                    SettingItem(
                        title = "General Inquiry",
                        subTitle = "General questions and feedback",
                        icon = Icons.Default.Support,
                        pressAction = {
                            uriHandler.openUri(supportUrl)
                        }
                    )
                }
            }
        }
    }
}
