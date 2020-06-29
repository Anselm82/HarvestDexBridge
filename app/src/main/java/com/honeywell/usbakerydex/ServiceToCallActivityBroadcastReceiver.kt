package com.honeywell.usbakerydex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.honeywell.usbakerydex.versatile.model.VersatileDexMode

class ServiceToCallActivityBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(
            ServiceToCallActivityBroadcastReceiver::class.java.simpleName,
            "Service Stops! Oooooooooooooppppssssss!!!!"
        )
        launchDEX(context)
        context!!.startService(Intent(context, ServiceToCallActivity::class.java))

    }

    fun launchDEX(context: Context?) {
        val data = ""
        val intent = build(VersatileDexMode.ACTION_START_DEX, data)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent)
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