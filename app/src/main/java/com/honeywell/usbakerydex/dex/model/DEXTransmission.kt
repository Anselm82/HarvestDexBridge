package com.honeywell.usbakerydex.dex.model

import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator
import com.honeywell.usbakerydex.honeywell.model.Configuration
import com.honeywell.usbakerydex.honeywell.model.Initialization
import com.honeywell.usbakerydex.honeywelldex.model.FunctionalIdentifier
import com.honeywell.usbakerydex.versatiledex.model.*
import com.honeywell.usbakerydex.versatiledex.utils.NEW_LINE
import com.honeywell.usbakerydex.versatiledex.utils.VersatilePackType
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

        fun build() : DEXTransmission = DEXTransmission(configuration!!, initialization!!, transaction!!)
    }

    private val vendors by lazy { buildVendors() }
    private val stops by lazy { buildStops() }
    private val customers by lazy { buildCustomers() }
    private val invoices by lazy { buildInvoices() }

    init {
        transaction.dxs?.dexVersion = configuration.retailer?.dexVersion ?: DEFAULT_DEX_VERSION
        transaction.dxs?.receiverIdentificationCode = configuration.retailer?.communicationsId
        transaction.dxs?.senderIdentificationCode = configuration.supplier?.communicationsId
        transaction.dxs?.testIndicator = TestIndicator.PRODUCTION
        transaction.dxs?.transmissionControlNumber = configuration.transmissionControlNumber
        transaction.dxs?.functionalIdentifierCode = FunctionalIdentifier.DX.value
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
                    val caseCount = if(itemDetail.innerPack != null) "${itemDetail.innerPack}" else ""
                    val item = Item(
                        vendor.dexVersion,
                        itemDetail.upc ?: "",
                        itemDetail?.upcCaseCode ?: "",
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
                    configuration?.supplier?.signatureKey ?: 123L,
                    identifier.supplierDunsNumber ?: "123456789",
                    configuration?.supplier?.location ?: "0001",
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
        val countStops =
            1 //TODO define how serveral stops could be reported... and then change this number
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

    fun buildResponse(response: VersatileDexResponse) : DEXTransmission {
        val transactionResponse = DEXTransaction(
            this.transaction.dxs,
            this.transaction.recordList,
            this.transaction.dxe
        )
        transactionResponse.recordList.forEach {
            it.identifier = g82Tog87(it.identifier, response)
            it.extraInformation = g88(it)
            it.
        }
        val dexTransmission = DEXTransmission(this.configuration, this.initialization, transactionResponse)
        return dexTransmission
    }

    private fun g88(it: InnerRecord): ExtraInformation? {

    }

    private fun g82Tog87(
        identifier: RecordIdentifier?,
        response: VersatileDexResponse
    ) : RecordIdentifier? {
        if(identifier is G82) {
            val invoiceNumber = identifier.supplierDeliveryReturnNumber!!
            val invoice = response.invoice(invoiceNumber)
            val initiator = response.initiator(invoiceNumber)
            val g87 = G87(

            )
        } else return identifier
    }
/*
    fun merge(response: VersatileDexResponse) {
        val g87s = mutableListOf<G87>()
        transaction.recordList.forEach {
            val identifier = it.identifier
            if(identifier is G82) {
                val invoiceData = response.lines.groupBy { it.invoice }[identifier.supplierDeliveryReturnNumber]
                if(!invoiceData.isNullOrEmpty()){
                    val initiator = when(it.code) {
                        VersatileResponseCode.SVR -> InitiatorCode.RETAILER
                        VersatileResponseCode.USR -> InitiatorCode.SUPPLIER
                    }
                }
                val g87 = G87(
                    identifier.creditDebitFlagCode,
                    identifier
                )
                g87s.add(g87)
                val g89 = G89(

                )
                val g88 = G88(

                )

            }
        }
        val invoices = response.lines.groupBy { it.invoice }

        response.lines.forEach {

            val initiator = when(it.code) {
                VersatileResponseCode.SVR -> InitiatorCode.RETAILER
                VersatileResponseCode.USR -> InitiatorCode.SUPPLIER
            }

            val messageType = if("${it.ucsType}" == BASE_RECORD) BASE_RECORD else ACK_ADJ_RECORD
            val date = it.timestamp
            val invoice = it.invoice
            val adjustmentType = it.adjustmentType
            when(adjustmentType) {

            }
            val g87 = G87(
                initiator, transaction.recordList.first { it.identifier.}
            )
        }
    }


 */
}

