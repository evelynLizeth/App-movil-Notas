package com.eve.notas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.navigation.compose.rememberNavController
import com.eve.notas.data.local.AppDatabase
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.navigation.NavGraph
import com.eve.notas.ui.main.MainViewModel
import com.eve.notas.ui.detail.DetailViewModel
import com.eve.notas.ui.theme.AppMovilNotasTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import com.eve.notas.ui.tasks.TasksViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notas_db"
        ).fallbackToDestructiveMigration()
            .build()

        // 2. Crear repositorio
        val repo = NotesRepository(db)

        // 3. Crear ViewModels
        val mainViewModel = MainViewModel(repo)
        val detailViewModel = DetailViewModel(repo)
        val tasksViewModel = TasksViewModel()

        // 4. Precargar datos de prueba (opcional, para no ver pantalla vacía)
        lifecycleScope.launch {

        }

        enableEdgeToEdge()
        setContent {
            AppMovilNotasTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        detailViewModel = detailViewModel,
                        tasksViewModel = tasksViewModel,
                        onNew = { mainViewModel.addStudent() },
                        onEdit = {  },   // 👈 edita los seleccionados
                        onDelete = { mainViewModel.deleteSelected() },
                        modifier = Modifier.padding(innerPadding)

                    )
                }
            }
        }
    }
}