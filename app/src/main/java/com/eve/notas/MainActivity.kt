package com.eve.notas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.navigation.NavGraph
import com.eve.notas.ui.main.MainViewModel
import com.eve.notas.ui.tasks.TasksViewModel
import com.eve.notas.ui.tasks.TasksViewModelFactory
import com.eve.notas.ui.theme.AppMovilNotasTheme

/**
 * Punto de entrada de la app.
 *
 * Responsabilidades:
 * - Inicializar la base de datos Room con fallbackToDestructiveMigration
 * - Crear el repositorio único que comparten todos los ViewModels
 * - Instanciar los ViewModels compartidos (MainViewModel, TasksViewModel)
 * - Configurar el árbol de Composables con el tema y la navegación
 */
class MainActivity : ComponentActivity() {

    /** ViewModel de la pantalla principal. Se inicializa en onCreate */
    private lateinit var mainViewModel: MainViewModel

    /** ViewModel de la pantalla de tareas. Se comparte con DetailScreen */
    private lateinit var tasksViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── 1. Inicializar la base de datos Room ──────────────────────────────
        // fallbackToDestructiveMigration garantiza que si no existe una migración
        // definida para el salto de versión, Room destruye y recrea la base de datos.
        // Las migraciones explícitas siguen definidas en AppDatabase.companion para
        // preservar los datos cuando estén disponibles.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notas_db"
        )
            .addMigrations(AppDatabase.MIGRATION_18_19)
            .fallbackToDestructiveMigration()
            .build()

        // ── 2. Crear el repositorio con los DAOs ──────────────────────────────
        // El repositorio es la única fuente de verdad para todos los ViewModels
        val repo = NotesRepository(
            db,
            db.studentDao(),
            db.gradeDao(),
            db.taskDao(),
            db.userDao(),
            db.institutionDao(),
            db.courseDao()
        )

        // ── 3. Instanciar los ViewModels ──────────────────────────────────────

        // MainViewModel requiere Application para acceder al contexto (PDF)
        mainViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(application, repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[MainViewModel::class.java]

        // TasksViewModel se comparte entre TasksScreen y DetailScreen
        val tasksFactory = TasksViewModelFactory(repo)
        tasksViewModel = ViewModelProvider(this, tasksFactory)[TasksViewModel::class.java]

        // ── 4. Configurar la UI ───────────────────────────────────────────────
        enableEdgeToEdge()
        setContent {
            AppMovilNotasTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pasa el padding del Scaffold para respetar barras del sistema
                    NavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        tasksViewModel = tasksViewModel,
                        repo = repo,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
