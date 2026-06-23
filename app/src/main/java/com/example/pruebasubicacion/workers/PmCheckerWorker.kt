package com.example.pruebasubicacion.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pruebasubicacion.data.ClimaRepository
import com.example.pruebasubicacion.presentation.ui.notifications.showSimpleNotificationOpenActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

private const val TAG = "PmCheckerWorker"

class PmCheckerWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    companion object {
        const val KEY_LAST_PM = "lastPm"
    }

    private val repository = ClimaRepository()

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val lastPm = inputData.getFloat(KEY_LAST_PM, 0f)

        return try {
            Log.i(TAG, "Iniciando verificación de PM en segundo plano")
            val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

            // Usamos .await() para esperar la ubicación de forma suspendida (CoroutineWorker)
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                val newPm = repository.getPm25Only(location.latitude, location.longitude)
                
                // Mostrar notificación si hubo cambios
                showSimpleNotificationOpenActivity(appContext, newPm, lastPm)
                
                Result.success()
            } else {
                Log.e(TAG, "No se pudo obtener la ubicación")
                Result.retry() // Reintentar si falló la ubicación
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos: ${e.message}")
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error en el worker: ${e.message}", e)
            Result.failure()
        }
    }
}
