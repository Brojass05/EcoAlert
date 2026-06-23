package com.example.pruebasubicacion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.pruebasubicacion.presentation.ui.notifications.showSimpleNotification
import com.example.pruebasubicacion.presentation.view.EcoAlertScreen
import com.example.pruebasubicacion.presentation.viewmodel.UbicacionViewModel
import com.example.pruebasubicacion.workers.PmCheckerWorker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // Instancia del ViewModel utilizando el delegado oficial de Android
    private val vistaModelo: UbicacionViewModel by viewModels()

    // Launcher para gestionar la solicitud de permisos de ubicación (Precisa y Coarse)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        ) {
            obtenerUbicacionRealTime() // 👈 Cambiado a la función en tiempo real
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()

        setContent {
            EcoAlertScreen(estado = vistaModelo.state)
        }

        // AUTOMATIZACIÓN: Disparamos la verificación de permisos y GPS apenas abre la app
        checkPermissionsAndGetLocation()


        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground
                obtenerUbicacionRealTime()
            }

            override fun onStop(owner: LifecycleOwner) {
                // App went to the background (User "exited" the UI)
                showSimpleNotification(this@MainActivity)
                setPeriodicTimeWorkRequest(vistaModelo.state.lastPm)
                println("Ultima PM: ${vistaModelo.state.lastPm}")
                println("App is now in the background!")
            }
        })
    }

    /**
     * Verifica si la aplicación ya cuenta con los permisos necesarios del sistema.
     * Si no los tiene, los solicita al usuario.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionsAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION

                )
            )
        } else {
            obtenerUbicacionRealTime() // 👈 Cambiado a la función en tiempo real
        }
    }

    /**
     * 🛠️ SOLUCIÓN: Forza el encendido del GPS para buscar las coordenadas exactas actuales
     * evitando usar el caché antiguo que te movía a Providencia.
     */
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


    private fun setPeriodicTimeWorkRequest(lastPm: Float) {


        val workRequest = PeriodicWorkRequestBuilder<PmCheckerWorker>(15, TimeUnit.MINUTES)
            .setInputData(createInputDataForWorkRequest(lastPm))
            .build()



        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
    private fun createInputDataForWorkRequest(lastPm: Float): Data {
        return workDataOf(PmCheckerWorker.KEY_LAST_PM to lastPm)
    }
}

private fun createNotificationChannel(context: Context) {
    // Only necessary for API 26+ (Android 8.0)
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

