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
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.navigation.compose.rememberNavController
import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.navigation.NavGraph
import com.eve.notas.ui.main.MainViewModel
import com.eve.notas.ui.detail.DetailViewModel
import com.eve.notas.ui.tasks.TasksViewModel
import com.eve.notas.ui.tasks.TasksViewModelFactory
import com.eve.notas.ui.theme.AppMovilNotasTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // ✅ propiedades de clase
    private lateinit var mainViewModel: MainViewModel
    private lateinit var tasksViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 1. Inicializar Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notas_db"
        ).fallbackToDestructiveMigration()
            .build()

        // 2. Crear repositorio con DAOs
        val repo = NotesRepository(
            db,
            db.studentDao(),
            db.gradeDao(),
            db.taskDao()
        )

        // 3. Crear ViewModels y asignarlos a las propiedades
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

        val tasksFactory = TasksViewModelFactory(repo)
        tasksViewModel = ViewModelProvider(this, tasksFactory)[TasksViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            AppMovilNotasTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                       // detailViewModel = detailViewModel,
                        tasksViewModel = tasksViewModel,
                        repo = repo,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}