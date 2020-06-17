package com.honeywell.usbakerydex.dex.model

import com.honeywell.usbakerydex.dex.model.blocks.*
import org.json.JSONObject

data class Record895(
    var dxs: DXS? = null, //Application header      M-1
    var st: ST? = null, //Transaction set header    M-1
    var g87: G87? = null, //Delivery/Return base record identifier  M-1
    var g88: G88? = null, //Delivery/Return base record identifier adjustment O-1
    var loop: Loop? = null, //Line Item looping extracted to a element to be able to nest
    var g72: List<G72>? = null, //Allowance or charge   O-20
    var g23: List<G23>? = null, //Terms of sale O-20 UNSUPPORTED
    var g84: G84? = null, //Delivery/Return record of totals    M-1
    var g86: G86? = null, //Signature   M-1
    var g85: G85? = null, //Record integrity check  M-1
    var se: SE? = null, //Transaction set trailer   M-1
    var dxe: DXE? //Application trailer M-1
)
{
    companion object {
        fun fromHoneywell(jsonString: String) : Record895 {
            TODO()
        }

        fun fromVersatile(jsonString: String) : Record895 {
            TODO()
        }
    }
}

internal fun Record895.toVersatile(): String {
    TODO()
}

internal fun Record895.toHoneywell(): JSONObject {
    TODO()
}