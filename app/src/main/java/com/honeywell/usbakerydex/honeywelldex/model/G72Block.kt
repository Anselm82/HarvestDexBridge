package com.honeywell.usbakerydex.honeywelldex.model

import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes

data class G72Block(
    val allowanceOrChargeCode: Int, //01
    val handlingMethod: HoneywellHandlingMethodCodes, //02
    val allowanceOrChargeNumber: String?, //03
    val exceptionNumber: String?, //04
    val allowanceOrChargeRate: Double?, //05
    val allowanceOrChargeQty: Double?, //06
    val unitOfMeasure: String?, //07
    val allowanceOrChargeTotalAmount: Double?, //08
    val allowanceOrChargePercent: Double?, //09
    val dollarBasisForPercent: Double?, //10
    val optionNumber: String? //11
)