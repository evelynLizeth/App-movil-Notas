package com.eve.notas.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val student by viewModel.getStudentById(studentId).collectAsState(initial = null)
    val tasks by tasksViewModel.tasks.collectAsState() // 👈 usamos Flow
    val promedio by viewModel.averageFlow.collectAsState(initial = 0.00)

    var notas by remember(tasks) {
        mutableStateOf(tasks.associate { it.id to 0.00 }.toMutableMap())
    }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Detalle de Notas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alumno: ${student?.name ?: "(sin nombre)"}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tarea", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleMedium)
            Text("Nota", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredTasks = if (searchQuery.isBlank()) tasks
        else tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredTasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(task.name, modifier = Modifier.weight(2f))

                    OutlinedTextField(
                        value = String.format("%.2f", notas[task.id] ?: 0.00),
                        onValueChange = { value ->
                            val nota = value.replace(",", ".").toDoubleOrNull() ?: 0.00
                            notas[task.id] = nota
                            val promedioFinal = notas.values.average()
                            viewModel.saveGrades(studentId, notas, promedioFinal)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Nota") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Promedio del alumno: ${String.format("%.2f", promedio)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}