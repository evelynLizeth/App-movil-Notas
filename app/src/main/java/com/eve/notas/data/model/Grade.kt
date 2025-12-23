package com.eve.notas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade")
data class Grade( @PrimaryKey(autoGenerate = true) val id: Long = 0,
                  val name: String,
                  val pinned: Boolean = false
)
