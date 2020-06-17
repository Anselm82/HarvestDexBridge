package com.honeywell.usbakerydex

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.honeywell.usbakerydex.dex.model.DEXTransaction
import com.honeywell.usbakerydex.honeywelldex.HoneywellParser
import org.json.JSONObject

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //moveTaskToBack(true)
        finish()

        val jsonString = "{\n" +
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
        val json = JSONObject(jsonString)

        //need to parse the config and initialization to fill the request
        val dex = DEXTransaction.fromJSON(json)
        //then, parse to versatile doing: dex.toVersatile()
        print(dex)
    }
}
