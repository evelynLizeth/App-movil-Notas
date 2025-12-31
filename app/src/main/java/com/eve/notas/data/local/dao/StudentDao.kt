package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.eve.notas.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAll(): Flow<List<Student>>
    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentByIdFlow(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :name || '%'")
    fun searchByName(name: String): Flow<List<Student>>   // 👈 búsqueda por nombre
    @Insert
    suspend fun insert(student: Student): Long
    @Delete
    suspend fun delete(student: Student)
    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET average = :average WHERE id = :studentId")
    suspend fun updateAverage(studentId: Long, average: Double)
}