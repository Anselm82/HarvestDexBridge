package com.honeywell.usbakerydex

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.honeywell.usbakerydex.DexConnectionService.Companion.ACTION_DEX_FINISHED
import com.honeywell.usbakerydex.dex.model.DEFAULT_DEX_VERSION
import com.honeywell.usbakerydex.versatile.VersatileConverter
import com.honeywell.usbakerydex.versatile.model.VersatileDexMode
import com.honeywell.usbakerydex.versatile.utils.cleanUCS

class ServiceToCallActivity : Service() {



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        launchDEX()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("EXIT", "ondestroy!")
        val broadcastIntent = Intent(this, ServiceToCallActivityBroadcastReceiver::class.java)
        sendBroadcast(broadcastIntent)
        //stopTask()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    /*val br : BroadcastReceiver get() {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                launchDEX()
            }

        }
    }

    companion object {
        var index : Int = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerReceiver(br, IntentFilter())
        startForeground(1000, Notification())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(br)
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
        startForeground(Random.nextInt(), Notification())
        //setDexParams(intent, startId)
        Log.e("${index++}", "onStartCommand")
        return START_NOT_STICKY
    }
*/
    fun launchDEX() {
        val data = ""
        val intent = build(VersatileDexMode.ACTION_START_DEX, data)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        Log.i("DEX", "launchDEX")
    }

    private fun build(mode: VersatileDexMode, content: String? = null): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = DexConnectionService.SEND_TYPE
        intent.`package` = DexConnectionService.DEX_APP_PKG_NAME
        intent.putExtra(DexConnectionService.DEX_MODE, mode.value)
        if (!content.isNullOrBlank())
            intent.putExtra(Intent.EXTRA_TEXT, content)
        Log.i("DEX", "build")
        return intent
    }

}