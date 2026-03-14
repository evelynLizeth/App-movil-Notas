package com.eve.notas.data.local.dao

import androidx.room.*
import com.eve.notas.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de tareas.
 * Proporciona todas las operaciones CRUD sobre la entidad [Task].
 */
@Dao
interface TaskDao {

    /**
     * Obtiene todas las tareas ordenadas alfabéticamente por nombre.
     * Retorna un [Flow] para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM task ORDER BY name ASC")
    fun getAll(): Flow<List<Task>>

    /**
     * Inserta una tarea. Si ya existe (mismo id) la reemplaza.
     * Se usa para crear nuevas tareas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    /**
     * Actualiza los datos de una tarea existente.
     * Room identifica el registro por el id de la entidad.
     */
    @Update
    suspend fun update(task: Task)

    /**
     * Elimina una tarea de la base de datos.
     * Room identifica el registro por el id de la entidad.
     */
    @Delete
    suspend fun delete(task: Task)

    /**
     * Obtiene todas las tareas como [Flow] para observar cambios desde la UI.
     * Alias de [getAll] sin ordenamiento específico.
     */
    @Query("SELECT * FROM task")
    fun getTasks(): Flow<List<Task>>

    /**
     * Obtiene la lista completa de tareas en una sola llamada suspendida (no reactiva).
     * Se usa cuando se necesita el dato una sola vez, como al calcular el promedio.
     */
    @Query("SELECT * FROM task")
    suspend fun getTasksList(): List<Task>

    @Query("SELECT * FROM task WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getByCourse(courseId: Long): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE courseId = :courseId")
    suspend fun getTasksListByCourse(courseId: Long): List<Task>
}
