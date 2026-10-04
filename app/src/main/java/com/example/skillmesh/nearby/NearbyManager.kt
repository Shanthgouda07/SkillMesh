package com.example.skillmesh.nearby

import android.content.Context
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.Profile
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class DiscoveredPeer(
    val endpointId: String,
    val shortId: String,
    val displayName: String
)

data class PendingConnection(
    val endpointId: String,
    val peerName: String,
    val authDigits: String
)

data class NearbyUiState(
    val isScanning: Boolean = false,
    val discoveredPeers: List<DiscoveredPeer> = emptyList(),
    val pendingConnection: PendingConnection? = null,
    val connectedPeerCount: Int = 0,
    val statusMessage: String? = null
)

class NearbyManager(
    private val context: Context,
    private val repository: Repository,
    private val meProvider: () -> Profile
) {
    private val client = Nearby.getConnectionsClient(context)
    private val serviceId = "com.skillmesh.hackathon2026"
    private val strategy = Strategy.P2P_CLUSTER
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    private val connectedEndpoints = mutableSetOf<String>()
    private val connectingEndpoints = mutableSetOf<String>()

    fun start() {
        if (_uiState.value.isScanning) return

        val myProfile = meProvider()
        val advertisedName = "${myProfile.id.take(6)}|${myProfile.name}"

        _uiState.update { it.copy(isScanning = true, statusMessage = "Scanning for nearby peers...") }

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        client.startAdvertising(
            advertisedName,
            serviceId,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnFailureListener { e ->
            _uiState.update { it.copy(statusMessage = "Advertising error: ${e.localizedMessage}") }
        }

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        client.startDiscovery(
            serviceId,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnFailureListener { e ->
            _uiState.update { it.copy(statusMessage = "Discovery error: ${e.localizedMessage}") }
        }
    }

    fun stop() {
        try {
            client.stopAdvertising()
            client.stopDiscovery()
            client.stopAllEndpoints()
        } catch (_: Exception) {}

        connectedEndpoints.clear()
        connectingEndpoints.clear()

        _uiState.update {
            NearbyUiState(
                isScanning = false,
                statusMessage = "Nearby discovery stopped"
            )
        }
    }

    fun approveConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener {
                _uiState.update { it.copy(pendingConnection = null) }
            }
            .addOnFailureListener { e ->
                _uiState.update {
                    it.copy(
                        pendingConnection = null,
                        statusMessage = "Accept failed: ${e.localizedMessage}"
                    )
                }
            }
    }

    fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId)
        connectingEndpoints.remove(endpointId)
        _uiState.update { it.copy(pendingConnection = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val rawName = info.endpointName
            val parts = rawName.split("|", limit = 2)
            val shortId = parts.getOrNull(0) ?: endpointId.take(4)
            val displayName = parts.getOrNull(1) ?: rawName

            val peer = DiscoveredPeer(endpointId, shortId, displayName)

            _uiState.update { current ->
                val updated = (current.discoveredPeers.filter { it.endpointId != endpointId } + peer)
                current.copy(
                    discoveredPeers = updated,
                    statusMessage = "Found peer: $displayName"
                )
            }

            // Automatically request connection if not already connecting or connected
            if (!connectedEndpoints.contains(endpointId) && !connectingEndpoints.contains(endpointId)) {
                connectingEndpoints.add(endpointId)
                val myProfile = meProvider()
                val myName = "${myProfile.id.take(6)}|${myProfile.name}"
                client.requestConnection(myName, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener { e ->
                        connectingEndpoints.remove(endpointId)
                        _uiState.update { it.copy(statusMessage = "Connection request failed: ${e.localizedMessage}") }
                    }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            connectingEndpoints.remove(endpointId)
            _uiState.update { current ->
                current.copy(
                    discoveredPeers = current.discoveredPeers.filter { it.endpointId != endpointId }
                )
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            val rawName = info.endpointName
            val displayName = rawName.substringAfter("|", rawName)
            val authDigits = info.authenticationDigits

            _uiState.update { current ->
                current.copy(
                    pendingConnection = PendingConnection(
                        endpointId = endpointId,
                        peerName = displayName,
                        authDigits = authDigits
                    )
                )
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            connectingEndpoints.remove(endpointId)

            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                _uiState.update {
                    it.copy(
                        connectedPeerCount = connectedEndpoints.size,
                        statusMessage = "Connected to peer, exchanging profile..."
                    )
                }

                // Send my profile
                scope.launch {
                    val myProfile = meProvider()
                    val profileJson = json.encodeToString(myProfile)
                    val payload = Payload.fromBytes(profileJson.toByteArray(Charsets.UTF_8))
                    client.sendPayload(endpointId, payload)
                }
            } else {
                _uiState.update {
                    it.copy(statusMessage = "Connection failed: ${result.status.statusMessage ?: "Rejected/Cancelled"}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            connectingEndpoints.remove(endpointId)
            _uiState.update {
                it.copy(connectedPeerCount = connectedEndpoints.size)
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val jsonString = String(bytes, Charsets.UTF_8)

            scope.launch {
                try {
                    val remoteProfile = json.decodeFromString<Profile>(jsonString)
                    repository.upsertNearby(remoteProfile)
                    _uiState.update {
                        it.copy(statusMessage = "Received profile for ${remoteProfile.name}!")
                    }
                    // Cleanly disconnect after exchange to preserve privacy & resources
                    client.disconnectFromEndpoint(endpointId)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(statusMessage = "Invalid profile payload received")
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
