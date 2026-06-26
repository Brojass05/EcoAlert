package com.example.pruebasubicacion

import android.Manifest
import android.app.NotificationChannel
import android.util.Log
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager

import android.os.Build
import android.os.Bundle

import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pruebasubicacion.data.UserPreferences
import com.example.pruebasubicacion.data.dataStore

import com.example.pruebasubicacion.presentation.view.EcoAlertScreen
import com.example.pruebasubicacion.presentation.viewmodel.UbicacionViewModel
import com.example.pruebasubicacion.util.getTime
import com.example.pruebasubicacion.workers.PmCheckerWorker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // Instancia del ViewModel utilizando el delegado oficial de Android
    private val vistaModelo: UbicacionViewModel by viewModels { UbicacionViewModel.Factory }

    // Launcher para gestionar la solicitud de permisos de ubicación (Precisa y Coarse)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val postNotificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        if (
            fineLocationGranted ||
            coarseLocationGranted ||
            postNotificationGranted ||
            backgroundLocationGranted
            ) {
            obtenerUbicacionRealTime()
        } else {

            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()
        getTime()


        setContent {
            EcoAlertScreen(estado = vistaModelo.state)
        }

        // AUTOMATIZACIÓN: Disparamos la verificación de permisos y GPS apenas abre la app

        setPeriodicTimeWorkRequest()
        checkPermissionsAndGetLocation()
    }

    private fun checkPermissionsAndGetLocation() {
        val permissionsToRequest = mutableListOf<String>()

        val fineLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // Si falta alguno de los de ubicación frontal, los pedimos
        if (!fineLocationGranted || !coarseLocationGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Si ya tenemos los frontales, verificamos el de segundo plano (Background)
            // En Android 11+ (API 30), este permiso DEBE pedirse por separado de los frontales.
            // Nota: ACCESS_BACKGROUND_LOCATION requiere API 29+ (que es nuestro minSdk)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            } else {
                obtenerUbicacionRealTime()
            }
        }
    }


    private fun obtenerUbicacionRealTime() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            // Reemplazamos 'lastLocation' por 'getCurrentLocation' con prioridad de alta precisión
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val lat: Double = location.latitude
                    val lon: Double = location.longitude

                    // Enviamos las coordenadas nativas con todos sus decimales al ViewModel
                    vistaModelo.cargarClima(lat, lon)

                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener la ubicación actual. Verifica que tu GPS esté encendido.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de permisos de seguridad: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun setPeriodicTimeWorkRequest() {
        Log.d("MainActivity", "setPeriodicTimeWorkRequest called")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PmCheckerWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PM_CHECKER_WORKER_UNIQUE",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }


}

private fun createNotificationChannel(context: Context) {
    // Solo necesario para API 26+ (Nuestro minSdk es 29, así que siempre se ejecuta)
    val name = "My App Notifications"
    val descriptionText = "This channel is used for general alerts"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    // THE ID MUST BE THE SAME AS YOU USE IN THE BUILDER ("CHANNEL_ID_EJEMPLO")
    val channel = NotificationChannel("CHANNEL_ID_EJEMPLO", name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

