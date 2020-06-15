package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.vo.CreditDebitFlagCode
import com.honeywell.usbakerydex.dex.model.vo.InitiatorCode

data class G87(
    var initiatorCode: String?, //01 M[1] Initiator of the transaction
    var creditDebitFlag: String?, //02 M[1] must have same value than 8201
    var supplierDeliveryOrReturnNumber: String?, //03 M[1-22]
    var integrityCheck: String?, //04 M[1-12] should contain the same value than 8501 of the preceding ack/adj record or of the base record if this is the first one. CRC16
    var adjustmentNumber: Int?, //05 M[1] sequential number of the adjustment. Max of 9 adjustments/acks
    var receiverDeliveryOrReturnNumber: String? = null //06 O[1-22] optional to allow lazy assignment.
) {
    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.initiatorCode = if (value.isNullOrBlank() || value !in InitiatorCode.values()) null else value
            "02" -> this.creditDebitFlag = if (value.isNullOrBlank() || value !in CreditDebitFlagCode.values()) null else value
            "03" -> this.supplierDeliveryOrReturnNumber = value
            "04" -> this.integrityCheck = value
            "06" -> this.receiverDeliveryOrReturnNumber = value
        }
    }

    fun putInt(key: String, value: Int?) {
        when (key) {
            "05" -> this.adjustmentNumber = value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "05" -> this.adjustmentNumber
            else -> null
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.initiatorCode
            "02" -> this.creditDebitFlag
            "03" -> this.supplierDeliveryOrReturnNumber
            "04" -> this.integrityCheck
            "06" -> this.receiverDeliveryOrReturnNumber
            else -> null
        }
    }

}