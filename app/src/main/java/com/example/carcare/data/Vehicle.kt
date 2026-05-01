package com.example.carcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val model: String,
    val year: Int,
    val engine: String,
    val plate: String,
    val mileage: Int,
    val fuelType: String,
    val color: String,
    val description: String
)
