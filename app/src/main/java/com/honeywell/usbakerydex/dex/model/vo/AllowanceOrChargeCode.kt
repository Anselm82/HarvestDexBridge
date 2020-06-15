package com.honeywell.usbakerydex.dex.model.vo

class AllowanceOrChargeCode {

    companion object {
        const val FREE_GOODS = 1
        const val GROUPED_ITEMS = 96
        const val CENTS_OFF = 97
        const val DEPOSIT_CHARGE_RESALE_ITEM = 525

        fun allowedValues() : List<Int> {
            return arrayListOf(FREE_GOODS, GROUPED_ITEMS, CENTS_OFF, DEPOSIT_CHARGE_RESALE_ITEM)
        }
    }
}