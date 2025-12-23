package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.launch
import com.eve.notas.data.model.Student

class DetailViewModel(private val repo: NotesRepository) : ViewModel() {
    // Ejemplo: cargar estudiante por id
    fun getStudentById(id: Long): Student? {
        return repo.getStudentById(id) // tu repositorio debe tener esta función
    }


    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            // implementar delete en tu DAO y repositorio
        }
    }
}