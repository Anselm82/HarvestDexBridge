package com.honeywell.usbakerydex.dex.model.blocks

data class G86(
    val signature: String?, //01 M-[1-12] to calculate it use G8201, G8202 the algorithm and the secretKey.
    val name: String? = null) //02