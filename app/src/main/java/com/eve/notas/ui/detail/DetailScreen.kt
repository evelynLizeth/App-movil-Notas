package com.eve.notas.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.eve.notas.ui.tasks.TasksViewModel

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    studentId: Long,
    modifier: Modifier = Modifier
) {
    // 🔹 Observamos estados desde los ViewModels
    val student by viewModel.getStudentById(studentId).collectAsState(initial = null)
    val tasks by tasksViewModel.tasks.collectAsState() // Flow → State
    val promedio by viewModel.averageFlow.collectAsState(initial = 0.00)

    // 🔹 Estado local de notas (persistente en recomposiciones)
    val notas = remember { mutableStateMapOf<Long, Double>() }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔹 Título
        Text(
            text = "Detalle de Notas",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Alumno
        Text(
            text = buildAnnotatedString {
                // 🔹 Parte en negrita
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Alumno: ")
                }
                // 🔹 Parte normal
                append(student?.name ?: "(sin nombre)")
            },
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 🔹 Promedio
        Text(
            text = buildAnnotatedString {
                // 🔹 Parte en negrita
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Promedio: ")
                }
                // 🔹 Parte normal (el número formateado)
                append(String.format("%.2f", promedio))
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            //textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Campo de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it},
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

        // 🔹 Encabezado de columnas
        Row(modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("", modifier = Modifier.weight(0.5f))
            Text(
                "Tareas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Text(
                "Notas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Filtrado de tareas
        val filteredTasks = if (searchQuery.isBlank()) tasks
        else tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }

        // 🔹 Lista de tareas con notas editables
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(filteredTasks) { index, task ->
                val backgroundColor = if (index % 2 == 0) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) // fondo suave
                } else {
                    Color.Transparent // sin fondo
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = 8.dp)
                ) {
                    Text(task.name, modifier = Modifier.weight(2f))

                    TextField(
                        value = notas[task.id]?.let { String.format("%.2f", it) } ?: "",
                        onValueChange = { value ->
                            // 🔹 Regex: hasta 2 enteros, opcional punto, hasta 2 decimales
                            val regex = Regex("^\\d{0,2}(\\.?\\d{0,2})?$")

                            if (regex.matches(value.replace(",", "."))) {
                                val nota = value.replace(",", ".").toDoubleOrNull()
                                if (nota != null) {
                                    notas[task.id] = nota
                                    val promedioFinal = notas.values.average()
                                    viewModel.saveGrades(studentId, notas, promedioFinal)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        label = { Text("Nota") }
                    )
                }
            }
        }
    }
}