package com.honeywell.usbakerydex

import org.json.JSONObject

enum class HoneywellSections(val value: String) {
    CONFIG("config"),
    TRANSACTION("transaction"),
    TRANSMISSION_CONTROL_NUMBER("transmissioncontrolnumber"),
    TRANSACTION_CONTROL_NUMBER("transactionsetcontrolnumber"),

}


class VersatileConverter {

    private fun getIgnoreCase(jSONObject: JSONObject, property: String?): String? {
        val keys: Iterator<*> = jSONObject.keys()
        while (keys.hasNext()) {
            val str2 = keys.next() as String
            if (str2.equals(property, ignoreCase = true)) {
                return str2
            }
        }
        return null
    }

    fun jsonToVersatileString(jsonObject: JSONObject) : String {
        var result = extractVendorSection(jsonObject)
        return result
    }

    private fun extractVendorSection(jsonObject: JSONObject): String {
        return ""
    }



}

