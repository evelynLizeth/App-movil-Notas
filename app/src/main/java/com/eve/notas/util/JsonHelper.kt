package com.eve.notas.util

import com.eve.notas.data.model.Student
import com.google.gson.Gson

fun List<Student>.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}