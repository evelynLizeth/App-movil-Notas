package com.eve.notas.ui.main

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Student
import com.eve.notas.data.repository.NotesRepository
import com.eve.notas.util.GeneratorPDF
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import com.eve.notas.util.ValidationHelper
import com.eve.notas.util.generatePdf

/**
 * ViewModel de la pantalla principal (lista de estudiantes).
 *
 * Gestiona:
 * - La lista de estudiantes con búsqueda reactiva
 * - La selección múltiple para eliminación
 * - Los diálogos de crear/editar/eliminar estudiantes
 * - La exportación de la lista a PDF
 * - Los mensajes de feedback para el usuario
 */
class MainViewModel(
    application: Application,
    private val repo: NotesRepository
) : AndroidViewModel(application) {

    // ── Lista de estudiantes ──────────────────────────────────────────────────

    /**
     * Lista completa de estudiantes desde Room como [Flow].
     * Se usa internamente para validar duplicados al crear o editar.
     */
    val students: Flow<List<Student>> = repo.getStudents()

    private val _courseId = MutableStateFlow(0L)
    val courseId: StateFlow<Long> = _courseId.asStateFlow()

    // ── Contexto seleccionado (institución y grado) ───────────────────────────

    /** Nombre de la institución seleccionada al ingresar desde CoursesScreen */
    private val _institutionName = MutableStateFlow("")
    val institutionName: StateFlow<String> = _institutionName.asStateFlow()

    /** Nombre del grado/curso seleccionado */
    private val _courseName = MutableStateFlow("")
    val courseName: StateFlow<String> = _courseName.asStateFlow()

    /** Establece la institución y el grado activos al navegar desde Courses a Main */
    fun setSelectedContext(institutionName: String, courseName: String, courseId: Long) {
        _institutionName.value = institutionName
        _courseName.value = courseName
        _courseId.value = courseId
        viewModelScope.launch {
            repo.getStudentsByCourse(courseId).first().firstOrNull()?.notaMaxima?.let {
                _notaMaxima.value = it
            }
        }
    }

    // ── Escala de calificación global ─────────────────────────────────────────

    /**
     * Escala de calificación global (10 o 20). Se aplica a todos los estudiantes.
     * Se inicializa desde el primer estudiante en la base de datos; si no hay
     * estudiantes, el valor por defecto es 20.
     */
    private val _notaMaxima = MutableStateFlow(10)
    val notaMaxima: StateFlow<Int> = _notaMaxima.asStateFlow()

    // ── Confirmación de cambio de escala ──────────────────────────────────────

    /** Escala pendiente de confirmar cuando hay notas existentes */
    private val _pendingNotaMaxima = MutableStateFlow<Int?>(null)
    val pendingNotaMaxima: StateFlow<Int?> = _pendingNotaMaxima.asStateFlow()

    /** Controla la visibilidad del diálogo de advertencia de cambio de escala */
    private val _showScaleChangeDialog = MutableStateFlow(false)
    val showScaleChangeDialog: StateFlow<Boolean> = _showScaleChangeDialog.asStateFlow()

    /**
     * Solicita cambiar la escala. Si hay notas registradas muestra el diálogo de
     * advertencia; si no, aplica el cambio directamente.
     */
    fun requestScaleChange(value: Int) {
        if (value == _notaMaxima.value) return
        viewModelScope.launch {
            if (repo.hasAnyGradesByCourse(_courseId.value)) {
                _pendingNotaMaxima.value = value
                _showScaleChangeDialog.value = true
            } else {
                applyScaleChange(value)
            }
        }
    }

    /** El usuario confirmó: borra todas las notas y aplica la nueva escala. */
    fun confirmScaleChange() {
        viewModelScope.launch {
            _pendingNotaMaxima.value?.let { applyScaleChange(it) }
            repo.deleteGradesAndResetAveragesByCourse(_courseId.value)
            _showScaleChangeDialog.value = false
            _pendingNotaMaxima.value = null
        }
    }

    /** El usuario canceló: descarta el cambio y cierra el diálogo. */
    fun cancelScaleChange() {
        _showScaleChangeDialog.value = false
        _pendingNotaMaxima.value = null
    }

    private suspend fun applyScaleChange(value: Int) {
        _notaMaxima.value = value
        repo.updateCourseStudentsNotaMaxima(_courseId.value, value)
    }

    // ── Búsqueda reactiva ─────────────────────────────────────────────────────

    /** Texto ingresado en el campo de búsqueda */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Lista de estudiantes filtrada según el texto de búsqueda.
     * Usa [flatMapLatest] para cancelar automáticamente la búsqueda anterior
     * cuando el usuario escribe un nuevo texto.
     */
    val filteredStudents: StateFlow<List<Student>> = combine(_searchQuery, _courseId) { query, courseId ->
        Pair(query, courseId)
    }.flatMapLatest { (query, courseId) ->
        if (courseId == 0L) flowOf(emptyList())
        else if (query.isBlank()) repo.getStudentsByCourse(courseId)
        else repo.searchStudentsByCourseAndName(courseId, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Selección múltiple ────────────────────────────────────────────────────

    /** Lista de estudiantes seleccionados para eliminación múltiple */
    private val _selectedStudents = MutableStateFlow<List<Student>>(emptyList())
    val selectedStudents: StateFlow<List<Student>> = _selectedStudents.asStateFlow()

    // ── Estados de diálogos ───────────────────────────────────────────────────

    /** Controla la visibilidad del diálogo de confirmación de borrado */
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    /** Controla la visibilidad del diálogo para crear un nuevo estudiante */
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    /** Estudiante que se está editando actualmente. Null si no hay edición activa */
    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

    // ── Mensajes ──────────────────────────────────────────────────────────────

    /** Mensaje de error para mostrar en el campo de nombre (vacío o duplicado) */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Mensaje de feedback para el usuario (éxito, eliminación, etc.) */
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // ── Acciones de búsqueda ──────────────────────────────────────────────────

    /** Actualiza el texto de búsqueda y dispara el filtrado reactivo */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // ── Selección múltiple ────────────────────────────────────────────────────

    /**
     * Agrega o quita un estudiante de la selección actual.
     * Si ya estaba seleccionado lo deselecciona, si no lo selecciona.
     */
    fun toggleSelection(student: Student) {
        val current = _selectedStudents.value.toMutableList()
        if (current.contains(student)) current.remove(student)
        else current.add(student)
        _selectedStudents.value = current
    }

    // ── Control del diálogo de borrado ────────────────────────────────────────

    fun openDeleteDialog() { _showDeleteDialog.value = true }

    /** Cierra el diálogo de borrado y limpia la selección actual */
    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedStudents.value = emptyList()
    }

    /**
     * Elimina todos los estudiantes seleccionados de la base de datos.
     * Al terminar limpia la selección y muestra un mensaje de confirmación.
     */
    fun deleteSelected() {
        viewModelScope.launch {
            _selectedStudents.value.forEach { repo.deleteStudent(it) }
            _selectedStudents.value = emptyList()
            closeDeleteDialog()
            _uiMessage.value = "Se elimino los registros exitosamente."
        }
    }

    // ── Control del diálogo de agregar ────────────────────────────────────────

    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }

    /** Limpia el mensaje de error del campo de nombre */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Crea un nuevo estudiante si el nombre es válido (no vacío y no duplicado).
     * Muestra un error en el campo si la validación falla.
     */
    fun addStudent(name: String) {
        viewModelScope.launch {
            val existingNames = filteredStudents.value.map { it.id to it.name }
            if (!ValidationHelper.isValidName(name, existingNames)) {
                _errorMessage.value = "El registro ya existe o está vacío"
                return@launch
            }
            repo.insert(Student(name = name, average = 0.0, notaMaxima = _notaMaxima.value, courseId = _courseId.value))
            _errorMessage.value = null
            closeAddDialog()
            _uiMessage.value = "Registro creado con éxito."
        }
    }

    // ── Edición de estudiante ─────────────────────────────────────────────────

    /** Abre el diálogo de edición para el estudiante seleccionado */
    fun startEditing(student: Student) {
        _editingStudent.value = student
    }

    /**
     * Valida y guarda el nuevo nombre del estudiante en edición.
     * Si el nombre es vacío o ya existe en otro registro, muestra un error.
     * El currentId excluye al propio estudiante de la validación de duplicados.
     */
    fun finishEditing(student: Student, newName: String) {
        viewModelScope.launch {
            val existingNames = filteredStudents.value.map { it.id to it.name }
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

    /** Cancela la edición activa y limpia cualquier mensaje de error */
    fun cancelEditing() {
        _editingStudent.value = null
        _errorMessage.value = null
    }

    fun deleteStudentDirect(student: Student) {
        viewModelScope.launch { repo.deleteStudentCascade(student) }
    }

    // ── Exportar PDF ──────────────────────────────────────────────────────────

    /**
     * Genera un PDF con la lista de estudiantes y sus promedios,
     * y abre el selector de apps para visualizarlo.
     *
     * Usa [FileProvider] para compartir el archivo de forma segura
     * con otras apps sin exponer la ruta interna del almacenamiento.
     */
    fun exportStudentsToPdf(context: Context, students: List<Student>) {
        val generator = GeneratorPDF()
        val file = generator.generate(context, students)

        // Obtiene una URI segura para compartir el archivo con FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        // Abre el selector de apps para que el usuario elija cómo visualizar el PDF
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir PDF con..."))
    }

    // ── Mensajes de UI ────────────────────────────────────────────────────────

    /** Limpia el mensaje de feedback para que el Snackbar desaparezca */
    fun clearMessage() {
        _uiMessage.value = null
    }
}
