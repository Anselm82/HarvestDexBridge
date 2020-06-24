package com.honeywell.usbakerydex.versatile.model

data class VersatileDexRequest(val vendor: Vendor, val stops: List<Stop>) {

    override fun toString(): String {
        var message = ""
        message += vendor
        if (!stops.isNullOrEmpty() && stops.size == 1) //Limitation due to embedded mode
            message += stops.joinToString("\n") { stop -> stop.toString() }
        return message
    }
}