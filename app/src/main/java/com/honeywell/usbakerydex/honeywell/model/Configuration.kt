package com.honeywell.usbakerydex.honeywell.model

data class Configuration(
    val retailer: Retailer?,
    val supplier: Supplier?,
    val transactionSetControlNumber: Int?,
    val transmissionControlNumber: Int?,
    val testIndicator: String?
)