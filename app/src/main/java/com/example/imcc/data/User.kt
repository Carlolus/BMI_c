package com.example.imcc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0, // Cambiado: autoGenerate = true eliminado, id fijo = 0
    val name: String
)