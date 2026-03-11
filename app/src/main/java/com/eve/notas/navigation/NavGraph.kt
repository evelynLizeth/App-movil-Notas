package com.eve.notas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/**
 * Define el grafo de navegación de la app usando Navigation Compose.
 * Cada destino es un Composable registrado con su ruta correspondiente de [Routes].
 *
 * @param navController Controlador de navegación que gestiona la pila de pantallas
 * @param mainViewModel ViewModel compartido de la pantalla principal
 * @param tasksViewModel ViewModel compartido de la pantalla de tareas
 * @param repo Repositorio inyectado para crear el DetailViewModel con factory
 * @param modifier Modificador opcional para aplicar padding u otros estilos
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    tasksViewModel: TasksViewModel,
    repo: NotesRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN, // pantalla inicial de la app
        modifier = modifier
    ) {

        // ── Pantalla principal: lista de estudiantes ──────────────────────────
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToDetail = { studentId ->
                    // Navega al detalle usando la ruta generada con el ID del estudiante
                    navController.navigate(Routes.detailRoute(studentId))
                },
                onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                modifier = modifier
            )
        }

        // ── Pantalla de detalle: notas de un estudiante ───────────────────────
        composable(
            route = Routes.DETAIL,
            // Declara que el argumento studentId es de tipo Long
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            // Extrae el studentId de los argumentos de la ruta
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L

            // Escala global definida en MainScreen, reactiva
            val notaMaxima by mainViewModel.notaMaxima.collectAsState()

            // Crea el DetailViewModel con su factory para pasarle el studentId
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModelFactory(repo, studentId)
            )

            DetailScreen(
                viewModel = detailViewModel,
                tasksViewModel = tasksViewModel,
                studentId = studentId,
                notaMaxima = notaMaxima
            )
        }

        // ── Pantalla de tareas: CRUD de tareas globales ───────────────────────
        composable(Routes.TASKS) {
            TasksScreen(viewModel = tasksViewModel, modifier = modifier)
        }
    }
}
