package com.eve.notas.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.User
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de registro de usuario.
 *
 * Gestiona:
 * - La validación de todos los campos requeridos
 * - La verificación de que el username no esté ya en uso
 * - La creación del nuevo usuario en la base de datos
 * - Los mensajes de error y el flag de éxito para la navegación
 */
class RegisterViewModel(private val repo: NotesRepository) : ViewModel() {

    /** Flag que indica si el registro fue completado exitosamente */
    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    /** Mensaje de error para mostrar en la UI */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Valida los datos e intenta registrar al usuario.
     * Verifica que todos los campos estén completos y que el username sea único.
     * Si las contraseñas no coinciden, muestra un error.
     *
     * @param name Nombre completo del usuario
     * @param username Nombre de usuario para el inicio de sesión
     * @param password Contraseña elegida
     * @param confirmPassword Confirmación de la contraseña
     * @param email Correo electrónico del usuario
     */
    fun register(
        name: String,
        username: String,
        password: String,
        confirmPassword: String,
        email: String
    ) {
        viewModelScope.launch {
            if (name.isBlank() || username.isBlank() || password.isBlank() ||
                confirmPassword.isBlank() || email.isBlank()
            ) {
                _errorMessage.value = "Todos los campos son obligatorios"
                return@launch
            }
            if (password != confirmPassword) {
                _errorMessage.value = "Las contraseñas no coinciden"
                return@launch
            }
            val existing = repo.getUserByUsername(username)
            if (existing != null) {
                _errorMessage.value = "El usuario ya existe"
                return@launch
            }
            repo.registerUser(
                User(
                    name = name,
                    username = username,
                    password = password,
                    email = email
                )
            )
            _registerSuccess.value = true
        }
    }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }
}
