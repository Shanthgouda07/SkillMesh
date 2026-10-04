package com.example.skillmesh

import android.app.Application
import com.example.skillmesh.ai.MediaPipeEngine
import com.example.skillmesh.data.AppDatabase
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.nearby.NearbyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SkillMeshApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var database: AppDatabase
        private set

    lateinit var repository: Repository
        private set

    lateinit var aiEngine: MediaPipeEngine
        private set

    lateinit var nearbyManager: NearbyManager
        private set

    private var currentProfileCache: Profile? = null

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = Repository(database)

        // Preload default self profile
        applicationScope.launch(Dispatchers.IO) {
            val self = repository.getOrCreateDefaultSelfProfile()
            currentProfileCache = self

            // Also seed demo data if empty so single-device demo immediately works
            if (!repository.isDemoDataLoaded()) {
                repository.seedDemoData()
            }
        }

        // Initialize on-device AI in background
        aiEngine = MediaPipeEngine(this)
        applicationScope.launch {
            aiEngine.initialize()
        }

        // Initialize NearbyManager
        nearbyManager = NearbyManager(
            context = this,
            repository = repository,
            meProvider = {
                currentProfileCache ?: Profile(
                    id = "local-me",
                    name = "SkillMesh User",
                    bio = "Offline peer on SkillMesh",
                    skills = listOf("Android", "Kotlin"),
                    interests = listOf("Offline Mesh"),
                    experience = "Intermediate",
                    available = true
                )
            }
        )

        // Observe profile updates to keep nearby broadcast cache updated
        applicationScope.launch {
            repository.selfProfile.collect { updated ->
                if (updated != null) {
                    currentProfileCache = updated
                }
            }
        }
    }
}
