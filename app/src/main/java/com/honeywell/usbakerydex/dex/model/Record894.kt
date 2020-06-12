package com.honeywell.usbakerydex.dex.model

import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.vo.CreditDebitFlagCode

const val FUNCTIONAL_GROUP = "DX"
const val DEFAULT_DEX_VERSION = "004010UCS"
const val DATE_FORMAT = "YYYYMMDD"
const val DEFAULT_LOOP_ID = "0100"



//From supplier to customer/receiver
data class Record894(
    var dxs: DXS? = null, //Application header      M-1
    var st: ST? = null, //Transaction set header    M-1
    var g82: G82? = null, //Delivery/Return base record identifier  M-1
    var n9: N9? = null, //Extended reference information    O-n
    var loop: Loop? = null, //Line Item looping extracted to a element to be able to nest
    var g72: List<G72>? = null, //Allowance or charge   O-20
    var g23: List<G23>? = null, //Terms of sale O-20 UNSUPPORTED
    var g84: G84? = null, //Delivery/Return record of totals    M-1
    var g86: G86? = null, //Signature   M-1
    var g85: G85? = null, //Record integrity check  M-1
    var se: SE? = null, //Transaction set trailer   M-1
    var dxe: DXE? //Application trailer M-1
)



