package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Student
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _averageFlow = MutableStateFlow(0.00)
    val averageFlow: Flow<Double> = _averageFlow.asStateFlow()

    fun getStudentById(id: Long): Flow<Student?> {
        return repository.getStudentByIdFlow(id)
    }

    fun saveGrades(studentId: Long, notas: Map<Long, Double>, promedio: Double) {
        viewModelScope.launch {
            notas.forEach { (taskId, nota) ->
                repository.insertOrUpdateGrade(studentId, taskId, nota)
            }
            repository.updateStudentAverage(studentId, promedio)
            _averageFlow.value = promedio
        }
    }
}