package com.honeywell.usbakerydex.versatiledex.model

data class VersatileDexResponse(val lines: Array<VersatileDexResponseEntry>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VersatileDexResponse

        if (!lines.contentEquals(other.lines)) return false

        return true
    }

    override fun hashCode(): Int {
        return lines.contentHashCode()
    }

    override fun toString(): String {
        return lines.joinToString { line -> line.toString() }
    }

}

