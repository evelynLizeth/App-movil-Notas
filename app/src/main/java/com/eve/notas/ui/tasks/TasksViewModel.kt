package com.eve.notas.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Modelo simple de tarea
data class Task(
    val id: Long,
    val title: String,
    val completed: Boolean = false
)

class TasksViewModel : ViewModel() {

    // Lista de tareas observable
    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    private var nextId = 1L

    // Crear nueva tarea
    fun addTask(title: String) {
        val newTask = Task(id = nextId++, title = title)
        _tasks.value = _tasks.value?.plus(newTask)
    }

    // Marcar tarea como completada
    fun toggleTaskCompleted(id: Long) {
        _tasks.value = _tasks.value?.map { task ->
            if (task.id == id) task.copy(completed = !task.completed) else task
        }
    }

    // Eliminar tarea
    fun deleteTask(id: Long) {
        _tasks.value = _tasks.value?.filter { it.id != id }
    }
}