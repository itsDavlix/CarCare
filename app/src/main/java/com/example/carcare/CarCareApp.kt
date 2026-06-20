package com.example.carcare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application con Hilt: punto de entrada del grafo de dependencias. */
@HiltAndroidApp
class CarCareApp : Application()
