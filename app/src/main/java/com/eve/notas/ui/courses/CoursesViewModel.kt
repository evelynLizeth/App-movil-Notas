package com.eve.notas.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Course
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de cursos.
 *
 * Gestiona:
 * - La lista reactiva de cursos de la institución seleccionada
 * - El diálogo para agregar un nuevo curso
 * - La validación del nombre y paralelo del curso
 * - La verificación de unicidad de la combinación nombre + paralelo
 * - Los mensajes de error para la UI
 */
class CoursesViewModel(
    private val repo: NotesRepository,
    private val institutionId: Long
) : ViewModel() {

    /**
     * Lista de cursos de la institución como [StateFlow] reactivo.
     * Se actualiza automáticamente al agregar o eliminar cursos.
     */
    val courses: StateFlow<List<Course>> =
        repo.getCoursesByInstitution(institutionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Nombre de la institución cargado desde el repositorio */
    private val _institutionName = MutableStateFlow("")
    val institutionName: StateFlow<String> = _institutionName.asStateFlow()

    init {
        viewModelScope.launch {
            _institutionName.value = repo.getInstitutionById(institutionId)?.name ?: ""
        }
    }

    /** Controla la visibilidad del diálogo para agregar curso */
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    /** Mensaje de error para mostrar en la UI */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Mensaje de feedback para el usuario (éxito, eliminación, etc.) */
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearMessage() { _uiMessage.value = null }

    /** Curso que se está editando actualmente. Null si no hay edición activa */
    private val _editingCourse = MutableStateFlow<Course?>(null)
    val editingCourse: StateFlow<Course?> = _editingCourse.asStateFlow()

    /** Abre el diálogo de agregar curso */
    fun openAddDialog() { _showAddDialog.value = true }

    /** Cierra el diálogo de agregar curso */
    fun closeAddDialog() { _showAddDialog.value = false }

    /** Limpia el mensaje de error */
    fun clearError() { _errorMessage.value = null }

    /** Abre el diálogo de edición para el curso seleccionado */
    fun startEditing(course: Course) {
        _editingCourse.value = course
    }

    /** Cancela la edición activa */
    fun cancelEditing() {
        _editingCourse.value = null
        _errorMessage.value = null
    }

    /** Valida y guarda el nuevo nombre y paralelo del curso */
    fun finishEditing(course: Course, newName: String, newParallel: String) {
        viewModelScope.launch {
            if (newName.isBlank() || newParallel.isBlank()) {
                _errorMessage.value = "Nombre y paralelo son obligatorios"
                return@launch
            }
            if (repo.courseExistsExcluding(institutionId, newName.trim(), newParallel.trim(), course.id)) {
                _errorMessage.value = "Ya existe un curso con ese nombre y paralelo"
                return@launch
            }
            repo.updateCourse(course.copy(name = newName.trim(), parallel = newParallel.trim()))
            _editingCourse.value = null
            _errorMessage.value = null
            _uiMessage.value = "Registro editado exitosamente."
        }
    }

    /**
     * Valida e inserta un nuevo curso para la institución actual.
     * El nombre y el paralelo son obligatorios.
     * La combinación de nombre + paralelo debe ser única en la institución.
     *
     * @param name Nombre del curso (ej: "Matemáticas")
     * @param parallel Paralelo del curso (ej: "A", "B")
     */
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repo.deleteCourseCascade(course)
            _uiMessage.value = "Registro eliminado exitosamente."
        }
    }

    fun addCourse(name: String, parallel: String) {
        viewModelScope.launch {
            if (name.isBlank() || parallel.isBlank()) {
                _errorMessage.value = "Nombre y paralelo son obligatorios"
                return@launch
            }
            if (repo.courseExists(institutionId, name, parallel)) {
                _errorMessage.value = "Ya existe un curso con ese nombre y paralelo"
                return@launch
            }
            repo.addCourse(
                Course(
                    institutionId = institutionId,
                    name = name,
                    parallel = parallel
                )
            )
            closeAddDialog()
        }
    }
}
