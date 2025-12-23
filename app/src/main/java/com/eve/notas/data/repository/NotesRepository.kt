package com.eve.notas.data.repository

import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.model.Student
import com.eve.notas.data.local.dao.StudentDao

class NotesRepository(private val db: AppDatabase) {
    private val studentDao: StudentDao = db.studentDao()

    fun getStudents(): kotlinx.coroutines.flow.Flow<List<Student>> {
        return studentDao.getStudents()
    }
    suspend fun addStudent(student: Student) {
        studentDao.insert(student)
    }
    fun getStudentById(id: Long): Student? {
        return studentDao.getStudentById(id)
    }
    fun searchByName(name: String) = studentDao.searchByName(name)
    suspend fun updateStudent(student: Student) = studentDao.update(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)
    // NotesRepository
    suspend fun insert(student: Student): Long {
        return studentDao.insert(student)
    }
}