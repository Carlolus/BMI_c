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

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUser(uid: String): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    // ---- BMI History ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BmiHistory)

    @Query("SELECT * FROM bmi_history WHERE userId = :userId ORDER BY date DESC")
    fun getHistory(userId: String): Flow<List<BmiHistory>>
}