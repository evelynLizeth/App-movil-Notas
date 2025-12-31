package com.eve.notas.ui.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun MessageSnackbar(
    message: String?,          // texto que quieres mostrar
    onDismiss: () -> Unit      // callback para limpiar el mensaje en el ViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Cada vez que cambie el mensaje, mostramos el snackbar
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short // ⏱ dura ~4 segundos y desaparece solo
            )
            onDismiss() // limpiamos el mensaje en el ViewModel
        }
    }

    // Host que renderiza el snackbar
    SnackbarHost(hostState = snackbarHostState) { data ->
        Snackbar { Text(data.visuals.message) }
    }
}