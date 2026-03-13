package com.eve.notas.ui.institutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eve.notas.data.repository.NotesRepository

/**
 * Factory para crear instancias de [InstitutionsViewModel] con el repositorio
 * y el userId inyectados. Sigue el patrón manual de inyección usado en toda la app.
 */
class InstitutionsViewModelFactory(
    private val repo: NotesRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstitutionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InstitutionsViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
