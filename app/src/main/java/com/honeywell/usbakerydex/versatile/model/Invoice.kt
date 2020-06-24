package com.honeywell.usbakerydex.versatile.model

import com.honeywell.usbakerydex.versatile.utils.*
import com.honeywell.usbakerydex.versatile.utils.isAlphanumeric
import com.honeywell.usbakerydex.versatile.utils.validLength

data class Invoice(
    private var id: String,
    private var orderType: VersatileOrderType = VersatileOrderType.DELIVERY,
    private var number: String,
    private var date: String,
    private var userCanAdjustQty: String?,
    private var userCanAdjustCost: String?,
    private var userCanAdjustUOM: String?,
    private var invAdjustments: List<InvoiceAdjustment>?,
    private var items: List<Item>?
) {

    companion object {
        const val SECTION = "Invoice"
        const val INVOICE = "INVOICE"
        const val TYPE = "ORDER_TYPE"
        const val NUMBER = "ORDER_NUMBER"
        const val USER_CAN_ADJUST_QTY = "USER_CAN_ADJUST_QTY"
        const val USER_CAN_ADJUST_COST = "USER_CAN_ADJUST_COST"
        const val USER_CAN_ADJUST_UOM = "USER_CAN_ADJUST_UOM"
    }

    private fun validId() = id.validLength(22) && id.isAlphanumeric()
    private fun validType() = VersatileOrderType.values().contains(orderType)
    private fun validDate() = date.toLong() != 0L
    private fun validNumber(): Boolean {
        return !number.isBlank() && number.validLength(22) && number.isAlphanumeric()
    }

    private fun validUserCanAdjustQty() =
        !userCanAdjustQty.isNullOrBlank() && userCanAdjustQty!!.isBoolean()

    private fun validUserCanAdjustCost() =
        !userCanAdjustCost.isNullOrBlank() && userCanAdjustCost!!.isBoolean()

    private fun validUserCanAdjustUOM() =
        !userCanAdjustUOM.isNullOrBlank() && userCanAdjustUOM!!.isBoolean()

    private fun isMandatoryDataSetAndValid() = validId() && validType()

    private fun validInvAdjustments(): Boolean {
        return when {
            invAdjustments.isNullOrEmpty() -> true
            invAdjustments!!.size <= 20 -> {
                !invAdjustments!!.map { item -> item.validAdjustment() }.contains(false)
            }
            else -> false
        }
    }

    override fun toString(): String {
        if (isMandatoryDataSetAndValid()) {
            var invoice = ""
            invoice += "$INVOICE $id$NEW_LINE"
            invoice += "$TYPE $orderType$NEW_LINE"
            if (validNumber() && validDate())
                invoice += "$NUMBER \"$number\" ${date} $NEW_LINE"
            if (validUserCanAdjustQty())
                invoice += "$USER_CAN_ADJUST_QTY ${userCanAdjustQty!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validUserCanAdjustCost())
                invoice += "$USER_CAN_ADJUST_COST ${userCanAdjustCost!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validUserCanAdjustUOM())
                invoice += "$USER_CAN_ADJUST_UOM ${userCanAdjustUOM!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validInvAdjustments() && !invAdjustments.isNullOrEmpty())
                invoice += NEW_LINE + invAdjustments!!.joinToString("\n") { adjustment -> adjustment.toString() }
            if (!items.isNullOrEmpty())
                invoice += NEW_LINE + items!!.joinToString("\n") { item -> item.toString() }
            return invoice
        } else
            throw MandatoryFieldException(SECTION)
    }
}


