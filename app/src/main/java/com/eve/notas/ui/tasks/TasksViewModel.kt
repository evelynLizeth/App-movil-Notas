package com.eve.notas.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Modelo de datos de una tarea
data class Task(
    val id: Long,
    val name: String
)

class TasksViewModel : ViewModel() {

    // 🔹 Lista de tareas
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // 🔹 Campo de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // 🔹 Lista filtrada según búsqueda
    val filteredTasks: StateFlow<List<Task>> = _searchQuery
        .combine(_tasks) { query, tasks ->
            if (query.isBlank()) tasks
            else tasks.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 🔹 Tareas seleccionadas
    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks.asStateFlow()

    fun toggleSelection(task: Task) {
        _selectedTasks.value = if (_selectedTasks.value.contains(task)) {
            _selectedTasks.value - task
        } else {
            _selectedTasks.value + task
        }
    }

    // 🔹 Estado del diálogo de creación
    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun confirmAddTask(name: String) {
        addTask(name)
        _isAddDialogOpen.value = false
    }

    // 🔹 CRUD de tareas
    fun addTask(name: String) {
        val newTask = Task(id = (_tasks.value.size + 1).toLong(), name = name)
        _tasks.value = _tasks.value + newTask
    }

    fun editTask(task: Task) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) task.copy(name = task.name + " (editado)") else it
        }
    }

    fun deleteTask(task: Task) {
        _tasks.value = _tasks.value.filterNot { it.id == task.id }
    }

    fun deleteSelectedTasks() {
        _tasks.value = _tasks.value.filterNot { _selectedTasks.value.contains(it) }
        _selectedTasks.value = emptyList()
    }
    // 🔹 Estado del diálogo de confirmación de eliminación
    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    fun openDeleteDialog() {
        _isDeleteDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteDialogOpen.value = false
    }

    fun confirmDeleteSelectedTasks() {
        _tasks.value = _tasks.value.filterNot { _selectedTasks.value.contains(it) }
        _selectedTasks.value = emptyList()
        _isDeleteDialogOpen.value = false
    }

}