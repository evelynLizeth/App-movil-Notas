package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un curso dentro de una institución educativa.
 *
 * Cada curso pertenece a una institución identificada por [institutionId].
 * La combinación de [name] y [parallel] debe ser única dentro de una institución.
 */
@Entity(tableName = "courses")
data class Course(
    /** Identificador único generado automáticamente por Room */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** ID de la institución a la que pertenece este curso */
    val institutionId: Long,

    /** Nombre del curso (ej: "Matemáticas", "Lengua") */
    val name: String,

    /** Paralelo del curso (ej: "A", "B", "1ro A") */
    val parallel: String
)
