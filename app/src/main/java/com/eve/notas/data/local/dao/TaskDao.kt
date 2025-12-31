package com.eve.notas.data.local.dao

import androidx.room.*
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY name ASC")
    fun getAll(): Flow<List<Task>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)
    @Update
    suspend fun update(task: Task)
    @Delete
    suspend fun delete(task: Task)
    @Query("UPDATE task SET nota = :nota WHERE id = :taskId")
    suspend fun updateNota(taskId: Long, nota: Double)

    @Query("SELECT * FROM task")
    fun getTasks(): Flow<List<Task>>   // Para observar en la UI

    @Query("SELECT * FROM task")
    suspend fun getTasksList(): List<Task>
}