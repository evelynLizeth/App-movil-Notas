package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Task
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repo: NotesRepository,
    private val studentId: Long
) : ViewModel() {

    // 🔹 Nombre del estudiante
    fun getStudentById(id: Long): Flow<Student?> = repo.getStudentByIdFlow(id)

    // 🔹 Todas las tareas
    val tasks: StateFlow<List<Task>> =
        repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹  notas del estudiante
    val grades: StateFlow<List<Grade>> =
        repo.getGradesByStudent(studentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹 Promedio calculado en tiempo real
    val promedio: StateFlow<Double> =
        combine(grades, tasks) { listaNotas, listaTareas ->
            val totalTareas = listaTareas.size
            if (totalTareas == 0) return@combine 0.0

            val mapaNotas = listaNotas.associateBy { it.taskId }
            val suma = listaTareas.sumOf { tarea ->
                mapaNotas[tarea.id]?.value ?: 0.0
            }
            suma / totalTareas
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // Actualizar nota
    fun updateNota(taskId: Long, nota: Double) {
        viewModelScope.launch {
            repo.updateNota(studentId, taskId, nota)
        }
    }

    // Recalcular promedio explícitamente (cuando presionas Guardar)
    fun calcularPromedio() {
        viewModelScope.launch {
            // 🔹 Obtener todas las tareas y notas del estudiante
            val allTasks = repo.getTasksList()
            val allGrades = repo.getGradesByStudentList(studentId)

            // 🔹 Calcular promedio: suma de notas / número de tareas
            val totalTareas = allTasks.size
            val sumaNotas = allGrades.sumOf { it.value }
            val promedio = if (totalTareas > 0) sumaNotas / totalTareas else 0.0

            // 🔹 Guardar el promedio en la base de datos
            repo.updateStudentAverage(studentId, promedio)
            _message.value = "Las notas se guardarón con éxito. "
        }
    }
    fun clearMessage() {
        _message.value = null
    }
}
