package com.eve.notas.ui.institutions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eve.notas.data.model.Institution
import com.eve.notas.ui.components.ConfirmDialog
import com.eve.notas.ui.components.MessageSnackbar

/**
 * Pantalla de lista de instituciones del usuario.
 *
 * Muestra las instituciones registradas con filas alternadas.
 * Permite navegar a los cursos de una institución al tocarla.
 * Incluye un botón para agregar nuevas instituciones mediante un diálogo.
 *
 * @param viewModel ViewModel que gestiona la lista de instituciones
 * @param onNavigateToCourses Callback para navegar a cursos con el institutionId
 * @param modifier Modificador opcional
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InstitutionsScreen(
    viewModel: InstitutionsViewModel,
    onNavigateToCourses: (Long) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val institutions by viewModel.institutions.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingInstitution by viewModel.editingInstitution.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var newName by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Institution?>(null) }

    if (pendingDelete != null) {
        ConfirmDialog(
            title = "Eliminar institución",
            message = "¿Eliminar \"${pendingDelete!!.name}\"? Se borrarán también todos sus cursos, estudiantes y notas.",
            onConfirm = {
                viewModel.deleteInstitution(pendingDelete!!)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    // Diálogo para editar institución
    editingInstitution?.let { institution ->
        var editedName by remember(institution.id) { mutableStateOf(institution.name) }
        AlertDialog(
            onDismissRequest = { viewModel.cancelEditing() },
            title = { Text("Editar institución") },
            text = {
                TextField(
                    value = editedName,
                    onValueChange = { editedName = it; viewModel.clearError() },
                    placeholder = { Text("Nombre de la institución") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = { errorMessage?.let { Text(it) } },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.finishEditing(institution, editedName) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelEditing() }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para agregar institución
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                newName = ""
                viewModel.clearError()
                viewModel.closeAddDialog()
            },
            title = { Text("Nueva Institución") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            viewModel.clearError()
                        },
                        placeholder = { Text("Nombre de la institución") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null,
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
                    viewModel.addInstitution(newName)
                    if (errorMessage == null) newName = ""
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    newName = ""
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
                title = {},
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
            text = "Mis Instituciones",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (institutions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay instituciones registradas")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(
                    items = institutions,
                    key = { _, institution -> institution.id }
                ) { index, institution ->
                    val backgroundColor = if (index % 2 == 0)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                pendingDelete = institution
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
                                    onClick = { onNavigateToCourses(institution.id) },
                                    onLongClick = { viewModel.startEditing(institution) }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = institution.name,
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
            Text("Agregar Institución")
        }
    }

    } // end Scaffold
}
