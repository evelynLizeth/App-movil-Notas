package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.eve.notas.data.model.Student
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de estudiantes.
 * Proporciona todas las operaciones CRUD sobre la entidad [Student].
 */
@Dao
interface StudentDao {

    /**
     * Obtiene todos los estudiantes ordenados alfabéticamente por nombre.
     * Retorna un [Flow] para observar cambios en tiempo real desde la UI.
     */
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAll(): Flow<List<Student>>

    /**
     * Obtiene un estudiante por su id como [Flow].
     * La pantalla de detalle lo usa para mostrar el nombre del alumno
     * y reaccionar si cambia (por ejemplo si se edita desde MainScreen).
     */
    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentByIdFlow(id: Long): Flow<Student?>

    /**
     * Busca estudiantes cuyo nombre contenga el texto dado (sin distinción de mayúsculas).
     * Se usa para la búsqueda reactiva en la pantalla principal.
     */
    @Query("SELECT * FROM students WHERE name LIKE '%' || :name || '%'")
    fun searchByName(name: String): Flow<List<Student>>

    /**
     * Inserta un nuevo estudiante y retorna el id generado por Room.
     * Se usa al crear un estudiante desde el diálogo en MainScreen.
     */
    @Insert
    suspend fun insert(student: Student): Long

    /**
     * Elimina un estudiante de la base de datos.
     * Room identifica el registro por el id de la entidad.
     */
    @Delete
    suspend fun delete(student: Student)

    /**
     * Elimina un estudiante directamente por su id.
     * Alternativa a [delete] cuando no se tiene el objeto completo.
     */
    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Actualiza los datos de un estudiante existente.
     * Room identifica el registro por el id de la entidad.
     */
    @Update
    suspend fun updateStudent(student: Student)

    /**
     * Actualiza únicamente el campo promedio de un estudiante.
     * Se llama desde [DetailViewModel] al presionar "Guardar" en la pantalla de detalle.
     */
    @Query("UPDATE students SET average = :average WHERE id = :studentId")
    suspend fun updateAverage(studentId: Long, average: Double)

    /**
     * Actualiza la escala de calificación para un estudiante específico (10 o 20).
     */
    @Query("UPDATE students SET notaMaxima = :notaMaxima WHERE id = :studentId")
    suspend fun updateNotaMaxima(studentId: Long, notaMaxima: Int)

    /**
     * Actualiza la escala de calificación para TODOS los estudiantes.
     * Se usa cuando el usuario cambia la escala global desde MainScreen.
     */
    @Query("UPDATE students SET notaMaxima = :notaMaxima")
    suspend fun updateAllNotaMaxima(notaMaxima: Int)

    /** Resetea el promedio de todos los estudiantes a 0. */
    @Query("UPDATE students SET average = 0.0")
    suspend fun resetAllAverages()
}
