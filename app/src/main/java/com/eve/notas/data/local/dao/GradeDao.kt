package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eve.notas.data.model.Grade
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Insert suspend fun insert(grade: Grade)
    @Query("SELECT * FROM grade ORDER BY name ASC")
    fun getAll(): Flow<List<Grade>>
}