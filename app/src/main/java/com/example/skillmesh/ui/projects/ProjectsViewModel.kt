package com.example.skillmesh.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmesh.ai.LocalAIEngine
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.MatchEngine
import com.example.skillmesh.domain.MatchResult
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Project
import com.example.skillmesh.domain.ProjectTask
import com.example.skillmesh.domain.Requirement
import com.example.skillmesh.domain.SkillTaxonomy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val selectedProjectTasks: List<ProjectTask> = emptyList(),
    val isCreatingProject: Boolean = false,
    val isGeneratingTasks: Boolean = false,
    val isSuggestingTeammates: Boolean = false,
    val suggestedTeammates: List<MatchResult> = emptyList(),
    val toastMessage: String? = null
)

class ProjectsViewModel(
    private val repository: Repository,
    private val aiEngine: LocalAIEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.projects.collect { list ->
                _uiState.update { it.copy(projects = list) }
            }
        }
    }

    fun selectProject(project: Project?) {
        _uiState.update { it.copy(selectedProject = project, suggestedTeammates = emptyList()) }
        if (project != null) {
            viewModelScope.launch {
                repository.getTasksForProject(project.id).collect { tasks ->
                    _uiState.update { it.copy(selectedProjectTasks = tasks) }
                }
            }
        }
    }

    fun showCreateDialog(show: Boolean) {
        _uiState.update { it.copy(isCreatingProject = show) }
    }

    fun createProject(title: String, description: String, rawSkills: String) {
        val extractedSkills = SkillTaxonomy.extractFromText(rawSkills).ifEmpty {
            rawSkills.split(",", ";").mapNotNull { SkillTaxonomy.canonicalize(it) }
        }

        viewModelScope.launch {
            val newProject = Project(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Hackathon Project" },
                description = description,
                requiredSkills = extractedSkills.ifEmpty { listOf("Android", "Machine Learning") }
            )
            repository.createProject(newProject)

            // Seed initial task
            val firstTask = ProjectTask(
                id = UUID.randomUUID().toString(),
                projectId = newProject.id,
                text = "Define MVP specifications and architecture",
                done = false
            )
            repository.addTask(firstTask)

            _uiState.update {
                it.copy(
                    isCreatingProject = false,
                    toastMessage = "Project created!"
                )
            }
            selectProject(newProject)
        }
    }

    fun toggleTask(task: ProjectTask) {
        viewModelScope.launch {
            repository.toggleTaskDone(task.id, !task.done)
        }
    }

    fun addTask(text: String) {
        val project = _uiState.value.selectedProject ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val task = ProjectTask(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                text = text.trim(),
                done = false
            )
            repository.addTask(task)
        }
    }

    fun generateTasksWithAI() {
        val project = _uiState.value.selectedProject ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingTasks = true) }
            val taskStrings = aiEngine.extractTasks(project.description, project.requiredSkills)
            taskStrings.forEach { text ->
                val task = ProjectTask(
                    id = UUID.randomUUID().toString(),
                    projectId = project.id,
                    text = text,
                    done = false
                )
                repository.addTask(task)
            }
            _uiState.update {
                it.copy(
                    isGeneratingTasks = false,
                    toastMessage = "Generated ${taskStrings.size} tasks with AI"
                )
            }
        }
    }

    fun suggestTeammates() {
        val project = _uiState.value.selectedProject ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSuggestingTeammates = true) }

            val req = Requirement(
                rawText = project.description,
                requiredSkills = project.requiredSkills
            )

            repository.discoverableProfiles.collect { people ->
                val ranked = MatchEngine.rank(req, people)
                val results = ranked.take(5).map { (profile, scoreTuple) ->
                    val (score, matched, missing) = scoreTuple
                    val explanation = aiEngine.explainMatch(req, profile, matched, missing)
                    MatchResult(
                        profile = profile,
                        score = score,
                        matchedSkills = matched,
                        missingSkills = missing,
                        explanation = explanation
                    )
                }
                _uiState.update {
                    it.copy(
                        suggestedTeammates = results,
                        isSuggestingTeammates = false
                    )
                }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
