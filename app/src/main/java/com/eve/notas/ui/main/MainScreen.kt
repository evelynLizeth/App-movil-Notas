package com.eve.notas.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.eve.notas.data.model.Student
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.eve.notas.ui.components.ConfirmDialog

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (Long) -> Unit,   // 👈 callback para navegar al detalle
    onNavigateToTasks: () -> Unit,        // 👈 callback para navegar a tareas
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val students by viewModel.students.collectAsState(initial = emptyList())
    val editingId by viewModel.editingStudentId.collectAsState()
    val selectedStudents by viewModel.selectedStudents.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()
    var newName by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

        Text(
            text = "Lista de Notas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Campo de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Buscar estudiante") },
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
                    if (selectedStudents.isNotEmpty()) {
                        viewModel.openDeleteDialog()
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
                IconButton(onClick = { viewModel.exportStudentsToPdf() }) {
                    Icon(Icons.Filled.Print, contentDescription = "Imprimir")
                }
                Button(onClick = onNavigateToTasks) {
                    Text("CREAR TAREAS")
                }
            }
        }

        // 🔹 Diálogo de confirmación de borrado
        if (showDeleteDialog) {
            ConfirmDialog(
                title = "¿Eliminar estudiantes?",
                message = "¿Estás segura de que deseas eliminar los estudiantes seleccionados?",
                onConfirm = { viewModel.deleteSelected() },
                onDismiss = { viewModel.closeDeleteDialog() }
            )
        }

        // 🔹 Diálogo para crear nuevo estudiante
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAddDialog() },
                title = { Text("Nuevo estudiante") },
                text = {
                    TextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addStudent(newName)
                        }
                        newName = ""
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    Button(onClick = {
                        newName = ""
                        viewModel.closeAddDialog()
                    }) { Text("Cancelar") }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Encabezado de la tabla estudiantes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(0.5f))
            Text("Nombres", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Promedio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Lista de estudiantes
        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos para mostrar")
            }
        } else {
            LazyColumn {
                items(students) { student ->
                    val isEditing = editingId == student.id
                    var editedName by remember { mutableStateOf(student.name) }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Checkbox(
                            checked = selectedStudents.contains(student),
                            onCheckedChange = { viewModel.toggleSelection(student) }
                        )

                        if (isEditing) {
                            TextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { viewModel.finishEditing(student, editedName) }) {
                                Text("Guardar")
                            }
                        } else {
                            Text(
                                student.name.ifBlank { "Ingrese nombre" },
                                modifier = Modifier.weight(1f).clickable { viewModel.startEditing(student.id) }
                            )
                        }

                        // 🔹 Promedio como botón que abre DetailScreen
                        Text(
                            student.average.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToDetail(student.id) }, // 👈 aquí navegas
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}