package com.honeywell.usbakerydex.dex.model

import com.honeywell.usbakerydex.dex.model.blocks.DXE
import com.honeywell.usbakerydex.dex.model.blocks.DXS
import com.honeywell.usbakerydex.dex.model.blocks.InnerRecord
import com.honeywell.usbakerydex.honeywell.HoneywellParser
import com.honeywell.usbakerydex.honeywell.vo.HKey
import org.json.JSONObject

data class DEXTransaction(
    var dxs: DXS? = null, //Application header      M-1
    var recordList: List<InnerRecord>,
    var dxe: DXE? = null //Application trailer M-1
) {
    companion object {
        fun readTransaction(rootJsonObject: JSONObject): DEXTransaction? {
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