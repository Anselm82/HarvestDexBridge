package com.honeywell.usbakerydex.dex.model.vo

class MethodOfHandling {
    companion object {
        const val BILL_BACK = "01"
        const val OFF_INVOICE = "02"
        const val CHARGE_TO_BE_PAID_BY_CUSTOMER = "06"
        const val NOT_PROCESSED = "12"
        const val INFORMATION_ONLY = "15"

        fun values() : List<String> {
            return arrayListOf(BILL_BACK, OFF_INVOICE, CHARGE_TO_BE_PAID_BY_CUSTOMER, NOT_PROCESSED, INFORMATION_ONLY)
        }
    }
}