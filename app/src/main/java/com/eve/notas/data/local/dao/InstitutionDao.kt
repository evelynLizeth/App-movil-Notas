package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eve.notas.data.model.Institution
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de instituciones.
 * Proporciona operaciones CRUD sobre la entidad [Institution].
 */
@Dao
interface InstitutionDao {

    /**
     * Inserta una nueva institución y retorna el id generado por Room.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(institution: Institution): Long

    /**
     * Obtiene todas las instituciones pertenecientes a un usuario como [Flow] reactivo.
     * Se actualiza automáticamente cuando se agrega o elimina una institución.
     *
     * @param userId ID del usuario propietario de las instituciones
     */
    @Query("SELECT * FROM institutions WHERE userId = :userId ORDER BY name ASC")
    fun getByUser(userId: Long): Flow<List<Institution>>

    /**
     * Obtiene una institución por su id en una sola llamada suspendida (no reactiva).
     * Retorna null si no existe la institución con ese id.
     */
    @Query("SELECT * FROM institutions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Institution?

    @Query("SELECT * FROM institutions WHERE userId = :userId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByName(userId: Long, name: String): Institution?

    @Update
    suspend fun update(institution: Institution)

    @Delete
    suspend fun delete(institution: Institution)
}
