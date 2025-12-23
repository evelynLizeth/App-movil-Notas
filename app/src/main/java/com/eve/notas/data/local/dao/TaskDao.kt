package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert suspend fun insert(task: Task)
    @Query("SELECT * FROM task ORDER BY name ASC")
    fun getAll(): Flow<List<Task>>
}