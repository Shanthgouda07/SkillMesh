package com.example.skillmesh.ai

import com.example.skillmesh.domain.SkillTaxonomy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object JsonSanitizer {
    private val lenientJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun extractFirstJsonBlock(rawOutput: String): String? {
        val start = rawOutput.indexOf('{')
        val end = rawOutput.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return rawOutput.substring(start, end + 1)
        }
        return null
    }

    fun parseExtractedProfile(rawOutput: String, sourceText: String): ExtractedProfile? {
        val jsonBlock = extractFirstJsonBlock(rawOutput) ?: return null
        return try {
            val root = lenientJson.parseToJsonElement(jsonBlock) as? JsonObject ?: return null

            val rawSkills = mutableListOf<String>()
            root["skills"]?.let { elem ->
                if (elem is kotlinx.serialization.json.JsonArray) {
                    elem.forEach { item ->
                        val str = item.jsonPrimitive.content.trim()
                        if (str.isNotEmpty()) rawSkills.add(str)
                    }
                }
            }

            val rawInterests = mutableListOf<String>()
            root["interests"]?.let { elem ->
                if (elem is kotlinx.serialization.json.JsonArray) {
                    elem.forEach { item ->
                        val str = item.jsonPrimitive.content.trim()
                        if (str.isNotEmpty()) rawInterests.add(str)
                    }
                }
            }

            val rawExperience = root["experience"]?.jsonPrimitive?.content?.trim() ?: "Intermediate"
            val normalizedExp = when (rawExperience.lowercase()) {
                "beginner", "junior" -> "Beginner"
                "advanced", "senior", "lead", "expert" -> "Advanced"
                else -> "Intermediate"
            }

            // Canonicalize and drop unknowns
            val canonicalSkills = rawSkills.mapNotNull { SkillTaxonomy.canonicalize(it) }.toMutableList()

            // Merge with deterministic extraction from source text so obvious skills are never missed
            val deterministicSkills = SkillTaxonomy.extractFromText(sourceText)
            for (s in deterministicSkills) {
                if (!canonicalSkills.contains(s)) {
                    canonicalSkills.add(s)
                }
            }

            ExtractedProfile(
                skills = canonicalSkills.distinct(),
                interests = rawInterests.distinct(),
                experience = normalizedExp
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseStringList(rawOutput: String): List<String> {
        val jsonBlock = rawOutput.substringAfter('[', "").substringBeforeLast(']', "")
        if (jsonBlock.isEmpty()) return emptyList()

        return try {
            val fullArrayString = "[$jsonBlock]"
            val elem = lenientJson.parseToJsonElement(fullArrayString) as? kotlinx.serialization.json.JsonArray
            elem?.mapNotNull { it.jsonPrimitive.content.trim().takeIf { s -> s.isNotEmpty() } } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
