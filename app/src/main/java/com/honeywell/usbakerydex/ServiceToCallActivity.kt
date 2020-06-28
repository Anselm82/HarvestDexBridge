package com.honeywell.usbakerydex

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.IBinder
import android.util.Log
import com.honeywell.usbakerydex.dex.model.DEXTransmission
import com.honeywell.usbakerydex.honeywell.HoneywellParser
import org.json.JSONObject
import java.lang.Exception

class ServiceToCallActivity : Service() {

    companion object {
        var index : Int = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1000, Notification())
    }
    private fun setDexParams(intent: Intent?, i: Int) {
        try {
            val stringExtra: String = intent!!.getStringExtra(DexConnectionService.CALLER_APP_ID)!!
            val launcherIntent = Intent(application, MainActivity::class.java)
            launcherIntent.flags = FLAG_ACTIVITY_NEW_TASK
            launcherIntent.putExtra(DexConnectionService.CALLER_APP_ID, stringExtra)
            launcherIntent.putExtra("JSON",intent.getStringExtra("JSON"))
            startActivity(launcherIntent)
            Log.e("${index++}", "set dex params")
        } catch (e: Exception) {
            Log.e("CALLING", e.message)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(1000, Notification())
        setDexParams(intent, startId)
        Log.e("${index++}", "onStartCommand")
        return START_NOT_STICKY
    }


}