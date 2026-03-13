package com.eve.notas.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla de lista de cursos de una institución.
 *
 * Muestra los cursos con su nombre y paralelo en filas alternadas.
 * Al tocar un curso navega a la pantalla principal (lista de estudiantes).
 * Incluye un botón para agregar nuevos cursos mediante un diálogo.
 *
 * @param viewModel ViewModel que gestiona la lista de cursos
 * @param institutionName Nombre de la institución para mostrar como título
 * @param onNavigateToMain Callback para navegar a la pantalla principal de estudiantes
 * @param modifier Modificador opcional
 */
@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    institutionName: String,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var newName by remember { mutableStateOf("") }
    var newParallel by remember { mutableStateOf("") }

    // Diálogo para agregar curso
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                newName = ""
                newParallel = ""
                viewModel.clearError()
                viewModel.closeAddDialog()
            },
            title = { Text("Nuevo Curso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            viewModel.clearError()
                        },
                        placeholder = { Text("Nombre del curso") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    TextField(
                        value = newParallel,
                        onValueChange = {
                            newParallel = it
                            viewModel.clearError()
                        },
                        placeholder = { Text("Paralelo (ej: A, B)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addCourse(newName, newParallel)
                    if (errorMessage == null) {
                        newName = ""
                        newParallel = ""
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    newName = ""
                    newParallel = ""
                    viewModel.clearError()
                    viewModel.closeAddDialog()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = institutionName,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay cursos registrados")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(
                    items = courses,
                    key = { _, course -> course.id }
                ) { index, course ->
                    val backgroundColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .clickable { onNavigateToMain() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${course.name} - ${course.parallel}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.openAddDialog() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar Curso")
        }
    }
}
