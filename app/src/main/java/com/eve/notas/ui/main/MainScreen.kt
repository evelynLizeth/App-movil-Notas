package com.eve.notas.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import com.eve.notas.ui.components.ConfirmDialog
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import com.eve.notas.ui.components.MessageSnackbar
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val selectedStudents by viewModel.selectedStudents.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showScaleChangeDialog by viewModel.showScaleChangeDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingStudent by viewModel.editingStudent.collectAsState()
    val notaMaxima by viewModel.notaMaxima.collectAsState()
    val context = LocalContext.current
    var newName by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val formatter = DecimalFormat("00.00")
    val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

    // ── Opciones de escala de calificación ────────────────────────────────────
    val escalas = listOf(10 to "10", 20 to "20")

    MessageSnackbar(
        message = uiMessage,
        onDismiss = { viewModel.clearMessage() }
    )

    Column(modifier = modifier.padding(16.dp)) {

        Text(
            text = "Lista de Notas",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

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
                IconButton(onClick = {
                    viewModel.exportStudentsToPdf(context, students)
                }) {
                    Icon(Icons.Filled.Print, contentDescription = "Imprimir")
                }
                IconButton(onClick = {
                    showSearch = !showSearch
                    if (!showSearch) viewModel.onSearchQueryChanged("")
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
                // 🔹 Empuja el botón hacia la derecha
                Spacer(modifier = Modifier.weight(1f))

                // 🔹 Botón a la derecha
                /*Button(onClick = onNavigateToTasks) {
                    Text("Crear tareas")
                }*/
                ExtendedFloatingActionButton(
                    onClick = onNavigateToTasks,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("Crear tareas")
                }
            }
        }

        // 🔹 Campo de búsqueda (visible solo al pulsar la lupa)
        if (showSearch) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Buscar estudiante") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 🔹 Diálogo de confirmación de borrado de estudiantes
        if (showDeleteDialog) {
            ConfirmDialog(
                title = "¿Eliminar estudiantes?",
                message = "¿Estás segura de que deseas eliminar los estudiantes seleccionados?",
                onConfirm = { viewModel.deleteSelected() },
                onDismiss = { viewModel.closeDeleteDialog() }
            )
        }

        // 🔹 Diálogo de advertencia al cambiar la escala de nota
        if (showScaleChangeDialog) {
            ConfirmDialog(
                title = "Cambiar escala de nota",
                message = "Si cambia la escala de nota se borrarán todas las notas ingresadas de los estudiantes. ¿Desea continuar?",
                onConfirm = { viewModel.confirmScaleChange() },
                onDismiss = { viewModel.cancelScaleChange() }
            )
        }

        // 🔹 Diálogo para crear nuevo estudiante
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAddDialog() },
                title = { Text("Nuevo estudiante") },
                text = {
                    TextField(
                        value = newName,
                        onValueChange = { newName = it
                                         viewModel.clearError()
                                        },
                        placeholder = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null,
                        supportingText = {
                            errorMessage?.let { Text(it) }
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addStudent(newName)
                        newName = ""
                    }) { Text("Crear") }
                },
                dismissButton = {
                    Button(onClick = {
                        newName = ""
                        viewModel.closeAddDialog()
                    }) { Text("Cancelar") }
                }
            )
        }
        // 🔹 Diálogo para editar estudiante
        editingStudent?.let { student ->
            var editedName by remember(student.id) { mutableStateOf(student.name) }

            AlertDialog(
                onDismissRequest = { viewModel.cancelEditing() },
                title = { Text("Editar estudiante") },
                text = {
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it
                            viewModel.clearError()
                            },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null, // 🔹 Marca error si existe mensaje
                        supportingText = {
                            errorMessage?.let { Text(it) } // 🔹 Muestra el mensaje debajo
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.finishEditing(student, editedName)
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    Button(onClick = { viewModel.cancelEditing() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Selector de escala de calificación global ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Nota / ")
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )
            escalas.forEach { (maximo, etiqueta) ->
                Checkbox(
                    checked = notaMaxima == maximo,
                    onCheckedChange = { viewModel.requestScaleChange(maximo) }
                )
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { viewModel.requestScaleChange(maximo) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Encabezado de la tabla estudiantes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("", modifier = Modifier.weight(0.5f))
            Text(
                "Nombres",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Text(
                "Promedio",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Lista de estudiantes
        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos para mostrar")
            }
        } else {
            // 🔹 Lista de estudiantes con filas alternadas
            LazyColumn {
                itemsIndexed(students, key = { index, student -> student.id }) { index, student ->
                    val backgroundColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) // fondo suave
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(vertical = 8.dp),
                             horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedStudents.contains(student),
                            onCheckedChange = { viewModel.toggleSelection(student) }
                        )

                        Text(
                            student.name.ifBlank { "Ingrese nombre" },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.startEditing(student) },
                                textAlign = TextAlign.Start
                        )

                        Text(
                            String.format("%05.2f", student.average),
                            modifier = Modifier.weight(1f)
                                .clickable { onNavigateToDetail(student.id) },
                            textAlign = TextAlign.Center,
                            // Umbral de color rojo: 70 % del máximo guardado por estudiante
                            // Escala 10 → rojo si promedio < 7
                            // Escala 20 → rojo si promedio < 14
                            color = if (student.average < notaMaxima * 0.7) Color.Red
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}