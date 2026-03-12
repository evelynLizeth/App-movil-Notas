package com.eve.notas.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eve.notas.data.repository.NotesRepository

/**
 * Factory para crear instancias de [LoginViewModel] con el repositorio inyectado.
 * Sigue el patrón manual de inyección de dependencias usado en toda la app.
 */
class LoginViewModelFactory(
    private val repo: NotesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
