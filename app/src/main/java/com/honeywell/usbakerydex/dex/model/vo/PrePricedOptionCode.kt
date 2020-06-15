package com.honeywell.usbakerydex.dex.model.vo

class PrePricedOptionCode {

    companion object {
        const val PRE_PRICED_PRICES_INCLUDED_AND_PRICE_QUALIFIERS_APPLY = "A"
        const val NOT_PRE_PRICED = "N"
        const val PRE_PRICED = "Y"
        const val PRE_PRICED_WITH_PRICES_NOT_INCLUDED = "Z"

        fun values(): List<String> {
            return arrayListOf(
                PRE_PRICED_PRICES_INCLUDED_AND_PRICE_QUALIFIERS_APPLY,
                NOT_PRE_PRICED,
                PRE_PRICED,
                PRE_PRICED_WITH_PRICES_NOT_INCLUDED
            )
        }
    }
}