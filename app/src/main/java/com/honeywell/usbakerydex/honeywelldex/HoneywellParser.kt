package com.honeywell.usbakerydex.honeywelldex

import android.util.Log
import com.honeywell.usbakerydex.DexConnectionService
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellReferenceNumberQualifier
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag
import com.honeywell.usbakerydex.honeywelldex.model.*
import com.honeywell.usbakerydex.honeywelldex.model.G83Block as ItemBlock
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellUnitOfMeasure
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HKey(val value: String) {
    companion object {
        const val ADJUSTMENTS = "adjustments"
        const val MTX = "mtx"
        const val N9 = "n9"
        const val SUPPLIER_LOCATION = "location"
        const val SUPPLIER_SIGNATURE_KEY = "signaturekey"
        const val SUPPLIER_COMMUNICATIONS_ID = "communicationsid"
        const val SUPPLIER_DUNS_NUMBER = "dunsnumber"
        const val RETAILER_LOCATION = "location"
        const val RETAILER_DEX_VERSION = "dexversion"
        const val RETAILER_COMMUNICATIONS_ID = "communicationsid"
        const val RETAILER_DUNS_NUMBER = "dunsnumber"
        const val CONFIG = "config"
        const val SUPPLIER = "supplier"
        const val RETAILER = "retailer"
        const val TRANSACTION = "transaction"
        const val INVOICES = "invoices"
        const val INITIALIZATION = "initialization"
        const val EVENT_SOURCE_ID = "evtSrcId"
        const val SYNCH_TYPE = "synchtype"
        const val COM_METHOD = "commethod"
        const val INI_FILE = "inifile"
        const val INSTANCE_NAME = "instancename"
        const val TRANSMISSION_CONTROL_NUMBER = "transmissioncontrolnumber"
        const val TRANSACTION_CONTROL_NUMBER = "transactionsetcontrolnumber"
        const val TEST_INDICATOR = "testindicator"
        const val ITEMS = "items"
        const val MATNO = "matno"
        const val TYPE = "type"
        const val ST = "st"
        const val G82 = "g82"
        const val G84 = "g84"
        const val DXS = "dxs"
        const val DXE = "dxe"
        const val _01 = "01"
        const val _02 = "02"
        const val _03 = "03"
        const val _04 = "04"
        const val _05 = "05"
        const val _06 = "06"
        const val _07 = "07"
        const val _08 = "08"
        const val _09 = "09"
        const val _10 = "10"
        const val _11 = "11"
        const val _12 = "12"
    }

}

class HoneywellParser {


    companion object {

        private fun getIgnoreCase(jsonObject: JSONObject, property: String?): String? {
            val keys: Iterator<*> = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                if (key.equals(property, ignoreCase = true)) {
                    return key
                }
            }
            return null
        }

        private fun <T : Any> extract(
            jsonObject: JSONObject,
            key: String,
            default: Any?,
            clazz: Class<T>
        ): Any? {
            val exactKey =
                getIgnoreCase(
                    jsonObject,
                    key
                )
            if (exactKey != null && jsonObject.has(exactKey)) {
                return when (clazz) {
                    String::class.java -> jsonObject.getString(exactKey)
                    Long::class.java -> jsonObject.getLong(exactKey)
                    Int::class.java -> jsonObject.getInt(exactKey)
                    Double::class.java -> jsonObject.getDouble(exactKey)
                    Boolean::class.java -> jsonObject.getBoolean(exactKey)
                    else -> jsonObject.get(exactKey)
                }
            }
            return default
        }

        fun fromJSON(rootJsonObject: JSONObject): HoneywellDexRequest {
            val dexRequest = HoneywellDexRequest(
                readConfiguration(
                    rootJsonObject
                ),
                readInitialization(
                    rootJsonObject
                ),
                readTransaction(
                    rootJsonObject
                )
            )
            //TODO("Check and set missing parameters in each field")
            return dexRequest
        }

        private fun readInitialization(rootJsonObject: JSONObject): Initialization? {
            val initialization =
                getIgnoreCase(
                    rootJsonObject,
                    HKey.INITIALIZATION
                )
            if (!initialization.isNullOrEmpty()) {
                val initializationJSONObject = rootJsonObject.getJSONObject(initialization)
                return Initialization(
                    extract(
                        initializationJSONObject,
                        HKey.COM_METHOD,
                        "BTLE",
                        String::class.java
                    ) as String,
                    extract(
                        initializationJSONObject,
                        HKey.EVENT_SOURCE_ID,
                        DexConnectionService.EVENT_SEND,
                        Int::class.java
                    ) as Int,
                    extract(
                        initializationJSONObject,
                        HKey.INI_FILE,
                        "//mnt/sdcard//Android//data//DSD12//config.ini",
                        String::class.java
                    ) as String,
                    extract(
                        initializationJSONObject,
                        HKey.INSTANCE_NAME,
                        "DEX",
                        String::class.java
                    ) as String,
                    extract(
                        initializationJSONObject,
                        HKey.SYNCH_TYPE,
                        2,
                        Int::class.java
                    ) as Int
                )
            }
            return null
        }

        private fun readConfiguration(rootJsonObject: JSONObject): Config? {
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
                val retailer =
                    getIgnoreCase(
                        configJsonObject,
                        HKey.RETAILER
                    )
                var retailerBlock: Retailer? = null
                var supplierBlock: Supplier? = null
                if (!retailer.isNullOrEmpty()) {
                    val retailerJsonObject = configJsonObject.getJSONObject(retailer)
                    retailerBlock = Retailer(
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_COMMUNICATIONS_ID,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_DUNS_NUMBER,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_LOCATION,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_DEX_VERSION,
                            "",
                            String::class.java
                        ) as String
                    )
                }
                if (!supplier.isNullOrEmpty()) {
                    val supplierJsonObject = configJsonObject.getJSONObject(supplier)
                    supplierBlock = Supplier(
                        extract(
                            supplierJsonObject,
                            HKey.SUPPLIER_COMMUNICATIONS_ID,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            supplierJsonObject,
                            HKey.SUPPLIER_DUNS_NUMBER,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            supplierJsonObject,
                            HKey.SUPPLIER_LOCATION,
                            "",
                            String::class.java
                        ) as String,
                        extract(
                            supplierJsonObject,
                            HKey.SUPPLIER_SIGNATURE_KEY,
                            0L,
                            Long::class.java
                        ) as Long
                    )
                }
                return Config(
                    retailerBlock, supplierBlock,
                    extract(
                        configJsonObject,
                        HKey.TRANSACTION_CONTROL_NUMBER,
                        1L,
                        Long::class.java
                    ) as Long,
                    extract(
                        configJsonObject,
                        HKey.TRANSMISSION_CONTROL_NUMBER,
                        1L,
                        Long::class.java
                    ) as Long,
                    extract(
                        configJsonObject,
                        HKey.TEST_INDICATOR,
                        "",
                        String::class.java
                    ) as String //test indicator should be on DXS block, Singleton/parameter and then capture from there?
                )
            }
            return null
        }

        private fun readTransaction(rootJsonObject: JSONObject): Transaction? {
            val transaction =
                getIgnoreCase(
                    rootJsonObject,
                    HKey.TRANSACTION
                )
            if (transaction != null && rootJsonObject.has(transaction)) {
                val transactionJsonObject = rootJsonObject.getJSONObject(transaction)
                return Transaction(
                    readDxs(
                        transactionJsonObject
                    ),
                    readInvoices(
                        transactionJsonObject
                    ),
                    readDxe(
                        transactionJsonObject
                    )
                )
            }
            return null
        }

        private fun readInvoices(transactionJsonObject: JSONObject): Map<String, Invoice> {
            val invoices =
                getIgnoreCase(
                    transactionJsonObject,
                    HKey.INVOICES
                )
            val invoicesMap = mutableMapOf<String, Invoice>()
            if (!invoices.isNullOrEmpty()) {
                val invoicesJSONObject = transactionJsonObject.getJSONObject(invoices)
                val keys: Iterator<*> = invoicesJSONObject.keys()
                while (keys.hasNext()) {
                    val invoiceNumber = keys.next() as String
                    val invoice =
                        readInvoice(
                            invoicesJSONObject.getJSONObject(invoiceNumber)
                        )
                    invoicesMap[invoiceNumber] = invoice
                }
            }
            return invoicesMap
        }

        private fun readInvoice(invoiceJSONObject: JSONObject): Invoice {
            val st =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.ST
                )
            var stBlock: STBlock? = null
            if (!st.isNullOrEmpty()) {
                val stJSONObject = invoiceJSONObject.getJSONObject(st)
                stBlock =
                    readStBlock(
                        stJSONObject
                    )
            }

            val g82 =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.G82
                )
            var g82Block: G82Block? = null
            if (!g82.isNullOrEmpty()) {
                val g82JSONObject = invoiceJSONObject.getJSONObject(g82)
                g82Block =
                    readG82Block(
                        g82JSONObject
                    )
            }

            val g84 =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.G84
                )
            var g84Block: G84Block? = null
            if (!g84.isNullOrEmpty()) {
                val g84JSONObject = invoiceJSONObject.getJSONObject(g84)
                g84Block =
                    readG84Block(
                        g84JSONObject
                    )
            }

            val n9 =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.N9
                )
            var n9Block: N9Block? = null
            if (!n9.isNullOrEmpty()) {
                val n9JSONObject = invoiceJSONObject.getJSONObject(n9)
                n9Block =
                    readN9Block(
                        n9JSONObject
                    )
            }

            val mtx =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.MTX
                )
            var mtxBlock: MtxBlock? = null
            if (!mtx.isNullOrEmpty()) {
                mtxBlock =
                    readMtxBlock(
                        invoiceJSONObject
                    )
            }

            val items =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.ITEMS
                )
            var itemsBlock: Map<String, ItemBlock?>? = null
            if (!items.isNullOrEmpty()) {
                val itemsJSONObject = invoiceJSONObject.getJSONObject(items)
                itemsBlock =
                    readItemsBlock(
                        itemsJSONObject,
                        stBlock!!.ucsType
                    )
            }

            val adjustments =
                getIgnoreCase(
                    invoiceJSONObject,
                    HKey.ADJUSTMENTS
                )
            var adjustmentsBlock: Map<String, G72Block?>? = null
            if (!adjustments.isNullOrEmpty()) {
                val adjustmentsJSONObject = invoiceJSONObject.getJSONObject(adjustments)
                adjustmentsBlock =
                    readAdjustmentsBlock(
                        adjustmentsJSONObject
                    )
            }
            return Invoice(
                stBlock!!,
                g82Block!!,
                g84Block!!,
                adjustmentsBlock,
                itemsBlock,
                n9Block,
                mtxBlock
            )
        }

        private fun readMtxBlock(mtxJSONObject: JSONObject): MtxBlock? {
            return MtxBlock(
                extract(
                    mtxJSONObject,
                    HKey.MTX,
                    "",
                    String::class.java
                ) as String
            )
        }

        private fun readN9Block(n9JSONObject: JSONObject): N9Block? {
            return N9Block(
                (extract(
                    n9JSONObject,
                    HKey._01,
                    "",
                    String::class.java
                ) as String).convertToHoneywellReferenceNumberQualifier(),
                (extract(
                    n9JSONObject,
                    HKey._02,
                    "",
                    String::class.java
                ) as String),
                extract(
                    n9JSONObject,
                    HKey._03,
                    "",
                    String::class.java
                ) as String,
                (extract(
                    n9JSONObject,
                    HKey._04,
                    "",
                    String::class.java
                ) as String).convertToTimestamp(),
                (extract(
                    n9JSONObject,
                    HKey._05,
                    "",
                    String::class.java
                ) as String),
                extract(
                    n9JSONObject,
                    HKey._06,
                    "",
                    String::class.java
                ) as String
            )
        }

        private fun readItemsBlock(
            itemsJSONObject: JSONObject,
            ucsType: String
        ): Map<String, ItemBlock?>? {
            val itemsMap = mutableMapOf<String, ItemBlock>()
            val keys: Iterator<*> = itemsJSONObject.keys()
            var count = 1
            while (keys.hasNext()) {
                val itemNumber = keys.next() as String
                val item =
                    readItem(
                        itemsJSONObject.getJSONObject(itemNumber),
                        ucsType,
                        "$count"
                    )
                itemsMap[itemNumber] = item
                count++
            }
            return itemsMap
        }

        private fun readItem(
            itemJSONObject: JSONObject,
            ucsType: String,
            itemCount: String = "1"
        ): ItemBlock {
            val adjustmentMap =
                readItemAdjustmentsBlock(
                    itemJSONObject
                )
            var g8303: String? = null
            var g8305: String? = null
            var g8307: String? = null
            var g8309: Int? = null
            var g8311: String? = null
            var g8312: String? = null
            if (ucsType.length > 3) { //No idea why...
                g8303 = extract(
                    itemJSONObject,
                    HKey._03,
                    "",
                    String::class.java
                ) as String
                g8305 = extract(
                    itemJSONObject,
                    HKey._05,
                    "",
                    String::class.java
                ) as String
                g8307 = extract(
                    itemJSONObject,
                    HKey._07,
                    "",
                    String::class.java
                ) as String
                g8309 = extract(
                    itemJSONObject,
                    HKey._09,
                    0,
                    Int::class.java
                ) as Int
                g8311 = extract(
                    itemJSONObject,
                    HKey._11,
                    "",
                    String::class.java
                ) as String
                g8312 = extract(
                    itemJSONObject,
                    HKey._12,
                    "",
                    String::class.java
                ) as String
            }
            return ItemBlock(
                adjustmentMap,
                extract(
                    itemJSONObject,
                    HKey.MATNO,
                    "",
                    String::class.java
                ) as String,
                extract(
                    itemJSONObject,
                    HKey.TYPE,
                    "",
                    String::class.java
                ) as String,
                itemCount,
                extract(
                    itemJSONObject,
                    HKey._02,
                    -1.0,
                    Double::class.java
                ) as Double,
                extract(
                    itemJSONObject,
                    HKey._04,
                    "",
                    String::class.java
                ) as String,
                extract(
                    itemJSONObject,
                    HKey._06,
                    "",
                    String::class.java
                ) as String,
                extract(
                    itemJSONObject,
                    HKey._08,
                    if (ucsType == "894") 0.0 else -1.0,
                    Double::class.java
                ) as Double,
                extract(
                    itemJSONObject,
                    HKey._10,
                    "",
                    String::class.java
                ) as String,
                if (!g8303.isNullOrBlank()) HoneywellUnitOfMeasure.fromValue(g8303) else null,
                g8305,
                g8307,
                g8309,
                g8311,
                g8312
            )
        }

        private fun readAdjustmentsBlock(adjustmentsJSONObject: JSONObject): Map<String, G72Block> {
            val itemsAdjustmentsMap = mutableMapOf<String, G72Block>()
            val keys: Iterator<*> = adjustmentsJSONObject.keys()
            while (keys.hasNext()) {
                val itemNumber = keys.next() as String
                val item =
                    readAdjustmentBlock(
                        adjustmentsJSONObject.getJSONObject(itemNumber)
                    )
                itemsAdjustmentsMap[itemNumber] = item
            }
            return itemsAdjustmentsMap
        }

        private fun readItemAdjustmentsBlock(itemJSONObject: JSONObject): Map<String, G72Block> {
            val itemsAdjustmentsMap = mutableMapOf<String, G72Block>()
            val itemAdjustment =
                getIgnoreCase(
                    itemJSONObject,
                    HKey.ADJUSTMENTS
                )
            if (!itemAdjustment.isNullOrEmpty()) {
                val adjustmentsJSONObject = itemJSONObject.getJSONObject(itemAdjustment)
                val keys: Iterator<*> = adjustmentsJSONObject.keys()
                while (keys.hasNext()) {
                    val itemNumber = keys.next() as String
                    val item =
                        readAdjustmentBlock(
                            adjustmentsJSONObject.getJSONObject(itemNumber)
                        )
                    itemsAdjustmentsMap[itemNumber] = item
                }
            }
            return itemsAdjustmentsMap
        }

        private fun readAdjustmentBlock(adjustmentJSONObject: JSONObject): G72Block {
            return G72Block(
                extract(
                    adjustmentJSONObject,
                    HKey._01,
                    0,
                    Int::class.java
                ) as Int,
                (extract(
                    adjustmentJSONObject,
                    HKey._02,
                    "",
                    String::class.java
                ) as String).convertToHandlingMethod(),
                extract(
                    adjustmentJSONObject,
                    HKey._03,
                    "",
                    String::class.java
                ) as String,
                extract(
                    adjustmentJSONObject,
                    HKey._04,
                    "",
                    String::class.java
                ) as String,
                (extract(
                    adjustmentJSONObject,
                    HKey._05,
                    0.0,
                    Double::class.java
                ) as Double),
                (extract(
                    adjustmentJSONObject,
                    HKey._06,
                    0.0,
                    Double::class.java
                ) as Double),
                extract(
                    adjustmentJSONObject,
                    HKey._07,
                    "",
                    String::class.java
                ) as String,
                extract(
                    adjustmentJSONObject,
                    HKey._08,
                    0.0,
                    Double::class.java
                ) as Double,
                (extract(
                    adjustmentJSONObject,
                    HKey._09,
                    0.0,
                    Double::class.java
                ) as Double),
                (extract(
                    adjustmentJSONObject,
                    HKey._10,
                    0.0,
                    Double::class.java
                ) as Double),
                extract(
                    adjustmentJSONObject,
                    HKey._11,
                    "",
                    String::class.java
                ) as String
            )
        }

        private fun readStBlock(stJSONObject: JSONObject): STBlock? {
            return STBlock(
                extract(
                    stJSONObject,
                    HKey._01,
                    "",
                    String::class.java
                ) as String,
                extract(
                    stJSONObject,
                    HKey._02,
                    0L,
                    Long::class.java
                ) as Long,
                extract(
                    stJSONObject,
                    HKey._03,
                    "",
                    String::class.java
                ) as String
            )
        }

        private fun readG84Block(g84JSONObject: JSONObject): G84Block? {
            return G84Block(
                (extract(
                    g84JSONObject,
                    HKey._01,
                    0.0,
                    Double::class.java
                ) as Double),
                (extract(
                    g84JSONObject,
                    HKey._02,
                    0.0,
                    Double::class.java
                ) as Double),
                (extract(
                    g84JSONObject,
                    HKey._03,
                    0.0,
                    Double::class.java
                ) as Double)
            )
        }

        private fun readG82Block(g82JSONObject: JSONObject): G82Block {
            val dateNow = Date().convertToString()
            return G82Block(
                (extract(
                    g82JSONObject,
                    HKey._01,
                    "",
                    String::class.java
                ) as String).convertToTypeFlag(),
                extract(
                    g82JSONObject,
                    HKey._02,
                    "",
                    String::class.java
                ) as String,
                extract(
                    g82JSONObject,
                    HKey._03,
                    "",
                    String::class.java
                ) as String,
                extract(
                    g82JSONObject,
                    HKey._04,
                    "",
                    String::class.java
                ) as String,
                extract(
                    g82JSONObject,
                    HKey._05,
                    "",
                    String::class.java
                ) as String,
                extract(
                    g82JSONObject,
                    HKey._06,
                    "",
                    String::class.java
                ) as String,
                (extract(
                    g82JSONObject,
                    HKey._07,
                    dateNow,
                    String::class.java
                ) as String).convertToTimestamp(),
                (extract(
                    g82JSONObject,
                    HKey._08,
                    dateNow,
                    String::class.java
                ) as String).convertToTimestamp(),
                extract(
                    g82JSONObject,
                    HKey._09,
                    "",
                    String::class.java
                ) as String,
                (extract(
                    g82JSONObject,
                    HKey._10,
                    dateNow,
                    String::class.java
                ) as String).convertToTimestamp(),
                extract(
                    g82JSONObject,
                    HKey._01,
                    "",
                    String::class.java
                ) as String,
                extract(
                    g82JSONObject,
                    HKey._12,
                    "",
                    String::class.java
                ) as String
            )
        }

        /**
         * Probably can be ignored because versatile doesn't need it.
         */
        private fun readDxe(transactionJsonObject: JSONObject): DxeBlock? {
            val dxe =
                getIgnoreCase(
                    transactionJsonObject,
                    HKey.DXE
                )
            if (!dxe.isNullOrEmpty()) {
                val dxeContent = transactionJsonObject.getJSONObject(HKey.DXE)
                if (!dxeContent.has(HKey._01) || dxeContent.getString(
                        HKey._01
                    ) // transmissionControlNumber can be obtained from config block
                        .isNullOrEmpty()
                ) //communicationIdNumber
                    Log.i("DEX", "EndTransaction() - no elements")
                else {
                    return DxeBlock(
                        extract(
                            dxeContent,
                            HKey._01,
                            0L,
                            Long::class.java
                        ) as Long
                    )
                }
            } else {
                Log.i("DEX", "EndTransaction() - no DXE")
            }
            return null
        }

        /**
         * Probably can be ignored because versatile doesn't need it.
         */
        private fun readDxs(transactionJsonObject: JSONObject): DxsBlock? {
            val dxs =
                getIgnoreCase(
                    transactionJsonObject,
                    HKey.DXS
                )
            if (!dxs.isNullOrEmpty()) {
                val dxsContent = transactionJsonObject.getJSONObject(HKey.DXS)
                if (!dxsContent.has(HKey._01) || dxsContent.getString(
                        HKey._01
                    )
                        .isNullOrEmpty()
                ) //communicationIdNumber
                    Log.i("DEX", "BeginTransaction() - no elements")
                else {
                    return DxsBlock(
                        dxsContent.getString(HKey._01),
                        dxsContent.getString(HKey._02),
                        dxsContent.getString(HKey._03),
                        extract(
                            dxsContent,
                            HKey._04,
                            0,
                            Int::class.java
                        ) as Int,
                        extract(
                            dxsContent,
                            HKey._05,
                            null,
                            String::class.java
                        ) as String,
                        extract(
                            dxsContent,
                            HKey._06,
                            null,
                            String::class.java
                        ) as String
                    )
                }
            } else {
                Log.i("DEX", "BeginTransaction() - no DXS")
            }
            return null
        }
    }
}

private fun String.convertToHoneywellReferenceNumberQualifier() =
    HoneywellReferenceNumberQualifier.valueOf(this)

private fun String.convertToTypeFlag(): HoneywellTypeFlag = HoneywellTypeFlag.fromValue(this)
private fun String.convertToHandlingMethod(): HoneywellHandlingMethodCodes =
    HoneywellHandlingMethodCodes.fromValue(this)

private fun String.convertToTimestamp(): Long = SimpleDateFormat("YYYYMMDD", Locale.US).parse(this)?.time ?: Date().time
private fun Date.convertToString(): String = SimpleDateFormat("YYYYMMDD", Locale.US).format(this)