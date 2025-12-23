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

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM students")
    fun getStudents(): kotlinx.coroutines.flow.Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Student?   // 👈 devuelve un alumno por id

    @Query("SELECT * FROM students WHERE name LIKE '%' || :name || '%'")
    fun searchByName(name: String): Flow<List<Student>>   // 👈 búsqueda por nombre

    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

}