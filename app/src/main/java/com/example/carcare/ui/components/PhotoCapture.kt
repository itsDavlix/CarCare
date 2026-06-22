package com.example.carcare.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

/** Comprime un Bitmap a JPEG base64 (chico, para evidencia). */
fun Bitmap.toBase64Jpeg(quality: Int = 50): String {
    val out = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}

/** Decodifica una imagen base64 a Bitmap (null si falla). */
fun String.base64ToBitmap(): Bitmap? = runCatching {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

/**
 * Toma una foto con la cámara y la entrega en base64 por [onCaptured]; muestra un thumbnail
 * de la tomada. Usa TakePicturePreview (no requiere el permiso CAMERA: lo maneja la app de cámara).
 */
@Composable
fun PhotoCaptureField(
    label: String,
    photoBase64: String?,
    onCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { onCaptured(it.toBase64Jpeg()) }
    }
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { launcher.launch(null) }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (photoBase64 == null) label else "Cambiar foto")
            }
            if (photoBase64 != null) {
                Spacer(Modifier.width(12.dp))
                val bmp = remember(photoBase64) { photoBase64.base64ToBitmap() }
                bmp?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Foto del combustible",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

/** Muestra una imagen base64 (o un texto si no decodifica). */
@Composable
fun Base64Image(base64: String, modifier: Modifier = Modifier) {
    val bmp = remember(base64) { base64.base64ToBitmap() }
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Foto",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
        Text("No se pudo cargar la foto.", style = MaterialTheme.typography.bodySmall)
    }
}
