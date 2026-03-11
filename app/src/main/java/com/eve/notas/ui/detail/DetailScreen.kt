package com.eve.notas.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.eve.notas.ui.components.MessageSnackbar

/**
 * Pantalla de detalle de notas de un estudiante.
 *
 * Comportamiento:
 * - Al guardar, las tareas sin nota reciben 0 automáticamente
 * - La lista no se superpone con el botón Guardar
 * - Si una nota está fuera del rango, los demás campos se bloquean hasta corregirla
 * - Los campos de nota abren teclado numérico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    tasksViewModel: TasksViewModel,
    studentId: Long,
    notaMaxima: Int,
    modifier: Modifier = Modifier
) {
    // ── Observar estado desde los ViewModels ──────────────────────────────────
    val student    by viewModel.getStudentById(studentId).collectAsState(initial = null)
    val tasks      by tasksViewModel.filteredTasks.collectAsState(initial = emptyList())
    // Todas las tareas (sin filtro de búsqueda) para auto-asignar 0 al guardar
    val allTasks   by tasksViewModel.tasks.collectAsState()
    val grades     by viewModel.grades.collectAsState()
    val promedio   by viewModel.promedio.collectAsState()
    val message    by viewModel.message.collectAsState()

    // ── Estado local de la UI ─────────────────────────────────────────────────
    // Buffer temporal: acumula las notas editadas antes de persistir al guardar
    val notasEditadas = remember { mutableStateMapOf<Long, String>() }
    var searchQuery by remember { mutableStateOf("") }

    // Umbral de color rojo: 70 % del máximo (7 para escala 10, 14 para escala 20)
    val umbralRojo = notaMaxima * 0.7
    val colorPromedio = if (promedio < umbralRojo) Color.Red
                        else MaterialTheme.colorScheme.onSurface

    // True si alguna nota del buffer está fuera del rango 0..máximo.
    // Cuando es true: botón Guardar bloqueado + otros campos deshabilitados.
    val hayNotasInvalidas = notasEditadas.values.any { texto ->
        val valor = texto.replace(",", ".").toDoubleOrNull() ?: 0.0
        valor < 0 || valor > notaMaxima
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Bloquea el guardado si alguna nota está fuera del rango
                    if (hayNotasInvalidas) return@ExtendedFloatingActionButton

                    // Persiste las notas editadas manualmente en el buffer
                    notasEditadas.forEach { (taskId, texto) ->
                        val normalized = texto.replace(",", ".")
                        val nota = normalized.toDoubleOrNull() ?: 0.0
                        viewModel.updateNota(taskId, nota)
                    }

                    // Auto-asigna 0 a las tareas que no tienen nota en Room ni en el buffer
                    allTasks.forEach { task ->
                        val tieneEnBuffer = notasEditadas.containsKey(task.id)
                        val tieneEnRoom   = grades.any { it.taskId == task.id }
                        if (!tieneEnBuffer && !tieneEnRoom) {
                            viewModel.updateNota(task.id, 0.0)
                        }
                    }

                    viewModel.calcularPromedio()
                    notasEditadas.clear()
                },
                // Color desactivado si hay notas fuera de rango
                containerColor = if (hayNotasInvalidas)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = if (hayNotasInvalidas)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    Color.White,
                icon = { Icon(Icons.Default.Save, contentDescription = "Guardar") },
                text = { Text("Guardar") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
            MessageSnackbar(message = message, onDismiss = { viewModel.clearMessage() })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ── Título ────────────────────────────────────────────────────────
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

            // ── Alumno ────────────────────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Alumno: ")
                    }
                    append(student?.name ?: "(sin nombre)")
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Promedio ──────────────────────────────────────────────────────
            // El valor se muestra en rojo cuando está por debajo del 70 % del máximo:
            // escala 10 → rojo si promedio < 7 | escala 20 → rojo si promedio < 14
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Promedio: ")
                    }
                    withStyle(SpanStyle(color = colorPromedio)) {
                        append(String.format("%.2f", promedio))
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Búsqueda de tareas ────────────────────────────────────────────
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar tarea") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Encabezado de la tabla ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    "Nota (0 a $notaMaxima)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Filtrar tareas por búsqueda ───────────────────────────────────
            val filteredTasks = if (searchQuery.isBlank()) tasks
            else tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }

            // ── Lista de tareas ───────────────────────────────────────────────
            // contentPadding bottom: deja espacio libre para el botón Guardar
            // sin que ninguna fila quede oculta detrás de él
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(filteredTasks) { index, task ->
                    val notaGuardada = grades.find { it.taskId == task.id }?.value
                        ?.let { String.format("%.2f", it) } ?: "0.00"
                    var notaTexto by remember(task.id) { mutableStateOf(notaGuardada) }

                    // Sincroniza el campo si la nota cambia en Room desde otro origen
                    LaunchedEffect(notaGuardada) { notaTexto = notaGuardada }

                    // Valida que el valor esté dentro del rango 0..máximo
                    val valorIngresado = notaTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val fueraDeRango   = notaTexto.isNotBlank() &&
                            (valorIngresado < 0 || valorIngresado > notaMaxima)

                    // Este campo está habilitado si:
                    // - No hay errores en ningún campo (hayNotasInvalidas = false), O
                    // - Este campo es el que tiene el error (fueraDeRango = true)
                    // Esto obliga al usuario a corregir el error antes de editar otro campo.
                    val campoHabilitado = !hayNotasInvalidas || fueraDeRango

                    val backgroundColor = if (index % 2 == 0)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    else
                        Color.Transparent

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
                                notasEditadas[task.id] = value
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = campoHabilitado,
                            // Teclado numérico con decimales al enfocar el campo
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = fueraDeRango,
                            // Mensaje en rojo si el valor está fuera del rango seleccionado
                            supportingText = if (fueraDeRango) {
                                {
                                    Text(
                                        "Ingrese una nota entre 0 y $notaMaxima",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else null,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor  = Color.Transparent,
                                focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}
