package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un usuario del sistema.
 *
 * Cada usuario puede tener una o más instituciones asociadas.
 * El campo [mustChangePassword] se activa cuando se genera una contraseña temporal
 * a través del flujo de recuperación, obligando al usuario a cambiarla al ingresar.
 */
@Entity(tableName = "users")
data class User(
    /** Identificador único generado automáticamente por Room */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** Nombre completo del usuario */
    val name: String,

    /** Nombre de usuario utilizado para ingresar al sistema */
    val username: String,

    /** Contraseña del usuario (puede ser temporal si mustChangePassword = true) */
    val password: String,

    /** Correo electrónico del usuario (usado para recuperación de contraseña) */
    val email: String,

    /** Indica si el usuario debe cambiar su contraseña al siguiente ingreso */
    val mustChangePassword: Boolean = false
)
