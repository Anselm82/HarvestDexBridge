package com.honeywell.usbakerydex.honeywell.model

data class Retailer(
    var communicationsId: String?,
    val dunsNumber: String,
    val location: String? = "{}",
    val dexVersion: String?
)
