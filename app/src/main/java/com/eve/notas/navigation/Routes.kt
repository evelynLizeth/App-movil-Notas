package com.eve.notas.navigation

/**
 * Centraliza todas las rutas de navegación de la app.
 * Usar estas constantes evita strings hardcodeados dispersos en NavGraph
 * y facilita detectar errores de tipeo en tiempo de compilación.
 */
object Routes {

    // ── Autenticación ─────────────────────────────────────────────────────────

    /** Pantalla de inicio de sesión. Destino inicial de la app */
    const val LOGIN = "login"

    /** Pantalla de registro de nuevo usuario */
    const val REGISTER = "register"

    /** Pantalla de cambio de contraseña obligatorio. El argumento {userId} es de tipo Long */
    const val CHANGE_PASSWORD = "change_password/{userId}"

    // ── Instituciones y cursos ────────────────────────────────────────────────

    /** Pantalla de lista de instituciones del usuario. El argumento {userId} es de tipo Long */
    const val INSTITUTIONS = "institutions/{userId}"

    /** Pantalla de lista de cursos de una institución. El argumento {institutionId} es de tipo Long */
    const val COURSES = "courses/{institutionId}"

    // ── Pantallas principales ─────────────────────────────────────────────────

    /** Pantalla principal: lista de estudiantes con su promedio general */
    const val MAIN = "main"

    /** Pantalla de detalle: notas editables de un estudiante.
     *  El argumento {studentId} es de tipo Long y se pasa en la URL. */
    const val DETAIL = "detail/{studentId}"

    /** Pantalla de gestión de tareas globales */
    const val TASKS = "tasks"

    // ── Funciones generadoras de rutas ────────────────────────────────────────

    /**
     * Genera la ruta concreta para navegar al cambio de contraseña de un usuario.
     * @param userId ID del usuario en la base de datos
     */
    fun changePasswordRoute(userId: Long) = "change_password/$userId"

    /**
     * Genera la ruta concreta para navegar a las instituciones de un usuario.
     * @param userId ID del usuario en la base de datos
     */
    fun institutionsRoute(userId: Long) = "institutions/$userId"

    /**
     * Genera la ruta concreta para navegar a los cursos de una institución.
     * @param institutionId ID de la institución en la base de datos
     */
    fun coursesRoute(institutionId: Long) = "courses/$institutionId"

    /**
     * Genera la ruta concreta para navegar al detalle de un estudiante.
     * @param studentId ID del estudiante en la base de datos
     */
    fun detailRoute(studentId: Long) = "detail/$studentId"
}
