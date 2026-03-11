package com.eve.notas.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eve.notas.ui.components.ConfirmDialog

/**
 * Pantalla de gestión de tareas globales.
 *
 * Permite:
 * - Buscar tareas por nombre
 * - Crear nuevas tareas mediante un diálogo
 * - Editar el nombre de una tarea existente (clic sobre la fila)
 * - Seleccionar múltiples tareas y eliminarlas en grupo
 *
 * @param viewModel ViewModel que gestiona el estado y la lógica de esta pantalla
 * @param modifier Modificador opcional para padding externo
 */
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel
) {
    // ── Observar estado desde el ViewModel ────────────────────────────────────
    val searchQuery by viewModel.searchQuery.collectAsState()
    // Lista filtrada y ordenada por fecha de creación (más reciente primero)
    val tasks by viewModel.filteredTasks.collectAsState()
    // Tareas marcadas para eliminación múltiple
    val selectedTasks by viewModel.selectedTasks.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    // ── Estado local de la UI ─────────────────────────────────────────────────
    // Texto del campo en el diálogo de crear tarea (estado local, no persiste en VM)
    var newTaskName by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ── Título de la pantalla ─────────────────────────────────────────
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

            // ── Campo de búsqueda ─────────────────────────────────────────────
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Buscar tarea") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Panel de acciones ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón para abrir el diálogo de crear tarea
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
                    }
                    // Botón de eliminar: solo activo si hay tareas seleccionadas
                    IconButton(onClick = {
                        if (selectedTasks.isNotEmpty()) {
                            viewModel.openDeleteDialog()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar seleccionadas")
                    }
                }
            }

            // ── Encabezado de la tabla ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("", modifier = Modifier.weight(0.5f))
                Text(
                    "Tareas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Lista de tareas ───────────────────────────────────────────────
            if (tasks.isEmpty()) {
                Text(
                    text = "No hay tareas creadas",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn {
                    itemsIndexed(tasks) { index, task ->
                        // Filas con color de fondo alternado para mejor legibilidad
                        val backgroundColor = if (index % 2 == 0)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else
                            Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .padding(vertical = 8.dp)
                                // Clic en la fila abre el diálogo de edición
                                .clickable { viewModel.startEditingTask(task) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Checkbox para selección múltiple
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

    // ── Diálogo para crear nueva tarea ────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Nueva tarea") },
            text = {
                TextField(
                    value = newTaskName,
                    onValueChange = {
                        newTaskName = it
                        viewModel.clearError() // limpia el error al escribir
                    },
                    placeholder = { Text("Nombre de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = { errorMessage?.let { Text(it) } }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addTask(newTaskName)
                    newTaskName = "" // limpia el campo en la UI tras crear
                }) { Text("Crear") }
            },
            dismissButton = {
                Button(onClick = {
                    newTaskName = ""
                    viewModel.closeAddDialog()
                }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo para editar tarea existente ───────────────────────────────────
    editingTask?.let { task ->
        // Estado local del nombre editado, reiniciado si cambia la tarea en edición
        var editedName by remember(task.id) { mutableStateOf(task.name) }

        AlertDialog(
            onDismissRequest = { viewModel.cancelEditingTask() },
            title = { Text("Editar tarea") },
            text = {
                TextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.clearError()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = { errorMessage?.let { Text(it) } }
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

    // ── Diálogo de confirmación de borrado ────────────────────────────────────
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Eliminar tareas",
            message = "¿Seguro que deseas eliminar las tareas seleccionadas?",
            onConfirm = { viewModel.deleteSelected() },
            onDismiss = { viewModel.closeDeleteDialog() }
        )
    }
}
