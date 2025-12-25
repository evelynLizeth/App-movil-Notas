package com.eve.notas.ui.main

import android.app.Application
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Student
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainViewModel(
    application: Application,
    private val repo: NotesRepository
) : AndroidViewModel(application) {

    // 🔹 Lista completa de estudiantes
    val students: Flow<List<Student>> = repo.getStudents()

    // 🔹 Alumno en edición (solo uno a la vez)
    private val _editingStudentId = MutableStateFlow<Long?>(null)
    val editingStudentId = _editingStudentId.asStateFlow()

    // 🔹 Alumnos seleccionados para borrado múltiple
    private val _selectedStudents = MutableStateFlow<List<Student>>(emptyList())
    val selectedStudents = _selectedStudents.asStateFlow()

    // 🔹 Estado del diálogo de confirmación de borrado
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    // 🔹 Búsqueda reactiva
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredStudents: LiveData<List<Student>> =
        _searchQuery.flatMapLatest { query: String ->
            if (query.isBlank()) {
                repo.getStudents()
            } else {
                repo.searchByName(query)
            }
        }.asLiveData()

    // -------------------------------
    // 🔹 Funciones de interacción
    // -------------------------------

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(student: Student) {
        val current = _selectedStudents.value.toMutableList()
        if (current.contains(student)) {
            current.remove(student)
        } else {
            current.add(student)
        }
        _selectedStudents.value = current
    }

    fun openDeleteDialog() { _showDeleteDialog.value = true }
    fun closeDeleteDialog() { _showDeleteDialog.value = false }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedStudents.value.forEach { repo.deleteStudent(it) }
            _selectedStudents.value = emptyList()
            closeDeleteDialog()
        }
    }

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }

    fun addStudent(name: String) {
        viewModelScope.launch {
            val newStudent = Student(name = name, average = 0.0)
            repo.insert(newStudent)
            closeAddDialog() // cerrar después de guardar
        }
    }

    fun startEditing(id: Long) {
        _editingStudentId.value = id
    }

    fun finishEditing(student: Student, newName: String) {
        viewModelScope.launch {
            val updated = student.copy(name = newName)
            repo.updateStudent(updated)
            _editingStudentId.value = null // salir de modo edición
        }
    }

    // 🔹 Exportar estudiantes a PDF
    fun exportStudentsToPdf() {
        viewModelScope.launch {
            val studentsList = students.first()
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint().apply { textSize = 12f }

            var y = 25f
            canvas.drawText("Lista de Estudiantes", 10f, y, paint)
            y += 20f

            studentsList.forEach { student ->
                canvas.drawText("${student.name} - Promedio: ${student.average}", 10f, y, paint)
                y += 20f
            }

            pdfDocument.finishPage(page)

            // ✅ Guardar en almacenamiento interno
            val file = File(getApplication<Application>().filesDir, "estudiantes.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
        }
    }
}