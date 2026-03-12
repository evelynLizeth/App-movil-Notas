package com.eve.notas.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión.
 *
 * Gestiona:
 * - La autenticación de usuarios con validación de credenciales
 * - La detección del flag mustChangePassword para redirigir al cambio obligatorio
 * - El flujo de recuperación de contraseña mediante correo electrónico
 * - Los mensajes de error y resultado del login
 */
class LoginViewModel(private val repo: NotesRepository) : ViewModel() {

    /** Resultado del proceso de inicio de sesión */
    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    /** Mensaje de error para mostrar en la UI */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Mensaje de resultado del proceso de recuperación de contraseña */
    private val _recoveryMessage = MutableStateFlow<String?>(null)
    val recoveryMessage: StateFlow<String?> = _recoveryMessage.asStateFlow()

    /**
     * Intenta autenticar al usuario con el nombre de usuario y contraseña dados.
     * Si el usuario tiene mustChangePassword = true, redirige al cambio de contraseña.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _errorMessage.value = "Ingrese usuario y contraseña"
                return@launch
            }
            val user = repo.getUserByUsername(username)
            when {
                user == null -> _errorMessage.value = "Usuario no encontrado"
                user.password != password -> _errorMessage.value = "Contraseña incorrecta"
                user.mustChangePassword -> _loginResult.value = LoginResult.MustChangePassword(user.id)
                else -> _loginResult.value = LoginResult.Success(user.id)
            }
        }
    }

    /**
     * Genera una contraseña temporal para el usuario identificado por username.
     * Guarda la contraseña temporal con mustChangePassword = true y lanza
     * un Intent de correo electrónico para enviarla al usuario.
     *
     * @param usernameOrEmail Username del usuario que solicita la recuperación
     * @param context Contexto de Android necesario para lanzar el Intent de correo
     */
    fun sendRecoveryPassword(usernameOrEmail: String, context: Context) {
        viewModelScope.launch {
            if (usernameOrEmail.isBlank()) {
                _recoveryMessage.value = "Ingrese su nombre de usuario"
                return@launch
            }
            val user = repo.getUserByUsername(usernameOrEmail)
                ?: repo.getUserByEmail(usernameOrEmail)
                ?: run {
                    _recoveryMessage.value = "Usuario no encontrado"
                    return@launch
                }
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val tempPassword = (1..8).map { chars.random() }.joinToString("")
            repo.updatePassword(user.id, tempPassword, mustChangePassword = true)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${user.email}")
                putExtra(Intent.EXTRA_SUBJECT, "Recuperación de contraseña")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Su contraseña temporal es: $tempPassword\nDebe cambiarla al ingresar."
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Enviar correo"))
            _recoveryMessage.value = "Se ha enviado la contraseña temporal a su correo"
        }
    }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }

    /** Limpia el resultado del login (para evitar re-navegación) */
    fun clearResult() { _loginResult.value = null }

    /** Limpia el mensaje de recuperación */
    fun clearRecoveryMessage() { _recoveryMessage.value = null }
}

/** Resultado sellado del proceso de inicio de sesión */
sealed class LoginResult {
    /** El usuario se autenticó correctamente y puede continuar */
    data class Success(val userId: Long) : LoginResult()

    /** El usuario se autenticó pero tiene contraseña temporal y debe cambiarla */
    data class MustChangePassword(val userId: Long) : LoginResult()
}
