package com.honeywell.usbakerydex

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log

class MainActivity : Activity() {

    companion object {
        const val VERSATILE_REQUEST = "ROUTE SAMPLE-1\n" +
                "NAME \"Sample Route 1\"\n" +
                "DUNS 126302830\n" +
                "LOCATION 001\n" +
                "COMM_ID 1122334455\n" +
                "STOP 1\n" +
                "CUST_NUMBER FO112\n" +
                "CUST_DUNS 038176921\n" +
                "CUST_LOCATION 570\n" +
                "CUST_NAME \"Some retailer\"\n" +
                "CUST_ADDR \"123 Any street\"\n" +
                "CUST_CITY Seattle\n" +
                "CUST_STATE WA\n" +
                "CUST_ZIP 98020\n" +
                "CUST_PHONE1 800-262-1633\n" +
                "CUST_PHONE2 425-778-8577\n" +
                "INVOICE 12-34567-SAMPLE\n" +
                "ORDER_TYPE DELIVERY\n" +
                "UPC 123456789012 123456789223\n" +
                "DESC \"2 PK Cheese Pizza\"\n" +
                "QUANT 18\n" +
                "PRICE 11.79\n" +
                "ADJUSTMENT A 51 02 123456789 \$ 0.7/18/EA\n" +
                "UPC 123456789404 123456789444\n" +
                "DESC \"2 PK Sausage Pizza\"\n" +
                "QUANT 30\n" +
                "PRICE 11.79"
        const val HONEYWELL_RESPONSE = "{\"ReceiveDexData\":{\"DXS\":{\"RetailerCommunicationID\":\"1111111111\",\"FunctionalIdentifierCode\":\"DX\",\"VersionOrReleaseOrIndustryIdentifierCode\":\"004010UCS\",\"TransmissionControlNumber\":3,\"SupplierCommunicationID\":\"1111111111\",\"TestIndicator\":\"P\"},\"InvoiceList\":[{\"Invoice Status\":{\"InvoiceNumber\":\"80010000080037000002\",\"Status\":\"Adjusted\"},\"ST\":{\"TransactionSetID\":\"895\",\"TransactionSetControlNumber\":2},\"G87\":{\"InitiatorCode\":\"R\",\"CreditDebitFlag\":\"D\",\"SupplierDeliveryReturnNumber\":\"80010000080037000002\",\"IntegrityCheckValue\":\"B837\",\"AdjustmentNumber\":1},\"G89\":[{\"SequenceNumber\":1,\"Quantity\":30.0},{\"SequenceNumber\":2,\"ItemListCost\":2.22},{\"SequenceNumber\":3,\"G72\":[{\"AllowanceCode\":\"96\",\"MethodOfHandling\":\"12\",\"AllowanceNumber\":\"REMOVE\"},{\"AllowanceCode\":\"97\",\"MethodOfHandling\":\"02\",\"AllowanceRate\":\"-.02\",\"AllowanceQuantity\":\"25\",\"UOMCode\":\"EA\"}]}],\"G84\":{\"TotalQuantity\":77.0,\"TotalInvoiceAmount\":293.78},\"G86\":{\"Signature\":\"5230\"},\"G85\":{\"IntegrityCheckValue\":\"E11C\"},\"SE\":{\"SegmentCount\":13,\"TransactionSetControlNumber\":2}}],\"DXE\":{\"TransmissionControlNumber\":3,\"NumberOfTransactionSetsIncluded\":1}}}"
        const val HONEYWELL_REQUEST = "{\n" +
                "\t\"CONFIG\": \n" +
                "\t{\n" +
                "\t\t\"RETAILER\":\n" +
                "\t\t{\n" +
                "\t\t\t\"COMMUNICATIONSID\" : \"1111111111\",\n" +
                "\t\t\t\"DEXVERSION\" : \"005010UCS\",\n" +
                "\t\t\t\"DUNSNUMBER\" : \"007654329\",\n" +
                "\t\t\t\"LOCATION\" : {}\n" +
                "\t\t},\n" +
                "\t\t\"SUPPLIER\" : \n" +
                "\t\t{\n" +
                "\t\t\t\"SIGNATUREKEY\" : 123\n" +
                "\t\t},\n" +
                "\t\t\"TRANSACTIONSETCONTROLNUMBER\" : 1,\n" +
                "\t\t\"TRANSMISSIONCONTROLNUMBER\" : 1\n" +
                "\t},\n" +
                "\t\"INITIALIZATION\" : \n" +
                "\t{\n" +
                "\t\t\"COMMETHOD\" : \"BTLE\",\n" +
                "\t\t\"EVTSRCID\" : 7363,\n" +
                "\t\t\"INIFILE\" : \"//mnt/sdcard//Android//data//DSD12//config.ini\",\n" +
                "\t\t\"INSTANCENAME\" : \"DEX\",\n" +
                "\t\t\"SYNCHTYPE\" : 2\n" +
                "\t},\n" +
                "\t\"transaction\" :\n" +
                "\t{\n" +
                "\t\t\"invoices\" : \n" +
                "\t\t{\n" +
                "\t\t\t\"80010000080037000001\" : \n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"G82\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : \"D\",\n" +
                "\t\t\t\t\t\"02\" : \"80010000080037000001\",\n" +
                "\t\t\t\t\t\"07\" : \"20200525\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"G84\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : 75.0,\n" +
                "\t\t\t\t\t\"02\" : \"222.84\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"ST\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : \"894\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"01\" : \"501\",\n" +
                "\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\"08\" : 83.57\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"items\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"40\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000689\",\n" +
                "\t\t\t\t\t\t\"08\" : \"1.77\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ PREM WHITE\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070001\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.023\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"2\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"25\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000146\",\n" +
                "\t\t\t\t\t\t\"08\" : \"1.98\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ CR WHT RDTP\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070004\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.0256\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"3\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"10\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000153\",\n" +
                "\t\t\t\t\t\t\"08\" : \"2.08\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ WH GR RD TP\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070005\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.027\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}"
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        finish()
    }*/




    private lateinit var mServiceIntent : Intent
    lateinit var service : ServiceToCallActivity

    lateinit var ctx : Context


    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)
            finish();
            return;
        }
        super.onCreate(savedInstanceState)
        ctx = this
        setContentView(R.layout.activity_main)
        service = ServiceToCallActivity()
        mServiceIntent = Intent(ctx, ServiceToCallActivity::class.java)
        if (!isMyServiceRunning(service.javaClass)) {
            startService(mServiceIntent)
            registerReceiver(mBroadcastReceiver, IntentFilter(DexConnectionService.ACTION_DEX_FINISHED))
        }
        moveTaskToBack(true)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>) : Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", "" + true)
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        unregisterReceiver(mBroadcastReceiver)
        stopService(mServiceIntent)

        Log.i("MAINACT", "onDestroy!")
        super.onDestroy()
    }

    val mBroadcastReceiver: BroadcastReceiver?
        get() {
            return object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, intent: Intent?) {
                    if (intent?.action.equals(DexConnectionService.ACTION_DEX_FINISHED)) {
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
    }
}
