package com.eve.notas.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.data.model.Student
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(private val repo: NotesRepository) : ViewModel() {
    val students = repo.getStudents().asLiveData()

    // 🔹 IDs de alumnos seleccionados
    private val _selectedStudents = mutableStateListOf<Long>()
    val selectedStudents: List<Long> get() = _selectedStudents

    fun toggleSelection(id: Long) {
        if (_selectedStudents.contains(id)) {
            _selectedStudents.remove(id)
        } else {
            _selectedStudents.add(id)
        }
    }

    fun editSelected() {
        viewModelScope.launch {
            val all = students.value ?: emptyList()
            val toEdit = all.filter { _selectedStudents.contains(it.id) }
            toEdit.forEach { student ->
                val updated = student.copy(name = student.name + " (editado)")
                repo.updateStudent(updated)
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val all = students.value ?: emptyList()
            val toDelete = all.filter { _selectedStudents.contains(it.id) }
            toDelete.forEach { student ->
                repo.deleteStudent(student)
            }
            _selectedStudents.clear()
        }
    }
    // 🔹 Solo un alumno puede estar en edición
    private val _editingStudentId = MutableLiveData<Long?>(null)
    val editingStudentId: LiveData<Long?> = _editingStudentId

    // MainViewModel
    fun addStudent() {
        viewModelScope.launch {
            val newStudent = Student(name = "", average = 0.0)
            val id = repo.insert(newStudent)
            _editingStudentId.value = id
        }
    }

    fun startEditing(id: Long) {
        _editingStudentId.value = id
    }

    fun finishEditing(student: Student, newName: String) {
        viewModelScope.launch {
            val updated = student.copy(name = newName)
            repo.updateStudent(updated)
            _editingStudentId.value = null            // salir de modo edición
        }
    }


    private val _filteredStudents = MutableLiveData<List<Student>>()
    val filteredStudents: LiveData<List<Student>> = _filteredStudents

    fun searchByName(name: String) {
        viewModelScope.launch {
            repo.searchByName(name).collect { result ->
                _filteredStudents.value = result
            }
        }
    }

    fun calculateAverage() {
        // Aquí implementas la lógica de promedio (ej. notas de los estudiantes)
    }
}