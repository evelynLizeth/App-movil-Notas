package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una tarea global de la app.
 *
 * Las tareas son globales (no pertenecen a un estudiante específico).
 * Las notas de cada estudiante para cada tarea se almacenan por separado
 * en la entidad [Grade] usando la relación (studentId, taskId).
 */
@Entity(tableName = "task")
data class Task(
    /** Identificador único generado automáticamente por Room */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** Nombre descriptivo de la tarea */
    val name: String,

    /** Timestamp de creación en milisegundos. Se usa para ordenar por más reciente */
    val createdAt: Long = System.currentTimeMillis()
)
