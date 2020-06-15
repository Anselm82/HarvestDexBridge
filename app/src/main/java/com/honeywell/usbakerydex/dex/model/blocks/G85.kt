package com.honeywell.usbakerydex.dex.model.blocks

data class G85(var integrityCheckValue: String?) {

    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.integrityCheckValue = value

        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.integrityCheckValue
            else -> null
        }
    }
} //01   M-[1-12] CRC16 check appendix, uses from ST to G86