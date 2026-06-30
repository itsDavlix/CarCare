package com.example.carcare

import com.example.carcare.util.Validators
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fija el contrato de Validators.validatePassword (usado por el cambio de contraseña). */
class ValidatorsPasswordTest {

    @Test
    fun corta_esInvalida() {
        assertFalse(Validators.validatePassword("123", "123").isValid)
    }

    @Test
    fun noCoinciden_esInvalida() {
        assertFalse(Validators.validatePassword("secreta1", "secreta2").isValid)
    }

    @Test
    fun validaYCoincide_esValida() {
        assertTrue(Validators.validatePassword("secreta1", "secreta1").isValid)
    }
}
