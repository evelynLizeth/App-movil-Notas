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
import com.eve.notas.ui.login.LoginScreen
import com.eve.notas.ui.login.LoginViewModel
import com.eve.notas.ui.login.LoginViewModelFactory
import com.eve.notas.ui.register.RegisterScreen
import com.eve.notas.ui.register.RegisterViewModel
import com.eve.notas.ui.register.RegisterViewModelFactory
import com.eve.notas.ui.changepassword.ChangePasswordScreen
import com.eve.notas.ui.changepassword.ChangePasswordViewModel
import com.eve.notas.ui.changepassword.ChangePasswordViewModelFactory
import com.eve.notas.ui.institutions.InstitutionsScreen
import com.eve.notas.ui.institutions.InstitutionsViewModel
import com.eve.notas.ui.institutions.InstitutionsViewModelFactory
import com.eve.notas.ui.courses.CoursesScreen
import com.eve.notas.ui.courses.CoursesViewModel
import com.eve.notas.ui.courses.CoursesViewModelFactory

/**
 * Define el grafo de navegación de la app usando Navigation Compose.
 * Cada destino es un Composable registrado con su ruta correspondiente de [Routes].
 *
 * La navegación comienza en LOGIN y progresa a través de:
 * LOGIN → INSTITUTIONS (o CHANGE_PASSWORD si tiene contraseña temporal)
 * INSTITUTIONS → COURSES → MAIN → DETAIL
 *
 * @param navController Controlador de navegación que gestiona la pila de pantallas
 * @param mainViewModel ViewModel compartido de la pantalla principal
 * @param tasksViewModel ViewModel compartido de la pantalla de tareas
 * @param repo Repositorio inyectado para crear ViewModels con factory
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
        startDestination = Routes.LOGIN, // la app comienza en el login
        modifier = modifier
    ) {

        // ── Pantalla de inicio de sesión ──────────────────────────────────────
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(repo)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToInstitutions = { userId ->
                    navController.navigate(Routes.institutionsRoute(userId)) {
                        // Elimina el login del back stack para que el usuario
                        // no pueda volver al login con el botón atrás
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToChangePassword = { userId ->
                    navController.navigate(Routes.changePasswordRoute(userId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                modifier = modifier
            )
        }

        // ── Pantalla de registro ──────────────────────────────────────────────
        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModelFactory(repo)
            )
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                modifier = modifier
            )
        }

        // ── Pantalla de cambio de contraseña obligatorio ──────────────────────
        composable(
            route = Routes.CHANGE_PASSWORD,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val changePasswordViewModel: ChangePasswordViewModel = viewModel(
                factory = ChangePasswordViewModelFactory(repo, userId)
            )
            ChangePasswordScreen(
                viewModel = changePasswordViewModel,
                onSuccess = {
                    navController.navigate(Routes.institutionsRoute(userId)) {
                        popUpTo(Routes.CHANGE_PASSWORD) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = modifier
            )
        }

        // ── Pantalla de instituciones ─────────────────────────────────────────
        composable(
            route = Routes.INSTITUTIONS,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val institutionsViewModel: InstitutionsViewModel = viewModel(
                factory = InstitutionsViewModelFactory(repo, userId)
            )
            InstitutionsScreen(
                viewModel = institutionsViewModel,
                onNavigateToCourses = { institutionId ->
                    navController.navigate(Routes.coursesRoute(institutionId))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = modifier
            )
        }

        // ── Pantalla de cursos ────────────────────────────────────────────────
        composable(
            route = Routes.COURSES,
            arguments = listOf(navArgument("institutionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getLong("institutionId") ?: 0L
            val coursesViewModel: CoursesViewModel = viewModel(
                factory = CoursesViewModelFactory(repo, institutionId)
            )
            val institutionName by coursesViewModel.institutionName.collectAsState()

            CoursesScreen(
                viewModel = coursesViewModel,
                onNavigateToMain = { courseName ->
                    mainViewModel.setSelectedContext(institutionName, courseName)
                    navController.navigate(Routes.MAIN)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = modifier
            )
        }

        // ── Pantalla principal: lista de estudiantes ──────────────────────────
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToDetail = { studentId ->
                    // Navega al detalle usando la ruta generada con el ID del estudiante
                    navController.navigate(Routes.detailRoute(studentId))
                },
                onNavigateToTasks = { navController.navigate(Routes.TASKS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
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
            val institutionName by mainViewModel.institutionName.collectAsState()
            val courseName by mainViewModel.courseName.collectAsState()

            // Crea el DetailViewModel con su factory para pasarle el studentId
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModelFactory(repo, studentId)
            )

            DetailScreen(
                viewModel = detailViewModel,
                tasksViewModel = tasksViewModel,
                studentId = studentId,
                notaMaxima = notaMaxima,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                institutionName = institutionName,
                courseName = courseName
            )
        }

        // ── Pantalla de tareas: CRUD de tareas globales ───────────────────────
        composable(Routes.TASKS) {
            val institutionName by mainViewModel.institutionName.collectAsState()
            val courseName by mainViewModel.courseName.collectAsState()
            TasksScreen(
                viewModel = tasksViewModel,
                modifier = modifier,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                institutionName = institutionName,
                courseName = courseName
            )
        }
    }
}
