package com.eve.notas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import com.eve.notas.ui.main.MainScreen
import com.eve.notas.ui.main.MainViewModel
import com.eve.notas.ui.detail.DetailScreen
import com.eve.notas.ui.detail.DetailViewModel
import com.eve.notas.ui.tasks.TasksScreen
import com.eve.notas.ui.tasks.TasksViewModel
import androidx.navigation.navArgument
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.ui.detail.DetailViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    //detailViewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    repo: NotesRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToDetail = { studentId ->
                    navController.navigate("detail/$studentId")
                },
                onNavigateToTasks = { navController.navigate("tasks") },
                modifier = modifier
            )
        }

        composable(
            route = "detail/{studentId}",
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L

            // 🔹 Crear el DetailViewModel con el studentId correcto
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModelFactory(repo, studentId)
            )

            DetailScreen(
                viewModel = detailViewModel,
                tasksViewModel = tasksViewModel,
                studentId = studentId
               // modifier = modifier
            )
        }

        composable("tasks") {
            TasksScreen(viewModel = tasksViewModel, modifier = modifier)
        }
    }
}