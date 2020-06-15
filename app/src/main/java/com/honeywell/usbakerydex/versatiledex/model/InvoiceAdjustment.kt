package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*

data class InvoiceAdjustment(
    val adjustmentType: VersatileAdjustmentType,
    val adjustmentCode: String,
    val handlingCode: VersatileHandlingCode,
    val vendorCode: String,
    val flag: VersatileInvoiceAdjustmentFlag,
    val adjustment: String
) : ItemAdjustment(
    adjustmentType,
    adjustmentCode,
    handlingCode,
    vendorCode,
    flag.toVersatileAdjustmentFlag(),
    adjustment
) {

    companion object {
        const val SECTION = "Invoice adjustment"
        const val INVOICE_ADJUSTMENT = "INVADJUSTMENT"
    }

    override fun toString(): String {
        if (validAdjustment())
            return "$INVOICE_ADJUSTMENT $adjustmentType $adjustmentCode $handlingCode $vendorCode $flag $adjustment$NEW_LINE"
        else
            throw MandatoryFieldException(SECTION)
    }
}