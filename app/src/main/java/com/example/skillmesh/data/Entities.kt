package com.example.skillmesh.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Project
import com.example.skillmesh.domain.ProjectTask

enum class ProfileSource {
    SELF,
    NEARBY,
    DEMO
}

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bio: String,
    val skills: List<String>,
    val interests: List<String>,
    val experience: String,
    val available: Boolean,
    val source: ProfileSource,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Profile = Profile(
        id = id,
        name = name,
        bio = bio,
        skills = skills,
        interests = interests,
        experience = experience,
        available = available
    )

    companion object {
        fun fromDomain(profile: Profile, source: ProfileSource): ProfileEntity = ProfileEntity(
            id = profile.id,
            name = profile.name,
            bio = profile.bio,
            skills = profile.skills,
            interests = profile.interests,
            experience = profile.experience,
            available = profile.available,
            source = source,
            lastSeenTimestamp = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val requiredSkills: List<String>,
    val memberIds: List<String>
) {
    fun toDomain(): Project = Project(
        id = id,
        title = title,
        description = description,
        requiredSkills = requiredSkills,
        memberIds = memberIds
    )

    companion object {
        fun fromDomain(project: Project): ProjectEntity = ProjectEntity(
            id = project.id,
            title = project.title,
            description = project.description,
            requiredSkills = project.requiredSkills,
            memberIds = project.memberIds
        )
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val text: String,
    val done: Boolean
) {
    fun toDomain(): ProjectTask = ProjectTask(
        id = id,
        projectId = projectId,
        text = text,
        done = done
    )

    companion object {
        fun fromDomain(task: ProjectTask): TaskEntity = TaskEntity(
            id = task.id,
            projectId = task.projectId,
            text = task.text,
            done = task.done
        )
    }
}
