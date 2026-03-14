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

    /** Mensaje de feedback para el usuario (éxito, eliminación, etc.) */
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearMessage() { _uiMessage.value = null }

    /** Institución que se está editando actualmente. Null si no hay edición activa */
    private val _editingInstitution = MutableStateFlow<Institution?>(null)
    val editingInstitution: StateFlow<Institution?> = _editingInstitution.asStateFlow()

    /** Abre el diálogo de agregar institución */
    fun openAddDialog() { _showAddDialog.value = true }

    /** Cierra el diálogo de agregar institución */
    fun closeAddDialog() { _showAddDialog.value = false }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }

    /** Abre el diálogo de edición para la institución seleccionada */
    fun startEditing(institution: Institution) {
        _editingInstitution.value = institution
    }

    /** Cancela la edición activa */
    fun cancelEditing() {
        _editingInstitution.value = null
        _errorMessage.value = null
    }

    /** Valida y guarda el nuevo nombre de la institución */
    fun finishEditing(institution: Institution, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank()) {
                _errorMessage.value = "El nombre no puede estar vacío"
                return@launch
            }
            val existing = repo.getInstitutionByName(userId, newName.trim())
            if (existing != null && existing.id != institution.id) {
                _errorMessage.value = "Ya existe una institución con ese nombre"
                return@launch
            }
            repo.updateInstitution(institution.copy(name = newName.trim()))
            _editingInstitution.value = null
            _errorMessage.value = null
            _uiMessage.value = "Registro editado exitosamente."
        }
    }

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
            if (repo.getInstitutionByName(userId, name.trim()) != null) {
                _errorMessage.value = "Ya existe una institución con ese nombre"
                return@launch
            }
            repo.addInstitution(Institution(userId = userId, name = name.trim()))
            closeAddDialog()
        }
    }

    fun deleteInstitution(institution: Institution) {
        viewModelScope.launch {
            repo.deleteInstitutionCascade(institution)
            _uiMessage.value = "Registro eliminado exitosamente."
        }
    }
}
