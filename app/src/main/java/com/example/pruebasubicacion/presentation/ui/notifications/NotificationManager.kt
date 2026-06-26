package com.example.pruebasubicacion.presentation.ui.notifications

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pruebasubicacion.MainActivity


val TAG = "NotificationManager"


@Composable
fun NotificationButton(nivelPm: Float) {
    val context = LocalContext.current
    // 1. We configure the permission "requester"
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // The user said yes, we launch the notification
            showSimpleNotification(context)
        } else {
            // The user said no, you could show an informative Toast

        }
    }

}

fun showSimpleNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID_EJEMPLO")
        .setSmallIcon(R.drawable.ic_dialog_info) // Mandatory icon
        .setContentTitle("Hello!")
        .setContentText("This is a notification from Jetpack Compose")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Closes when touched
    with(NotificationManagerCompat.from(context)) {
        // ID 101 is unique to this notification (you can use it to update it later)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(101, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle the error: log it or notify the user
            Log.e("Notification", "Security error: missing permission", e)
        }
    }
}
fun showSimpleDebugNotification(context: Context, message: String) {
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID_EJEMPLO")
        .setSmallIcon(R.drawable.ic_dialog_info) // Mandatory icon
        .setContentTitle("DebugNotification")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Closes when touched
    with(NotificationManagerCompat.from(context)) {
        // ID 101 is unique to this notification (you can use it to update it later)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(100, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle the error: log it or notify the user
            Log.e("Notification", "Security error: missing permission", e)
        }
    }
}

@SuppressLint("SuspiciousIndentation")
fun showSimpleNotificationOpenActivity(
    context: Context, notId: Int = 102,
    newPm: Float, lastPm: Float?
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
    if(newPm>lastPM){
        notificacionSubida(builder,pendingIntent,newPm,lastPm)
    }else if(newPm<lastPM){
        notificacionBajada(builder,pendingIntent,newPm,lastPm)
    }else if(newPm==lastPm){
        //notificacionNoCambioSinContenido()
        notificacionNoCambio(builder,pendingIntent,newPm,lastPm)
        //return
    }

    // 4. LAUNCH (With permission check to avoid errors)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(102, builder.build())
    }
}
fun notificacionSubida(builder: NotificationCompat.Builder, pendingIntent: PendingIntent, newPm: Float, lastPm: Float?) {
    builder.setSmallIcon(R.drawable.ic_dialog_alert)
        .setContentTitle("Alerta!!!")
        //.setContentText("El nivel de contaminacion ha subido \nTen cuidado si vas a salir \nNueva: $newPm \nAnterior: $lastPm")
        .setContentText("Subio \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionBajada(builder: NotificationCompat.Builder, pendingIntent: PendingIntent, newPm: Float, lastPm: Float?) {
    builder.setSmallIcon(R.drawable.ic_dialog_info)
        .setContentTitle("Informacion")
        //.setContentText("El nivel de contaminacion ha bajado \nNueva: $newPm \nAnterior: $lastPm")
        .setContentText("Bajo \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionNoCambio(builder: NotificationCompat.Builder, pendingIntent:PendingIntent, newPm: Float, lastPm: Float ) {
    builder.setSmallIcon(R.drawable.ic_dialog_email)
        .setContentTitle("Informacion")
        //.setContentText("El nivel de contaminacion no han habido cambios \nNueva: $newPm \nAnterior: $lastPm")
        .setContentText("No Cambio \nNueva: $newPm \nAnterior: $lastPm")
        .setContentIntent(pendingIntent) // <--- Link the click with the destination
        .setAutoCancel(true) // Deleted when touched
}
fun notificacionNoCambioSinContenido( ) {
    Log.i(TAG, "No han habido cambios")

}





