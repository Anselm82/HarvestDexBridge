package com.honeywell.usbakerydex.dex.model.vo

class InitiatorCode {
    companion object {
        const val RETAILER = "R"
        const val SUPPLIER = "S"

        fun values() : List<String> {
            return arrayListOf(RETAILER, SUPPLIER)
        }
    }
}