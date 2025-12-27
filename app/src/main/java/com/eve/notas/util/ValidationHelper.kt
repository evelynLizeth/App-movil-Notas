package com.eve.notas.util

object ValidationHelper {

    fun isValidName(
        name: String,
        existingNames: List<Pair<Long, String>>,
        currentId: Long? = null
    ): Boolean {
        if (name.isBlank()) return false
        val exists = existingNames.any { (id, existingName) ->
            existingName.equals(name, ignoreCase = true) && id != currentId
        }
        return !exists
    }
}