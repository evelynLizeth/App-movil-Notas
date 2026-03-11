package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eve.notas.data.model.Grade
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de notas.
 * Gestiona las operaciones sobre la entidad [Grade], que relaciona
 * estudiantes con tareas a través de una clave primaria compuesta.
 */
@Dao
interface GradeDao {

    /**
     * Obtiene todas las notas de la base de datos como [Flow].
     * Útil para observar el estado global de las notas.
     */
    @Query("SELECT * FROM grade")
    fun getAll(): Flow<List<Grade>>

    /**
     * Inserta o actualiza una nota.
     * Si ya existe una nota para la misma combinación (studentId, taskId) la reemplaza.
     * Se usa al guardar notas desde la pantalla de detalle.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(grade: Grade)

    /**
     * Obtiene todas las notas de un estudiante como [Flow] reactivo.
     * Usado en la pantalla de detalle para mostrar las notas en tiempo real.
     */
    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>>

    /**
     * Actualiza el valor de una nota existente identificada por (studentId, taskId).
     * Actualización parcial sin necesidad de pasar el objeto completo.
     */
    @Query("UPDATE grade SET value = :nota WHERE taskId = :taskId AND studentId = :studentId")
    suspend fun updateGrade(studentId: Long, taskId: Long, nota: Double)

    /**
     * Alias de [getGradesByStudent]. Retorna las notas de un estudiante como [Flow].
     * Usado en [NotesRepository.getGradesFlow] para el cálculo del promedio.
     */
    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    fun getGradesFlow(studentId: Long): Flow<List<Grade>>

    /**
     * Obtiene las notas de un estudiante en una sola llamada suspendida (no reactiva).
     * Se usa al recalcular el promedio al guardar notas desde [DetailViewModel].
     */
    @Query("SELECT * FROM grade WHERE studentId = :studentId")
    suspend fun getGradesByStudentList(studentId: Long): List<Grade>

    /**
     * Calcula el promedio de un estudiante usando la función AVG de SQL.
     * Retorna null si el estudiante no tiene notas registradas.
     * El repositorio convierte el null a 0.0.
     */
    @Query("SELECT AVG(value) FROM grade WHERE studentId = :studentId")
    fun getPromedioByStudent(studentId: Long): Flow<Double?>

    /** Elimina todas las notas de todos los estudiantes. */
    @Query("DELETE FROM grade")
    suspend fun deleteAllGrades()

    /** Retorna true si existe al menos una nota registrada. */
    @Query("SELECT EXISTS(SELECT 1 FROM grade LIMIT 1)")
    suspend fun hasAnyGrades(): Boolean
}
