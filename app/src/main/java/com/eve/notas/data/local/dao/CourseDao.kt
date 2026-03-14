package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eve.notas.data.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de cursos.
 * Proporciona operaciones CRUD sobre la entidad [Course].
 */
@Dao
interface CourseDao {

    /**
     * Inserta un nuevo curso y retorna el id generado por Room.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(course: Course): Long

    /**
     * Obtiene todos los cursos de una institución como [Flow] reactivo.
     * Se actualiza automáticamente cuando se agrega o elimina un curso.
     *
     * @param institutionId ID de la institución propietaria de los cursos
     */
    @Query("SELECT * FROM courses WHERE institutionId = :institutionId ORDER BY name ASC")
    fun getByInstitution(institutionId: Long): Flow<List<Course>>

    /**
     * Verifica si ya existe un curso con el mismo nombre y paralelo en una institución.
     * La comparación es insensible a mayúsculas/minúsculas.
     * Retorna true si existe al menos un curso con esa combinación.
     *
     * @param institutionId ID de la institución donde buscar el curso
     * @param name Nombre del curso a verificar
     * @param parallel Paralelo del curso a verificar
     */
    @Query(
        "SELECT EXISTS(" +
        "SELECT 1 FROM courses " +
        "WHERE institutionId = :institutionId " +
        "AND LOWER(name) = LOWER(:name) " +
        "AND LOWER(parallel) = LOWER(:parallel)" +
        ")"
    )
    suspend fun existsCourse(institutionId: Long, name: String, parallel: String): Boolean

    @Query(
        "SELECT EXISTS(" +
        "SELECT 1 FROM courses " +
        "WHERE institutionId = :institutionId " +
        "AND LOWER(name) = LOWER(:name) " +
        "AND LOWER(parallel) = LOWER(:parallel) " +
        "AND id != :excludeId" +
        ")"
    )
    suspend fun existsCourseExcluding(institutionId: Long, name: String, parallel: String, excludeId: Long): Boolean

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Query("SELECT * FROM courses WHERE institutionId = :institutionId")
    suspend fun getByInstitutionList(institutionId: Long): List<Course>
}
