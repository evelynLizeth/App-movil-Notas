package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Student
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    // 🔹 Obtener un estudiante por ID como Flow
    fun getStudentById(id: Long): Flow<Student?> {
        return repository.getStudentByIdFlow(id)
    }

    // 🔹 Guardar notas y actualizar promedio
    fun saveGrades(studentId: Long, notas: Map<Long, Double>, promedio: Double) {
        viewModelScope.launch {
            // Guardar cada nota en la tabla grades
            notas.forEach { (taskId, nota) ->
                repository.insertOrUpdateGrade(studentId, taskId, nota)
            }
            // Actualizar el promedio del estudiante en la tabla students
            repository.updateStudentAverage(studentId, promedio)
        }
    }
}