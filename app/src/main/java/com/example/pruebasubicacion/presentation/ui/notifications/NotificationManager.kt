package com.example.pruebasubicacion.presentation.ui.notifications

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pruebasubicacion.data.UserPreferences
import com.example.pruebasubicacion.MainActivity
import com.example.pruebasubicacion.util.ChequeadorBackground
val TAG = "NotificationManager"

suspend fun showSimpleNotification(
    context: Context, id: Int = 111,
    titulo: String = "Notificacion", mensaje: String,
    idNoti:String = "n1", userPreferences: UserPreferences) {
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID_EJEMPLO")
        .setSmallIcon(R.drawable.ic_dialog_info) // Mandatory icon
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Closes when touched
    with(NotificationManagerCompat.from(context)) {
        // ID 101 is unique to this notification (you can use it to update it later)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(id, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle the error: log it or notify the user
            Log.e("Notification", "Security error: missing permission", e)
        }
    }
    userPreferences.saveLastSetNoti(idNoti)

}


suspend fun showSimpleNotificationOpenActivity(
    context: Context, notId: Int = 102,
    newPm: Float, lastPm: Float?,
    userPreferences: UserPreferences // Pasamos las preferencias para poder guardar
) {
    Log.i("Notification", "New PM: $newPm, Last PM: $lastPm")
    val lastPM = lastPm ?: return
    // 1. THE DESTINATION: This is where you specify MenuActivity
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    // 2. THE PENDING INTENT: The "permission" for the system to open the activity
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE // Mandatory in modern Android
    )
    // 3. BUILD THE NOTIFICATION
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID_EJEMPLO")
    if(ChequeadorBackground.segundoPlano){
        Log.i("DebugTag","Si estas en segundo plano")
        if(newPm>lastPM){
            notificacionSubida(builder,pendingIntent,newPm,lastPm)
            userPreferences.saveLastSetNoti("n1")

        }else if(newPm<lastPM){
            notificacionBajada(builder,pendingIntent,newPm,lastPm)
            userPreferences.saveLastSetNoti("n2")
        }else {
            //notificacionNoCambioSinContenido()
            //notificacionNoCambio(builder,pendingIntent,newPm,lastPm)
            userPreferences.saveLastSetNoti("n3")
            return
        }

    }else{
        Log.i("DebugTag","No estas en segundo plano")
        return
    }

    // 4. LAUNCH (With permission check to avoid errors)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(notId, builder.build())
    }
}
fun notificacionSubida(builder: NotificationCompat.Builder, pendingIntent: PendingIntent, newPm: Float, lastPm: Float?) {
    builder.setSmallIcon(R.drawable.ic_dialog_alert)
        .setContentTitle("Alerta!!!")
        .setContentText("El nivel de contaminacion ha subido \nTen cuidado si vas a salir")
        //.setContentText("Subio \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionBajada(builder: NotificationCompat.Builder, pendingIntent: PendingIntent, newPm: Float, lastPm: Float?) {
    builder.setSmallIcon(R.drawable.ic_dialog_info)
        .setContentTitle("Informacion")
        .setContentText("El nivel de contaminacion ha bajado")
        //.setContentText("Bajo \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionNoCambio(builder: NotificationCompat.Builder, pendingIntent:PendingIntent, newPm: Float, lastPm: Float ) {
    builder.setSmallIcon(R.drawable.ic_dialog_email)
        .setContentTitle("Informacion")
        .setContentText("El nivel de contaminacion no han habido cambios")
        //.setContentText("No Cambio \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionNoCambioSinContenido( ) {
    Log.i(TAG, "No han habido cambios")

}

// Esta función ya no es necesaria si llamamos directamente a userPreferences
// Pero si la quieres mantener, debería ser suspend y recibir las dependencias:
suspend fun saveLastSetNoti(userPreferences: UserPreferences, id: String) {
    userPreferences.saveLastSetNoti(id)
}






