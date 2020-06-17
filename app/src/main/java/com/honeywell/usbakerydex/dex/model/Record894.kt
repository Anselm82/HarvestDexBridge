package com.honeywell.usbakerydex.dex.model

import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.honeywelldex.HKey
import org.json.JSONObject

/*
//From supplier to customer/receiver
data class Record894 (
    var dxs: DXS? = null, //Application header      M-1
    var invoicesList: List<InnerRecord894>,
    var dxe: DXE? = null //Application trailer M-1
) : MessageBody()


{
    companion object {

        fun fromHoneywell(rootJsonObject: JSONObject) : Record894? {
            val transaction = getIgnoreCase(rootJsonObject, HKey.TRANSACTION)
            if (transaction != null && rootJsonObject.has(transaction)) {
                val signature = HoneywellParser.readSignature(rootJsonObject)
                val jsonTransaction = rootJsonObject.getJSONObject(transaction)
                val dxs = HoneywellParser.readDXS(jsonTransaction)
                val invoices = getIgnoreCase(jsonTransaction, HKey.INVOICES)
                val invoicesList = mutableListOf<InnerRecord894>()
                if (!invoices.isNullOrEmpty()) {
                    val invoicesJSONCollection = jsonTransaction.getJSONObject(invoices)
                    val keys: Iterator<*> = invoicesJSONCollection.keys()
                    while (keys.hasNext()) {
                        var g84Quantity = 0.0
                        val invoiceNumber = keys.next() as String
                        val jsonInvoice = invoicesJSONCollection.getJSONObject(invoiceNumber)
                        val st = HoneywellParser.readST(jsonInvoice)
                        val g82 = HoneywellParser.readG82(jsonInvoice)
                        val n9 = HoneywellParser.readN9(jsonInvoice)
                        val items = getIgnoreCase(jsonInvoice, HKey.ITEMS)
                        val itemList = mutableListOf<LoopInnerBlock>()
                        if (!items.isNullOrEmpty()) {
                            val itemsJSONObject = jsonInvoice.getJSONObject(items)
                            val itemKeys: Iterator<*> = itemsJSONObject.keys()
                            while (itemKeys.hasNext()) {
                                val itemNumber = keys.next() as String
                                val jsonItem = itemsJSONObject.getJSONObject(itemNumber)
                                val g83 = HoneywellParser.readG83(
                                    jsonItem,
                                    itemNumber.toIntOrNull(),
                                    st?.transactionSetIdentifierCode ?: BASE_RECORD
                                )
                                if (g83 != null)
                                    g84Quantity += g83.quantity!!
                                val itemAdjustmentList = HoneywellParser.readAdjustments(jsonItem)
                                val g22 = HoneywellParser.readG22(jsonItem)
                                val g23 = HoneywellParser.readG23(jsonItem)
                                itemList.add(
                                    itemNumber.toInt(),
                                    LoopInnerBlock(g83, g22, itemAdjustmentList, g23, null)
                                ) //TODO("Change to a recursive parsing version for the LOOP")
                            }
                        }
                        val invoiceAdjustments = HoneywellParser.readAdjustments(jsonInvoice)
                        val invoiceG23 = HoneywellParser.readG23(jsonInvoice)
                        val g84 = HoneywellParser.readG84(jsonInvoice)
                        val g86 = G86(signature)
                        val innerRecord = InnerRecord894(
                            st,
                            g82,
                            n9,
                            Loop(itemList),
                            invoiceAdjustments,
                            invoiceG23,
                            g84,
                            g86,
                            null
                        )
                        val g85 = HoneywellParser.readG85(innerRecord)
                        innerRecord.g85 = g85
                        invoicesList.add(innerRecord)
                    }
                }
                return Record894(
                    dxs, invoicesList, DXE(dxs?.getInt("04") ?: 1, invoicesList.size)
                )
            }
            return null
        }
        //TODO("Test transformation")

        fun fromVersatile(versatileString: String) : Record894 {
            TODO()
        }
    }
}*/

data class InnerRecord(
    var st: ST? = null, //Transaction set header    M-1
    var identifier: RecordIdentifier?, //In 894 is: G82 Delivery/Return base record identifier  M-1  __OR__ //In 895 is: G87 Delivery/Return base record identifier  M-1
    var extraInformation: ExtraInformation? = null, //In 894 is N9: Extended reference information    O-n __OR__ //In 895 is G88:: Delivery/Return base record identifier adjustment O-1
    var messages: List<Loop>?,
    var g72: List<G72>? = null, //Allowance or charge   O-20
    var g23: List<G23>? = null, //Terms of sale O-20 UNSUPPORTED
    var g84: G84? = null, //Delivery/Return record of totals    M-1
    var g86: G86? = null, //Signature   M-1
    var g85: G85? = null, //Record integrity check  M-1
    var se: SE? = null //Transaction set trailer   M-1
) {
    companion object {
        fun fromJSON(rootJsonObject: JSONObject, signature: String?) : InnerRecord? {
            var g84Quantity = 0.0

            val st = HoneywellParser.readST(rootJsonObject)
            //messages
            var recordIdentifier: RecordIdentifier? = null
            var extraInformation: ExtraInformation? = null
            val messages = mutableListOf<Loop>()
            if(st!!.transactionSetIdentifierCode.equals(BASE_RECORD)) {
                recordIdentifier = HoneywellParser.readG82(rootJsonObject)
                extraInformation = HoneywellParser.readN9(rootJsonObject)
                val items = getIgnoreCase(rootJsonObject, HKey.ITEMS)
                val itemList = mutableListOf<LoopInnerBlock>()
                if (!items.isNullOrEmpty()) {
                    val itemsJSONObject = rootJsonObject.getJSONObject(items)
                    val itemKeys: Iterator<*> = itemsJSONObject.keys()
                    while (itemKeys.hasNext()) {
                        val itemNumber = itemKeys.next() as String
                        val jsonItem = itemsJSONObject.getJSONObject(itemNumber)
                        val g83 = HoneywellParser.readG83(
                            jsonItem,
                            itemNumber.toIntOrNull(),
                            BASE_RECORD
                        )
                        if (g83 != null)
                            g84Quantity += g83.quantity!!
                        val item = createInnerLoop(g83, jsonItem)
                        itemList.add(item)
                    }
                }
            } else if(st.transactionSetIdentifierCode.equals(ACK_ADJ_RECORD)) {
                recordIdentifier = HoneywellParser.readG87(rootJsonObject)
                extraInformation = HoneywellParser.readG88(rootJsonObject)

                val items = getIgnoreCase(rootJsonObject, HKey.G89)
                val itemList = mutableListOf<LoopInnerBlock>()
                if (!items.isNullOrEmpty()) {
                    val itemsJSONObject = rootJsonObject.getJSONObject(items)
                    val itemKeys: Iterator<*> = itemsJSONObject.keys()
                    while (itemKeys.hasNext()) {
                        val itemNumber = itemKeys.next() as String
                        val jsonItem = itemsJSONObject.getJSONObject(itemNumber)
                        val g89 = HoneywellParser.readG89(
                            jsonItem,
                            itemNumber.toIntOrNull(),
                            ACK_ADJ_RECORD
                        )
                        if (g89 != null)
                            g84Quantity += g89.quantity!!
                        val item = createInnerLoop(g89, jsonItem)
                        itemList.add(item)
                    }
                }
            }
            val invoiceG72s = HoneywellParser.readAdjustments(rootJsonObject)
            val invoiceG23s = HoneywellParser.readG23(rootJsonObject)
            val g84 = HoneywellParser.readG84(rootJsonObject)
            val g86 = G86(signature)
            val innerRecord = InnerRecord(
                st,
                recordIdentifier,
                extraInformation,
                messages,
                invoiceG72s,
                invoiceG23s,
                g84,
                g86,
                null
            )
            val g85 = HoneywellParser.readG85(innerRecord)
            innerRecord.g85 = g85
            return innerRecord
        }

        //TODO("Change to a recursive parsing version for the LOOP")
        private fun createInnerLoop(itemRecord: ItemDetailRecord?, jsonItem: JSONObject): LoopInnerBlock {
            val itemAdjustmentList = HoneywellParser.readAdjustments(jsonItem)
            val g22 = HoneywellParser.readG22(jsonItem)
            val g23 = HoneywellParser.readG23(jsonItem)
            return LoopInnerBlock(itemRecord, g22, itemAdjustmentList, g23, null)
        }
    }
}

data class DEXTransaction(
    var dxs: DXS? = null, //Application header      M-1
    var recordList: List<InnerRecord>,
    var dxe: DXE? = null //Application trailer M-1
) {
    companion object {
        fun fromJSON(rootJsonObject: JSONObject): DEXTransaction? {
            val transaction = getIgnoreCase(rootJsonObject, HKey.TRANSACTION)
            if (transaction != null && rootJsonObject.has(transaction)) {
                val signature = HoneywellParser.readSignature(rootJsonObject)
                val jsonTransaction = rootJsonObject.getJSONObject(transaction)
                val dxs = HoneywellParser.readDXS(jsonTransaction)
                val invoices = getIgnoreCase(jsonTransaction, HKey.INVOICES)
                val invoicesList = mutableListOf<InnerRecord>()
                if (!invoices.isNullOrEmpty()) {
                    val invoicesJSONCollection = jsonTransaction.getJSONObject(invoices)
                    val keys: Iterator<*> = invoicesJSONCollection.keys()
                    while (keys.hasNext()) {
                        val invoiceNumber = keys.next() as String
                        val jsonInvoice = invoicesJSONCollection.getJSONObject(invoiceNumber)
                        val innerRecord = InnerRecord.fromJSON(jsonInvoice, signature)
                        if (innerRecord != null)
                            invoicesList.add(innerRecord)
                    }
                }
                val dxe = DXE(dxs?.getInt("04") ?: 1, invoicesList.size)
                return DEXTransaction(
                    dxs, invoicesList, dxe
                )
            }
            return null
        }
    }
}
