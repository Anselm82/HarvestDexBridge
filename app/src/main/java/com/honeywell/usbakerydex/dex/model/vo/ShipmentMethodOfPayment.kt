package com.honeywell.usbakerydex.dex.model.vo

class ShipmentMethodOfPayment {
    companion object {
        const val COLLECT = "CC"
        const val CUSTOMER_PICKUP_OR_BACKHAUL = "PB"
        const val PREPAID_BUT_CHARGED_TO_CUSTOMER = "PC"
        const val PREPAID_BY_SELLER = "PP"

        fun values(): List<String> {
            return arrayListOf(
                COLLECT,
                CUSTOMER_PICKUP_OR_BACKHAUL,
                PREPAID_BUT_CHARGED_TO_CUSTOMER,
                PREPAID_BY_SELLER
            )
        }
    }
}