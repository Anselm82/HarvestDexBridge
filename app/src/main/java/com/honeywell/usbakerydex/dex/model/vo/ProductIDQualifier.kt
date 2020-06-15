package com.honeywell.usbakerydex.dex.model.vo

open class ProductIDQualifier {

    companion object {
        const val CASE = "CA"
        const val DEPOSIT_ITEM_NUMBER = "DI"
        const val GTIN_13 = "EN"
        const val GTIN_8 = "EO"
        const val NON_RESALEABLE = "NR"
        const val GTIN_14 = "UK"
        const val GTIN_12 = "UP"
        const val VENDOR_ITEM_NUMBER = "VN"

        fun values(): List<String> {
            return arrayListOf(
                CASE,
                DEPOSIT_ITEM_NUMBER,
                GTIN_12,
                GTIN_13,
                GTIN_14,
                GTIN_8,
                NON_RESALEABLE,
                VENDOR_ITEM_NUMBER
            )
        }
    }
}