package com.eve.notas.ui.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de cambio de contraseña.
 *
 * Usado cuando el usuario tiene mustChangePassword = true (contraseña temporal)
 * o cuando desea cambiar su contraseña voluntariamente.
 * Valida que las contraseñas coincidan y no estén vacías antes de persistir.
 */
class ChangePasswordViewModel(
    private val repo: NotesRepository,
    private val userId: Long
) : ViewModel() {

    /** Flag que indica si el cambio fue exitoso */
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    /** Mensaje de error para mostrar en la UI */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Valida y aplica el cambio de contraseña.
     * Verifica que la nueva contraseña no esté vacía y que coincida con la confirmación.
     *
     * @param newPassword Nueva contraseña elegida por el usuario
     * @param confirmPassword Confirmación de la nueva contraseña
     */
    fun changePassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword.isBlank()) {
                _errorMessage.value = "La contraseña no puede estar vacía"
                return@launch
            }
            if (newPassword != confirmPassword) {
                _errorMessage.value = "Las contraseñas no coinciden"
                return@launch
            }
            repo.updatePassword(userId, newPassword, mustChangePassword = false)
            _success.value = true
        }
    }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }
}
