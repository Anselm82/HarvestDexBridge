package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import com.honeywell.usbakerydex.versatiledex.utils.isDecimal
import com.honeywell.usbakerydex.versatiledex.utils.isNumeric
import com.honeywell.usbakerydex.versatiledex.utils.validLength

open class ItemAdjustment(private val adjustmentType : VersatileAdjustmentType, private val adjustmentCode: String,
                          private val handlingCode: VersatileHandlingCode, private val vendorCode: String,
                          private val flag: VersatileAdjustmentFlag,private val adjustment: String) {

    companion object {
        const val SECTION = "Item Adjustment"
        const val ADJUSTMENT = "ADJUSTMENT"
    }

    fun validAdjustment() : Boolean {
        return validAdjustmentType() && validAdjustmentCode() && validHandlingCode() && validVendorCode() && validFlag() && validAdjustmentFlagValue()
    }

    private fun validAdjustmentFlagValue(): Boolean {
        if(flag == VersatileAdjustmentFlag.TOTAL) {
            return adjustment.validLength(5) && adjustment.isDecimal()
        } else if (flag == VersatileAdjustmentFlag.PERCENTAGE || flag == VersatileAdjustmentFlag.RATE_PER_QUANTITY) {
            val values = adjustment.split(PERCENTAGE_SPLITER)
            val value = values[0]
            val percentage = values[1]
            return !value.isNullOrBlank() && value.validLength(5) && value.isDecimal() && !percentage.isNullOrBlank() && percentage.validLength(5) && percentage.isDecimal()
        }
        return false
    }

    private fun validFlag(): Boolean {
        return VersatileAdjustmentFlag.values().contains(flag)
    }

    private fun validVendorCode(): Boolean {
        return !vendorCode.isNullOrBlank() && vendorCode.validLength(16) && vendorCode.isNumeric()
    }

    private fun validHandlingCode(): Boolean {
        return VersatileHandlingCode.values().contains(handlingCode)
    }

    private fun validAdjustmentCode(): Boolean {
        return !adjustmentCode.isNullOrBlank() && adjustmentCode.validLength(3) && adjustmentCode.isNumeric()
    }

    private fun validAdjustmentType(): Boolean {
        return VersatileAdjustmentType.values().contains(adjustmentType)
    }

    override fun toString(): String {
        if(validAdjustment())
            return "$ADJUSTMENT $adjustmentType $adjustmentCode $handlingCode $vendorCode $flag $adjustment$NEW_LINE"
        else
            throw MandatoryFieldException(SECTION)
    }
}