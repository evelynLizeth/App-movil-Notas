package com.eve.notas.data.repository


import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.local.dao.StudentDao
import com.eve.notas.data.local.dao.GradeDao
import com.eve.notas.data.local.dao.TaskDao
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class NotesRepository(
                      private val db: AppDatabase,
                      private val studentDao: StudentDao,
                      private val gradeDao: GradeDao,
                      private val dao: TaskDao

) {
    fun getStudents(): Flow<List<Student>> {
        return studentDao.getAll()
    }

    suspend fun addStudent(student: Student) {
        studentDao.insert(student)
    }

    fun getStudentByIdFlow(id: Long): Flow<Student?> {
        return studentDao.getStudentByIdFlow(id)
    }

    fun searchByName(name: String) = studentDao.searchByName(name)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) = studentDao.delete(student)

    suspend fun insert(student: Student): Long {
        return studentDao.insert(student)
    }

    suspend fun deleteById(id: Long) = studentDao.deleteById(id)

    // Pantalla 2 DETALLE DE NOTAS POR ESTUDIANTE
    suspend fun insertOrUpdateGrade(studentId: Long, taskId: Long, value: Double) {
        val grade = Grade(studentId = studentId, taskId = taskId, value = value)
        gradeDao.insertOrUpdate(grade)
    }

    suspend fun updateStudentAverage(studentId: Long, average: Double) {
        // 🔹 Obtenemos el Student directamente con firstOrNull()
        val student = studentDao.getStudentByIdFlow(studentId).firstOrNull()
        if (student != null) {
            val updatedStudent = student.copy(average = average)
            studentDao.updateStudent(updatedStudent)
        }
    }
    val tasks: Flow<List<Task>> = dao.getAll()

    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)

}

