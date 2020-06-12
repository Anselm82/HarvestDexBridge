package com.honeywell.usbakerydex

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.honeywell.usbakerydex.honeywelldex.HoneywellParser
import org.json.JSONObject

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //moveTaskToBack(true)
        finish()
    }
}
