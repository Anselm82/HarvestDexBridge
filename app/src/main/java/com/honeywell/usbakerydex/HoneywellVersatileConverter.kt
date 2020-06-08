package com.honeywell.usbakerydex

import com.honeywell.usbakerydex.honeywelldex.model.G72Block
import com.honeywell.usbakerydex.honeywelldex.model.HoneywellDexRequest
import com.honeywell.usbakerydex.honeywelldex.model.HoneywellDexResponse
import com.honeywell.usbakerydex.honeywelldex.model.G83Block as ItemBlock
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellUnitOfMeasure
import com.honeywell.usbakerydex.versatiledex.model.*
import com.honeywell.usbakerydex.versatiledex.utils.*
import java.lang.Exception
import com.honeywell.usbakerydex.honeywelldex.model.Invoice as HONInvoice

class HoneywellVersatileConverter {

    companion object {

        fun toVersatileDexRequest(honeywellDexRequest: HoneywellDexRequest) : VersatileDexRequest {
            return VersatileDexRequest(
                getVendor(honeywellDexRequest),
                getStops(honeywellDexRequest)
            )
        }

        private fun getStops(honeywellDexRequest: HoneywellDexRequest): List<Stop> {
            val list = mutableListOf<Stop>()
            //due to embedded mode, num
            //TODO(is this going to be like this always?) ask Jenni
            val count = 1
            for(i in 0 until count) {
                val stop = Stop(
                    "$count",
                    getCustomer(honeywellDexRequest),
                    getAdditionalOptions(honeywellDexRequest),
                    getInvoices(honeywellDexRequest)
                )
                list.add(stop)
            }
            return list
        }

        private fun getInvoices(honeywellDexRequest: HoneywellDexRequest): List<Invoice>? {
            val invoicesList = mutableListOf<Invoice>()
            val invoices = honeywellDexRequest.transaction?.invoices ?: mapOf()
            val dexVersion = honeywellDexRequest.config?.retailer?.dexVersion ?: "005010UCS"
            val vendorCode = honeywellDexRequest.config?.retailer?.dunsNumber ?: ""
            for (invoiceNumber in invoices.keys) {
                val invoice = invoices[invoiceNumber]
                val orderType = invoice?.g82?.typeFlag?.convertToVersatileOrderType() ?: VersatileOrderType.DELIVERY
                val orderNumber = if(orderType == VersatileOrderType.DELIVERY) invoice?.g82?.receiverDunsNumber ?: "" else invoice?.g82?.supplierDunsNumber ?: ""
                val date = if(!orderNumber.isBlank()) invoice?.g82?.deliveryReturnDate ?: 0L else 0L
                val versatileInvoice = Invoice(
                    invoiceNumber,
                    orderType,
                    orderNumber,
                    date,
                    null,
                    null,
                    null,
                    getInvoiceAdjustments(invoice, vendorCode),
                    getItems(invoice, dexVersion, vendorCode)
                )
                invoicesList.add(versatileInvoice)
            }
            return invoicesList
        }

        private fun getItems(invoice: HONInvoice?, dexVersion: String, vendorCode: String): List<Item>? {
            val itemsList = mutableListOf<Item>()
            if(!invoice?.items?.keys.isNullOrEmpty()) {
                for (itemNumber in invoice!!.items!!.keys) {
                    val honItem = invoice.items!![itemNumber]
                    val item = Item(
                        dexVersion,
                        honItem?.upc ?: "",
                        honItem?.upcCaseCode ?: "",
                        if(honItem!!.unitOfMeasure == HoneywellUnitOfMeasure.CASE) "" else "", //Unsupported, must come from json file at movilizer.
                        "", //Actually other quantity is not supported. Always dex by each.
                        honItem.description ?: honItem.materialNumber!!,
                        "${honItem.quantity}",
                        "${honItem.itemListCost}",
                        honItem.unitOfMeasure?.convertToVersatilePackType() ?: VersatilePackType.EACH,
                        getItemAdjustment(honItem, vendorCode),
                        honItem.materialNumber,
                        "FALSE" //Unsupported, must come from json file at movilizer.
                    )
                    itemsList.add(item)
                }
            }
            return itemsList
        }

        private fun getItemAdjustment(honItem: ItemBlock, vendorCode: String): List<ItemAdjustment>? {
            val list = mutableListOf<ItemAdjustment>()
            for(adjustmentNumber in honItem.adjustments.keys) {
                val adjustment = honItem.adjustments[adjustmentNumber]
                val versatileFlag = getVersatileItemFlagForAmount(adjustment!!)
                val versatileItemAdjustment = ItemAdjustment(
                    getVersatileTypeFromAmount(adjustment),
                    "${adjustment.allowanceOrChargeCode}",
                    adjustment.handlingMethod.convertToVersatileHandlingCode(),
                    vendorCode,
                    versatileFlag,
                    "${getVersatileItemAdjustmentValue(adjustment, versatileFlag)}"
                )
                list.add(versatileItemAdjustment)
            }
            return list
        }

        private fun getVersatileItemAdjustmentValue(
            g72: G72Block,
            versatileFlag: VersatileAdjustmentFlag
        ) = when(versatileFlag) {
            VersatileAdjustmentFlag.PERCENTAGE -> "${g72.allowanceOrChargePercent}/${g72.dollarBasisForPercent}"
            VersatileAdjustmentFlag.TOTAL -> g72.allowanceOrChargeTotalAmount
            VersatileAdjustmentFlag.RATE_PER_QUANTITY -> "${g72.allowanceOrChargeRate}/${g72.allowanceOrChargeQty}"
        }

        private fun getVersatileInvoiceAdjustmentValue(
            g72: G72Block,
            versatileFlag: VersatileInvoiceAdjustmentFlag
        ) = when(versatileFlag) {
            VersatileInvoiceAdjustmentFlag.PERCENTAGE -> "${g72.allowanceOrChargePercent}/${g72.dollarBasisForPercent}"
            VersatileInvoiceAdjustmentFlag.TOTAL -> g72.allowanceOrChargeTotalAmount
        }

        private fun getVersatileItemFlagForAmount(g72: G72Block) = when {
            g72.allowanceOrChargeRate != null -> VersatileAdjustmentFlag.RATE_PER_QUANTITY
            g72.allowanceOrChargePercent != null -> VersatileAdjustmentFlag.PERCENTAGE
            else -> VersatileAdjustmentFlag.TOTAL
        }

        private fun getVersatileTypeFromAmount(g72: G72Block): VersatileAdjustmentType {
            if(g72.allowanceOrChargePercent.isNotNullAndPositive() || g72.allowanceOrChargeRate.isNotNullAndPositive()
                || g72.allowanceOrChargeTotalAmount.isNotNullAndPositive())
                return VersatileAdjustmentType.CHARGE
            return VersatileAdjustmentType.ALLOWANCE
        }

        private fun getInvoiceAdjustments(invoice: HONInvoice?, vendorCode: String): List<InvoiceAdjustment> {
            val list = mutableListOf<InvoiceAdjustment>()
            if(invoice?.adjustments?.keys != null) {
                for (adjustmentNumber in invoice.adjustments.keys) {
                    val adjustment = invoice.adjustments[adjustmentNumber]
                    val versatileFlag = getVersatileInvoiceFlagForAmount(adjustment!!)
                    val versatileItemAdjustment = InvoiceAdjustment(
                        getVersatileTypeFromAmount(adjustment),
                        "${adjustment.allowanceOrChargeCode}",
                        adjustment.handlingMethod.convertToVersatileHandlingCode(),
                        vendorCode,
                        versatileFlag,
                        "${getVersatileInvoiceAdjustmentValue(adjustment, versatileFlag)}"
                    )
                    list.add(versatileItemAdjustment)
                }
            }
            return list
        }

        private fun getVersatileInvoiceFlagForAmount(g72: G72Block) = when {
            g72.allowanceOrChargePercent != null -> VersatileInvoiceAdjustmentFlag.PERCENTAGE
            else -> VersatileInvoiceAdjustmentFlag.TOTAL
        }


        /**
         * I doubt they are set in the Honeywell Dex Side.
         */
        private fun getAdditionalOptions(honeywellDexRequest: HoneywellDexRequest): Stop.AdditionalOptions? {
            return Stop.AdditionalOptions(
                dexVersion = null,
                testData = null,
                promptBeforeACK = null,
                enableG8602 = null,
                enable895G84 = null,
                enable895G8403 = null,
                ackG88only = null,
                enable895GAdjOnly = null,
                exclude895G84HCode06 = null,
                preserveUPC12 = null,
                rejectAdjItemCostReduction = null,
                rejectAdjSvrNewItem = null
            )
        }

        //Supplier from Honeywell. All the other parameters are not sent.
        private fun getCustomer(honeywellDexRequest: HoneywellDexRequest): Customer {
            return Customer(
                honeywellDexRequest.config?.supplier?.signatureKey ?: 123L,
                honeywellDexRequest.config?.supplier?.dunsNumber ?: "",
                honeywellDexRequest.config?.supplier?.location ?: "",
                honeywellDexRequest.config?.supplier?.communicationsId ?: "1111111111"
            )
        }

        //Retailer from honeywell
        private fun getVendor(honeywellDexRequest: HoneywellDexRequest): Vendor {
            //TODO(REMOVE THIS TEMPORARY FIX)
            try {
                honeywellDexRequest.config?.retailer?.communicationsId?.toInt()
            } catch (e: Exception) {
                honeywellDexRequest.config?.retailer?.communicationsId = "1111111111"
            }
            //END TODO
            return Vendor(
                null,
                null,
                honeywellDexRequest.config?.retailer?.dunsNumber ?: "",
                honeywellDexRequest.config?.retailer?.location ?: "",
                honeywellDexRequest.config?.retailer?.communicationsId ?: "1111111111",
                honeywellDexRequest.config?.retailer?.dexVersion ?: "005010UCS"
            )
        }

        fun toHoneywellDexResponse(versatileDexResponse: VersatileDexResponse) : HoneywellDexResponse {
            //TODO("Parse response object")
            return HoneywellDexResponse()
        }
    }
}

private fun HoneywellHandlingMethodCodes.convertToVersatileHandlingCode() = VersatileHandlingCode.fromValue(this.value)

private fun HoneywellUnitOfMeasure.convertToVersatilePackType() = when(this) {
    HoneywellUnitOfMeasure.CASE -> VersatilePackType.CASE
    else -> VersatilePackType.EACH
}

/**
 * When G8201 equals "D" (debit), information in this segment is for a delivery. When G8201 equals "C" (credit), information in this segment is for a return.
 */
private fun HoneywellTypeFlag.convertToVersatileOrderType() = when(this) {
    HoneywellTypeFlag.CREDIT -> VersatileOrderType.RETURN
    else -> VersatileOrderType.DELIVERY
}

private fun Double?.isNotNullAndPositive() = if(this != null) this >= 0 else false