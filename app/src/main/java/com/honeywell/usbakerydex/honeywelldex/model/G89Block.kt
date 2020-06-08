package com.honeywell.usbakerydex.honeywelldex.model

data class G89Block(
    val dsdSequenceNumber: String?, //01 must have same value as 8301, but when added by retailer, same as 8901 received
    val quantity: Double?, //02
    val unitOfMeasure: String?, //03
    val upc: String?, //04
    val productIdQualifier: String?, //05
    val productId: String?, //06
    val caseCode: String?, //07
    val itemListCost: Double?, //08
    val pack: Int?, //09
    val innerPack: Int?, //10
    val caseIdQualifier: String?, //11
    val caseId: String? //12
)