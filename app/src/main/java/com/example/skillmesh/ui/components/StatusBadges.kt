package com.example.skillmesh.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmesh.theme.AccentPrimary
import com.example.skillmesh.theme.MatchedGreen
import com.example.skillmesh.theme.ScoreAverage

@Composable
fun StatusBadge(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun isDeviceOffline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val activeNet = cm.activeNetwork ?: return true
    val caps = cm.getNetworkCapabilities(activeNet) ?: return true
    return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun OfflineStatusHeader(
    isLocalAiActive: Boolean,
    nearbyCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOffline = remember { isDeviceOffline(context) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AI Model Badge
        if (isLocalAiActive) {
            StatusBadge(
                label = "Gemma LLM",
                icon = Icons.Default.Memory,
                accentColor = MatchedGreen
            )
        } else {
            StatusBadge(
                label = "Rule Fallback",
                icon = Icons.Default.Memory,
                accentColor = ScoreAverage
            )
        }

        // Nearby Badge
        StatusBadge(
            label = if (nearbyCount > 0) "$nearbyCount Nearby" else "Nearby Idle",
            icon = Icons.Default.Sensors,
            accentColor = AccentPrimary
        )

        // Offline / Airplane indicator
        StatusBadge(
            label = if (isOffline) "100% Offline" else "Local Only",
            icon = if (isOffline) Icons.Default.AirplanemodeActive else Icons.Default.WifiOff,
            accentColor = MatchedGreen
        )
    }
}
