package com.example.pruebasubicacion.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pruebasubicacion.data.ClimaRepository
import com.example.pruebasubicacion.presentation.ui.notifications.showSimpleNotificationOpenActivity
import com.example.pruebasubicacion.data.UserPreferences
import com.example.pruebasubicacion.data.dataStore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.IOException

private const val TAG = "PmCheckerWorker"

class PmCheckerWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val repository = ClimaRepository()
    private val userPreferences = UserPreferences(ctx.applicationContext.dataStore)

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

        return try {
            Log.i(TAG, "Iniciando verificación de PM en segundo plano")

            // Leer el valor guardado en DataStore
            val lastPm = userPreferences.lastPmFlow.first()

            // Obtener ubicación actual
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                val newPm = repository.getPm25Only(location.latitude, location.longitude)

                // Guardar el nuevo valor en DataStore
                userPreferences.saveLastPm(newPm)

                // Mostrar notificación si hubo cambios
                showSimpleNotificationOpenActivity(
                    context = appContext,
                    newPm = newPm,
                    lastPm = lastPm,
                    userPreferences = userPreferences
                )

                Result.success()
            } else {
                Log.e(TAG, "No se pudo obtener la ubicación")
                Result.retry()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos: ${e.message}")
            Result.failure()
        } catch (e: CancellationException) {
            Log.i(TAG, "Worker cancelado")
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Error de red en el worker (reintentando): ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en el worker: ${e.message}", e)
            Result.failure()
        }
    }
}
