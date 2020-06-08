package com.honeywell.usbakerydex.honeywelldex.model

data class Config(
    val retailer: Retailer?,
    val supplier: Supplier?,
    val transactionSetControlNumber: Long,
    val transmissionControlNumber: Long,
    val testIndicator: String?
)