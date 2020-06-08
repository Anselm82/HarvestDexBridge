package com.honeywell.usbakerydex.honeywelldex.model

import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellReferenceNumberQualifier

data class N9Block(
    val referenceNumberQualifier: HoneywellReferenceNumberQualifier, //01
    val referenceNumber: String?, //02
    val freeFormDescription: String?, //03
    val date: Long?, //04 YYYYMMDD
    val time: String?, //05 hhmmdd
    val timeCode: String? //06
)