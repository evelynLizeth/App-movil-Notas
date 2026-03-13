package com.eve.notas.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eve.notas.data.repository.NotesRepository

/**
 * Factory para crear instancias de [CoursesViewModel] con el repositorio
 * y el institutionId inyectados. Sigue el patrón manual de inyección usado en toda la app.
 */
class CoursesViewModelFactory(
    private val repo: NotesRepository,
    private val institutionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoursesViewModel(repo, institutionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
