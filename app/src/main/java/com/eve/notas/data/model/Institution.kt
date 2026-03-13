package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una institución educativa.
 *
 * Cada institución pertenece a un usuario específico identificado por [userId].
 * Una institución puede tener múltiples cursos asociados.
 */
@Entity(tableName = "institutions")
data class Institution(
    /** Identificador único generado automáticamente por Room */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** ID del usuario propietario de esta institución */
    val userId: Long,

    /** Nombre de la institución educativa */
    val name: String
)
