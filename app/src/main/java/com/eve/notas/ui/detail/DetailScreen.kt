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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.Alignment
import com.eve.notas.data.model.Grade
import com.eve.notas.ui.components.MessageSnackbar

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    studentId: Long,
    modifier: Modifier = Modifier
) {
    // 🔹 Observamos estados desde los ViewModels
    val student by viewModel.getStudentById(studentId).collectAsState(initial = null)
    val tasks by tasksViewModel.filteredTasks.collectAsState(initial = emptyList())
    val grades by viewModel.grades.collectAsState()
    //val promedio by viewModel.promedio.collectAsState(initial = 0.0)
    val promedio by viewModel.promedio.collectAsState()
    Text(text = "Promedio: $promedio")

    // 🔹 Estado local de notas editadas
    val notasEditadas = remember { mutableStateMapOf<Long, String>() }
    var searchQuery by remember { mutableStateOf("") }
    val message by viewModel.message.collectAsState()

    Scaffold(
       /* bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        notasEditadas.forEach { (taskId, texto) ->
                            val normalized = texto.replace(",", ".")
                            val nota = normalized.toDoubleOrNull() ?: 0.0
                            viewModel.updateNota(taskId, nota)
                        }
                        viewModel.calcularPromedio()
                        notasEditadas.clear()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(48.dp),// defines el tamaño
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // 🔹 Snackbar
                MessageSnackbar(
                    message = message,
                    onDismiss = { viewModel.clearMessage() }
                )

            }
        }*/
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    notasEditadas.forEach { (taskId, texto) ->
                        val normalized = texto.replace(",", ".")
                        val nota = normalized.toDoubleOrNull() ?: 0.0
                        viewModel.updateNota(taskId, nota)
                    }
                    viewModel.calcularPromedio()
                    notasEditadas.clear()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar"
                    )
                },
                text = { Text("Guardar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
            MessageSnackbar(
                message = message,
                onDismiss = { viewModel.clearMessage() }
            )
        }



    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    // Parte en color primario
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append("Alumno: ")
                    }
                    // Parte normal (negro)
                    append(student?.name ?: "(sin nombre)")
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

// 🔹 Promedio
            Text(
                text = buildAnnotatedString {
                    // Parte en color primario
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append("Promedio: ")
                    }
                    // Parte normal (negro)
                    append(String.format("%.2f", promedio))
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Filtrado de tareas
        val filteredTasks = if (searchQuery.isBlank()) tasks
        else tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }

        // 🔹 Lista de tareas con notas editables
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filteredTasks) { index, task ->
                    // 🔹 Busca la nota guardada en Room
                    val notaGuardada = grades.find { it.taskId == task.id }?.value?.toString() ?: ""

                    // 🔹 Estado editable
                    var notaTexto by remember(task.id) { mutableStateOf(notaGuardada) }

                    // 🔹 Cada vez que cambie la nota en Room, actualiza el TextField
                    LaunchedEffect(notaGuardada) {
                        notaTexto = notaGuardada
                    }

                    val backgroundColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task.name,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Start
                        )
                        TextField(
                            value = notaTexto,
                            onValueChange = { value ->
                                notaTexto = value
                                notasEditadas[task.id] = value //buffer local
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent  //MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}