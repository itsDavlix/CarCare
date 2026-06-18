package com.example.carcare.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.carcare.R
import com.example.carcare.ui.viewmodel.AuthViewModel
import com.example.carcare.util.Validators

/**
 * Pantalla bloqueante del primer ingreso: el conductor entró con la contraseña
 * inicial (= su cédula, o la que le puso el admin) y debe cambiarla antes de usar
 * la app. No se puede saltar; el botón "atrás" cierra la sesión en vez de avanzar.
 */
@Composable
fun ForceChangePasswordScreen(
    authViewModel: AuthViewModel,
    cedula: String,
    onChanged: () -> Unit,
    onLogout: () -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val validation = Validators.validatePassword(nueva, confirm, current = actual)
    val transform = if (visible) VisualTransformation.None else PasswordVisualTransformation()

    // No se sale sin cambiar: el back cierra sesión y vuelve al login.
    BackHandler { onLogout() }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.force_pw_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.force_pw_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = actual,
                onValueChange = { actual = it; error = null },
                label = { Text(stringResource(R.string.pw_current)) },
                singleLine = true,
                visualTransformation = transform,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = attempted && actual.isBlank(),
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nueva,
                onValueChange = { nueva = it; error = null },
                label = { Text(stringResource(R.string.pw_new)) },
                singleLine = true,
                visualTransformation = transform,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = attempted && !validation.isValid,
                enabled = !submitting,
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) stringResource(R.string.action_hide) else stringResource(R.string.action_show)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it; error = null },
                label = { Text(stringResource(R.string.pw_confirm)) },
                singleLine = true,
                visualTransformation = transform,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = attempted && !validation.isValid,
                enabled = !submitting,
                supportingText = if (attempted && !validation.isValid) {
                    { Text(validation.errorMessage ?: "") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    attempted = true
                    error = null
                    if (actual.isBlank() || !validation.isValid || submitting) return@Button
                    submitting = true
                    authViewModel.changePassword(
                        cedula = cedula,
                        actual = actual,
                        nueva = nueva,
                        onSuccess = { submitting = false; onChanged() },
                        onError = { submitting = false; error = it }
                    )
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.force_pw_save))
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onLogout, enabled = !submitting) {
                Text(stringResource(R.string.force_pw_logout))
            }
        }
    }
}
