package com.eve.notas.data.repository

import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.local.dao.StudentDao
import com.eve.notas.data.local.dao.GradeDao
import com.eve.notas.data.local.dao.TaskDao
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesRepository(
    private val db: AppDatabase,
    private val studentDao: StudentDao,
    private val gradeDao: GradeDao,
    private val taskDao: TaskDao
) {
    // 🔹 Estudiantes
    fun getStudents(): Flow<List<Student>> = studentDao.getAll()
    suspend fun addStudent(student: Student) = studentDao.insert(student)
    fun getStudentByIdFlow(id: Long): Flow<Student?> = studentDao.getStudentByIdFlow(id)
    fun searchByName(name: String) = studentDao.searchByName(name)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)
    suspend fun insert(student: Student): Long = studentDao.insert(student)
    suspend fun deleteById(id: Long) = studentDao.deleteById(id)

    // 🔹 Notas
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>> =
        gradeDao.getGradesByStudent(studentId)

    suspend fun getGradesByStudentList(studentId: Long): List<Grade> =
        gradeDao.getGradesByStudentList(studentId)

    fun getGradesFlow(studentId: Long): Flow<List<Grade>> =
        gradeDao.getGradesFlow(studentId)

    suspend fun insertOrUpdateGrade(studentId: Long, taskId: Long, value: Double) {
        val grade = Grade(studentId = studentId, taskId = taskId, value = value)
        gradeDao.insertOrUpdate(grade)
    }

    suspend fun updateGrade(studentId: Long, taskId: Long, value: Double) =
        gradeDao.updateGrade(studentId, taskId, value)

    suspend fun updateNota(studentId: Long, taskId: Long, nota: Double) {
        val grade = Grade(studentId = studentId, taskId = taskId, value = nota)
        gradeDao.insertOrUpdate(grade)
    }

    // 🔹 Promedio
    suspend fun updateStudentAverage(studentId: Long, promedio: Double) =
        studentDao.updateAverage(studentId, promedio)

    // 🔹 Tareas
    val tasks: Flow<List<Task>> = taskDao.getTasks()
    suspend fun getTasksList(): List<Task> = taskDao.getTasksList()
    suspend fun insert(task: Task) = taskDao.insert(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun delete(task: Task) = taskDao.delete(task)
    fun promedioFlow(studentId: Long): Flow<Double> {
        return getGradesFlow(studentId).map { grades ->
            val totalTareas = grades.size
            val sumaNotas = grades.sumOf { it.value }
            if (totalTareas > 0) sumaNotas / totalTareas else 0.0
        }
    }
    fun getPromedioByStudent(studentId: Long): Flow<Double> =
        gradeDao.getPromedioByStudent(studentId)
            .map { it ?: 0.0 } // si no hay notas, devuelve 0.0

}