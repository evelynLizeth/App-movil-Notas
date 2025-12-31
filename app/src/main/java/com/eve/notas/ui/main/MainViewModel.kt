package com.eve.notas.ui.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Student
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.util.GeneratorPDF
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.eve.notas.util.ValidationHelper
import com.eve.notas.util.generatePdf

class MainViewModel(
    application: Application,
    private val repo: NotesRepository
) : AndroidViewModel(application) {

    // 🔹 Lista completa de estudiantes como Flow
    val students: Flow<List<Student>> = repo.getStudents()

    // 🔹 Alumno en edición (objeto completo)
    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

    // 🔹 Mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 🔹 Alumnos seleccionados para borrado múltiple
    private val _selectedStudents = MutableStateFlow<List<Student>>(emptyList())
    val selectedStudents: StateFlow<List<Student>> = _selectedStudents.asStateFlow()

    // 🔹 Estado del diálogo de confirmación de borrado
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    // 🔹 Estado del diálogo de agregar estudiante
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // 🔹 Búsqueda reactiva
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredStudents: StateFlow<List<Student>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repo.getStudents()
            else repo.searchByName(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔹 Mensajes de UI (éxito, info, etc.)
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearMessage() {
        _uiMessage.value = null
    }
    val count = _selectedStudents.value.size


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
    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedStudents.value = emptyList()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedStudents.value.forEach { repo.deleteStudent(it) }
            _selectedStudents.value = emptyList()
            closeDeleteDialog()
            _uiMessage.value = "Se elimino los registros exitosamente."
        }
    }

    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addStudent(name: String) {
        viewModelScope.launch {
            val existingNames = students.first().map { it.id to it.name }
            if (!ValidationHelper.isValidName(name, existingNames)) {
                _errorMessage.value = "El registro ya existe o está vacío"
                return@launch
            }
            repo.insert(Student(name = name, average = 0.0))
            _errorMessage.value = null
            closeAddDialog()
            _uiMessage.value = "Registro creado con éxito."
        }
    }

    // 🔹 Iniciar edición con el objeto completo
    fun startEditing(student: Student) {
        _editingStudent.value = student
    }

    // 🔹 Finalizar edición
    fun finishEditing(student: Student, newName: String) {
        viewModelScope.launch {
            val existingNames = students.first().map { it.id to it.name }
            if (!ValidationHelper.isValidName(newName, existingNames, student.id)) {
                _errorMessage.value = "El registro ya existe o está vacío"
                return@launch
            }
            repo.updateStudent(student.copy(name = newName))
            _editingStudent.value = null
            _errorMessage.value = null
            _uiMessage.value = "Registro editado exitosamente."
        }
    }

    // 🔹 Cancelar edición
    fun cancelEditing() {
        _editingStudent.value = null
        _errorMessage.value = null
    }

    // 🔹 Exportar estudiantes a PDF
    fun exportStudentsToPdf(context: Context, students: List<Student>) {
        val generator = GeneratorPDF()
        val file = generator.generate(context, students)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Abrir PDF con..."))
    }
}