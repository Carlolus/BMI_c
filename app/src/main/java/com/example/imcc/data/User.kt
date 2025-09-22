package com.example.imcc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String, // UUID de Google
    val name: String,
    val email: String? = null
)