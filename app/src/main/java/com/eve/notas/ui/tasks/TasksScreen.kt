package com.eve.notas.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TasksScreen(
    viewModel: TasksViewModel, // 👈 si tienes un ViewModel para tareas
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Gestión de Tareas",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Aquí puedes poner tu formulario o lista de tareas
        Text("Tareas")
    }
}