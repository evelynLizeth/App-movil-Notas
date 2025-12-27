package com.eve.notas.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.data.model.Task
import com.eve.notas.util.ValidationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(private val repo: NotesRepository) : ViewModel() {

    // Todas las tareas desde Room → Flow convertido a StateFlow
    val tasks: StateFlow<List<Task>> =
        repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Filtrado reactivo con combine
    val filteredTasks: StateFlow<List<Task>> =
        combine(tasks, searchQuery) { allTasks, query ->
            if (query.isBlank()) allTasks
            else allTasks.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selección múltiple
    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(task: Task) {
        _selectedTasks.value =
            if (_selectedTasks.value.contains(task))
                _selectedTasks.value - task
            else
                _selectedTasks.value + task
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repo.delete(task)
        }
    }
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repo.update(task)
        }
    }
    fun startEditingTask(task: Task) {
        _editingTask.value = task
    }

    fun finishEditingTask(task: Task, newName: String) {
        viewModelScope.launch {
            val existingNames = tasks.value.map { it.id to it.name } // si usas StateFlow
            if (!ValidationHelper.isValidName(newName, existingNames, task.id)) {
                _errorMessage.value = "El registro ya existe o está vacío"
                return@launch
            }
            repo.update(task.copy(name = newName))
            _errorMessage.value = null
            _editingTask.value = null
        }
    }
    fun cancelEditingTask() {
        _editingTask.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addTask(name: String) {
        viewModelScope.launch {
            val existingNames = tasks.value.map { it.id to it.name } // si usas StateFlow
            if (!ValidationHelper.isValidName(name, existingNames)) {
                _errorMessage.value = "La tarea ya existe o está vacía"
                return@launch
            }
            repo.insert(Task(name = name))
            _errorMessage.value = null
            closeAddDialog()
        }

    }
    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }
    fun openDeleteDialog() { _showDeleteDialog.value = true }
    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedTasks.value = emptyList()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedTasks.value.forEach { repo.delete(it) }
            _selectedTasks.value = emptyList()
            closeDeleteDialog()
        }
    }
}