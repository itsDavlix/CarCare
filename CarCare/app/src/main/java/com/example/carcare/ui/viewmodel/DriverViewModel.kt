package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.network.toUserMessage
import com.example.carcare.data.repository.DriverRepository
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import kotlinx.coroutines.launch

@dagger.hilt.android.lifecycle.HiltViewModel
class DriverViewModel @javax.inject.Inject constructor(repository: DriverRepository) :
    BaseListViewModel<Driver, DriverRepository>(repository, "DriverVM") {

    val drivers: List<Driver> get() = items

    val filteredDrivers by derivedStateOf {
        items.filter { matchesQuery(it.fullName, it.idCardNumber) }
    }

    fun loadDrivers() = load()

    /** Al crear, el backend genera la cuenta de acceso; onSuccess muestra sus credenciales. */
    fun addDriver(driver: Driver, onSuccess: () -> Unit = {}) = create(driver, onSuccess)

    fun updateDriver(updatedDriver: Driver) =
        optimisticReplace(updatedDriver) { repository.update(updatedDriver) }

    fun deleteDriver(driverId: String) = optimisticDelete(driverId)

    fun updateStatus(driverId: String, newStatus: DriverStatus) {
        val current = items.firstOrNull { it.id == driverId } ?: return
        optimisticReplace(current.copy(status = newStatus)) {
            repository.changeStatus(driverId, newStatus)
        }
    }

    /** Restablece la contraseña de acceso del conductor (acción de admin desde su ficha). */
    fun resetPassword(
        driverId: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.resetPassword(driverId, newPassword)
                onSuccess()
            } catch (e: Exception) {
                onError(e.toUserMessage())
            }
        }
    }
}