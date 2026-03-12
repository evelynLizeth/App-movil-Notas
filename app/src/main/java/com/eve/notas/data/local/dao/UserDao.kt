package com.eve.notas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eve.notas.data.model.User

/**
 * DAO (Data Access Object) para la tabla de usuarios.
 * Proporciona operaciones de autenticación, registro y actualización de contraseña.
 */
@Dao
interface UserDao {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * Retorna el id generado por Room.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    /**
     * Busca un usuario por su nombre de usuario.
     * Retorna null si no existe ningún usuario con ese username.
     * Se usa durante el proceso de autenticación y registro.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * Retorna null si no existe. Se usa en el flujo de recuperación de contraseña.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Obtiene un usuario por su id único.
     * Retorna null si no existe el usuario con ese id.
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    /**
     * Actualiza la contraseña de un usuario y el flag de cambio obligatorio.
     * Se usa tanto para el cambio de contraseña voluntario como para el forzado
     * después de recuperación.
     *
     * @param userId ID del usuario cuya contraseña se actualizará
     * @param password Nueva contraseña (puede ser temporal si mustChangePassword = true)
     * @param mustChangePassword true si el usuario deberá cambiarla al siguiente ingreso
     */
    @Query("UPDATE users SET password = :password, mustChangePassword = :mustChangePassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, password: String, mustChangePassword: Boolean)
}
