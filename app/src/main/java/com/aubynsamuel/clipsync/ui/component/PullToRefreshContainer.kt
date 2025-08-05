package com.aubynsamuel.clipsync.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPullToRefreshBox(
    refreshPairedDevices: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        state = refreshState,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                refreshPairedDevices()
                delay(1000)
                isRefreshing = false
            }
        },
        indicator = {
            Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = refreshState,
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) {
        content()
    }
}

