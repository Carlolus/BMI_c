package com.example.imcc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bmi_history")
data class BmiHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Cambiado a String para UUID de Google
    val result: Double,
    val weight: Double,
    val height: Double,
    val date: Long = System.currentTimeMillis()
)