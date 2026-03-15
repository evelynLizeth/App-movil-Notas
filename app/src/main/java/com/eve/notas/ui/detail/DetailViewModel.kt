package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eve.notas.data.model.Grade
import com.eve.notas.data.model.Student
import com.eve.notas.data.model.Task
import com.eve.notas.data.repository.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de detalle de notas.
 *
 * Se crea mediante [DetailViewModelFactory] para recibir el [studentId]
 * como parámetro, ya que cada instancia corresponde a un estudiante específico.
 *
 * Gestiona:
 * - Las notas del estudiante actualizadas en tiempo real
 * - El cálculo reactivo del promedio combinando notas y tareas
 * - La persistencia automática del promedio en Student.average para que
 *   MainScreen siempre muestre el mismo valor que DetailScreen
 */
class DetailViewModel(
    private val repo: NotesRepository,
    private val studentId: Long,
    private val courseId: Long
) : ViewModel() {

    // ── Datos del estudiante ──────────────────────────────────────────────────

    /**
     * Obtiene los datos de un estudiante por id como [Flow] reactivo.
     * Retorna null si el estudiante fue eliminado.
     */
    fun getStudentById(id: Long): Flow<Student?> = repo.getStudentByIdFlow(id)

    // ── Tareas ────────────────────────────────────────────────────────────────

    /**
     * Lista de todas las tareas globales como [StateFlow].
     * Se necesita para saber cuántas tareas existen al calcular el promedio,
     * contando como 0 las tareas sin nota asignada.
     */
    val tasks: StateFlow<List<Task>> =
        repo.getTasksByCourse(courseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Notas del estudiante ──────────────────────────────────────────────────

    /**
     * Lista de notas del estudiante como [StateFlow].
     * Se actualiza automáticamente cuando se guarda una nueva nota en Room.
     */
    val grades: StateFlow<List<Grade>> =
        repo.getGradesByStudent(studentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Promedio en tiempo real ───────────────────────────────────────────────

    /**
     * Promedio calculado en tiempo real combinando [grades] y [tasks].
     *
     * Lógica:
     * - Se divide la suma de notas entre el total de tareas (no entre las notas ingresadas)
     * - Las tareas sin nota asignada cuentan como 0 en el cálculo
     * - Esto penaliza al estudiante si no entregó todas las tareas
     *
     * Se actualiza automáticamente cada vez que cambian las notas o las tareas.
     */
    val promedio: StateFlow<Double> =
        combine(grades, tasks) { listaNotas, listaTareas ->
            val totalTareas = listaTareas.size
            if (totalTareas == 0) return@combine 0.0

            // Crea un mapa taskId → nota para búsqueda eficiente O(1)
            val mapaNotas = listaNotas.associateBy { it.taskId }
            val suma = listaTareas.sumOf { tarea ->
                mapaNotas[tarea.id]?.value ?: 0.0 // tarea sin nota cuenta como 0
            }
            suma / totalTareas
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ── Auto-guardado del promedio ────────────────────────────────────────────

    init {
        // IMPORTANTE: este init va después de la declaración de `promedio`
        // para que la propiedad ya esté inicializada cuando se ejecute.
        //
        // Auto-guarda el promedio en Student.average cada vez que cambia.
        // Esto mantiene MainScreen sincronizado automáticamente:
        // guardar notas → grades cambia en Room → promedio recalcula
        // → este collector persiste el nuevo valor → MainScreen se actualiza.
        // distinctUntilChanged evita escrituras redundantes si el valor no cambió.
        viewModelScope.launch {
            // StateFlow ya solo emite cuando el valor cambia, no se necesita distinctUntilChanged
            promedio.collect { nuevoPromedio ->
                repo.updateStudentAverage(studentId, nuevoPromedio)
            }
        }
    }

    // ── Mensajes de feedback ──────────────────────────────────────────────────

    /** Mensaje para mostrar al usuario después de guardar */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // ── Acciones ──────────────────────────────────────────────────────────────

    /**
     * Persiste la nota de una tarea específica para este estudiante.
     * Usa REPLACE, por lo que crea la nota si no existía o la actualiza si ya existía.
     *
     * @param taskId ID de la tarea a la que pertenece la nota
     * @param nota Valor numérico de la nota
     */
    fun updateNota(taskId: Long, nota: Double) {
        viewModelScope.launch {
            repo.updateNota(studentId, taskId, nota)
        }
    }

    /**
     * Se llama al presionar "Guardar" en la UI.
     * Las notas ya fueron persistidas en Room por [updateNota], lo que disparó
     * el recálculo reactivo del [promedio] y su auto-guardado vía el collector del init.
     * Aquí solo se muestra el mensaje de confirmación al usuario.
     */
    fun calcularPromedio() {
        _message.value = "Las notas se guardarón con éxito."
    }

    /** Elimina la nota de una tarea específica para este estudiante. */
    fun deleteGrade(taskId: Long) {
        viewModelScope.launch { repo.deleteGrade(studentId, taskId) }
    }

    /** Limpia el mensaje de feedback para que el Snackbar desaparezca */
    fun clearMessage() {
        _message.value = null
    }
}
