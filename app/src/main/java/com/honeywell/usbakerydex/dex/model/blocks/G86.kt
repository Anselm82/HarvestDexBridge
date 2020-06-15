package com.honeywell.usbakerydex.dex.model.blocks

data class G86(
    var signature: String?, //01 M-[1-12] to calculate it use G8201, G8202 the algorithm and the secretKey.
    var name: String? = null
) //02
{
    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.signature = value
            "02" -> this.name = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.signature
            "02" -> this.name
            else -> null
        }
    }
}