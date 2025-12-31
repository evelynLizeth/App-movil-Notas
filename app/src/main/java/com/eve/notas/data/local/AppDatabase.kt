package com.eve.notas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Task
import com.eve.notas.data.model.Grade
import com.eve.notas.data.local.dao.StudentDao
import com.eve.notas.data.local.dao.TaskDao
import com.eve.notas.data.local.dao.GradeDao

@Database(
    entities = [Student::class, Task::class, Grade::class],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun taskDao(): TaskDao
    abstract fun gradeDao(): GradeDao
}