package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade")
data class Grade( @PrimaryKey(autoGenerate = true) val id: Long = 0,

                  val pinned: Boolean = false,
                  val studentId: Long,
                  val taskId: Long,
                  val value: Double

)
