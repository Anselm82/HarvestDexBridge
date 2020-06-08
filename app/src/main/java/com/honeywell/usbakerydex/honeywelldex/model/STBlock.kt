package com.honeywell.usbakerydex.honeywelldex.model

data class STBlock(
    val ucsType: String, //01
    val transactionIndex: Long? = 0, //02
    val implementationConventionRelease: String? = null //03
)