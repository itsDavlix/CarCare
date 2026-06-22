package com.example.carcare.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provee la base local (Room) y sus DAOs para el caché offline-first. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CarCareDatabase =
        Room.databaseBuilder(context, CarCareDatabase::class.java, "carcare-cache.db")
            // Es un caché desechable: si cambia el esquema, se reconstruye (los datos se re-bajan de la API).
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideVehiculoDao(db: CarCareDatabase): VehiculoDao = db.vehiculoDao()

    @Provides
    fun provideConductorDao(db: CarCareDatabase): ConductorDao = db.conductorDao()

    @Provides
    fun provideMantenimientoDao(db: CarCareDatabase): MantenimientoDao = db.mantenimientoDao()

    @Provides
    fun provideAsignacionDao(db: CarCareDatabase): AsignacionDao = db.asignacionDao()
}
