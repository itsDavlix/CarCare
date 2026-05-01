package com.example.carcare.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE vehicleId = :vehicleId")
    fun getTotalExpenses(vehicleId: Int): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
