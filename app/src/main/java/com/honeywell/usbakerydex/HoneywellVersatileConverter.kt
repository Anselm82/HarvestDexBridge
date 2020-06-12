package com.honeywell.usbakerydex

import com.honeywell.usbakerydex.honeywelldex.model.*
import com.honeywell.usbakerydex.honeywelldex.model.G83Block as ItemBlock
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellInitiatorCode
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellUnitOfMeasure
import com.honeywell.usbakerydex.versatiledex.model.*
import com.honeywell.usbakerydex.versatiledex.utils.*
import java.lang.Exception
import com.honeywell.usbakerydex.honeywelldex.model.Invoice as HONInvoice

class HoneywellVersatileConverter {

    companion object {

        fun toVersatileDexRequest(honeywellDexRequest: HoneywellDexRequest): VersatileDexRequest {
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
            for (i in 0 until count) {
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
                val orderType = invoice?.g82?.typeFlag?.convertToVersatileOrderType()
                    ?: VersatileOrderType.DELIVERY
                val orderNumber =
                    if (orderType == VersatileOrderType.DELIVERY) invoice?.g82?.receiverDunsNumber
                        ?: "" else invoice?.g82?.supplierDunsNumber ?: ""
                val date =
                    if (!orderNumber.isBlank()) invoice?.g82?.deliveryReturnDate ?: 0L else 0L
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

        private fun getItems(
            invoice: HONInvoice?,
            dexVersion: String,
            vendorCode: String
        ): List<Item>? {
            val itemsList = mutableListOf<Item>()
            if (!invoice?.items?.keys.isNullOrEmpty()) {
                for (itemNumber in invoice!!.items!!.keys) {
                    val honItem = invoice.items!![itemNumber]
                    val item = Item(
                        dexVersion,
                        honItem?.upc ?: "",
                        honItem?.upcCaseCode ?: "",
                        if (honItem!!.unitOfMeasure == HoneywellUnitOfMeasure.CASE) "" else "", //Unsupported, must come from json file at movilizer.
                        "", //Actually other quantity is not supported. Always dex by each.
                        honItem.description ?: honItem.materialNumber!!,
                        "${honItem.quantity}",
                        "${honItem.itemListCost}",
                        honItem.unitOfMeasure?.convertToVersatilePackType()
                            ?: VersatilePackType.EACH,
                        getItemAdjustment(honItem, vendorCode),
                        honItem.materialNumber,
                        "FALSE" //Unsupported, must come from json file at movilizer.
                    )
                    itemsList.add(item)
                }
            }
            return itemsList
        }

        private fun getItemAdjustment(
            honItem: ItemBlock,
            vendorCode: String
        ): List<ItemAdjustment>? {
            val list = mutableListOf<ItemAdjustment>()
            for (adjustmentNumber in honItem.adjustments.keys) {
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
        ) = when (versatileFlag) {
            VersatileAdjustmentFlag.PERCENTAGE -> "${g72.allowanceOrChargePercent}/${g72.dollarBasisForPercent}"
            VersatileAdjustmentFlag.TOTAL -> g72.allowanceOrChargeTotalAmount
            VersatileAdjustmentFlag.RATE_PER_QUANTITY -> "${g72.allowanceOrChargeRate}/${g72.allowanceOrChargeQty}"
        }

        private fun getVersatileInvoiceAdjustmentValue(
            g72: G72Block,
            versatileFlag: VersatileInvoiceAdjustmentFlag
        ) = when (versatileFlag) {
            VersatileInvoiceAdjustmentFlag.PERCENTAGE -> "${g72.allowanceOrChargePercent}/${g72.dollarBasisForPercent}"
            VersatileInvoiceAdjustmentFlag.TOTAL -> g72.allowanceOrChargeTotalAmount
        }

        private fun getVersatileItemFlagForAmount(g72: G72Block) = when {
            g72.allowanceOrChargeRate != null -> VersatileAdjustmentFlag.RATE_PER_QUANTITY
            g72.allowanceOrChargePercent != null -> VersatileAdjustmentFlag.PERCENTAGE
            else -> VersatileAdjustmentFlag.TOTAL
        }

        private fun getVersatileTypeFromAmount(g72: G72Block): VersatileAdjustmentType {
            if (g72.allowanceOrChargePercent.isNotNullAndPositive() || g72.allowanceOrChargeRate.isNotNullAndPositive()
                || g72.allowanceOrChargeTotalAmount.isNotNullAndPositive()
            )
                return VersatileAdjustmentType.CHARGE
            return VersatileAdjustmentType.ALLOWANCE
        }

        private fun getInvoiceAdjustments(
            invoice: HONInvoice?,
            vendorCode: String
        ): List<InvoiceAdjustment> {
            val list = mutableListOf<InvoiceAdjustment>()
            if (invoice?.adjustments?.keys != null) {
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

        fun toHoneywellDexResponse(
            versatileDexResponse: VersatileDexResponse,
            honeywellDexRequest: HoneywellDexRequest
        ): HoneywellDexResponse {
            return HoneywellDexResponse().apply {
                val invoiceList = getInvoiceEntriesList(versatileDexResponse, honeywellDexRequest)
                this.receiveDexData = ReceiveDexData(
                    getDxsBlock(honeywellDexRequest),
                    invoiceList,
                    getDxeBlock(versatileDexResponse.lines.map { it.invoice }.size, honeywellDexRequest)
                )
            }
        }

        private fun getInvoiceEntriesList(
            versatileDexResponse: VersatileDexResponse,
            honeywellDexRequest: HoneywellDexRequest
        ): List<InvoiceEntry> {
            val entries = mutableListOf<InvoiceEntry>()
            val invoices = versatileDexResponse.lines.map { it.invoice }
            var index = 1
            for(invoice in invoices) {
                val invoiceFromRequest = honeywellDexRequest.transaction!!.invoices[invoice] ?: error("Invoice not found")
                val linesByInvoice = versatileDexResponse.lines.filter { it.invoice == invoice }
                val invoiceStatus = getInvoiceStatus(invoice,
                    linesByInvoice)
                val entry = InvoiceEntry(
                    invoiceStatus,
                    getSTBlock(index, linesByInvoice, honeywellDexRequest),
                    getG89Blocks(invoiceFromRequest, linesByInvoice),
                    getG87Block(invoiceFromRequest, linesByInvoice),
                    getG86Block(linesByInvoice, honeywellDexRequest),
                    getG85Block(invoiceFromRequest),
                    getG84Block(linesByInvoice, honeywellDexRequest),
                    getSeBlock(index, linesByInvoice)
                )
                index++
                entries.add(entry)
            }
            return entries
        }

        private fun getSeBlock(
            index: Int,
            linesByInvoice: List<VersatileDexResponseEntry>
        ): SEBlock? {
            return SEBlock(
                transactionSetControlNumber = index,
                segmentCount = linesByInvoice.count()
            )
        }

        private fun getG84Block(
            linesByInvoice: List<VersatileDexResponseEntry>,
            honeywellDexRequest: HoneywellDexRequest
        ): G84Block? {
            TODO("Not yet implemented")
        }

        private fun getG85Block(
            invoice: HONInvoice
        ): G85Block? {
            return G85Block(
                invoice.adjustments
            )
        }

        private fun getG86Block(
            linesByInvoice: List<VersatileDexResponseEntry>,
            honeywellDexRequest: HoneywellDexRequest
        ): G86Block? {
            TODO("Not yet implemented")
        }

        private fun getG87Block(
            invoice: HONInvoice,
            linesByInvoice: List<VersatileDexResponseEntry>
        ): G87Block? {
            return G87Block(
                HoneywellInitiatorCode.RETAILER,
                invoice.g82.typeFlag,
                invoice.g82.invoiceNumber,
                "", //TODO("Integrity check? Ignored on MEL")
                linesByInvoice.filter { it.adjustmentType in arrayOf(VersatileResponseAdjustmentType.ADJ_INVC_ALLOWANCE,
                    VersatileResponseAdjustmentType.ADJ_INVC_CHARGE,
                    VersatileResponseAdjustmentType.ADJ_INVC_KILL_PREVIOUS_ALLOW_CHG  ) }.size,
                null //TODO("Ask for availability on Versatile")
            )
        }

        private fun getG89Blocks(
            invoice: HONInvoice,
            linesByInvoice: List<VersatileDexResponseEntry>
        ): List<G89Block>? {
            val g89Blocks = mutableListOf<G89Block>()
            val itemAdjustments = linesByInvoice.groupBy { (it.params[VersatileResponseParams.ITEM_INDEX] as String?)!!.toInt() }
            for (itemAdjustment in itemAdjustments.entries) {
                val g89Block = getG89Block(invoice, itemAdjustment.key, itemAdjustment.value)
                g89Blocks.add(g89Block)
            }
            return g89Blocks
        }

        private fun getG89Block(
            invoice: HONInvoice,
            index: Int,
            itemAdjustments: List<VersatileDexResponseEntry>?
        ): G89Block {
            /*val quantity =
                (itemAdjustment.params[VersatileResponseParams.QTY] as String?)?.toDoubleOrNull()
            val upc = itemAdjustment.params[VersatileResponseParams.UPC] as String?
            val unitOfMeasure =
                itemAdjustment.params[VersatileResponseParams.UNIT_OF_MEASURE] as String?
            val productIdQualifier =
                itemAdjustment.params[VersatileResponseParams.PROD_ID_QUALIFIER] as String?
            val productId = itemAdjustment.params[VersatileResponseParams.PROD_ID] as String?
            val caseCode = itemAdjustment.params[VersatileResponseParams.CASE_UPC] as String?
            val itemListCost =
                (itemAdjustment.params[VersatileResponseParams.ITEM_LIST_COST] as String?)?.toDoubleOrNull()
            val pack =
                (itemAdjustment.params[VersatileResponseParams.PACK] as String?)?.toIntOrNull()
            val innerPack =
                (itemAdjustment.params[VersatileResponseParams.INNER_PACK] as String?)?.toIntOrNull()
            val caseIdQualifier =
                itemAdjustment.params[VersatileResponseParams.CASE_QUALIFIER] as String?
            val caseId = itemAdjustment.params[VersatileResponseParams.CASE_ID] as String?*/
            //TODO(CHECK if there is any new ones agains g83 items and treat is as added Item)
            val originalItem = invoice.items?.get("$index")
            if(originalItem != null) {
                val g72List = getG72AdjustmentsFor(itemAdjustments, originalItem)
                val g89 = G89Block(
                    dsdSequenceNumber = "$index",
                    quantity = originalItem.quantity, //if qty adjustment change here also
                    unitOfMeasure = originalItem.unitOfMeasure?.value,
                    caseCode = originalItem.upcCaseCode,
                    caseId = originalItem.caseId,
                    caseIdQualifier = originalItem.caseIdQualifier,
                    innerPack = null,
                    itemListCost = originalItem.itemListCost,
                    pack = originalItem.pack,
                    productId = originalItem.productId,
                    productIdQualifier = originalItem.productIdQualifier,
                    upc = originalItem.upc,
                    g72List = g72List
                )
            }

            for(key in itemAdjustment.keys) {
                val item = itemAdjustment[key]
                if (item != null) {
                    val originalItem = invoice.items?.get(key)
                    var g72 = G72Block(null,
                        null, null,
                        null,null,null,
                        null,null,
                        null,null,null)

                    )
                    for (adjustment in item) {
                        val type = adjustment.adjustmentType
                        originalItem.adjustments
                        when (type) {
                            VersatileResponseAdjustmentType.ADJ_PACKTYPE ->

                        }
                    }
                }

                /**/
            }

        }

        private fun getG72AdjustmentsFor(
            adjustments: List<VersatileDexResponseEntry>?,
            originalItem: com.honeywell.usbakerydex.honeywelldex.model.G83Block
        ): List<G72Block>? {
            if(adjustments.isNullOrEmpty())
                return null
            val itemAdjustments = adjustments.filter { it.adjustmentType in ITEM_ADJUSTMENT_TYPES }
            return if(itemAdjustments.isNullOrEmpty())
                null
            else {
                val g72List = mutableListOf<G72Block>()
                if(itemAdjustments.firstOrNull { it.ucsType == 895 } != null)
                    g72List.add(removeItemAdjustment())
                for (itemAdjustment in itemAdjustments) {
                    val g72Block = getG72Block(itemAdjustment, originalItem)
                    g72List.add(g72Block)
                }
                g72List
            }
        }

        enum class ItemType(val value: String) {
            SALES("00"),
            SAMPLES("01"),
            MEMOTICKET("02"),
            RETURNSALES("03"),
            ONHANDS("04"),
            BUYBACK("05"),
            RETURNS("06")
        }

        private fun getG72Block(
            itemAdjustment: VersatileDexResponseEntry,
            originalItem: ItemBlock
        ): G72Block {
            val isRate = itemAdjustment.params[VersatileResponseParams.RATE] != null
            val isTotal = itemAdjustment.params[VersatileResponseParams.PRICE] != null
            val code = if(isTotal) 501 else if(isRate) if(originalItem.type == ItemType.SAMPLES.value) 1 else 97 else 0
            when(itemAdjustment.adjustmentType) {
                VersatileResponseAdjustmentType.ADJ_PRICE -> ,
                    VersatileResponseAdjustmentType.ADJ_ALLOWANCE -> ,
            VersatileResponseAdjustmentType.ADJ_QTY ->,
                VersatileResponseAdjustmentType.ADJ_CHARGE ->,
            VersatileResponseAdjustmentType.ADJ_DEL_ITEM ->

            }
            return G72Block(
                code,
                HoneywellHandlingMethodCodes.OFF_INVOICE,



            )
        }

        private fun removeItemAdjustment(): G72Block {
            return G72Block(
                96,
            HoneywellHandlingMethodCodes.NOT_PROCESSED,
            "REMOVE"
            )
        }

        private fun getSTBlock(
            index: Int,
            linesByInvoice: List<VersatileDexResponseEntry>,
            honeywellDexRequest: HoneywellDexRequest
        ): STBlock? {
            return STBlock(
                linesByInvoice.map { it.ucsType }.first().toString(),
                index.toLong(),
                null
            )
        }

        private fun getInvoiceStatus(invoice: String, entries: List<VersatileDexResponseEntry>): InvoiceStatus {
            val entry = entries.first { it.adjustmentType == VersatileResponseAdjustmentType.INVC_STATUS }
            val status = VersatileResponseInvoiceStatus.fromValue((entry.getValueOrDefault(VersatileResponseParams.INVOICE_STATUS, entry.params, VersatileResponseInvoiceStatus.CLOSED.value.toString()) as String).toInt())
            val isAdjusted = status != VersatileResponseInvoiceStatus.CLOSED
            return InvoiceStatus(invoice, if(isAdjusted) HoneywellInvoiceStatus.ADJUSTED.value else HoneywellInvoiceStatus.ACKNOWLEDGED.value)
        }

        private fun getDxeBlock(
            numberOfIncludedSets: Int,
            honeywellDexRequest: HoneywellDexRequest
        ): DxeBlock? {
            return DxeBlock(
                honeywellDexRequest.config!!.transmissionControlNumber,
                numberOfIncludedSets
            )
        }

        private fun getDxsBlock(
            honeywellDexRequest: HoneywellDexRequest
        ): DxsBlock? {
            return DxsBlock(
                honeywellDexRequest.config?.retailer?.communicationsId,
                FunctionalIdentifier.DX.value,
                honeywellDexRequest.config?.retailer?.dexVersion,
                honeywellDexRequest.config?.transmissionControlNumber,
                honeywellDexRequest.config?.supplier?.communicationsId,
                honeywellDexRequest.config?.testIndicator?.let { TestIndicator.fromValue(it).value }
            )
        }
    }
}

private fun VersatileResponseInvoiceStatus.convertToHoneywellString() = when(this) {
    //Movilizer only accepts "Acknowledged" or "Adjusted"... I guess it depends on the operations performed.
    VersatileResponseInvoiceStatus.CLOSED -> "Closed" // "Acknowledged"
    VersatileResponseInvoiceStatus.RECEIVED -> "Received"
    VersatileResponseInvoiceStatus.SENT -> "Sent"
    VersatileResponseInvoiceStatus.UNSENT -> "Unsent"
}

private fun HoneywellHandlingMethodCodes.convertToVersatileHandlingCode() =
    VersatileHandlingCode.fromValue(this.value)

private fun HoneywellUnitOfMeasure.convertToVersatilePackType() = when (this) {
    HoneywellUnitOfMeasure.CASE -> VersatilePackType.CASE
    else -> VersatilePackType.EACH
}

/**
 * When G8201 equals "D" (debit), information in this segment is for a delivery. When G8201 equals "C" (credit), information in this segment is for a return.
 */
private fun HoneywellTypeFlag.convertToVersatileOrderType() = when (this) {
    HoneywellTypeFlag.CREDIT -> VersatileOrderType.RETURN
    else -> VersatileOrderType.DELIVERY
}

private fun Double?.isNotNullAndPositive() = if (this != null) this >= 0 else false