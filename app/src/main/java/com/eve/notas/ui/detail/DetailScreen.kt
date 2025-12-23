package com.eve.notas.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eve.notas.data.model.Student
import com.eve.notas.ui.tasks.Task
import com.eve.notas.ui.tasks.TasksViewModel

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    studentId: Long,
    modifier: Modifier = Modifier
) {
    val student = viewModel.getStudentById(studentId)
    val tasks by tasksViewModel.tasks.observeAsState(emptyList())


    // Estado local para las notas de cada tarea
    var notas by remember { mutableStateOf(mutableMapOf<Long, Double>()) }

    // Calcular promedio dinámicamente
    val promedio = if (notas.isNotEmpty()) notas.values.average() else 0.0

    Column(modifier = modifier.padding(16.dp)) {
        Text("Alumno: Evelyn")

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tarea 1")
        }


        // 🔹 Encabezado de la tabla
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tarea", modifier = Modifier.weight(2f))
            Text("Nota", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Lista de tareas con columna de notas
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Nombre de la tarea (no editable)
                    Text(task.title, modifier = Modifier.weight(2f))

                    // Campo para insertar nota
                    OutlinedTextField(
                        value = notas[task.id]?.toString() ?: "",
                        onValueChange = { value ->
                            val nota = value.toDoubleOrNull()
                            if (nota != null) {
                                notas[task.id] = nota
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Nota") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Promedio final
        Text("Promedio del alumno: $promedio", style = MaterialTheme.typography.titleMedium)
    }
}