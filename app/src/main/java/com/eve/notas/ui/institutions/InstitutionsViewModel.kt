package com.eve.notas.ui.institutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Institution
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de instituciones.
 *
 * Gestiona:
 * - La lista reactiva de instituciones del usuario autenticado
 * - El diálogo para agregar una nueva institución
 * - La validación del nombre de la institución
 * - Los mensajes de error para la UI
 */
class InstitutionsViewModel(
    private val repo: NotesRepository,
    private val userId: Long
) : ViewModel() {

    /**
     * Lista de instituciones del usuario como [StateFlow] reactivo.
     * Se actualiza automáticamente al agregar o eliminar instituciones.
     */
    val institutions: StateFlow<List<Institution>> =
        repo.getInstitutionsByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Controla la visibilidad del diálogo para agregar institución */
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    /** Mensaje de error para mostrar en la UI */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Abre el diálogo de agregar institución */
    fun openAddDialog() { _showAddDialog.value = true }

    /** Cierra el diálogo de agregar institución */
    fun closeAddDialog() { _showAddDialog.value = false }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }

    /**
     * Valida e inserta una nueva institución para el usuario actual.
     * El nombre no puede estar vacío.
     *
     * @param name Nombre de la nueva institución
     */
    fun addInstitution(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _errorMessage.value = "El nombre no puede estar vacío"
                return@launch
            }
            repo.addInstitution(Institution(userId = userId, name = name))
            closeAddDialog()
        }
    }
}
