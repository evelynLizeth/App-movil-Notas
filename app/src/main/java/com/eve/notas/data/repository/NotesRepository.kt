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

/**
 * Repositorio central de la app. Actúa como única fuente de verdad
 * entre los ViewModels y los DAOs de Room.
 *
 * Centraliza el acceso a las tres entidades: [Student], [Task] y [Grade].
 */
class NotesRepository(
    private val db: AppDatabase,
    private val studentDao: StudentDao,
    private val gradeDao: GradeDao,
    private val taskDao: TaskDao
) {

    // ── Estudiantes ───────────────────────────────────────────────────────────

    /** Obtiene todos los estudiantes ordenados por nombre (reactivo) */
    fun getStudents(): Flow<List<Student>> = studentDao.getAll()

    /** Inserta un nuevo estudiante y retorna su id generado */
    suspend fun addStudent(student: Student) = studentDao.insert(student)

    /** Obtiene un estudiante por id como [Flow] para observar cambios en tiempo real */
    fun getStudentByIdFlow(id: Long): Flow<Student?> = studentDao.getStudentByIdFlow(id)

    /** Busca estudiantes cuyo nombre contenga el texto dado (reactivo) */
    fun searchByName(name: String) = studentDao.searchByName(name)

    /** Actualiza los datos de un estudiante existente */
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    /** Elimina un estudiante de la base de datos */
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)

    /** Inserta un estudiante y retorna su id generado */
    suspend fun insert(student: Student): Long = studentDao.insert(student)

    /** Elimina un estudiante por su id */
    suspend fun deleteById(id: Long) = studentDao.deleteById(id)

    // ── Notas (Grade) ─────────────────────────────────────────────────────────

    /**
     * Obtiene las notas de un estudiante como [Flow].
     * Se actualiza automáticamente cuando se modifica alguna nota.
     */
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>> =
        gradeDao.getGradesByStudent(studentId)

    /** Obtiene la lista de notas de un estudiante en una sola llamada (no reactiva) */
    suspend fun getGradesByStudentList(studentId: Long): List<Grade> =
        gradeDao.getGradesByStudentList(studentId)

    /**
     * Alias de [getGradesByStudent]. Retorna las notas de un estudiante como [Flow].
     * Se usa en [DetailViewModel] para combinar notas y calcular el promedio.
     */
    fun getGradesFlow(studentId: Long): Flow<List<Grade>> =
        gradeDao.getGradesFlow(studentId)

    /**
     * Inserta o actualiza una nota para la combinación (estudiante, tarea).
     * Si ya existe la nota se reemplaza; si no existe se crea.
     */
    suspend fun insertOrUpdateGrade(studentId: Long, taskId: Long, value: Double) {
        val grade = Grade(studentId = studentId, taskId = taskId, value = value)
        gradeDao.insertOrUpdate(grade)
    }

    /** Actualiza el valor de una nota existente */
    suspend fun updateGrade(studentId: Long, taskId: Long, value: Double) =
        gradeDao.updateGrade(studentId, taskId, value)

    /**
     * Inserta o actualiza la nota de un estudiante para una tarea específica.
     * Equivalente a [insertOrUpdateGrade], usado desde [DetailViewModel].
     */
    suspend fun updateNota(studentId: Long, taskId: Long, nota: Double) {
        val grade = Grade(studentId = studentId, taskId = taskId, value = nota)
        gradeDao.insertOrUpdate(grade)
    }

    // ── Promedio ──────────────────────────────────────────────────────────────

    /** Actualiza el promedio persistido en el registro del estudiante */
    suspend fun updateStudentAverage(studentId: Long, promedio: Double) =
        studentDao.updateAverage(studentId, promedio)

    /**
     * Persiste la escala de calificación seleccionada (10 o 20) en el estudiante.
     */
    suspend fun updateNotaMaxima(studentId: Long, notaMaxima: Int) =
        studentDao.updateNotaMaxima(studentId, notaMaxima)

    /**
     * Actualiza la escala de calificación para TODOS los estudiantes.
     * Se llama desde MainViewModel cuando el usuario cambia la escala global.
     */
    suspend fun updateAllStudentsNotaMaxima(notaMaxima: Int) =
        studentDao.updateAllNotaMaxima(notaMaxima)

    /** Elimina todas las notas registradas y resetea los promedios a 0. */
    suspend fun deleteAllGradesAndResetAverages() {
        gradeDao.deleteAllGrades()
        studentDao.resetAllAverages()
    }

    /** Retorna true si hay al menos una nota registrada en la base de datos. */
    suspend fun hasAnyGrades(): Boolean = gradeDao.hasAnyGrades()

    /**
     * Obtiene el promedio de un estudiante usando la función AVG de SQL.
     * Retorna 0.0 si el estudiante no tiene notas registradas.
     */
    fun getPromedioByStudent(studentId: Long): Flow<Double> =
        gradeDao.getPromedioByStudent(studentId)
            .map { it ?: 0.0 }

    // ── Tareas ────────────────────────────────────────────────────────────────

    /** Flujo reactivo con todas las tareas. Usado en ViewModels que observan cambios */
    val tasks: Flow<List<Task>> = taskDao.getTasks()

    /** Obtiene la lista de tareas en una sola llamada (no reactiva) */
    suspend fun getTasksList(): List<Task> = taskDao.getTasksList()

    /** Inserta una nueva tarea en la base de datos */
    suspend fun insert(task: Task) = taskDao.insert(task)

    /** Actualiza los datos de una tarea existente */
    suspend fun update(task: Task) = taskDao.update(task)

    /** Elimina una tarea de la base de datos */
    suspend fun delete(task: Task) = taskDao.delete(task)
}
