package com.honeywell.usbakerydex.dex.model

import com.google.gson.Gson
import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.vo.AllowanceOrChargeCode
import com.honeywell.usbakerydex.dex.model.vo.AllowanceOrChargeCode.Companion.CENTS_OFF
import com.honeywell.usbakerydex.dex.model.vo.FunctionalIdentifier
import com.honeywell.usbakerydex.dex.model.vo.MethodOfHandling
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator
import com.honeywell.usbakerydex.honeywell.model.Configuration
import com.honeywell.usbakerydex.honeywell.model.Initialization
import com.honeywell.usbakerydex.honeywell.model.json.*
import com.honeywell.usbakerydex.honeywell.vo.HInvoiceStatus
import com.honeywell.usbakerydex.versatile.model.*
import com.honeywell.usbakerydex.versatile.utils.*
import org.json.JSONObject
import java.util.*

class DEXTransmission private constructor(
    val configuration: Configuration,
    val initialization: Initialization,
    val transaction: DEXTransaction
) {

    data class Builder(
        var configuration: Configuration? = null,
        var initialization: Initialization? = null,
        var transaction: DEXTransaction? = null
    ) {

        fun with(configuration: Configuration) = apply { this.configuration = configuration }

        fun with(initialization: Initialization) = apply { this.initialization = initialization }

        fun with(transaction: DEXTransaction) = apply { this.transaction = transaction }

        fun build(): DEXTransmission =
            DEXTransmission(configuration!!, initialization!!, transaction!!)
    }

    private var response: VersatileDexResponse? = null
    private val vendors by lazy { buildVendors() }
    private val stops by lazy { buildStops() }
    private val customers by lazy { buildCustomers() }
    private val invoices by lazy { buildInvoices() }

    init {
        transaction.dxs?.dexVersion = configuration.retailer?.dexVersion ?: DEFAULT_DEX_VERSION

        transaction.dxs?.senderIdentificationCode = configuration.supplier?.communicationsId
        transaction.dxs?.testIndicator = TestIndicator.PRODUCTION
        transaction.dxs?.transmissionControlNumber = configuration.transmissionControlNumber
        transaction.dxs?.functionalIdentifierCode = FunctionalIdentifier.DX
        transaction.dxe?.transmissionControlNumber = configuration.transmissionControlNumber
        transaction.dxe?.numberOfTransactionSetsIncluded = transaction.recordList.size
        transaction.recordList.forEach {
            it.st?.implementationConventionRelease =
                transaction.dxs?.dexVersion ?: DEFAULT_DEX_VERSION
            val identifier = it.identifier
            if (identifier is G82) {
                identifier.supplierDunsNumber = configuration.supplier?.dunsNumber
                identifier.receiverDunsNumber = configuration.retailer?.dunsNumber
            }
        }
    }

    fun toVersatile(): String {
        val requests = mutableListOf<VersatileDexRequest>()
        for (i in vendors.indices) {
            val request = VersatileDexRequest(
                vendors[i], stops[i]
            )
            requests.add(request)
        }
        return requests.joinToString { it.toString() + NEW_LINE }
    }

    private fun buildInvoices(): Map<String, List<Invoice>> {
        val map = mutableMapOf<String, List<Invoice>>()
        var count = 0
        transaction.recordList.forEach {
            val vendor = vendors[count]
            val invoiceList = mutableListOf<Invoice>()
            val identifier = it.identifier
            if (identifier is G82) {
                val orderType = identifier.creditDebitFlagCode
                val orderNumber = identifier.supplierDeliveryReturnNumber
                val date = identifier.physicalDeliveryOrReturnDate ?: Date().toYYYYMMDD()
                val invoiceAdjustments = buildInvoiceAdjustments(it.g72s, vendor.duns)
                val items = buildItems(it.messages, vendor)
                val versatileInvoice = Invoice(
                    orderNumber!!,
                    orderType!!.toVersatileOrderType(),
                    orderNumber,
                    date,
                    null,
                    null,
                    null,
                    invoiceAdjustments,
                    items
                )
                invoiceList.add(versatileInvoice)
            }
            map[vendor.duns] = invoiceList
            count++
        }
        return map
    }

    private fun buildItems(
        loops: List<Loop>?,
        vendor: Vendor
    ): List<Item>? {
        val itemsList = mutableListOf<Item>()
        loops?.forEach {
            it.loopInnerBlocks?.forEach { loopInnerBlock ->
                val itemDetail = loopInnerBlock.itemDetail
                if (itemDetail is G83) {
                    val caseCount =
                        if (itemDetail.innerPack != null) "${itemDetail.innerPack}" else ""
                    val item = Item(
                        vendor.dexVersion,
                        itemDetail.upc ?: "",
                        itemDetail.upcCaseCode ?: "",
                        caseCount, //Unsupported, must come from json file at movilizer.
                        "", //Actually other quantity is not supported. Always dex by each.
                        itemDetail.cashRegisterItemDescription
                            ?: "", /// MATNO? from JSON comming form Honeywell
                        "${itemDetail.quantity}",
                        "${itemDetail.itemListCost}",
                        itemDetail.unitOfMeasure?.toVersatilePackType() ?: VersatilePackType.EACH,
                        buildItemAdjustments(loopInnerBlock.itemG72s, vendor),
                        itemDetail.productID, // ????? TODO(What goes here)
                        "FALSE" //Unsupported, must come from json file at movilizer.
                    )
                    itemsList.add(item)
                }
            }
        }
        return itemsList
    }

    private fun buildItemAdjustments(
        g72s: List<G72>?,
        vendor: Vendor
    ): List<ItemAdjustment>? {
        val list = mutableListOf<ItemAdjustment>()
        if (g72s != null) {
            for (g72 in g72s) {
                val versatileFlag = getVersatileItemFlagForAmount(g72)
                val versatileItemAdjustment = ItemAdjustment(
                    getVersatileTypeFromAmount(g72),
                    "${g72.allowanceOrChargeCode}",
                    g72.handlingMethod!!.toVersatileHandlingCode(),
                    vendor.duns,
                    versatileFlag,
                    "${getVersatileItemAdjustmentValue(g72, versatileFlag)}"
                )
                list.add(versatileItemAdjustment)
            }
        }
        return list
    }

    private fun buildInvoiceAdjustments(
        g72s: List<G72>?,
        vendorCode: String
    ): List<InvoiceAdjustment>? {
        val list = mutableListOf<InvoiceAdjustment>()
        g72s?.forEach {
            val g72 = it
            val versatileFlag = getVersatileInvoiceFlagForAmount(g72)
            val versatileItemAdjustment = InvoiceAdjustment(
                getVersatileTypeFromAmount(g72),
                "${g72.allowanceOrChargeCode}",
                g72.handlingMethod!!.toVersatileHandlingCode(),
                vendorCode,
                versatileFlag,
                "${getVersatileInvoiceAdjustmentValue(g72, versatileFlag)}"
            )
            list.add(versatileItemAdjustment)
        }
        return list
    }


    /**
     * Supplier from Honeywell. All the other parameters are not sent.
     */
    private fun buildCustomers(): List<Customer> {
        val customers = mutableListOf<Customer>()
        transaction.recordList.forEach {
            val identifier = it.identifier
            if (identifier is G82) {
                identifier.supplierDunsNumber = configuration.supplier?.dunsNumber
                val customer = Customer(
                    configuration.supplier?.signatureKey ?: 123L,
                    identifier.supplierDunsNumber ?: "123456789",
                    configuration.supplier?.location ?: "0001",
                    transaction.dxs?.senderIdentificationCode ?: "1111111111"
                )
                customers.add(customer)
            }
        }
        return customers
    }

    private fun buildStops(): List<List<Stop>> {
        val list = mutableListOf<List<Stop>>()
        //due to embedded mode, only one stop is allowed
        //TODO(is this going to be like this always?) ask Jenni
        val count = vendors.size
        val countStops = 1 //TODO define how serveral stops could be reported... and then change this number
        for (i in 0 until count) {
            val stopsList = mutableListOf<Stop>()
            for (j in 0 until countStops) {
                val stop = Stop(
                    "${i + 1}",
                    customers[i],
                    buildAdditionalOptions(null),
                    invoices[vendors[i].duns]
                )
                stopsList.add(stop)
            }
            list.add(stopsList)
        }
        return list
    }

    private fun buildAdditionalOptions(jsonObject: JSONObject?): Stop.AdditionalOptions? {
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

    private fun buildVendors(): List<Vendor> {
        val vendors = mutableListOf<Vendor>()
        transaction.recordList.forEach {
            val identifier = it.identifier
            if (identifier is G82) {
                identifier.supplierDunsNumber = configuration.supplier?.dunsNumber
                identifier.receiverDunsNumber = configuration.retailer?.dunsNumber
                val vendor = Vendor(
                    null,
                    null,
                    identifier.receiverDunsNumber!!,
                    configuration.retailer?.location ?: "",
                    transaction.dxs?.receiverIdentificationCode ?: "1111111111",
                    transaction.dxs?.dexVersion ?: DEFAULT_DEX_VERSION
                )
                vendors.add(vendor)
            }
        }
        return vendors
    }

    fun buildResponse(response: VersatileDexResponse): DEXTransmission {
        this.response = response
        val transactionResponse = DEXTransaction(
            this.transaction.dxs,
            this.transaction.recordList,
            this.transaction.dxe
        )
        transactionResponse.recordList.forEach {
            if (it.identifier is G82) {
                val invoiceNumber = (it.identifier as G82).supplierDeliveryReturnNumber!!
                it.st?.transactionSetIdentifierCode = ACK_ADJ_RECORD
                it.identifier = g82Tog87(it.identifier as G82, response, invoiceNumber)
                it.g72s = invoiceG72s(response, invoiceNumber)
                it.extraInformation = g88(it)
                val items = mutableListOf<G89>()
                val responseItemAdjustments = response.itemAdjustments(invoiceNumber)
                it.messages?.forEach {loop ->
                    loop.loopInnerBlocks?.forEach { detail ->
                        val g83 = detail.itemDetail as G83
                        detail.itemDetail = G89(
                            g83.sequenceNumber,
                            g83.quantity,
                            g83.unitOfMeasure,
                            g83.upc,
                            g83.productIDQualifier,
                            g83.productID,
                            g83.upcCaseCode,
                            g83.itemListCost,
                            g83.pack,
                            g83.innerPack,
                            g83.caseIDQualifier,
                            g83.caseID
                        )
                        val adjustedItems = mutableMapOf<G89, List<G72>>()
                        responseItemAdjustments?.forEach { itAdj ->
                            val item = detail.itemDetail as G89
                            var adjustments = mutableListOf<G72>()
                            if (adjustedItems.containsKey(item)) {
                                adjustments = adjustedItems[item] as MutableList<G72>
                            }
                            var newValue: Any?
                            var oldValue: Any?
                            var removeChanges = false

                            var g72 = G72(CENTS_OFF, MethodOfHandling.OFF_INVOICE, "")
                            val lineItem = itAdj.params[VersatileResponseParams.ITEM_INDEX] as Int
                            when (itAdj.adjustmentType) {
                                VersatileResponseAdjustmentType.ADJ_PACKTYPE -> {
                                    oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                    newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                    if(newValue != oldValue) item.pack = newValue as Int?
                                }
                                VersatileResponseAdjustmentType.ADJ_PRICE -> {
                                    oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                    newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                    if(newValue != oldValue) item.itemListCost = newValue as Double?
                                }
                                VersatileResponseAdjustmentType.ADJ_QTY -> {
                                    oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                    newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                    if(newValue != oldValue) item.quantity = newValue as Double?
                                }
                                VersatileResponseAdjustmentType.ADJ_UPC -> {
                                    val dex = transaction.dxs?.dexVersion?.cleanUCS()?.toInt()
                                    if(dex != null && dex <= 4010) {
                                        oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                        newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                        if(newValue != oldValue) item.upc = newValue as String?
                                    }
                                }
                                VersatileResponseAdjustmentType.ADJ_CASEUPC -> {
                                    val dex = transaction.dxs?.dexVersion?.cleanUCS()?.toInt()
                                    if(dex != null && dex <= 4010) {
                                        oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                        newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                        if(newValue != oldValue) item.upcCaseCode = newValue as String?
                                    }
                                }
                                VersatileResponseAdjustmentType.ADJ_PACK -> {
                                    oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                    newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                    if(newValue != oldValue) item.pack = newValue as Int?
                                }
                                VersatileResponseAdjustmentType.ADJ_INNERPACK -> {
                                    oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                    newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                    if(newValue != oldValue) item.innerPack = newValue as Int?
                                }
                                VersatileResponseAdjustmentType.ADJ_PROD_QUALIFIER -> {
                                    val dex = transaction.dxs?.dexVersion?.cleanUCS()?.toInt()
                                    if(dex != null && dex > 4010) {
                                        oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                        newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                        if (newValue != oldValue) item.productIDQualifier = newValue as String?
                                    }
                                }
                                VersatileResponseAdjustmentType.ADJ_PROD_ID -> {
                                    val dex = transaction.dxs?.dexVersion?.cleanUCS()?.toInt()
                                    if(dex != null && dex > 4010) {
                                        oldValue = itAdj.params[VersatileResponseParams.OLD_VALUE]
                                        newValue = itAdj.params[VersatileResponseParams.NEW_VALUE]
                                        if (newValue != oldValue) item.productID = newValue as String?
                                    }
                                }
                                VersatileResponseAdjustmentType.ADJ_ALLOWANCE,
                                VersatileResponseAdjustmentType.ADJ_CHARGE -> {
                                    g72.allowanceOrChargeRate = itAdj.params[VersatileResponseParams.RATE] as Double
                                    g72.allowanceOrChargeCode = itAdj.params[VersatileResponseParams.ADJ_CODE] as Int
                                }

                                VersatileResponseAdjustmentType.ADJ_CHARGE_REJECTED,
                                VersatileResponseAdjustmentType.ADJ_ALLOWANCE_REJECTED -> {
                                    g72.exceptionNumber = "REJ: " + itAdj.params[VersatileResponseParams.REASON] as String?
                                }
                                VersatileResponseAdjustmentType.ADJ_KILL_PREVIOUS_ALLOW_CHG -> removeChanges = true
                                VersatileResponseAdjustmentType.ADJ_NEW_ITEM -> {
                                    val dex = transaction.dxs?.dexVersion?.cleanUCS()?.toInt()
                                    val newItem = G89(
                                        lineItem,
                                        itAdj.params[VersatileResponseParams.QTY] as Double?,
                                        itAdj.params[VersatileResponseParams.PACKTYPE] as String?,
                                        (if(dex != null && dex <= 4010) itAdj.params[VersatileResponseParams.UPC] else null)  as String?,
                                        (if(dex != null && dex > 4010) itAdj.params[VersatileResponseParams.PROD_ID_QUALIFIER] else null) as String?,
                                        (if(dex != null && dex > 4010) itAdj.params[VersatileResponseParams.PROD_ID] else null) as String?,
                                        (if(dex != null && dex <= 4010) itAdj.params[VersatileResponseParams.CASE_UPC] else null) as String?,
                                        itAdj.params[VersatileResponseParams.PRICE] as Double?,
                                        itAdj.params[VersatileResponseParams.PACK] as Int?,
                                        itAdj.params[VersatileResponseParams.INNER_PACK] as Int?,
                                        (if(dex != null && dex > 4010) itAdj.params[VersatileResponseParams.CASE_QUALIFIER] else null) as String?,
                                        (if(dex != null && dex > 4010) itAdj.params[VersatileResponseParams.CASE_ID] else null) as String?
                                    )
                                    items.add(newItem)
                                }
                                VersatileResponseAdjustmentType.ADJ_DEL_ITEM -> adjustments.add(G72(AllowanceOrChargeCode.GROUPED_ITEMS, MethodOfHandling.NOT_PROCESSED, "REMOVE" ))
                                else -> null
                            }
                            adjustments.add(g72)
                            if(!removeChanges)
                                adjustedItems[item] = adjustments
                            else
                                adjustedItems[item] = mutableListOf()
                        }
                        detail.itemG72s = adjustedItems[detail.itemDetail as G89]
                    }
                }
            }
        }

        val dexTransmission =
            DEXTransmission(this.configuration, this.initialization, transactionResponse)
        return dexTransmission
    }


    private fun invoiceG72s(response: VersatileDexResponse, invoiceNumber: String): List<G72>? {
        val adjustments = response.invoiceAdjustments(invoiceNumber)
        val invoiceAdjustments = mutableListOf<G72>()
        var deleteAjustments = false
        adjustments?.forEach {
            var rate : Double? = null
            var adjustmentCode = 0
            when(it.adjustmentType) {
                VersatileResponseAdjustmentType.ADJ_INVC_ALLOWANCE -> {
                    rate = it.params[VersatileResponseParams.RATE] as Double
                    adjustmentCode = it.params[VersatileResponseParams.ADJ_CODE] as Int
                }
                VersatileResponseAdjustmentType.ADJ_INVC_CHARGE -> {
                    rate = it.params[VersatileResponseParams.RATE] as Double
                    adjustmentCode = it.params[VersatileResponseParams.ADJ_CODE] as Int
                }
                VersatileResponseAdjustmentType.ADJ_INVC_KILL_PREVIOUS_ALLOW_CHG -> deleteAjustments = true
                VersatileResponseAdjustmentType.INVC_STATUS_MANUALLY_CHANGED -> null
                VersatileResponseAdjustmentType.INVC_STATUS -> null
                else -> null
            }
            val invoiceAdjustment = G72(
                adjustmentCode,
                MethodOfHandling.OFF_INVOICE, // where does this value came from?
                null,
                null,
                rate)
            invoiceAdjustments.add(invoiceAdjustment)
        }
        if(!invoiceAdjustments.isNullOrEmpty()) {
            invoiceAdjustments.add(G72(AllowanceOrChargeCode.GROUPED_ITEMS, MethodOfHandling.NOT_PROCESSED, "REMOVE" ))
        }
        return if(deleteAjustments)
            mutableListOf()
        else
            invoiceAdjustments
    }

    private fun g88(it: InnerRecord): ExtraInformation? {
        val g82 = it.identifier as G82
        return G88( g82.physicalDeliveryOrReturnDate, g82.productOwnershipTransferDate, g82.purchaseOrderNumber, g82.purchaseOrderDate, g82.receiverLocationNumber)
    }

    private fun g82Tog87(
        identifier: G82,
        response: VersatileDexResponse,
        invoiceNumber: String
    ): G87? {
        val initiator = response.initiator(invoiceNumber)
        val flag = identifier.creditDebitFlagCode
        return G87(
            initiator.value,
            flag,
            invoiceNumber,
            null, //integrity check, taken maybe from another place
            null, // do it after return
            null // receiver delivery/return number should be the same.
        )
    }

    fun toHoneywell(): String? {

        val invoiceEntries = mutableListOf<InvoiceEntry>()
        this.transaction.recordList.forEach {
            var identifier = it.identifier as G87
            val invoiceNumber = identifier.supplierDeliveryOrReturnNumber ?: identifier.receiverDeliveryOrReturnNumber
            val extra = it.extraInformation as G88
            val invoiceStatus = response?.status(invoiceNumber!!)
            val isAdjusted = invoiceStatus?.toInt() != VersatileResponseInvoiceStatus.CLOSED.value
            val g89s = mutableListOf<G89Block>()
            it.messages?.forEach {loop ->
                loop.loopInnerBlocks?.forEach { lib ->
                    val g72s = mutableListOf<G72Block>()
                    lib.itemG72s?.forEach {adj ->
                        val g72 = G72Block(
                            adj.allowanceOrChargeCode!!,
                            adj.handlingMethod,
                            adj.allowanceOrChargeNumber,
                            adj.allowanceOrChargeRate,
                            adj.allowanceOrChargeQty,
                            adj.unitOfMeasure
                        )
                        g72s.add(g72)
                    }
                    val g89 = lib.itemDetail as G89
                    val g89Block = G89Block(
                        g89.sequenceNumber.toString(),
                        g89.quantity,
                        g89.itemListCost,
                        g72s
                    )
                    g89s.add(g89Block)
                }
            }
            val invoiceEntry = InvoiceEntry(
                InvoiceStatus(
                    invoiceNumber!!,
                    if (isAdjusted) HInvoiceStatus.ADJUSTED.value else HInvoiceStatus.ACKNOWLEDGED.value
                ),
                STBlock(
                    it.st?.transactionSetIdentifierCode!!,
                    it.st?.transactionSetControlNumber?.toLong()
                ),
                g89s,
                G87Block(
                    identifier.initiatorCode,
                    identifier.creditDebitFlag,
                    identifier.supplierDeliveryOrReturnNumber,
                    identifier.integrityCheck,
                    identifier.adjustmentNumber,
                    identifier.receiverDeliveryOrReturnNumber
                ),
                G86Block(
                    it.g86?.signature
                ),
                G85Block(
                    it.g85?.integrityCheckValue
                ),
                G84Block(
                    it.g84?.quantity,
                    it.g84?.totalInvoiceAmount
                ),
                SEBlock(
                    it.se?.numberOfIncludedSegments!!,
                    it.se?.transactionControlNumber!!
                )
            )
            invoiceEntries.add(invoiceEntry)
        }
        val response = HoneywellDexResponse(
            ReceiveDexData(
                DXSBlock(
                    this.configuration.retailer?.communicationsId,
                    this.transaction.dxs?.functionalIdentifierCode,
                    this.transaction.dxs?.dexVersion,
                    this.transaction.dxs?.transmissionControlNumber,
                    this.configuration.supplier?.communicationsId,
                    this.transaction.dxs?.testIndicator
                ),invoiceEntries,
                DXEBlock(
                    this.transaction.dxe?.transmissionControlNumber,
                    this.transaction.dxe?.numberOfTransactionSetsIncluded
                )
            )
        )
        return Gson().toJson(response)
    }
}
