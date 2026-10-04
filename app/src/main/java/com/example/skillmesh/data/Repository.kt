package com.example.skillmesh.data

import com.example.skillmesh.demo.DemoData
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Project
import com.example.skillmesh.domain.ProjectTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class Repository(
    private val db: AppDatabase
) {
    private val profileDao = db.profileDao()
    private val projectDao = db.projectDao()
    private val taskDao = db.taskDao()

    val selfProfile: Flow<Profile?> = profileDao.getSelfProfileFlow().map { it?.toDomain() }

    val discoverableProfiles: Flow<List<Profile>> = profileDao.getDiscoverableProfilesFlow().map { list ->
        list.map { it.toDomain() }
    }

    val nearbyProfiles: Flow<List<Profile>> = profileDao.getProfilesBySourceFlow(ProfileSource.NEARBY).map { list ->
        list.map { it.toDomain() }
    }

    val projects: Flow<List<Project>> = projectDao.getAllProjectsFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getSelfProfile(): Profile? = profileDao.getSelfProfile()?.toDomain()

    suspend fun saveSelfProfile(profile: Profile) {
        profileDao.upsertProfile(ProfileEntity.fromDomain(profile, ProfileSource.SELF))
    }

    suspend fun getOrCreateDefaultSelfProfile(): Profile {
        val existing = getSelfProfile()
        if (existing != null) return existing

        val defaultProfile = Profile(
            id = UUID.randomUUID().toString(),
            name = "My Device",
            bio = "Exploring offline collaboration and edge AI.",
            skills = listOf("Android", "Kotlin"),
            interests = listOf("Edge Computing", "Open Source"),
            experience = "Intermediate",
            available = true
        )
        saveSelfProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun upsertNearby(profile: Profile) {
        profileDao.upsertProfile(ProfileEntity.fromDomain(profile, ProfileSource.NEARBY))
    }

    suspend fun clearNearby() {
        profileDao.clearNearbyProfiles()
    }

    suspend fun seedDemoData() {
        val demoEntities = DemoData.people.map {
            ProfileEntity.fromDomain(it, ProfileSource.DEMO)
        }
        profileDao.upsertAllProfiles(demoEntities)

        val projectEntities = DemoData.projects.map {
            ProjectEntity.fromDomain(it)
        }
        projectDao.insertAllProjects(projectEntities)

        DemoData.tasks.values.flatten().forEach { task ->
            taskDao.insertTask(TaskEntity.fromDomain(task))
        }
    }

    suspend fun clearDemoData() {
        profileDao.clearDemoProfiles()
    }

    suspend fun isDemoDataLoaded(): Boolean {
        return profileDao.countDemoProfiles() > 0
    }

    suspend fun toggleDemoData(enable: Boolean) {
        if (enable) {
            seedDemoData()
        } else {
            clearDemoData()
        }
    }

    fun getProject(id: String): Flow<Project?> = projectDao.getProjectByIdFlow(id).map { it?.toDomain() }

    suspend fun getProjectById(id: String): Project? = projectDao.getProjectById(id)?.toDomain()

    suspend fun createProject(project: Project) {
        projectDao.insertProject(ProjectEntity.fromDomain(project))
    }

    suspend fun deleteProject(id: String) {
        taskDao.deleteTasksForProject(id)
        projectDao.deleteProject(id)
    }

    fun getTasksForProject(projectId: String): Flow<List<ProjectTask>> =
        taskDao.getTasksForProjectFlow(projectId).map { list -> list.map { it.toDomain() } }

    suspend fun addTask(task: ProjectTask) {
        taskDao.insertTask(TaskEntity.fromDomain(task))
    }

    suspend fun toggleTaskDone(taskId: String, done: Boolean) {
        taskDao.setTaskDone(taskId, done)
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
    }
}
