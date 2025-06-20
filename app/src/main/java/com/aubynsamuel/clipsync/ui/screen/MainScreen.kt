package com.aubynsamuel.clipsync.ui.screen

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.core.Essentials.addresses
import com.aubynsamuel.clipsync.core.Essentials.isDarkMode
import com.aubynsamuel.clipsync.core.Essentials.serviceStarted
import com.aubynsamuel.clipsync.core.changeTheme
import com.aubynsamuel.clipsync.core.showToast
import com.aubynsamuel.clipsync.ui.component.ActionButtons
import com.aubynsamuel.clipsync.ui.component.DarkModeToggle
import com.aubynsamuel.clipsync.ui.component.DeviceItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startBluetoothService: (Set<String>) -> Unit,
    pairedDevices: Set<BluetoothDevice>,
    refresh: () -> Unit,
    stopBluetoothService: () -> Unit,
    navController: NavHostController,
) {
    var selectedDeviceAddresses by remember { mutableStateOf<Set<String>>(addresses.toSet()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(selectedDeviceAddresses) {
        delay(300)
        addresses = selectedDeviceAddresses.toTypedArray()
        delay(300)
        if (serviceStarted) {
            Essentials.bluetoothService?.updateSelectedDevices()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.background,
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = colorScheme.primary,
                        scrolledContainerColor = colorScheme.primary
                    ),
                title = {
                    Text(
                        text = "ClipSync",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimary
                    )
                    AnimatedVisibility(
                        selectedDeviceAddresses.isNotEmpty(),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = selectedDeviceAddresses.count().toString(),
                            fontSize = 18.sp,
                            color = colorScheme.onPrimary, fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        DarkModeToggle(
                            isDarkMode = isDarkMode,
                            onToggle = { changeTheme(context) },
                        )
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings Button",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("SettingsScreen")
                                }
                                .size(25.dp)
                        )
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier
                                .clickable {
                                    refresh()
                                    showToast("Paired Devices Refreshed", context)
                                }
                                .padding(end = 5.dp)
                                .size(25.dp)
                        )
                    }
                },
            )

        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select devices to share clipboard with",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(pairedDevices.toList()) { device ->
                    val name = if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) "Unknown Device" else device.name ?: "Unknown Device"
                    val address = device.address
                    val isSelected = selectedDeviceAddresses.contains(address)

                    DeviceItem(
                        onChecked = {
                            selectedDeviceAddresses =
                                if (!isSelected) selectedDeviceAddresses + address
                                else selectedDeviceAddresses - address
                        },
                        checked = isSelected,
                        name = name,
                        address = address
                    )
                }
            }
            ActionButtons(
                startBluetoothService = startBluetoothService,
                stopBluetoothService = stopBluetoothService,
                selectedDeviceAddresses = selectedDeviceAddresses,
                scope = scope,
                context = context
            )
        }
    }
}