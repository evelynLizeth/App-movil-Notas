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
){
    val student by viewModel.getStudentById(studentId).collectAsState(initial = null)

    Text("Alumno: ${student?.name ?: "(sin nombre)"}")

    val tasks by tasksViewModel.tasks.observeAsState(emptyList())

    // Estado local para las notas de cada tarea (inicializadas en 0)
    var notas by remember {
        mutableStateOf(tasks.associate { it.id to 0.0 }.toMutableMap())
    }

    // 🔹 Calcular promedio dinámicamente
    val promedio = if (notas.isNotEmpty()) notas.values.average() else 0.0

    Column(modifier = modifier.padding(16.dp)) {
        Text("Alumno: ${student?.name ?: "(sin nombre)"}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Encabezado de la tabla
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tarea", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleMedium)
            Text("Nota", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
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
                    // Columna 1: Nombre de la tarea
                    Text(task.title, modifier = Modifier.weight(2f))

                    // Columna 2: Campo editable para la nota
                    OutlinedTextField(
                        value = notas[task.id]?.toString() ?: "0",
                        onValueChange = { value ->
                            val nota = value.toDoubleOrNull() ?: 0.0
                            notas[task.id] = nota
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Nota") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Promedio final calculado
        Text(
            "Promedio del alumno: $promedio",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botón Guardar
        Button(onClick = {
            // Normalizamos todas las notas: si alguna quedó vacía, la ponemos en 0
            tasks.forEach { task ->
                if (notas[task.id] == null) {
                    notas[task.id] = 0.0
                }
            }
            val promedioFinal = if (notas.isNotEmpty()) notas.values.average() else 0.0
            viewModel.saveGrades(studentId, notas, promedioFinal)
        }) {
            Text("Guardar notas")
        }
    }
}