package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eve.notas.data.model.Grade
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Insert suspend fun insert(grade: Grade)
    @Query("SELECT * FROM grade ")
    fun getAll(): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(grade: Grade)

    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>>

}