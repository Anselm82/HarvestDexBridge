package com.honeywell.usbakerydex.dex.model

import android.util.Log
import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.blocks.InnerRecord894
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator
import com.honeywell.usbakerydex.honeywelldex.HKey
import org.json.JSONObject
import java.util.*

class HoneywellParser {

    companion object {

        fun readAdjustments(jsonAdjustments: JSONObject): List<G72>? {
            val adjustments = getIgnoreCase(jsonAdjustments, HKey.ADJUSTMENTS)
            val itemAdjustmentList = mutableListOf<G72>()
            if(!adjustments.isNullOrEmpty()) {
                val itemAdjustmentsJSONObject = jsonAdjustments.getJSONObject(adjustments)
                val adjustmentsKeys: Iterator<*> = itemAdjustmentsJSONObject.keys()
                while (adjustmentsKeys.hasNext()) {
                    val adjustmentNumber = adjustmentsKeys.next() as String
                    val jsonAdjustment = itemAdjustmentsJSONObject.getJSONObject(adjustmentNumber)
                    val g72 = readG72(jsonAdjustment, adjustmentNumber.toIntOrNull())
                    itemAdjustmentList.add(g72)
                }
                return itemAdjustmentList
            }
            return null
        }

        fun readDXS(jsonTransaction: JSONObject) : DXS? {
            val dxs = getIgnoreCase(jsonTransaction, HKey.DXS)
            if (!dxs.isNullOrEmpty()) {
                val dxsContent = jsonTransaction.getJSONObject(HKey.DXS)
                if (!dxsContent.has(HKey._01) || dxsContent.getString(HKey._01).isNullOrEmpty())
                    Log.i("DEX", "BeginTransaction() - no elements")
                else {
                    return DXS(
                        extract(dxsContent, HKey._01),
                        extract(dxsContent, HKey._02)!!,
                        extract(dxsContent, HKey._03)!!,
                        extract(dxsContent, HKey._04, 1),
                        extract(dxsContent, HKey._05),
                        extract(dxsContent, HKey._06, TestIndicator.TEST)!!
                    )
                }
            } else {
                Log.i("DEX", "BeginTransaction() - no DXS")
            }
            return null
        }

        fun readST(jsonInvoice: JSONObject): ST? {
            val st =
                getIgnoreCase(
                    jsonInvoice,
                    HKey.ST
                )
            return if (!st.isNullOrEmpty()) {
                val stJSONObject = jsonInvoice.getJSONObject(st)
                ST(
                    extract(stJSONObject, HKey._01),
                    extract(stJSONObject, HKey._02),
                    extract(stJSONObject, HKey._03)
                )
            } else {
                null
            }
        }

        fun readG82(jsonInvoice: JSONObject): G82? {
            val g82 = getIgnoreCase(jsonInvoice, HKey.G82)
            if (!g82.isNullOrEmpty()) {
                val g82JSONObject = jsonInvoice.getJSONObject(g82)
                val dateNow = Date().toYYYYMMDD()
                return G82(
                    extract(g82JSONObject, HKey._01),
                    extract(g82JSONObject, HKey._02),
                    extract(g82JSONObject, HKey._03),
                    extract(g82JSONObject, HKey._04),
                    extract(g82JSONObject, HKey._05),
                    extract(g82JSONObject, HKey._06),
                    extract(g82JSONObject, HKey._07),
                    extract(g82JSONObject, HKey._08, dateNow),
                    extract(g82JSONObject, HKey._09),
                    extract(g82JSONObject, HKey._10, dateNow),
                    extract(g82JSONObject, HKey._11),
                    extract(g82JSONObject, HKey._12)
                )
            }
            return null
        }

        fun readN9(jsonInvoice: JSONObject): N9? {
            val n9 = getIgnoreCase(jsonInvoice, HKey.N9)
            return if (!n9.isNullOrEmpty()) {
                val n9JSONObject = jsonInvoice.getJSONObject(n9)
                N9(
                    extract(n9JSONObject, HKey._01),
                    extract(n9JSONObject, HKey._02),
                    extract(n9JSONObject, HKey._03),
                    extract(n9JSONObject, HKey._04),
                    extract(n9JSONObject, HKey._05),
                    extract(n9JSONObject, HKey._06)
                )
            } else {
                null
            }
        }

        fun readG83(jsonItem: JSONObject, sequenceNumber: Int? = 1, ucsType: String): G83? {
            return G83(sequenceNumber,
                extract(jsonItem, HKey._02,-1.0),
                extract(jsonItem, HKey._03),
                extract(jsonItem, HKey._04),
                extract(jsonItem, HKey._05),
                extract(jsonItem, HKey._06),
                extract(jsonItem, HKey._07),
                extract(jsonItem, HKey._08, if (ucsType == BASE_RECORD) 0.0 else -1.0),
                extract(jsonItem, HKey._09, 0),
                extract(jsonItem, HKey._10),
                extract(jsonItem, HKey._11),
                extract(jsonItem, HKey._12)
            )
        }

        fun readG72(jsonAdjustment: JSONObject, adjustmentNumber: Int? = 1): G72 {
            return G72(
                extract(jsonAdjustment, HKey._01),
                extract(jsonAdjustment, HKey._02),
                extract(jsonAdjustment, HKey._03),
                extract(jsonAdjustment, HKey._04),
                extract<Double?>(jsonAdjustment, HKey._05, null),
                extract(jsonAdjustment, HKey._06,0.0),
                extract(jsonAdjustment, HKey._07),
                extract<Double?>(jsonAdjustment, HKey._08, null),
                extract<Double?>(jsonAdjustment, HKey._09, null),
                extract(jsonAdjustment, HKey._10,0.0),
                extract(jsonAdjustment, HKey._11)
            )
        }

        fun readG22(jsonItem: JSONObject): G22 {
            return G22(

            )
        }

        fun readG23(jsonItem: JSONObject): List<G23>? {
            return null
        }

        fun readG84(jsonInvoice: JSONObject): G84? {
            val g84 = getIgnoreCase(jsonInvoice, HKey.G84)
            if(!g84.isNullOrEmpty()) {
                val g84JsonObject = jsonInvoice.getJSONObject(g84)
                return G84(
                    extract(g84JsonObject, HKey._01),
                    extract(g84JsonObject, HKey._02),
                    extract(g84JsonObject, HKey._03)
                )
            }
            return null
        }

        fun readG85(innerRecord: InnerRecord894): G85 {
            TODO("Calculate using CRC16 algorithm")
        }

        fun readSignature(rootJsonObject: JSONObject): String? {
            val config =
                getIgnoreCase(
                    rootJsonObject,
                    HKey.CONFIG
                )
            if (config != null && rootJsonObject.has(config)) {
                val configJsonObject = rootJsonObject.getJSONObject(config)
                val supplier =
                    getIgnoreCase(
                        configJsonObject,
                        HKey.SUPPLIER
                    )
                if (!supplier.isNullOrEmpty()) {
                    val supplierJsonObject = configJsonObject.getJSONObject(supplier)
                    return extract<String>(supplierJsonObject, HKey.SUPPLIER_SIGNATURE_KEY)
                }
            }
            return null
        }
    }
}

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

