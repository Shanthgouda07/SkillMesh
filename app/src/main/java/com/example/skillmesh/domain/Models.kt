package com.example.skillmesh.domain

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,                    // random UUID generated once per install
    val name: String,
    val bio: String,
    val skills: List<String>,          // canonical names, e.g. "Android", "Computer Vision"
    val interests: List<String>,
    val experience: String,            // "Beginner" | "Intermediate" | "Advanced"
    val available: Boolean
)

@Serializable
data class Requirement(
    val rawText: String,
    val requiredSkills: List<String>,  // canonical names
    val interests: List<String> = emptyList()
)

data class MatchResult(
    val profile: Profile,
    val score: Int,                    // 0..100
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val explanation: String            // AI-written, fallback to template
)

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val requiredSkills: List<String>,
    val memberIds: List<String> = emptyList()
)

data class ProjectTask(
    val id: String,
    val projectId: String,
    val text: String,
    val done: Boolean = false
)
