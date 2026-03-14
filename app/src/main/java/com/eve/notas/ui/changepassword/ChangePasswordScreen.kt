package com.eve.notas.ui.changepassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla de cambio de contraseña.
 *
 * Se muestra cuando el usuario tiene una contraseña temporal (mustChangePassword = true)
 * y debe establecer una nueva antes de continuar usando la app.
 * Al completarse exitosamente, navega a la pantalla de instituciones.
 *
 * @param viewModel ViewModel que gestiona la lógica de cambio de contraseña
 * @param onSuccess Callback para navegar a instituciones tras el cambio exitoso
 * @param modifier Modificador opcional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onSuccess: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Observar estado desde el ViewModel ───────────────────────────────────
    val success by viewModel.success.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ── Estado local de los campos ────────────────────────────────────────────
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Cuando el cambio es exitoso navega a instituciones (mustChangePassword = false)
    LaunchedEffect(success) {
        if (success) {
            onSuccess()
        }
    }

    // ── Colores compartidos para los campos de contraseña ────────────────────
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Título ────────────────────────────────────────────────────────────
        Text(
            text = "Cambiar Contraseña",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Mensaje explicativo ───────────────────────────────────────────────
        // Informa al usuario por qué debe cambiar su contraseña en este momento
        Text(
            text = "Su cuenta tiene una contraseña temporal. Debe establecer una nueva contraseña para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Campo nueva contraseña ────────────────────────────────────────────
        // PasswordVisualTransformation oculta el texto con puntos
        TextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                viewModel.clearError()
            },
            placeholder = { Text("Nueva contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Campo confirmación ────────────────────────────────────────────────
        // El ViewModel valida que coincida con newPassword antes de guardar
        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                viewModel.clearError()
            },
            placeholder = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        // ── Mensaje de error ──────────────────────────────────────────────────
        // Aparece si las contraseñas no coinciden o el campo está vacío
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Botón guardar ─────────────────────────────────────────────────────
        // Persiste la nueva contraseña y desactiva mustChangePassword en Room
        Button(
            onClick = { viewModel.changePassword(newPassword, confirmPassword) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }

    } // end Scaffold
}
