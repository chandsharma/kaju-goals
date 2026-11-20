package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.data.sync.SyncPhase
import com.chandsharma.kajugoals.data.sync.SyncUiState
import com.chandsharma.kajugoals.ui.format.formatTimestamp
import com.chandsharma.kajugoals.ui.theme.KajuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(
    syncState: SyncUiState,
    onConnectOrSync: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("You", style = MaterialTheme.typography.headlineLarge) },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KajuTheme.extendedColors.mint, RoundedCornerShape(28.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Private by default", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Your goals stay on this device unless you choose Google Drive sync. There are no ads, analytics, trackers, or Kaju cloud account.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KajuTheme.extendedColors.glacier, RoundedCornerShape(28.dp))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Google Drive sync", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        when (syncState.phase) {
                            SyncPhase.DISCONNECTED -> "Optional · off"
                            SyncPhase.CONNECTING -> "Connecting…"
                            SyncPhase.SYNCING -> "Syncing changes…"
                            SyncPhase.NEEDS_AUTHORIZATION -> "Reconnect needed"
                            SyncPhase.ERROR -> "Needs attention"
                            SyncPhase.IDLE -> syncState.lastSyncedAt?.let {
                                "Last synced ${formatTimestamp(it)}"
                            } ?: "Connected"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Kaju Goals stores one hidden app-data file in the Google account you choose. It cannot see other Drive files, and the data is never sent to us.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    syncState.message?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Button(
                        onClick = onConnectOrSync,
                        enabled = syncState.phase != SyncPhase.CONNECTING &&
                            syncState.phase != SyncPhase.SYNCING,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (syncState.phase == SyncPhase.CONNECTING || syncState.phase == SyncPhase.SYNCING) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 10.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        Text(
                            when (syncState.phase) {
                                SyncPhase.DISCONNECTED -> "Connect Google Drive"
                                SyncPhase.NEEDS_AUTHORIZATION,
                                SyncPhase.ERROR,
                                -> "Reconnect and sync"
                                else -> "Sync now"
                            },
                        )
                    }
                    if (syncState.isConnected &&
                        syncState.phase != SyncPhase.CONNECTING &&
                        syncState.phase != SyncPhase.SYNCING
                    ) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Turn off sync")
                        }
                        Text(
                            "Turning sync off keeps both the local copy and the hidden Drive copy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Kaju Goals", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Development build 0.1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Open-source development build. Cloud restore is merge-first: a fresh empty install downloads existing Drive data before it can upload anything.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
