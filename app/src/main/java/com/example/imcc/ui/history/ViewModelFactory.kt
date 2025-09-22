package com.example.imcc.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imcc.data.BmiDao

class ViewModelFactory(private val bmiDao: BmiDao, private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(bmiDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
