package com.example.pruebasubicacion

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyService", "Servicio iniciado")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "Servicio destruido")
    }
}
