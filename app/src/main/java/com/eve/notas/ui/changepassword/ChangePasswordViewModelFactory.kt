package com.eve.notas.ui.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eve.notas.data.repository.NotesRepository

/**
 * Factory para crear instancias de [ChangePasswordViewModel] con el repositorio
 * y el userId inyectados. Sigue el patrón manual de inyección usado en toda la app.
 */
class ChangePasswordViewModelFactory(
    private val repo: NotesRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChangePasswordViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
