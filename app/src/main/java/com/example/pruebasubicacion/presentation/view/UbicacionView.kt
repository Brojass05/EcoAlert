package com.example.pruebasubicacion.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebasubicacion.data.model.ClimaEstado
import com.example.pruebasubicacion.data.model.ClimaModel
import com.example.pruebasubicacion.data.model.HourlyData
import com.example.pruebasubicacion.util.Log // 👈 Mantenemos únicamente tu Log personalizado

@Composable
fun UbicacionView(
    estado: ClimaEstado,
    onFetchLocation: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la aplicación
            Text(
                text = "Calidad del Aire",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Control de flujo según el estado de la pantalla
            // Control de flujo según el estado de la pantalla
            when {
                estado.estaCargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                estado.mensajeError != null -> {
                    ErrorCard(mensaje = estado.mensajeError, onRetry = onFetchLocation)
                }
                estado.clima != null -> {
                    ClimaContent(
                        clima = estado.clima,
                        nombreUbicacion = estado.nombreUbicacion ?: "Ubicación Desconocida"
                    )
                }
                else -> {
                    // 👈 AUTOMATIZACIÓN: Si el estado está vacío (al abrir la app),
                    // mostramos la carga de inmediato en lugar del botón viejo.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ClimaContent(clima: ClimaModel, nombreUbicacion: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Tarjeta de información principal (Comuna + Valor Actual)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre de la Comuna o Ciudad obtenido de Nominatim
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = nombreUbicacion,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Coordenadas en formato secundario y discreto
                Text(
                    text = "Lat: ${clima.latitude}, Lon: ${clima.longitude}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // 👈 CORREGIDO: Se adapta para usar tu función Log(mensaje) personalizada
                val msg = "Lat: ${clima.latitude}, Lon: ${clima.longitude} | Lugar: $nombreUbicacion"
                Log(mensaje = msg)

                Spacer(Modifier.height(24.dp))

                // Valor actual de PM2.5 (Tomamos el primero de la lista horaria)
                val currentPm25 = clima.hourly.pm25?.firstOrNull() ?: 0f
                Text(
                    text = "$currentPm25",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "PM2.5 (µg/m³)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Subtítulo del listado de pronósticos
        Text(
            text = "Pronóstico por Hora",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        // Lista de las próximas horas
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(clima.hourly.time) { index, time ->
                val value = clima.hourly.pm25?.getOrNull(index) ?: 0f
                // Se recorta la fecha para mostrar solo la hora (ej: 14:00)
                HourlyItem(time = time.substringAfter("T"), value = value)
            }
        }
    }
}

@Composable
fun HourlyItem(time: String, value: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = time, color = Color.White)
            Text(
                text = "$value µg/m³",
                color = if (value > 50) Color.Yellow else Color.Cyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorCard(mensaje: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reintentar", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UbicacionViewPreview() {
    val mockClima = ClimaModel(
        latitude = -33.45,
        longitude = -70.66,
        hourly = HourlyData(
            time = listOf("2026-06-17T10:00", "2026-06-17T11:00", "2026-06-17T12:00"),
            pm25 = listOf(12.5f, 15.2f, 48.8f),
            pm10 = null,
            nitrogenDioxide = null,
            ozone = null,
            sulphurDioxide = null,
            carbonMonoxide = null,
            uvIndex = null,
            humidity = null,
            temperature = null
        )
    )
    UbicacionView(
        estado = ClimaEstado(
            clima = mockClima,
            nombreUbicacion = "Santiago Centro",
            estaCargando = false,
            mensajeError = null
        ),
        onFetchLocation = {}
    )
}