package com.honeywell.usbakerydex.versatiledex

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

enum class VersatileDEXMode(val value : Int) {
    ACTION_START_DEX(0),
    ACTION_LICENSE_REFRESH(1),
    ACTION_START_ACTIVATE(2),
    ACTION_LICENSE_STATUS(3)
}

abstract class VersatileDexConnection : Activity() {

    companion object {
        const val SEND_TYPE = "text/dex_route_data"
        const val DEX_APP_PKG_NAME = "com.vms.android.VersatileDEX"
        const val ACTION_DEX_FINISHED = "com.vms.android.VersatileDEX.ACTION_DEX_FINISHED"
        const val DEX_MODE = "mode"
    }

    private val mBroadcastReceiver : BroadcastReceiver
        get() {
            return object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, intent: Intent?) {
                    if(intent?.action.equals(ACTION_DEX_FINISHED)){
                        handleIntent(intent!!)
                    }
                }
            }
        }


    fun handleIntent(intent : Intent){
        //get data from intent
        val mode = intent.getIntExtra("mode", 0)
        val status = intent.getIntExtra("status",-99)
        val lic_status = intent.getIntExtra("lic_status", -99)
        val days_remaining = intent.getIntExtra("lic_days_remaining", 0)
        val comm_status = intent.getIntExtra("comm_status", -99)
        val sResults = intent.getStringExtra("results")
        val sActivity = intent.getStringExtra("activity")
        //check values of concern
        if(status!=0){
            //operation failed
        }
        if(lic_status!=0){
            //license is not active
        }
        //if this value < 15, refreshing might be failing?
        //if it gets down to 3, you should intervene if expecting it to be higher
        if(days_remaining < 4){
            //license is not active
        }
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(ACTION_DEX_FINISHED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver);
    }

    abstract fun getRouteData() : String


    private fun build(mode: VersatileDEXMode, content: String? = null) : Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = SEND_TYPE
        intent.`package` = DEX_APP_PKG_NAME
        intent.putExtra(DEX_MODE, mode.value)
        if(!content.isNullOrBlank())
            intent.putExtra(Intent.EXTRA_TEXT, content)
        return intent
    }

    fun refreshClientLicense () {
        val intent = build(VersatileDEXMode.ACTION_LICENSE_REFRESH)
        startActivity(intent)
    }

    fun startActivation(){
        val intent = build(VersatileDEXMode.ACTION_START_ACTIVATE)
        startActivity(intent)
    }

    fun licenseStatus() {
        val intent = build(VersatileDEXMode.ACTION_LICENSE_STATUS)
        startActivity(intent)
    }

    fun launchDEX(){
        val data = getRouteData()
        val intent = build(VersatileDEXMode.ACTION_START_DEX, data)
        startActivity(intent)
    }
}