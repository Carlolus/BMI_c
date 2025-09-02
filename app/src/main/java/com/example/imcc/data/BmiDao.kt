package com.example.imcc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BmiDao {

    // ---- User ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Query("SELECT * FROM users WHERE id = 0 LIMIT 1") // Asumiendo que el ID 0 es para el único usuario
    fun getUser(): Flow<User?>

    // ---- BMI History ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BmiHistory)

    // Modificado para aceptar userId
    @Query("SELECT * FROM bmi_history WHERE userId = :userId ORDER BY date DESC")
    fun getHistory(userId: Int): Flow<List<BmiHistory>>
}