package com.honeywell.usbakerydex

import com.honeywell.usbakerydex.versatiledex.utils.InvalidValueException
import com.honeywell.usbakerydex.versatiledex.utils.VersatileAdjustmentFlag
import com.honeywell.usbakerydex.versatiledex.utils.VersatileInvoiceAdjustmentFlag
import org.json.JSONObject

fun VersatileInvoiceAdjustmentFlag.toVersatileAdjustmentFlag() : VersatileAdjustmentFlag {
    return when(this) {
        VersatileInvoiceAdjustmentFlag.TOTAL -> VersatileAdjustmentFlag.TOTAL
        VersatileInvoiceAdjustmentFlag.PERCENTAGE -> VersatileAdjustmentFlag.PERCENTAGE
        else -> throw InvalidValueException("Flag")
    }
}

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
}

