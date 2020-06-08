package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import com.honeywell.usbakerydex.versatiledex.utils.isAlphanumeric
import com.honeywell.usbakerydex.versatiledex.utils.isDecimal
import com.honeywell.usbakerydex.versatiledex.utils.isNumeric
import com.honeywell.usbakerydex.versatiledex.utils.validLength

data class Item(val dexVersion: String = "5010", val itemCode: String, val caseCode: String? = "", val caseCount: String? = "", val itemsByCase: String? = "",
                val description: String, val quantity: String, val price: String, val packType: VersatilePackType = VersatilePackType.EACH,
                val itemAdjustments: List<ItemAdjustment>?, val sku: String?,
                val preserveCost: String?) {

    companion object {
        const val SECTION = "Item"
        const val UPC = "UPC"
        const val GTIN = "GTIN"
        const val DESCRIPTION = "DESC"
        const val QUANTITY = "QTY"
        const val PRICE = "PRICE"
        const val PACK_TYPE = "PACKTYPE"
        const val SKU = "SKU"
        const val PRESERVE_COST = "PRESERVE_COST"
    }

    private fun validItem() : Boolean {
        return validItemCode() && validCaseCode() && validCaseCount() && validItemsByCase()
    }

    private fun validItemCode() = if(dexVersion.cleanUCS().toInt() >= 5010) //GTIN
            itemCode.validLength(8, 14) && itemCode.isNumeric()
        else //UPC
            itemCode.validLength(10, 12) && itemCode.isNumeric()

    private fun validCaseCode() = if(caseCode.isNullOrBlank()) true else validItemCode() && !caseCode.isNullOrBlank() && if(dexVersion.cleanUCS().toInt() >= 5010) //GTIN
        caseCode.validLength(8, 14) && caseCode.isNumeric()
    else //UPC
        caseCode.validLength(10, 12) && caseCode.isNumeric()

    private fun validCaseCount() =  if(caseCount.isNullOrBlank()) true else validCaseCode() && !caseCount.isNullOrBlank() && caseCount.validLength(4) && caseCount.isNumeric()
    private fun validItemsByCase() = if(itemsByCase.isNullOrBlank()) true else validCaseCount() && !itemsByCase.isNullOrBlank() && itemsByCase.validLength(3) && itemsByCase.isNumeric()
    private fun validDescription() = description.validLength(20) && description.isAlphanumeric()
    private fun validQuantity() = quantity.validLength(4) && quantity.isNumeric()
    private fun validPrice() = price.validLength(5) && quantity.isDecimal()
    private fun validSku() = !sku.isNullOrBlank() && sku.validLength(15) && sku.isAlphanumeric()
    private fun validPreserveCost() = !preserveCost.isNullOrBlank() && preserveCost.isBoolean()

    private fun validAdjustments() : Boolean {
        return when {
            itemAdjustments.isNullOrEmpty() -> true
            itemAdjustments.size <= 10 -> {
                !itemAdjustments.map { item -> item.validAdjustment() }.contains(false)
            }
            else -> false
        }
    }

    private fun validPackType() = VersatilePackType.values().contains(packType)

    private fun isMandatoryDataSetAndValid() = validItem() && validQuantity() && validPrice() && validPackType()

    override fun toString(): String {
        if(isMandatoryDataSetAndValid()) {
            var item = ""
            if(validSku())
                item += "$SKU $sku$NEW_LINE"
            item += if (dexVersion.cleanUCS().toInt() >= 5010) "$GTIN $itemCode $caseCode $caseCount $itemsByCase$NEW_LINE" else "$UPC $itemCode $caseCode $caseCount $itemsByCase$NEW_LINE"
            if(validDescription())
                item += "$DESCRIPTION \"$description\"$NEW_LINE"
            else if(validSku())
                item += "$DESCRIPTION \"$sku\"$NEW_LINE"
            else
                item += "$DESCRIPTION \"NO DESCRIPTION\"$NEW_LINE"
            item += "$QUANTITY $quantity$NEW_LINE"
            item += "$PRICE $price$NEW_LINE"
            item += "$PACK_TYPE ${packType.value}$NEW_LINE"
            if(validAdjustments() && !itemAdjustments.isNullOrEmpty())
                item += itemAdjustments.joinToString("\n") { adjustment -> adjustment.toString() }
            if(validPreserveCost())
                item += "$PRESERVE_COST $preserveCost$NEW_LINE"
           return item
        } else {
            throw MandatoryFieldException("$SECTION: ${validItem()}, ${validDescription()}, ${validQuantity()}, ${validPrice()}, ${validPackType()}.")
        }
    }
}
