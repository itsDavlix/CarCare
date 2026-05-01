package com.example.carcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.Expense
import com.example.carcare.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: VehicleRepository) : ViewModel() {

    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> = 
        repository.getExpensesForVehicle(vehicleId)
    
    fun getTotalExpenses(vehicleId: Int): Flow<Double?> = 
        repository.getTotalExpenses(vehicleId)
    
    fun insertExpense(expense: Expense) = viewModelScope.launch { 
        repository.insertExpense(expense) 
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch { 
        repository.updateExpense(expense) 
    }
    
    fun deleteExpense(expense: Expense) = viewModelScope.launch { 
        repository.deleteExpense(expense) 
    }
}

class ExpenseViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
