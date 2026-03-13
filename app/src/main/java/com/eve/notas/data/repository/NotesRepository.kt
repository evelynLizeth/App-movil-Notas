package com.eve.notas.data.repository

import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.local.dao.StudentDao
import com.eve.notas.data.local.dao.GradeDao
import com.eve.notas.data.local.dao.TaskDao
import com.eve.notas.data.local.dao.UserDao
import com.eve.notas.data.local.dao.InstitutionDao
import com.eve.notas.data.local.dao.CourseDao
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.Task
import com.eve.notas.data.model.User
import com.eve.notas.data.model.Institution
import com.eve.notas.data.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio central de la app. Actúa como única fuente de verdad
 * entre los ViewModels y los DAOs de Room.
 *
 * Centraliza el acceso a todas las entidades:
 * [Student], [Task], [Grade], [User], [Institution] y [Course].
 */
class NotesRepository(
    private val db: AppDatabase,
    private val studentDao: StudentDao,
    private val gradeDao: GradeDao,
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val institutionDao: InstitutionDao,
    private val courseDao: CourseDao
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

    /**
     * Obtiene los estudiantes de un curso específico como [Flow] reactivo.
     * Se usa en las pantallas que muestran estudiantes filtrados por curso.
     *
     * @param courseId ID del curso al que pertenecen los estudiantes
     */
    fun getStudentsByCourse(courseId: Long): Flow<List<Student>> =
        studentDao.getByCourse(courseId)

    /**
     * Inserta un estudiante asignándolo a un curso específico.
     * Retorna el id generado por Room.
     *
     * @param student Datos del nuevo estudiante
     * @param courseId ID del curso al que pertenece
     */
    suspend fun addStudentToCourse(student: Student, courseId: Long): Long =
        studentDao.insert(student.copy(courseId = courseId))

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

    // ── Usuarios ──────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en la base de datos.
     * Retorna el id generado por Room.
     */
    suspend fun registerUser(user: User): Long = userDao.insert(user)

    /**
     * Busca un usuario por su nombre de usuario.
     * Retorna null si no existe. Se usa durante el inicio de sesión.
     */
    suspend fun getUserByUsername(username: String): User? =
        userDao.getUserByUsername(username)

    /**
     * Busca un usuario por su correo electrónico.
     * Retorna null si no existe. Se usa en la recuperación de contraseña.
     */
    suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)

    /**
     * Obtiene un usuario por su id.
     * Retorna null si no existe.
     */
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)

    /**
     * Actualiza la contraseña de un usuario y el indicador de cambio obligatorio.
     *
     * @param userId ID del usuario a actualizar
     * @param password Nueva contraseña
     * @param mustChangePassword true si el usuario debe cambiar la contraseña al ingresar
     */
    suspend fun updatePassword(userId: Long, password: String, mustChangePassword: Boolean) =
        userDao.updatePassword(userId, password, mustChangePassword)

    // ── Instituciones ─────────────────────────────────────────────────────────

    /**
     * Obtiene las instituciones de un usuario como [Flow] reactivo.
     * Se actualiza automáticamente cuando se agregan o eliminan instituciones.
     */
    fun getInstitutionsByUser(userId: Long): Flow<List<Institution>> =
        institutionDao.getByUser(userId)

    /**
     * Inserta una nueva institución y retorna el id generado.
     */
    suspend fun addInstitution(institution: Institution): Long =
        institutionDao.insert(institution)

    /**
     * Obtiene una institución por su id en una sola llamada.
     * Retorna null si no existe.
     */
    suspend fun getInstitutionById(id: Long): Institution? =
        institutionDao.getById(id)

    // ── Cursos ────────────────────────────────────────────────────────────────

    /**
     * Obtiene los cursos de una institución como [Flow] reactivo.
     * Se actualiza automáticamente cuando se agregan o eliminan cursos.
     */
    fun getCoursesByInstitution(institutionId: Long): Flow<List<Course>> =
        courseDao.getByInstitution(institutionId)

    /**
     * Inserta un nuevo curso y retorna el id generado.
     */
    suspend fun addCourse(course: Course): Long = courseDao.insert(course)

    /**
     * Verifica si ya existe un curso con el mismo nombre y paralelo en la institución.
     * La comparación es insensible a mayúsculas/minúsculas.
     */
    suspend fun courseExists(institutionId: Long, name: String, parallel: String): Boolean =
        courseDao.existsCourse(institutionId, name, parallel)
}
