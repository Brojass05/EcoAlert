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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Data
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

import com.example.pruebasubicacion.presentation.view.EcoAlertScreen
import com.example.pruebasubicacion.presentation.viewmodel.UbicacionViewModel
import com.example.pruebasubicacion.presentation.ui.notifications.showSimpleNotificationOpenActivity
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


        setContent {
            EcoAlertScreen(estado = vistaModelo.state)
        }

        // AUTOMATIZACIÓN: Disparamos la verificación de permisos y GPS apenas abre la app

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground
                obtenerUbicacionRealTime()
            }

            override fun onStop(owner: LifecycleOwner) {
                // App went to the background (User "exited" the UI)
                Log.d("MainActivity", "Enqueuing unique work onStop")
                //showSimpleNotificationOpenActivity(this@MainActivity,vistaModelo.state.lastPm,vistaModelo.state.lastPm)
                //showSimpleNotificationOpenActivity(this@MainActivity,87f,vistaModelo.state.lastPm)
                //showSimpleNotificationOpenActivity(this@MainActivity,(vistaModelo.state.lastPm)-20f,vistaModelo.state.lastPm)
                setPeriodicTimeWorkRequest(vistaModelo.state.lastPm)
                Log.i("MainActivity","Ultima PM: ${vistaModelo.state.lastPm}")
                Log.i("MainActivity","App is now in the background!")
            }
        })
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


    private fun setPeriodicTimeWorkRequest(lastPm: Float) {
        Log.d("MainActivity", "setPeriodicTimeWorkRequest called with lastPm: $lastPm")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PmCheckerWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(createInputDataForWorkRequest(lastPm))
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PM_CHECKER_WORKER_UNIQUE",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun createInputDataForWorkRequest(lastPm: Float): Data {
        return workDataOf(PmCheckerWorker.KEY_LAST_PM to lastPm)
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

