package com.eve.notas.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eve.notas.data.model.Task
import com.eve.notas.ui.components.ConfirmDialog
import com.eve.notas.util.ValidationHelper
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel
) {
    // 🔹 Estados observados desde el ViewModel (todos son StateFlow → collectAsState)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val selectedTasks by viewModel.selectedTasks.collectAsState()

    // Estados para diálogos
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    var newTaskName by remember { mutableStateOf("") }
    // Estado para Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Gestión de Tareas",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Campo de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Buscar tarea") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),   // fondo suave con foco
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // fondo suave sin foco
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),  // fondo suave deshabilitado
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,    // línea azul/primaria al enfocar
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // línea gris cuando no está enfocado
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp))
                {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
                    }
                    IconButton(onClick = {
                            if (selectedTasks.isNotEmpty()) {
                                viewModel.openDeleteDialog()
                            }
                        }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar seleccionadas")
                    }
                }
            }
            // 🔹 Encabezado de la tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                 ) {
                Text("", modifier = Modifier.weight(0.5f))
                Text(
                    "Tareas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No hay tareas creadas",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn {
                    itemsIndexed(tasks) { index, task ->
                          val backgroundColor = if (index % 2 == 0) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) // fondo suave
                            } else {
                                Color.Transparent
                            }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().background(backgroundColor)
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.startEditingTask(task) // ✅ abre el diálogo
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Checkbox(
                                checked = selectedTasks.contains(task),
                                onCheckedChange = { viewModel.toggleSelection(task) }
                            )
                            Text(
                                text = task.name,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔹 Diálogo para crear tarea
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Nueva tarea") },
            text = {
                TextField(
                    value = newTaskName,
                    onValueChange = { newTaskName = it
                                     viewModel.clearError()
                                    },
                    placeholder = { Text("Nombre de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addTask(newTaskName)
                    newTaskName = ""   // ✅ limpiar el campo en la UI
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                Button(onClick = {
                    newTaskName = ""
                    viewModel.closeAddDialog()
                }) { Text("Cancelar") }
            }
        )
    }

    // 🔹 Diálogo para editar tarea
    editingTask?.let { task ->
        var editedName by remember(task.id) { mutableStateOf(task.name) }

        AlertDialog(
            onDismissRequest = { viewModel.cancelEditingTask() },
            title = { Text("Editar tarea") },
            text = {
                TextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.clearError() // ✅ limpia el error al escribir
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.finishEditingTask(task, editedName)
                }) { Text("Guardar") }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelEditingTask() }) {
                    Text("Cancelar")
                }
            }
        )
    }
    // 🔹 Diálogo de confirmación de borrado
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Eliminar tareas",
            message = "¿Seguro que deseas eliminar las tareas seleccionadas?",
            onConfirm = {
                viewModel.deleteSelected() // ✅ borra las seleccionadas
            },
            onDismiss = {
                viewModel.closeDeleteDialog() // ✅ cierra el diálogo
            }
        )
    }

}