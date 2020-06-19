package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.vo.PrePricedOptionCode

data class G22(
    var prePricedOptionCode: String? = null, //01 M-[1]
    var priceNewOrSuggestedRetail: Double? = 0.0, //02 O[2/7] price
    var multiplePriceQuantity: Int? = 0, //03 O[1/2] Number of units for a give price
    var freeFormMessage : String? = null, //04 O[1-60] text
    var date: String? = null //05 O[8-8] YYYYMMDD date of the effective price. Not used.
) {

    init {
        putString("01", prePricedOptionCode)
    }

    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.prePricedOptionCode = if (value.isNullOrBlank() || value !in PrePricedOptionCode.values()) null else value
            "04" -> this.freeFormMessage = value
            "05" -> this.date = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.prePricedOptionCode
            "04" -> this.freeFormMessage
            "05" -> this.date
            else -> null
        }
    }

    fun putDouble(key: String, value: Double?) {
        when (key) {
            "02" -> this.priceNewOrSuggestedRetail = value
        }
    }

    fun getDouble(key: String): Double? {
        return when (key) {
            "02" -> this.priceNewOrSuggestedRetail
            else -> null
        }
    }

    fun putInt(key: String, value: Int?) {
        when (key) {
            "03" -> this.multiplePriceQuantity = value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "03" -> this.multiplePriceQuantity
            else -> null
        }
    }
}