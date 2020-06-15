package com.honeywell.usbakerydex.honeywelldex.model

import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellUnitOfMeasure

data class G83Block(
    val adjustments: Map<String, G72Block>,
    val materialNumber: String?,
    val type: String?,
    val sequenceNumber: String,
    val quantity: Double,
    val upc: String,
    val productId: String,
    val itemListCost: Double,
    val description: String? = "NO DESC",
    val unitOfMeasure: HoneywellUnitOfMeasure? = HoneywellUnitOfMeasure.EACH,
    val productIdQualifier: String? = null, // TODO define an enum according to DEX UCS Programmer guide, page 31/114
    val upcCaseCode: String? = null,
    val pack: Int? = null,
    val caseIdQualifier: String? = null, // TODO define an enum according to DEX UCS Programmer guide, page 33/114
    val caseId: String? = null
)
