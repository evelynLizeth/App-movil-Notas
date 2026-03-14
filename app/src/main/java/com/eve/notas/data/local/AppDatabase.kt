package com.eve.notas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Task
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.User
import com.eve.notas.data.model.Institution
import com.eve.notas.data.model.Course
import com.eve.notas.data.local.dao.StudentDao
import com.eve.notas.data.local.dao.TaskDao
import com.eve.notas.data.local.dao.GradeDao
import com.eve.notas.data.local.dao.UserDao
import com.eve.notas.data.local.dao.InstitutionDao
import com.eve.notas.data.local.dao.CourseDao

/**
 * Base de datos principal de la app usando Room.
 *
 * Contiene todas las entidades del dominio:
 * - [Student]: lista de estudiantes con su promedio
 * - [Task]: tareas globales de la app
 * - [Grade]: notas por combinación (estudiante, tarea)
 * - [User]: usuarios del sistema con autenticación
 * - [Institution]: instituciones educativas por usuario
 * - [Course]: cursos dentro de cada institución
 *
 * Las migraciones se definen en el companion object para mantener
 * el historial de cambios del esquema en un solo lugar.
 */
@Database(
    entities = [
        Student::class,
        Task::class,
        Grade::class,
        User::class,
        Institution::class,
        Course::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO para operaciones sobre la tabla de estudiantes */
    abstract fun studentDao(): StudentDao

    /** DAO para operaciones sobre la tabla de tareas */
    abstract fun taskDao(): TaskDao

    /** DAO para operaciones sobre la tabla de notas */
    abstract fun gradeDao(): GradeDao

    /** DAO para operaciones sobre la tabla de usuarios */
    abstract fun userDao(): UserDao

    /** DAO para operaciones sobre la tabla de instituciones */
    abstract fun institutionDao(): InstitutionDao

    /** DAO para operaciones sobre la tabla de cursos */
    abstract fun courseDao(): CourseDao

    companion object {

        /**
         * Migración de la versión 16 a la 17.
         *
         * Agrega la columna 'notaMaxima' a la tabla 'students' con valor por defecto 20.
         * ALTER TABLE es suficiente porque SQLite soporta agregar columnas directamente.
         * Los estudiantes existentes quedarán con escala 20 ("De 0 a 20").
         */
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE students ADD COLUMN notaMaxima INTEGER NOT NULL DEFAULT 20"
                )
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `task` ADD COLUMN `courseId` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Migración de la versión 15 a la 16.
         *
         * Se eliminan las columnas 'nota' y 'pinned' de la tabla 'task'
         * porque las notas se almacenan en la tabla 'grade' (relación estudiante-tarea)
         * y 'pinned' nunca fue utilizado.
         *
         * SQLite no soporta DROP COLUMN directamente, por lo que se
         * recrea la tabla con el nuevo esquema y se migran los datos.
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crear tabla temporal con el nuevo esquema (sin nota ni pinned)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_new` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL)"
                )
                // 2. Copiar los datos existentes (solo las columnas que se conservan)
                database.execSQL(
                    "INSERT INTO `task_new` (`id`, `name`, `createdAt`) " +
                    "SELECT `id`, `name`, `createdAt` FROM `task`"
                )
                // 3. Eliminar la tabla original
                database.execSQL("DROP TABLE `task`")
                // 4. Renombrar la nueva tabla con el nombre definitivo
                database.execSQL("ALTER TABLE `task_new` RENAME TO `task`")
            }
        }
    }
}
