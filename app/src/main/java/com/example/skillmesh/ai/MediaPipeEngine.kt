package com.example.skillmesh.ai

import android.content.Context
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Requirement
import com.example.skillmesh.domain.SkillTaxonomy
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class MediaPipeEngine(
    private val context: Context
) : LocalAIEngine {

    private var llmInference: LlmInference? = null
    private val _status = MutableStateFlow(AIModelStatus.LOADING)
    val status: StateFlow<AIModelStatus> = _status.asStateFlow()

    override val isLocalModelActive: Boolean
        get() = llmInference != null

    override val statusLabel: String
        get() = when (_status.value) {
            AIModelStatus.READY -> "AI: On-Device (Gemma)"
            AIModelStatus.LOADING -> "AI: Initializing..."
            AIModelStatus.FALLBACK -> "AI: Fallback Mode"
        }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val modelFile = findModelFile()
        if (modelFile == null || !modelFile.exists()) {
            _status.value = AIModelStatus.FALLBACK
            return@withContext
        }

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .build()

            val instance = LlmInference.createFromOptions(context, options)
            llmInference = instance
            // Quick warm-up query
            try {
                instance.generateResponse("Ping")
            } catch (_: Exception) {}

            _status.value = AIModelStatus.READY
        } catch (e: Exception) {
            llmInference = null
            _status.value = AIModelStatus.FALLBACK
        }
    }

    private fun findModelFile(): File? {
        val modelsDir = File(context.getExternalFilesDir(null), "models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }

        // Check common filenames
        val candidates = listOf(
            "gemma.task",
            "gemma-2b-it-cpu-int4.task",
            "gemma-2b-it-gpu-int4.task",
            "model.task"
        )
        for (name in candidates) {
            val f = File(modelsDir, name)
            if (f.exists() && f.length() > 0) return f
        }

        // Return first .task or .bin file found in models dir
        val anyTaskFile = modelsDir.listFiles { _, name -> name.endsWith(".task") || name.endsWith(".bin") }?.firstOrNull()
        return anyTaskFile
    }

    private suspend fun runInferenceWithTimeout(prompt: String): String? = withContext(Dispatchers.IO) {
        val llm = llmInference ?: return@withContext null
        withTimeoutOrNull(12_000L) {
            try {
                llm.generateResponse(prompt)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun extractProfile(freeText: String): ExtractedProfile {
        if (llmInference == null) {
            return FallbackEngine.extractProfile(freeText)
        }

        val prompt = """
            Extract data from the text. Reply with ONLY valid JSON, no explanation.
            Schema: {"skills":[string],"interests":[string],"experience":"Beginner|Intermediate|Advanced"}
            Text: \"\"\"$freeText\"\"\"
            JSON:
        """.trimIndent()

        val output = runInferenceWithTimeout(prompt)
        if (output != null) {
            val parsed = JsonSanitizer.parseExtractedProfile(output, freeText)
            if (parsed != null && (parsed.skills.isNotEmpty() || parsed.interests.isNotEmpty())) {
                return parsed
            }
        }

        // Seamless fallback
        return FallbackEngine.extractProfile(freeText)
    }

    override suspend fun extractRequirement(freeText: String): Requirement {
        if (llmInference == null) {
            return FallbackEngine.extractRequirement(freeText)
        }

        val prompt = """
            Extract technical skills and interests required from this query. Reply with ONLY valid JSON.
            Schema: {"skills":[string],"interests":[string]}
            Text: \"\"\"$freeText\"\"\"
            JSON:
        """.trimIndent()

        val output = runInferenceWithTimeout(prompt)
        if (output != null) {
            val parsed = JsonSanitizer.parseExtractedProfile(output, freeText)
            if (parsed != null) {
                return Requirement(
                    rawText = freeText,
                    requiredSkills = parsed.skills,
                    interests = parsed.interests
                )
            }
        }

        return FallbackEngine.extractRequirement(freeText)
    }

    override suspend fun explainMatch(
        req: Requirement,
        p: Profile,
        matched: List<String>,
        missing: List<String>
    ): String {
        if (llmInference == null) {
            return FallbackEngine.explainMatch(req, p, matched, missing)
        }

        val prompt = """
            Write a 1-2 sentence concise explanation for why ${p.name} matches the requirement.
            Matched skills: ${matched.joinToString(", ")}.
            Missing skills: ${missing.joinToString(", ")}.
            Candidate Experience: ${p.experience}.
            Explanation:
        """.trimIndent()

        val output = runInferenceWithTimeout(prompt)
        val cleanExplanation = output?.trim()?.lines()?.firstOrNull { it.isNotBlank() }
        if (!cleanExplanation.isNullOrBlank() && cleanExplanation.length > 10) {
            return cleanExplanation
        }

        return FallbackEngine.explainMatch(req, p, matched, missing)
    }

    override suspend fun extractTasks(
        projectDescription: String,
        requiredSkills: List<String>
    ): List<String> {
        if (llmInference == null) {
            return FallbackEngine.extractTasks(projectDescription, requiredSkills)
        }

        val prompt = """
            Generate 3 to 4 concise engineering tasks for:
            Description: $projectDescription
            Required skills: ${requiredSkills.joinToString(", ")}
            Reply with ONLY a JSON array of strings, e.g. ["task 1", "task 2"].
            Tasks:
        """.trimIndent()

        val output = runInferenceWithTimeout(prompt)
        if (output != null) {
            val tasks = JsonSanitizer.parseStringList(output)
            if (tasks.isNotEmpty()) {
                return tasks
            }
        }

        return FallbackEngine.extractTasks(projectDescription, requiredSkills)
    }
}
