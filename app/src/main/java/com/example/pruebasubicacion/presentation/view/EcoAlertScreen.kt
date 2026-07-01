package com.example.pruebasubicacion.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebasubicacion.R
import com.example.pruebasubicacion.data.model.ClimaEstado
import com.example.pruebasubicacion.presentation.ui.notifications.showSimpleNotification
import com.example.pruebasubicacion.util.sumarHorasUtc
import com.example.pruebasubicacion.data.UserPreferences
import com.example.pruebasubicacion.data.dataStore
import com.example.pruebasubicacion.presentation.ui.notifications.TAG
import com.example.pruebasubicacion.util.Log
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Paleta de Colores Suavizada ---
val EcoGreen = Color(0xFF2D9CDB) // Azul suave para la base si no hay riesgo

//val EcoBrandGreen = Color(0xFF109D48)
val EcoBrandGreen = Color(0xFF0837A9)

// Escala ICA con tonalidades menos brillantes (más pasteles/mates)
val ColorIcaBueno = Color(0xFF52C41A)
val ColorIcaModerado = Color(0xF2FCDA00)
val ColorIcaSensible = Color(0xFFFA8C16)
val ColorIcaSalud = Color(0xFFF5222D)
val ColorIcaMuyDanino = Color(0xFF722ED1)
val ColorIcaPeligroso = Color(0xFF820014)

val EcoTextDark = Color(0xFF1E293B)
val EcoTextMuted = Color(0xFF64748B)

data class RiskInfo(
    val category: String,
    val color: Color,
    val message: String
)

fun getRiskInfo(pm25: Float): RiskInfo {
    return when {
        pm25 <= 12.0f -> RiskInfo(
            "Bueno",
            ColorIcaBueno,
            "La calidad del aire es satisfactoria y el riesgo es mínimo."
        )

        pm25 <= 35.4f -> RiskInfo(
            "Mala",
            ColorIcaModerado,
            "Calidad aceptable, representa riesgo leve para personas sensibles."
        )

        pm25 <= 55.4f -> RiskInfo(
            "Riesgosa",
            ColorIcaSensible,
            "Grupos sensibles pueden experimentar problemas de salud."
        )

        pm25 <= 150.4f -> RiskInfo(
            "Insalubre",
            ColorIcaSalud,
            "Riesgo aumentado de efectos respiratorios y cardíacos."
        )

        pm25 <= 250.4f -> RiskInfo(
            "Muy dañino",
            ColorIcaMuyDanino,
            "Alerta de salud; es probable que todos se vean afectados."
        )

        else -> RiskInfo(
            "Peligroso",
            ColorIcaPeligroso,
            "Situación de emergencia. Riesgos severos para todos."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoAlertScreen(
    estado: ClimaEstado = ClimaEstado()
) {

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBackgroundColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }

    val currentGradient = Brush.verticalGradient(listOf(EcoBrandGreen, Color(0xFF0837A9)))

    CompositionLocalProvider(LocalContentColor provides textColor) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = backgroundColor
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderSection(
                    context = context,
                    selectedTab = selectedTab,
                    currentGradient = currentGradient,
                    onTabSelected = { selectedTab = it },
                    onNotificationClick = { showNotifications = true }
                )

                if (showNotifications) {
                    NotificationsView(onClose = { showNotifications = false })
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Card Principal con orden corregido (Ubicación -> PM2.5 -> Mensaje)
                        ConditionsCardMinimal(estado)

                        if (selectedTab == 0) {
                            // Pronóstico 24h segmentado
                            HourlyForecast24h(estado, cardBackgroundColor, textColor)
                            // Pronóstico semanal (tipo clima)
                            WeeklyForecast(estado, cardBackgroundColor, textColor)
                        } else {
                            // Vista Sensible con gráfico completo
                            MainPollutantsCardExtended(
                                estado,
                                cardBackgroundColor,
                                textColor,
                                textMutedColor
                            )
                        }

                        // Mapa Interactivo
                        //InteractivePollutionMap(estado.clima?.latitude, estado.clima?.longitude)
                    }
                }


            }
        }
    }
}

@Composable
fun HeaderSection(
    context: Context,
    selectedTab: Int,
    currentGradient: Brush,
    onTabSelected: (Int) -> Unit,
    onNotificationClick: () -> Unit
) {
    val userPreferences = remember { UserPreferences(context.applicationContext.dataStore) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(currentGradient)
            .padding(top = 20.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ECO-ALERT",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Monitoreo Calidad de aire",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.background(Color.White.copy(0.15f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Notifications, "Notif", tint = Color.White)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            showSimpleNotification(
                                context,
                                id = 101,
                                titulo = "Alerta!!!",
                                mensaje = "El nivel de contaminacion ha subido \nTen cuidado si vas a salir",
                                idNoti = "n1",
                                userPreferences = userPreferences
                            )
                            showSimpleNotification(
                                context,
                                id = 201,
                                titulo = "Información",
                                mensaje = "El nivel de contaminacion ha bajado",
                                idNoti = "n2",
                                userPreferences = userPreferences
                            )
                        }

                    }, modifier = Modifier.background(Color.White.copy(0.15f), CircleShape)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eco1_stats), // Ahora sí existe en drawable
                            contentDescription = "Stats",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(0.12f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItemMinimal(
                    "Deportista",
                    Icons.AutoMirrored.Outlined.TrendingUp,
                    selectedTab == 0,
                    { onTabSelected(0) },
                    Modifier.weight(1f)
                )
                TabItemMinimal(
                    "Sensible/Asma",
                    Icons.Outlined.FavoriteBorder,
                    selectedTab == 1,
                    { onTabSelected(1) },
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TabItemMinimal(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                null,
                tint = if (isSelected) EcoBrandGreen else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                title,
                color = if (isSelected) EcoBrandGreen else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}


@Composable
fun ConditionsCardMinimal(estado: ClimaEstado) {

    var pm25 = estado.clima?.hourly?.pm25?.firstOrNull() ?: 0f // Default obtiene valor automaticamente
    val risk = getRiskInfo(pm25)
    val contentColor = Color.White
    var cardColor by remember(risk.color) { mutableStateOf(risk.color) }
    var categoria = ""
    var mensaje = ""


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // 1. UBICACIÓN
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Place,
                    null,
                    tint = contentColor.copy(0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    estado.nombreUbicacion ?: "Cargando...",
                    color = contentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. NIVEL PM2.5 (Más grande)
            Column {
                val listaPm25 = listOf<Float>(pm25,10f,35f,55f,150f,250f,300f)
                var currentIndex by remember { mutableStateOf(0) }

                // Funcion de desarrollador exclusiva para presentacion
                TextButton(
                    onClick = {

                        // Cycles through the list 0 -> 1 -> 2 -> 0 -> 1...
                        currentIndex = (currentIndex + 1) % listaPm25.size
                        pm25 = listaPm25[currentIndex]
                        mensaje = getRiskInfo(pm25).message
                        categoria = getRiskInfo(pm25).category
                        cardColor = getRiskInfo(pm25).color



                    }
                ) {
                    Text(text = "${listaPm25[currentIndex]} µg/m³",
                        color = contentColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black)
                }

                // Texto Original
                /*Text(
                    "${pm25.toInt()} µg/m³",
                    color = contentColor,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )*/

                Text(

                    risk.category.uppercase(),
                    color = contentColor.copy(0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }


            // 3. MENSAJE (Tamaño normal)
            Text(
                risk2.message,
                color = contentColor.copy(0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Indicadores inferiores
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val hum = estado.clima?.hourly?.humidity?.firstOrNull() ?: 0f
                val temp = estado.clima?.hourly?.temperature?.firstOrNull() ?: 0f
                var co2 = estado.clima?.hourly?.carbonDioxide?.firstOrNull() ?: 0f
                co2 /= 10


                IndicatorSmall(
                    Icons.Outlined.WaterDrop,
                    "Humedad",
                    "${hum.toInt()}%",
                    contentColor,
                    Modifier.weight(1f)
                )
                IndicatorSmall(
                    Icons.Outlined.Thermostat,
                    "Temp.",
                    "${temp.toInt()}°C",
                    contentColor,
                    Modifier.weight(1f)
                )
                IndicatorSmall(
                    Icons.Outlined.Air,
                    "CO\u2082",
                    "${co2.toInt()}%",
                    contentColor,
                    Modifier.weight(1f)
                )
            }
        }

    }

}

@Composable
fun IndicatorSmall(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier
) {
    Box(modifier
        .clip(RoundedCornerShape(16.dp))
        .background(color.copy(0.15f))
        .padding(10.dp)) {
        Column {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, color = color.copy(0.7f), fontSize = 10.sp)
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HourlyForecast24h(estado: ClimaEstado, cardBg: Color, textColor: Color) {
    val clima = estado.clima ?: return
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(cardBg)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Próximas 24 Horas", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Segmentado exactamente por las próximas 24 horas desde ahora
                val currentHour = LocalDateTime.now().hour
                val horas = sumarHorasUtc(clima.hourly.time, currentHour)
                //val data = clima.hourly.time.zip(clima.hourly.pm25 ?: emptyList()).drop(currentHour).take(24)
                val data = horas.zip(clima.hourly.pm25 ?: emptyList()).take(24)
                //val data = clima.hourly.time.zip(clima.hourly.pm25 ?: emptyList()).take(24)

                items(data) { (time, pm) ->
                    val icon = getRiskIconForPM25(pm)
                    val risk = getRiskInfo(pm)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            time.substringAfter("T").take(5),
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = risk.color,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .size(24.dp)
                        )
                        Text("${pm.toInt()} µg/m³", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyForecast(estado: ClimaEstado, cardBg: Color, textColor: Color) {
    val clima = estado.clima ?: return
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(cardBg)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Pronóstico Próximos Días", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))

            val hourlyTimes = clima.hourly.time
            val pm25Values = clima.hourly.pm25 ?: emptyList()

            // Agrupar todos los datos disponibles por día
            val dayGroups = hourlyTimes.indices.groupBy { index ->
                hourlyTimes[index].substringBefore("T")
            }.values.toList()

            dayGroups.forEachIndexed { i, indices ->
                val dayPm = indices.map { pm25Values.getOrNull(it) ?: 0f }
                val avg = if (dayPm.isNotEmpty()) dayPm.average().toFloat() else 0f
                val date = LocalDateTime.parse(
                    hourlyTimes[indices.first()],
                    DateTimeFormatter.ISO_DATE_TIME
                )
                val label = if (i == 0) "Hoy" else date.format(
                    DateTimeFormatter.ofPattern(
                        "EEE",
                        Locale("es")
                    )
                )
                val risk = getRiskInfo(avg)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label.uppercase(),
                        Modifier.width(60.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = getRiskIconForPM25(avg),
                            contentDescription = null,
                            tint = risk.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("${avg.toInt()} µg/m³", color = textColor.copy(0.7f), fontSize = 12.sp)
                    }
                    Box(Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(risk.color))
                }
            }
        }
    }
}

@Composable
fun InteractivePollutionMap(lat: Double?, lon: Double?) {
    if (lat == null || lon == null) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(
                0xFF1E293B
            ) else Color(0xFFE2E8F0)
        )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Map,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = LocalContentColor.current.copy(0.3f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Espacio reservado para mapa",
                    color = LocalContentColor.current.copy(0.5f),
                    fontSize = 14.sp
                )
            }
        }
    }
    Text(
        "Visualización de mapa próximamente",
        fontSize = 11.sp,
        color = EcoTextMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
fun MainPollutantsCardExtended(
    estado: ClimaEstado,
    cardBg: Color,
    textColor: Color,
    textMuted: Color
) {
    val h = estado.clima?.hourly ?: return
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(cardBg)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Detalle de Contaminantes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            val list = listOf(
                "PM2.5" to (h.pm25?.firstOrNull() ?: 0f) to 250f,
                "PM10" to (h.pm10?.firstOrNull() ?: 0f) to 500f,
                "NO2" to (h.nitrogenDioxide?.firstOrNull() ?: 0f) to 200f,
                "O3" to (h.ozone?.firstOrNull() ?: 0f) to 180f,
                "SO2" to (h.sulphurDioxide?.firstOrNull() ?: 0f) to 350f,
                "CO" to (h.carbonMonoxide?.firstOrNull() ?: 0f) to 10000f
            )
            list.forEach { (data, max) ->
                val (name, value) = data
                PollutantRowMinimal(name, value, max, textColor, textMuted)
            }
        }
    }
}

@Composable
fun PollutantRowMinimal(
    name: String,
    value: Float,
    maxValue: Float,
    textColor: Color,
    textMuted: Color
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    val barColor = if (name == "PM2.5") getRiskInfo(value).color else EcoBrandGreen

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = textMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                "${value.toInt()}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFF1F5F9),
        )
    }
}

fun getRiskIconForPM25(pm25: Float): ImageVector = when {
    pm25 <= 12.0f -> Icons.Outlined.CheckCircle
    pm25 <= 35.4f -> Icons.Outlined.Info
    pm25 <= 55.4f -> Icons.Outlined.WarningAmber
    else -> Icons.Outlined.Dangerous
}

@Composable
fun CriticalAlertCard(estado: ClimaEstado) {
    val pm25 = estado.clima?.hourly?.pm25?.firstOrNull() ?: 0f
    val risk = getRiskInfo(pm25)
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(risk.color.copy(0.1f))
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Warning, null, tint = risk.color, modifier = Modifier.size(32.dp))
            Column {
                Text(
                    "RECOMENDACIÓN",
                    color = risk.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    risk.message,
                    color = EcoTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NotificationsView(onClose: () -> Unit) {
    val context = LocalContext.current
    // 1. Cargamos las preferencias (se mantiene mientras la ventana esté abierta)
    val userPreferences = remember { UserPreferences(context.applicationContext.dataStore) }

    // 2. collectAsState garantiza que CADA VEZ que abras la ventana,
    // se obtenga lo último que haya en el disco (DataStore).
    val sendedNotifications by userPreferences.sendedNotificationFlow.collectAsState(initial = emptyList())

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notificaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
        }

        // 3. Si la lista obtenida está vacía, mostramos un mensaje informativo
        if (sendedNotifications.isEmpty()) {
            Box(Modifier
                .fillMaxWidth()
                .padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones nuevas", color = EcoTextMuted)
            }
        } else {
            // 4. Si hay contenido, iteramos.
            for (id in sendedNotifications) {
                when (id) {
                    "n1" -> NotificationItemMinimal(
                        "Alerta!!!",
                        "El nivel de contaminacion ha subido \nTen cuidado si vas a salir"
                    )
                    "n2" -> NotificationItemMinimal(
                        "Información",
                        "El nivel de contaminación ha bajado"
                    )
                    "n3" -> NotificationItemMinimal(
                        "Información",
                        "El nivel de contaminación es el mismo"
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItemMinimal(title: String, desc: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 13.sp, color = EcoTextMuted)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EcoAlertScreenPreview() {
    EcoAlertScreen()
}
