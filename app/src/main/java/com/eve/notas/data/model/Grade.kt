package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "grade",
    primaryKeys = ["studentId", "taskId"]
)
data class Grade(
    val studentId: Long,
    val taskId: Long,
    val value: Double,
    val pinned: Boolean = false
)