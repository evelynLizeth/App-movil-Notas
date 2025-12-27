package com.eve.notas.data.local.dao

import androidx.room.*
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Query("SELECT * FROM task ORDER BY name ASC")
    fun getAll(): Flow<List<Task>>

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}