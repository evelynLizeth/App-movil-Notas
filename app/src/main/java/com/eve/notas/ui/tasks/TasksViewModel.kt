package com.eve.notas.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.data.model.Task
import com.eve.notas.util.ValidationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de gestión de tareas.
 * Expone el estado de la UI como [StateFlow] y maneja la lógica de negocio
 * para crear, editar, eliminar y buscar tareas.
 */
class TasksViewModel(private val repo: NotesRepository) : ViewModel() {

    // ── Lista de tareas ───────────────────────────────────────────────────────

    /**
     * Lista completa de tareas desde Room, convertida a [StateFlow].
     * Se actualiza automáticamente cuando la base de datos cambia.
     */
    val tasks: StateFlow<List<Task>> =
        repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    /** Texto ingresado en el campo de búsqueda */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Lista filtrada de tareas según el texto de búsqueda.
     * Las tareas se ordenan por fecha de creación (más reciente primero).
     * Si la búsqueda está vacía, muestra todas.
     */
    val filteredTasks: StateFlow<List<Task>> =
        combine(tasks, searchQuery) { allTasks, query ->
            // Ordena por createdAt descendente para mostrar las más recientes primero
            val ordered = allTasks.sortedByDescending { it.createdAt }
            if (query.isBlank()) ordered
            else ordered.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Selección múltiple ────────────────────────────────────────────────────

    /** Lista de tareas actualmente seleccionadas para eliminación múltiple */
    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks

    // ── Estados de UI ─────────────────────────────────────────────────────────

    /** Mensaje de error para mostrar en el campo de nombre (duplicado o vacío) */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Controla la visibilidad del diálogo para crear una nueva tarea */
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    /** Controla la visibilidad del diálogo de confirmación de borrado */
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    /** Tarea actualmente en edición. Null cuando no hay edición activa */
    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    /** Mensaje de feedback para el usuario (éxito, info, etc.) */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // ── Acciones de búsqueda ──────────────────────────────────────────────────

    /** Actualiza el texto de búsqueda y dispara el filtrado reactivo */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // ── Selección múltiple ────────────────────────────────────────────────────

    /**
     * Agrega o quita una tarea de la selección actual.
     * Si ya estaba seleccionada la deselecciona, si no la selecciona.
     */
    fun toggleSelection(task: Task) {
        _selectedTasks.value =
            if (_selectedTasks.value.contains(task))
                _selectedTasks.value - task
            else
                _selectedTasks.value + task
    }

    // ── CRUD de tareas ────────────────────────────────────────────────────────

    /** Elimina una tarea específica de la base de datos */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repo.delete(task)
        }
    }

    /** Actualiza los datos de una tarea existente en la base de datos */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repo.update(task)
        }
    }

    // ── Edición ───────────────────────────────────────────────────────────────

    /** Abre el diálogo de edición para la tarea seleccionada */
    fun startEditingTask(task: Task) {
        _editingTask.value = task
    }

    /**
     * Valida y guarda el nuevo nombre de la tarea en edición.
     * Si el nombre es vacío o duplicado, muestra un error.
     */
    fun finishEditingTask(task: Task, newName: String) {
        viewModelScope.launch {
            val existingNames = tasks.value.map { it.id to it.name }
            if (!ValidationHelper.isValidName(newName, existingNames, task.id)) {
                _errorMessage.value = "El registro ya existe o está vacío"
                return@launch
            }
            repo.update(task.copy(name = newName))
            _errorMessage.value = null
            _editingTask.value = null
        }
    }

    /** Cancela la edición activa y limpia cualquier mensaje de error */
    fun cancelEditingTask() {
        _editingTask.value = null
        _errorMessage.value = null
    }

    /** Limpia el mensaje de error del campo de nombre */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Crea una nueva tarea si el nombre es válido (no vacío y no duplicado).
     * Cierra el diálogo al completarse correctamente.
     */
    fun addTask(name: String) {
        viewModelScope.launch {
            val existingNames = tasks.value.map { it.id to it.name }
            if (!ValidationHelper.isValidName(name, existingNames)) {
                _errorMessage.value = "La tarea ya existe o está vacía"
                return@launch
            }
            repo.insert(Task(name = name))
            _errorMessage.value = null
            closeAddDialog()
        }
    }

    // ── Control de diálogos ───────────────────────────────────────────────────

    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }
    fun openDeleteDialog() { _showDeleteDialog.value = true }

    /** Cierra el diálogo de borrado y limpia la selección actual */
    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedTasks.value = emptyList()
    }

    /**
     * Elimina todas las tareas seleccionadas de la base de datos.
     * Al terminar limpia la selección y cierra el diálogo de confirmación.
     */
    fun deleteSelected() {
        viewModelScope.launch {
            _selectedTasks.value.forEach { repo.delete(it) }
            _selectedTasks.value = emptyList()
            closeDeleteDialog()
        }
    }

    /** Limpia el mensaje de feedback para el usuario */
    fun clearMessage() {
        _message.value = null
    }
}
