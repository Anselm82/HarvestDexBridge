package com.honeywell.usbakerydex.dex.model

import android.util.Log
import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.blocks.InnerRecord894
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator
import com.honeywell.usbakerydex.honeywelldex.HKey
import org.json.JSONObject
import java.util.*

//From supplier to customer/receiver
data class Record894(
    var dxs: DXS? = null, //Application header      M-1
    var invoicesList: List<InnerRecord894>,
    var dxe: DXE? = null //Application trailer M-1
)
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
}

internal fun Record894.toVersatile(): String {
    TODO()
}

internal fun Record894.toHoneywell(): JSONObject {
    TODO()
}

