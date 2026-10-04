package com.example.skillmesh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE source = 'SELF' LIMIT 1")
    fun getSelfProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE source = 'SELF' LIMIT 1")
    suspend fun getSelfProfile(): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE source != 'SELF' ORDER BY lastSeenTimestamp DESC")
    fun getDiscoverableProfilesFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE source = :source ORDER BY lastSeenTimestamp DESC")
    fun getProfilesBySourceFlow(source: ProfileSource): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllProfiles(profiles: List<ProfileEntity>)

    @Query("DELETE FROM profiles WHERE source = 'DEMO'")
    suspend fun clearDemoProfiles()

    @Query("DELETE FROM profiles WHERE source = 'NEARBY'")
    suspend fun clearNearbyProfiles()

    @Query("SELECT COUNT(*) FROM profiles WHERE source = 'DEMO'")
    suspend fun countDemoProfiles(): Int
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectByIdFlow(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectEntity>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksForProjectFlow(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    suspend fun getTasksForProject(projectId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET done = :done WHERE id = :id")
    suspend fun setTaskDone(id: String, done: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteTasksForProject(projectId: String)
}
