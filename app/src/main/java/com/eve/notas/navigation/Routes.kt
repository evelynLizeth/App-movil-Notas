package com.eve.notas.navigation

/**
 * Centraliza todas las rutas de navegación de la app.
 * Usar estas constantes evita strings hardcodeados dispersos en NavGraph
 * y facilita detectar errores de tipeo en tiempo de compilación.
 */
object Routes {

    /** Pantalla principal: lista de estudiantes con su promedio general */
    const val MAIN = "main"

    /** Pantalla de detalle: notas editables de un estudiante.
     *  El argumento {studentId} es de tipo Long y se pasa en la URL. */
    const val DETAIL = "detail/{studentId}"

    /** Pantalla de gestión de tareas globales */
    const val TASKS = "tasks"

    /**
     * Genera la ruta concreta para navegar al detalle de un estudiante.
     * @param studentId ID del estudiante en la base de datos
     */
    fun detailRoute(studentId: Long) = "detail/$studentId"
}
