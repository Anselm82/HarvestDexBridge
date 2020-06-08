package com.honeywell.usbakerydex.honeywelldex.model

import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellInitiatorCode
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag

data class G87Block(
    val initiatorCode: HoneywellInitiatorCode, //01
    val creditDebitFlag: HoneywellTypeFlag, //02 must have same value than 8201
    val supplierDeliveryOrReturnNumber: String?, //03
    val integrityCheck: String?, //04
    val adjustmentNumber: Int?, //05
    val receiverDeliveryOrReturnNumber: String? //06
)