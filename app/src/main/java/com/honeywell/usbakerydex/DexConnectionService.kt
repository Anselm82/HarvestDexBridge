package com.honeywell.usbakerydex

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.honeywell.usbakerydex.versatiledex.VersatileDexConnection
import org.json.JSONObject
import java.util.*

enum class CommunicationMethod {
    NONE,
    RS232,
    BTLE
}

class DexConnectionService : Service() {

    companion object {
        const val CALLER_APP_ID = "callerAppID"

        val CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        val DEX_CHARACTERISTIC_DATA =
            UUID.fromString("F000C0E1-0451-4000-B000-000000000000")

        val DEX_SERVICE_SPP =
            UUID.fromString("F000C0E0-0451-4000-B000-000000000000")

        val EVENT_START = arrayOf(7363, 7328)

        const val EVENT_CLOSE = 2573
    }
    lateinit var jsonObject : JSONObject

    val connection : VersatileDexConnection
        get() {
            return object : VersatileDexConnection() {
                override fun getRouteData(): String {
                    return VersatileConverter.versatileRequestFromJSON(jsonObject)
                }
            }
        }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}