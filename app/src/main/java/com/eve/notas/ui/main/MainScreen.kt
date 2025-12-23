package com.eve.notas.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eve.notas.data.model.Student
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToTasks: () -> Unit, // 👈 callback para pantalla 3
    onNew: () -> Unit,       // 👈 nuevo alumno
    onEdit: () -> Unit,      // 👈 editar alumno
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students: List<Student> by viewModel.students.observeAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

            Text("Lista de Notas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

        // 🔹 Buscar estudiante con campo + botón lupita en una sola línea
        var searchQuery by remember { mutableStateOf("") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar estudiante") },
                modifier = Modifier.weight(1f),
                singleLine = true   // 👈 asegura que se pueda escribir en una sola línea
            )
            IconButton(
                onClick = { viewModel.searchByName(searchQuery) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Panel de acciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNew) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            Button(onClick = onNavigateToTasks) {
                Text("CREAR TAREAS")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val students: List<Student> by viewModel.students.observeAsState(initial = emptyList())

        if (students.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay datos para mostrar")
            }
        } else {
            LazyColumn {
                items(students) { student ->
                    // tu fila con Checkbox, campo editable, etc.
                }
            }
        }

        // 🔹 Encabezado de la tabla
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(0.5f))
            Text("Nombre", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Promedio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Acciones", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Lista de estudiantes con 3 columnas
        val editingId by viewModel.editingStudentId.observeAsState()

        LazyColumn {
            items(students) { student ->
                val isEditing = editingId == student.id
                var editedName by remember { mutableStateOf(student.name) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Checkbox(
                        checked = false, // aquí puedes usar tu lógica de selección
                        onCheckedChange = { viewModel.startEditing(student.id) }
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            viewModel.finishEditing(student, editedName)
                        }) {
                            Text("Guardar")
                        }
                    } else {
                        Text(student.name, modifier = Modifier.weight(1f))
                    }

                    Text(student.average.toString(), modifier = Modifier.weight(1f))
                    Button(onClick = { onNavigateToDetail(student.id) }) {
                        Text("Ver")
                    }
                }
            }
        }
    }
}