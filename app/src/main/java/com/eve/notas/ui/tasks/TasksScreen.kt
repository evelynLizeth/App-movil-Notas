package com.eve.notas.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eve.notas.ui.components.ConfirmDialog

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val selectedTasks by viewModel.selectedTasks.collectAsState()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()

    var newTaskName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔹 Título
        Text(
            text = "Gestión de Tareas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Campo de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { query -> viewModel.onSearchQueryChanged(query) },
            placeholder = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Panel de acciones
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.openAddDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo")
                }
                IconButton(onClick = {
                    if (selectedTasks.isNotEmpty()) {
                        viewModel.deleteSelectedTasks()
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar seleccionados")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Lista o mensaje si no hay tareas
        if (tasks.isEmpty()) {
            Text(
                text = "No hay tareas creadas",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                items(tasks) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔹 Checkbox para seleccionar tarea
                        Checkbox(
                            checked = selectedTasks.contains(task),
                            onCheckedChange = { viewModel.toggleSelection(task) }
                        )

                        // 🔹 Nombre de la tarea
                        Text(
                            text = task.name,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.editTask(task) }
                        )

                        // 🔹 Botón eliminar individual
                        IconButton(onClick = { viewModel.deleteTask(task) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea")
                        }
                    }
                }
            }
        }
    }

    // 🔹 Diálogo de creación de tarea
    if (isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Nueva tarea") },
            text = {
                TextField(
                    value = newTaskName,
                    onValueChange = { newTaskName = it },
                    placeholder = { Text("Nombre de la tarea") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmAddTask(newTaskName)
                    newTaskName = ""
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }
    // 🔹 Diálogo de confirmación de eliminación múltiple
    if (isDeleteDialogOpen) {
        ConfirmDialog(
            title = "Eliminar tareas",
            message = "¿Seguro que deseas eliminar las tareas seleccionadas?",
            onConfirm = { viewModel.confirmDeleteSelectedTasks() },
            onDismiss = { viewModel.closeDeleteDialog() }
        )
    }
}