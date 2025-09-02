package com.example.imcc.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imcc.data.BmiDao
import com.example.imcc.data.BmiHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(private val bmiDao: BmiDao) : ViewModel() {
    val historyUiState: StateFlow<List<BmiHistory>> =
        bmiDao.getHistory(userId = 0)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}