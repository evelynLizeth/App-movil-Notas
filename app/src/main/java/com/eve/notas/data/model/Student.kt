package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un estudiante en la base de datos.
 *
 * El campo [average] se calcula a partir de las notas almacenadas en [Grade]
 * y se persiste aquí para mostrarlo directamente en la lista principal
 * sin necesidad de recalcularlo en cada consulta.
 */
@Entity(tableName = "students")
data class Student(
    /** Identificador único generado automáticamente por Room */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** Nombre completo del estudiante */
    val name: String,

    /**
     * Promedio general del estudiante.
     * Se calcula en [DetailViewModel] al guardar notas y se persiste aquí
     * para mostrarlo directamente en la pantalla principal.
     */
    val average: Double = 0.0,

    /**
     * Escala de calificación seleccionada para este estudiante (10 o 20).
     * Se usa para determinar el umbral de color rojo en ambas pantallas:
     * - Escala 10 → rojo si promedio < 7  (70 % de 10)
     * - Escala 20 → rojo si promedio < 14 (70 % de 20)
     */
    val notaMaxima: Int = 20,

    /**
     * ID del curso al que pertenece este estudiante.
     * Valor 0 indica que el estudiante no está asignado a ningún curso específico.
     */
    val courseId: Long = 0
)
