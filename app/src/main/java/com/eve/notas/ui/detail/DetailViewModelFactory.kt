package com.eve.notas.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eve.notas.data.repository.NotesRepository

class DetailViewModelFactory(
    private val repo: NotesRepository,
    private val studentId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repo, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}