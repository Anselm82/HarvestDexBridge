package com.honeywell.usbakerydex.dex.model.blocks


data class SE(
    var numberOfIncludedSegments: Int? = 0, //01  M-[1-10] segments
    var transactionControlNumber: String? = null //02   M-[4-9] TODO(Copy from ST02)
) {
    fun putInt(key: String, value: Int?) {
        when (key) {
            "01" -> this.numberOfIncludedSegments = value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "01" -> this.numberOfIncludedSegments
            else -> null
        }
    }

    fun putString(key: String, value: String?) {
        when (key) {
            "02" -> this.transactionControlNumber = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "02" -> this.transactionControlNumber
            else -> null
        }
    }
}