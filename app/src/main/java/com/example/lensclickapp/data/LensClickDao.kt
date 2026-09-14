package com.example.lensclickapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LensClickDao {
    @Query("SELECT * FROM photographers ORDER BY name")
    fun observePhotographers(): Flow<List<Photographer>>

    @Query("SELECT COUNT(*) FROM photographers")
    suspend fun photographerCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhotographers(photographers: List<Photographer>)

    @Insert
    suspend fun insertPhotographer(photographer: Photographer): Long

    @Query("SELECT * FROM budgets ORDER BY createdAt DESC, id DESC")
    fun observeBudgets(): Flow<List<Budget>>

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun budgetCount(): Int

    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudgets(budgets: List<Budget>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Transaction
    suspend fun insertPhotographerAccount(user: User, photographer: Photographer): Long {
        val userId = insertUser(user)
        if (userId == -1L) return -1L
        insertPhotographer(photographer.copy(userId = userId))
        return userId
    }

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findUser(email: String): User?
}
