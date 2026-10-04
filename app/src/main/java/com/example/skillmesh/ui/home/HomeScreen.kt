package com.example.skillmesh.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.theme.AccentPrimary
import com.example.skillmesh.theme.MatchedGreen
import com.example.skillmesh.theme.ScoreAverage
import com.example.skillmesh.ui.components.ChipType
import com.example.skillmesh.ui.components.OfflineStatusHeader
import com.example.skillmesh.ui.components.SkillChip
import com.example.skillmesh.ui.components.isDeviceOffline

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDiscover: () -> Unit,
    onNavigateToNearby: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isOffline = isDeviceOffline(context)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // App Bar Title (with Long-Press demo mode trigger)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { viewModel.toggleDemoMode() }
                    )
                ) {
                    Text(
                        text = "SkillMesh",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentPrimary
                    )
                    Text(
                        text = "Decentralized On-Device Skill Network",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.myProfile?.name?.take(1)?.uppercase() ?: "U",
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline Status Badges
            OfflineStatusHeader(
                isLocalAiActive = state.isLocalAiActive,
                nearbyCount = state.nearbyCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isLocalAiActive) "Gemma On-Device LLM" else "Deterministic AI Fallback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isLocalAiActive) "Zero cloud dependency, full privacy" else "Rule-based taxonomy & keyword engine active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Card 2: Nearby P2P Mesh
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToNearby() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MatchedGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MatchedGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nearby P2P Cluster",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.nearbyCount} peers detected • Tap to scan radar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Match Action banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDiscover() }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AccentPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Looking for a collaborator?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "e.g. 'I need someone with Android and computer vision'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToDiscover,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        Text("Find Nearby Peers", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Discoverable Candidates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVERABLE TALENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToDiscover() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            state.topPeers.forEach { peer ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToDiscover() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = peer.name.take(1),
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = peer.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                peer.skills.take(3).forEach { skill ->
                                    SkillChip(text = skill, type = ChipType.NEUTRAL)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Active Hackathon Projects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROJECT ROOMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View Projects",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToProjects() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            state.recentProjects.forEach { proj ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToProjects() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = proj.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = proj.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Demo Mode Toast
        state.bannerMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    action = {
                        IconButton(onClick = viewModel::clearBanner) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    },
                    containerColor = MatchedGreen
                ) {
                    Text(msg, color = Color.White)
                }
            }
        }
    }
}
