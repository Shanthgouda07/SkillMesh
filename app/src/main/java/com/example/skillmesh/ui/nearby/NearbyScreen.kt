package com.example.skillmesh.ui.nearby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmesh.nearby.DiscoveredPeer
import com.example.skillmesh.theme.AccentPrimary
import com.example.skillmesh.theme.MatchedGreen
import com.example.skillmesh.ui.components.NearbyApprovalDialog

@Composable
fun NearbyScreen(
    viewModel: NearbyViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val nearbyProfiles by viewModel.nearbyProfiles.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nearby Radar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Discover peers over Bluetooth & Wi-Fi without internet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Radar Visual Animation
            RadarAnimation(isScanning = state.isScanning)

            Spacer(modifier = Modifier.height(20.dp))

            // Scan / Stop Toggle Button
            Button(
                onClick = {
                    if (state.isScanning) viewModel.stopScanning() else viewModel.startScanning()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isScanning) MaterialTheme.colorScheme.surfaceVariant else AccentPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (state.isScanning) AccentPrimary else Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isScanning) "Stop Discovery" else "Start Discovery",
                    color = if (state.isScanning) MaterialTheme.colorScheme.onSurface else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Discovered / Connected Peers Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVERED PEERS (${state.discoveredPeers.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${nearbyProfiles.size} Synced Profiles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MatchedGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Peer list
            if (state.discoveredPeers.isEmpty() && nearbyProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.isScanning) "Broadcasting and listening for peers..." else "Tap 'Start Discovery' to scan nearby phones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(state.discoveredPeers, key = { it.endpointId }) { peer ->
                        DiscoveredPeerItem(peer = peer)
                    }
                }
            }
        }

        // Pending Connection Approval Dialog with digits
        state.pendingConnection?.let { pending ->
            NearbyApprovalDialog(
                pending = pending,
                onApprove = { viewModel.approveConnection(pending.endpointId) },
                onDecline = { viewModel.rejectConnection(pending.endpointId) }
            )
        }

        // Status banner
        state.statusMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    action = {
                        IconButton(onClick = viewModel::clearStatusMessage) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(msg, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun DiscoveredPeerItem(peer: DiscoveredPeer) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${peer.shortId} • Nearby P2P Cluster",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MatchedGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RadarAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val maxRadius = size.minDimension / 2

            // Static concentric tracks
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.1f),
                radius = maxRadius * 0.4f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.1f),
                radius = maxRadius * 0.7f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.1f),
                radius = maxRadius,
                style = Stroke(width = 1.dp.toPx())
            )

            // Animated pulses
            if (isScanning) {
                drawCircle(
                    color = AccentPrimary.copy(alpha = (1f - pulse1) * 0.4f),
                    radius = maxRadius * pulse1,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = AccentPrimary.copy(alpha = (1f - pulse2) * 0.4f),
                    radius = maxRadius * pulse2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center Beacon icon
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isScanning) AccentPrimary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = if (isScanning) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
