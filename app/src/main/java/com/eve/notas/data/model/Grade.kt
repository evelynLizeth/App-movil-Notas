package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la nota de un estudiante en una tarea específica.
 *
 * La clave primaria es compuesta (studentId + taskId), lo que garantiza
 * que exista una única nota por combinación estudiante-tarea.
 * Al insertar con [OnConflictStrategy.REPLACE] se actualiza si ya existía.
 */
@Entity(
    tableName = "grade",
    primaryKeys = ["studentId", "taskId"]
)
data class Grade(
    /** ID del estudiante al que pertenece esta nota */
    val studentId: Long,

    /** ID de la tarea a la que corresponde esta nota */
    val taskId: Long,

    /** Valor numérico de la nota */
    val value: Double,

    /** Campo reservado para posible uso futuro (actualmente no utilizado en la UI) */
    val pinned: Boolean = false
)
