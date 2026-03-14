package com.eve.notas.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

/**
 * Pantalla de inicio de sesión.
 *
 * Presenta los campos de usuario y contraseña, un botón para ingresar,
 * accesos a registro y recuperación de contraseña.
 * Maneja la navegación reactiva según el resultado del [LoginViewModel].
 *
 * @param viewModel ViewModel que gestiona la autenticación
 * @param onNavigateToRegister Callback para navegar a la pantalla de registro
 * @param onNavigateToInstitutions Callback para navegar a instituciones con el userId
 * @param onNavigateToChangePassword Callback para navegar al cambio de contraseña con el userId
 * @param modifier Modificador opcional
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToInstitutions: (Long) -> Unit,
    onNavigateToChangePassword: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val loginResult by viewModel.loginResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val recoveryMessage by viewModel.recoveryMessage.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryInput by remember { mutableStateOf("") }

    // Reacción al resultado del login
    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginResult.Success -> {
                viewModel.clearResult()
                onNavigateToInstitutions(result.userId)
            }
            is LoginResult.MustChangePassword -> {
                viewModel.clearResult()
                onNavigateToChangePassword(result.userId)
            }
            null -> Unit
        }
    }

    // Diálogo de recuperación de contraseña
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = {
                showRecoveryDialog = false
                recoveryInput = ""
                viewModel.clearRecoveryMessage()
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingrese su nombre de usuario para recibir una contraseña temporal.")
                    TextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it },
                        placeholder = { Text("Usuario") },
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
                    recoveryMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.startsWith("Se ha enviado"))
                                MaterialTheme.colorScheme.primary
                            else Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendRecoveryPassword(recoveryInput, context)
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showRecoveryDialog = false
                    recoveryInput = ""
                    viewModel.clearRecoveryMessage()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aplicación de registro de notas",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
            placeholder = { Text("Usuario") },
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

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            placeholder = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        // Mensaje de error bajo el campo de contraseña
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        TextButton(
            onClick = {
                recoveryInput = ""
                viewModel.clearRecoveryMessage()
                showRecoveryDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Olvidó su contraseña?")
        }
    }
}
