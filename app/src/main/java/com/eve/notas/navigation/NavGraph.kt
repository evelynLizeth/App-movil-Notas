package com.eve.notas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eve.notas.ui.main.MainScreen
import com.eve.notas.ui.main.MainViewModel
import com.eve.notas.ui.detail.DetailScreen
import com.eve.notas.ui.detail.DetailViewModel
import com.eve.notas.ui.tasks.TasksScreen
import com.eve.notas.ui.tasks.TasksViewModel
import androidx.compose.foundation.layout.padding

@Composable
fun NavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    detailViewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    onNew: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier

) {
    NavHost(
        navController = navController,
        startDestination = "main",   // 👈 Pantalla inicial será la lista de alumnos
        modifier = modifier
    ) {
        // Pantalla principal (lista de alumnos)
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                onNavigateToTasks = { navController.navigate("tasks") },
                onNew = onNew,
                onEdit = onEdit,
                onDelete = onDelete,
                modifier = modifier
            )
        }

        // Pantalla de detalle de alumno
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            DetailScreen(
                viewModel = detailViewModel,
                tasksViewModel = tasksViewModel,   // 👈 faltaba este parámetro
                studentId = id,
                modifier = modifier
            )
        }

        // Pantalla de tareas (pantalla 3)
        composable("tasks") {
            TasksScreen(
                viewModel = tasksViewModel,
                modifier = modifier
            )
        }
    }
}