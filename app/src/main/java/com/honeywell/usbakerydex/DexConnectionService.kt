package com.honeywell.usbakerydex

import android.app.*
import android.content.*
import android.content.Intent.*
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.honeywell.usbakerydex.dex.model.DEFAULT_DEX_VERSION
import com.honeywell.usbakerydex.dex.model.DEXTransmission
import com.honeywell.usbakerydex.honeywell.HoneywellParser
import com.honeywell.usbakerydex.versatile.VersatileConverter
import com.honeywell.usbakerydex.versatile.model.VersatileDexMode
import com.honeywell.usbakerydex.versatile.utils.cleanUCS
import org.json.JSONObject


class DexConnectionService : Service() {

    companion object {
        const val CALLER_APP_ID = "callerAppID"
        const val EXTERNAL_EVENT_ACTION = "com.movilizer.client.android.EXT_EVENT"
        const val EVENT_SEND = 7363
        const val EVENT_RECEIVE = 7328
        const val EVENT_FINISHED = 721
        const val EVENT_CLOSE = 2573
        const val SEND_TYPE = "text/dex_route_data"
        const val DEX_APP_PKG_NAME = "com.vms.android.VersatileDEX"
        const val ACTION_DEX_FINISHED = "com.vms.android.VersatileDEX.ACTION_DEX_FINISHED"
        const val DEX_MODE = "mode"
    }

    lateinit var jsonObject: JSONObject

    var messenger: Messenger? = null
    var connection: ServiceConnection? = null
    var eventSourceId: Int = 0
    var errorCode = 0L
    var errorMessage = ""
    var callMe: CallMe? = null
    var registered = false

    lateinit var honeywellDexRequest: DEXTransmission
    lateinit var result : String

    inner class IntentBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("DEX", "ACTION: " + intent?.action)
            if (intent?.action.equals(ACTION_DEX_FINISHED)) {
                handleIntent(intent!!)
                Log.i("DEX", "receiver")
            }
        }
    }

    var broadcastReceiver : IntentBroadcastReceiver? = null

    private val mBroadcastReceiver: BroadcastReceiver?
        get() {
            return object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, intent: Intent?) {
                    if (intent?.action.equals(ACTION_DEX_FINISHED)) {
                        handleIntent(intent!!)
                        Log.i("DEX", "receiver")
                    }
                }
            }
        }


    fun handleIntent(intent: Intent) {
        //get data from intent
        Log.i("DEX", "handleIntent")
        val mode = intent.getIntExtra("mode", 0)
        val status = intent.getIntExtra("status", -99)
        val lic_status = intent.getIntExtra("lic_status", -99)
        val days_remaining = intent.getIntExtra("lic_days_remaining", 0)
        val comm_status = intent.getIntExtra("comm_status", -99)
        val sResults = intent.getStringExtra("results")
        val sActivity = intent.getStringExtra("activity")
        //check values of concern
        if (status != 0) {
            //operation failed
        }
        if (lic_status != 0) {
            //license is not active
        }
        //if this value < 15, refreshing might be failing?
        //if it gets down to 3, you should intervene if expecting it to be higher
        if (days_remaining < 4) {
            //license is not active
        }
        if(status == 0) {
            try {
                val response = VersatileConverter.toVersatileDexResponse(
                    sResults,
                    honeywellDexRequest.configuration.retailer?.dexVersion?.cleanUCS()?.toInt()
                        ?: DEFAULT_DEX_VERSION.toInt()
                )
                    val result = honeywellDexRequest.buildResponse(response)
                    this.result = result.toHoneywell() ?: "No result"
                    print(this.result)
                    this.eventSourceId = EVENT_RECEIVE
                    callMe?.connectionNotifier?.isConnected = true
                    callMe?.connectionNotifier?.doWork()
            } catch(e: Exception) {
                Log.e("Error", e.message ?: "No message")
            }
        } else if(status == 95) {
            val response = VersatileConverter.toVersatileDexResponse(
                sResults,
                honeywellDexRequest.configuration.retailer?.dexVersion?.cleanUCS()?.toInt()
                    ?: DEFAULT_DEX_VERSION.toInt()
            )
            if(response.invoices().size > 1) {
                val result = honeywellDexRequest.buildResponse(response)
                this.result = result.toHoneywell() ?: "No result"
                print(this.result)
                this.eventSourceId = EVENT_RECEIVE
                callMe?.connectionNotifier?.isConnected = true
                callMe?.connectionNotifier?.doWork()
            }
        }
    }

    private fun getRouteData() = honeywellDexRequest.toVersatile()


    override fun onBind(p0: Intent?): IBinder? = null

    fun doSendMessage(code: Long, str: String) {
        val responseJSONObject = JSONObject()
        try {
            responseJSONObject.put("result", code)
            responseJSONObject.put("message", str)
        } catch (e: Exception) {
            val sb = StringBuilder()
            sb.append("Exception in doSendMessage(): ")
            sb.append(e.message)
        }
        val obtain = Message.obtain()
        val bundle = Bundle()
        bundle.putString("JSON", responseJSONObject.toString())
        obtain.what = honeywellDexRequest.initialization.eventSourceId
        obtain.arg1 = System.currentTimeMillis().toInt()
        obtain.arg2 = honeywellDexRequest.initialization.syncHType
        obtain.replyTo = Messenger(IncomingHandler())
        obtain.data = bundle
        try {
            this.messenger?.send(obtain)
        } catch (e2: RemoteException) {
        } catch (th: Throwable) {
            //stopSelf()
            throw th
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("DEX", "onCreate")
        handleStart()
        startForegroundNotification()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startNewForegroundNotification() {
        try {
            val channelName = "Harvest Food Solutions DEX Service"
            val chan = NotificationChannel(
                App.CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)

            val data = getRouteData()
            val intent = build(VersatileDexMode.ACTION_START_DEX, data)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            val resultPendingIntent : PendingIntent? = PendingIntent.getActivity(
                applicationContext, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notificationBuilder =
                NotificationCompat.Builder(this, App.CHANNEL_ID).setContentIntent(resultPendingIntent
                )
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Harvest Food Solutions DEX Service")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            startForeground(2, notification)
        } catch (e: java.lang.Exception) {
            Log.e("ERROR", e.message)
        }
    }

    private fun startForegroundNotification() {
        startForeground(
            1, NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Harvest Food Solutions DEX Service").setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java),
                        0
                    )
                ).build()
        )
    }

    override fun onDestroy() {
        if (broadcastReceiver != null) {
            connection?.let { unbindService(it) }
            unregisterReceiver(broadcastReceiver)
            registered = false
        }
        super.onDestroy()
        Log.i("DEX", "onDestroy")
    }

    private fun build(mode: VersatileDexMode, content: String? = null): Intent {
        val intent = Intent(ACTION_SEND)
        intent.type = SEND_TYPE
        intent.`package` = DEX_APP_PKG_NAME
        intent.putExtra(DEX_MODE, mode.value)
        if (!content.isNullOrBlank())
            intent.putExtra(Intent.EXTRA_TEXT, content)
        Log.i("DEX", "build")
        return intent
    }

    fun refreshClientLicense() {
        val intent = build(VersatileDexMode.ACTION_LICENSE_REFRESH)
        startActivity(intent)
    }

    fun startActivation() {
        val intent = build(VersatileDexMode.ACTION_START_ACTIVATE)
        startActivity(intent)
    }

    fun licenseStatus() {
        val intent = build(VersatileDexMode.ACTION_LICENSE_STATUS)
        startActivity(intent)
    }

    fun launchDEX() {
        Log.i("DEX", "broadcast " + (broadcastReceiver != null))
        val data = getRouteData()
        val intent = build(VersatileDexMode.ACTION_START_DEX, data)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        Log.i("DEX", "launchDEX")
    }

    private class IncomingHandler() : Handler() {
        override fun handleMessage(message: Message) {}
    }

    private fun handleStart() {
        val filter = IntentFilter(ACTION_DEX_FINISHED)
        if(broadcastReceiver == null) {
            broadcastReceiver = IntentBroadcastReceiver()
        }
        if(!registered) {
            registerReceiver(broadcastReceiver, filter)
            registered = true
        }
        this.connection = RemoteServiceConnection()
        this.callMe = CallMe()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startNewForegroundNotification()
        else
            startForegroundNotification()
        Log.i("DEX", "handleStart")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleStart()
        setDexParams(intent, startId)
        Log.i("DEX", "onStartCommand")
        return START_NOT_STICKY
    }


    private fun setDexParams(intent: Intent?, i: Int) {
        if(intent?.hasExtra(CALLER_APP_ID) == true) {
            val stringExtra: String = intent!!.getStringExtra(CALLER_APP_ID)!!
            val newIntent = Intent(EXTERNAL_EVENT_ACTION)
            if (intent.hasExtra("JSON")) {
                jsonObject = JSONObject(intent.getStringExtra("JSON")!!)
                honeywellDexRequest = DEXTransmission.Builder()
                    .with(HoneywellParser.readConfiguration(jsonObject)!!)
                    .with(HoneywellParser.readInitialization(jsonObject)!!)
                    .with(HoneywellParser.readTransaction(jsonObject)!!).build()
                newIntent.setPackage(stringExtra)
                eventSourceId = honeywellDexRequest.initialization.eventSourceId
                newIntent.putExtra("evtSrcId", eventSourceId)
                bindService(newIntent, connection!!, Context.BIND_AUTO_CREATE)
            }
            Log.i("DEX", "setDexParams")
        }
    }

    inner class RemoteServiceConnection internal constructor() : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            iBinder: IBinder
        ) {
            this@DexConnectionService.messenger = Messenger(iBinder)
            this@DexConnectionService.callMe?.connectionNotifier?.isConnected = true
            this@DexConnectionService.callMe?.connectionNotifier?.doWork()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            this@DexConnectionService.messenger = null
            this@DexConnectionService.callMe?.connectionNotifier?.isConnected = false
        }
    }

    inner class CallMe : ConnectionEvent {

        var connectionNotifier = ConnectionNotifier(this)

        override fun connectionEvent() {
            this@DexConnectionService.doSendMessage(
                this@DexConnectionService.errorCode,
                this@DexConnectionService.errorMessage
            )
        }
    }

    interface ConnectionEvent {
        fun connectionEvent()
    }

    inner class ConnectionNotifier(private val connectionEvent: ConnectionEvent) {

        var isConnected = false

        fun doWork() {
            if (isConnected) {
                try {
                    if (this@DexConnectionService.eventSourceId == EVENT_SEND || this@DexConnectionService.eventSourceId == EVENT_RECEIVE) {
                        Log.d("DEX", "Transmission")
                        isConnected = true
                    }
                    if (this@DexConnectionService.eventSourceId == EVENT_CLOSE) {
                        Log.i("DEX", "Close Process")
                        connectionEvent.connectionEvent()
                        return
                    }
                    //SEND
                    Log.i("DEX", eventSourceId.toString())
                    if (this@DexConnectionService.eventSourceId == EVENT_SEND) {
                        Log.i("DEX", "Launching Dex.")
                        if(broadcastReceiver == null)
                            broadcastReceiver = IntentBroadcastReceiver()
                        if(!registered) {
                            registerReceiver(broadcastReceiver, IntentFilter(ACTION_DEX_FINISHED))
                            registered = true
                        }
                        launchDEX()
                        //RECEIVE
                    } else if (this@DexConnectionService.eventSourceId == EVENT_RECEIVE) {
                        errorMessage = result
                        connectionEvent.connectionEvent()
                    }
                } catch (e: Exception) {
                    errorCode = 9001
                    errorMessage = e.message ?: "Error in DEX transmission"
                    Log.i("DEX", e.message)
                }
            }
        }
    }
}