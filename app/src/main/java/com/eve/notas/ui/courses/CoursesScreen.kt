package com.eve.notas.ui.courses

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import com.eve.notas.data.model.Course
import com.eve.notas.ui.components.ConfirmDialog
import com.eve.notas.ui.components.MessageSnackbar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    onNavigateToMain: (courseId: Long, courseName: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val institutionName by viewModel.institutionName.collectAsState()
    val editingCourse by viewModel.editingCourse.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var newName by remember { mutableStateOf("") }
    var newParallel by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Course?>(null) }

    if (pendingDelete != null) {
        ConfirmDialog(
            title = "Eliminar curso",
            message = "¿Eliminar \"${pendingDelete!!.name} - ${pendingDelete!!.parallel}\"? Se borrarán también todos sus estudiantes y notas.",
            onConfirm = {
                viewModel.deleteCourse(pendingDelete!!)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    // Diálogo para editar curso
    editingCourse?.let { course ->
        var editedName by remember(course.id) { mutableStateOf(course.name) }
        var editedParallel by remember(course.id) { mutableStateOf(course.parallel) }
        AlertDialog(
            onDismissRequest = { viewModel.cancelEditing() },
            title = { Text("Editar curso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it; viewModel.clearError() },
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
                        value = editedParallel,
                        onValueChange = { editedParallel = it; viewModel.clearError() },
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
                        Text(text = msg, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.finishEditing(course, editedName, editedParallel) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelEditing() }) { Text("Cancelar") }
            }
        )
    }

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

    Scaffold(
        snackbarHost = {
            MessageSnackbar(message = uiMessage, onDismiss = { viewModel.clearMessage() })
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = institutionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                actions = {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp).height(32.dp)
                    ) {
                        Text("Cerrar sesión", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { innerPadding ->

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
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
                    val backgroundColor = if (index % 2 == 0)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                pendingDelete = course
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .combinedClickable(
                                    onClick = { onNavigateToMain(course.id, "${course.name} - ${course.parallel}") },
                                    onLongClick = { viewModel.startEditing(course) }
                                )
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.openAddDialog() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar Curso")
        }
    }

    } // end Scaffold
}
