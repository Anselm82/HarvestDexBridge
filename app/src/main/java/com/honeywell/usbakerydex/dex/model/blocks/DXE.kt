package com.honeywell.usbakerydex.dex.model.blocks

data class DXE(
    var transmissionControlNumber: Int?, //01 M-[1-5] TODO(Copy from DSX04)
    var numberOfTransactionSetsIncluded: Int? //02 M-[1-6] TODO(Count of ST, at this point always one)
) {
    fun putInt(key: String, value: Int?) {
        when (key) {
            "01" -> this.transmissionControlNumber = value
            "02" -> this.numberOfTransactionSetsIncluded = value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "01" -> this.transmissionControlNumber
            "02" -> this.numberOfTransactionSetsIncluded
            else -> null
        }
    }
}