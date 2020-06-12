package com.honeywell.usbakerydex.dex.model.vo

class CaseIDQualifier {
    companion object {
        const val AGGREGATION_CODE = "AG"
        const val CASE = "CA"
        const val GTIN_13 = "EN"
        const val GTIN_8 = "EO"
        const val PRODUCT_VARIANT = "UC"
        const val USER_DEFINED = "UF"
        const val GTIN_14 = "UK"
        const val GTIN_20 = "UO"
        const val GTIN_12 = "UP"
        const val RANDOM_WEIGHT_AGGREGATION_CODE = "WA"

        fun values() : List<String> {
            return arrayListOf(AGGREGATION_CODE, CASE, GTIN_12, GTIN_13, GTIN_14, GTIN_20, GTIN_8, PRODUCT_VARIANT, USER_DEFINED, RANDOM_WEIGHT_AGGREGATION_CODE)
        }
    }
}