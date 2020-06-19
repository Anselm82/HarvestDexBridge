package com.honeywell.usbakerydex.honeywell.model

data class Supplier(
    val communicationsId: String? = "",
    val dunsNumber: String? = "",
    val location: String? = "",
    val signatureKey: Long?
)
