package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.DEFAULT_DEX_VERSION
import com.honeywell.usbakerydex.dex.model.FUNCTIONAL_GROUP
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator

data class DXS(
    var senderIdentificationCode : String? = null, //01, M-[2-80] sender identification number GS1 US or UCS Comm id
    var functionalIdentifierCode : String = FUNCTIONAL_GROUP, //02, M-[2-2]
    var dexVersion : String = DEFAULT_DEX_VERSION, //03, M-[1-12] VVVvvvUCS all subsets must use same version
    var transmissionControlNumber : Int? = 1, //04, M-[1-99999] different by transmission
    var receiverIdentificationCode : String? = null, //05, O-[2-80] receiver identification number GS1 US or UCS Comm id
    var testIndicator : String = TestIndicator.TEST //06, O-1 environment, test by default
) {

    fun putString(key: String, value: String?) {
        when(key) {
            "01" -> this.senderIdentificationCode = value
            "02" -> this.functionalIdentifierCode = value ?: FUNCTIONAL_GROUP
            "03" -> this.dexVersion = value ?: DEFAULT_DEX_VERSION
            "05" -> this.receiverIdentificationCode = value
            "06" -> this.testIndicator = if(value.isNullOrBlank() || value !in TestIndicator.values()) TestIndicator.TEST else value
        }
    }

    fun putInt(key: String, value: Int?) {
        when(key) {
            "04" -> this.transmissionControlNumber = value
        }
    }

    fun getInt(key: String) : Int? {
        return when(key) {
            "04" -> this.transmissionControlNumber
            else -> null
        }
    }

    fun getString(key: String) : String? {
        return when(key) {
            "01" -> this.senderIdentificationCode
            "02" -> this.functionalIdentifierCode
            "03" -> this.dexVersion
            "05" -> this.receiverIdentificationCode
            "06" -> this.testIndicator
            else -> null
        }
    }
}