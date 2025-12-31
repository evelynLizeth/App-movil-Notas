package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eve.notas.data.model.Grade
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {

    @Query("SELECT * FROM grade ")
    fun getAll(): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(grade: Grade)

    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>>

    @Query("UPDATE grade SET value = :nota WHERE taskId = :taskId AND studentId = :studentId")
    suspend fun updateGrade(studentId: Long, taskId: Long, nota: Double)

    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    fun getGradesFlow(studentId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    suspend fun getGradesByStudentList(studentId: Long): List<Grade>

    @Query("SELECT AVG(value) FROM grade WHERE studentId = :studentId")
    fun getPromedioByStudent(studentId: Long): Flow<Double?>

}