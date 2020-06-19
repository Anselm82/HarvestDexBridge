package com.honeywell.usbakerydex.dex.model.blocks

import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.dex.model.vo.AllowanceOrChargeCode
import com.honeywell.usbakerydex.dex.model.vo.MethodOfHandling
import com.honeywell.usbakerydex.dex.model.vo.PrePricedOptionCode
import com.honeywell.usbakerydex.dex.model.vo.UOM
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes

/**
 *
 */
data class G72(
    var allowanceOrChargeCode: Int?, //01 M[1-3]
    var handlingMethod: String?, //02 Method of handling M[2]
    var allowanceOrChargeNumber: String?, //03 O[1-16] promotion, allowance or charge identificator, used with 5 or 8 or 9
    var exceptionNumber: String? = null, //04 O[1-16] exception number from supplier
    var allowanceOrChargeRate: Double? = null, //05 X[1-15] Allowance or charge rate per unit. Positive for charges, negative for allowances
    var allowanceOrChargeQty: Double? = null, //06 x[1-10] together with 07, quantity bases when allowance or charge quantity is different from the purchase order or invoice quantity TODO(Clarify)
    var unitOfMeasure: String? = null, //07 O[2] mandatory with 06
    var allowanceOrChargeTotalAmount: Double? = null, //08 X[1-15] if used, sets the total amount in hundredths of dollars (negative -> allowance, positive -> charge)
    var allowanceOrChargePercent: Double? = null, //09 X[1-16] percent used together with 10
    var dollarBasisForPercent: Double? = null, //10 X[1-9] dollar basis to be used in the percent calculation of the allowance/charge or tax. Used with 9, to apply the percent to this dollar basis.
    var optionNumber: String? = null //11 O[1/20] number of the selected promotion when more than one is available. Makes 03 required
){
    init {
        putInt("01", this.allowanceOrChargeCode)
        putString("02", this.handlingMethod)
        putString("07", this.unitOfMeasure)
    }

    fun putString(key: String, value: String?) {
        when (key) {
            "02" -> this.handlingMethod = if (value.isNullOrBlank() || value !in MethodOfHandling.values()) null else value
            "03" -> this.allowanceOrChargeNumber = value
            "04" -> this.exceptionNumber = value
            "07" -> this.unitOfMeasure = if(value.isNullOrBlank() || value !in UOM.supportedValues()) null else value
            "11" -> this.optionNumber = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "02" -> this.handlingMethod
            "03" -> this.allowanceOrChargeNumber
            "04" -> this.exceptionNumber
            "07" -> this.unitOfMeasure
            "11" -> this.optionNumber
            else -> null
        }
    }

    fun putDouble(key: String, value: Double?) {
        when (key) {
            "05" -> this.allowanceOrChargeRate = value
            "06" -> this.allowanceOrChargeQty = value
            "08" -> this.allowanceOrChargeTotalAmount = value
            "09" -> this.allowanceOrChargePercent = value
            "10" -> this.dollarBasisForPercent = value
        }
    }

    fun getDouble(key: String): Double? {
        return when (key) {
            "05" -> this.allowanceOrChargeRate
            "06" -> this.allowanceOrChargeQty
            "08" -> this.allowanceOrChargeTotalAmount
            "09" -> this.allowanceOrChargePercent
            "10" -> this.dollarBasisForPercent
            else -> null
        }
    }

    fun putInt(key: String, value: Int?) {
        when (key) {
            "01" -> this.allowanceOrChargeCode = if (value == null || value !in AllowanceOrChargeCode.allowedValues()) null else value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "01" -> this.allowanceOrChargeCode
            else -> null
        }
    }
}