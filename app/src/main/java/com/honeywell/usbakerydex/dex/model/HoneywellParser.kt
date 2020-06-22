package com.honeywell.usbakerydex.dex.model

import android.util.Log
import com.honeywell.usbakerydex.DexConnectionService
import com.honeywell.usbakerydex.dex.model.blocks.*
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator
import com.honeywell.usbakerydex.honeywell.model.Configuration
import com.honeywell.usbakerydex.honeywell.model.Initialization
import com.honeywell.usbakerydex.honeywell.model.Retailer
import com.honeywell.usbakerydex.honeywell.model.Supplier
import com.honeywell.usbakerydex.honeywell.vo.HKey
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
                    val g72 = readG72(jsonAdjustment)
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

        /**
         * If no identificators have been provided (productId or upc) we took the material from honeywell in both. Is this valid?
         */
        fun readG83(jsonItem: JSONObject, sequenceNumber: Int? = 1, ucsType: String): G83? {
            val id = extract<String>(jsonItem, HKey._06, null)
            val upc =  extract<String>(jsonItem, HKey._04, null)
            val materialNumber = extract<String>(jsonItem, HKey.MATNO)
            return G83(sequenceNumber,
                extract(jsonItem, HKey._02,-1.0),
                extract(jsonItem, HKey._03),
                upc ?: materialNumber,
                extract(jsonItem, HKey._05),
                id ?: materialNumber,
                extract(jsonItem, HKey._07),
                extract(jsonItem, HKey._08, if (ucsType == BASE_RECORD) 0.0 else -1.0),
                extract(jsonItem, HKey._09, 0),
                extract(jsonItem, HKey._10),
                extract(jsonItem, HKey._11),
                extract(jsonItem, HKey._12)
            )
        }

        fun readG72(jsonAdjustment: JSONObject): G72 {
            return G72(
                extract(jsonAdjustment, HKey._01),
                extract(jsonAdjustment, HKey._02),
                extract(jsonAdjustment, HKey._03),
                extract(jsonAdjustment, HKey._04),
                extract<Double?>(jsonAdjustment, HKey._05, null),
                extract<Double?>(jsonAdjustment, HKey._06,null),
                extract(jsonAdjustment, HKey._07),
                extract<Double?>(jsonAdjustment, HKey._08, null),
                extract<Double?>(jsonAdjustment, HKey._09, null),
                extract<Double?>(jsonAdjustment, HKey._10,null),
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

        //TODO("Calculate using CRC16 algorithm")
        fun readG85(innerRecord: InnerRecord): G85 {
            return G85("1234")
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

        fun readG87(rootJsonObject: JSONObject): G87? {
            return G87(
                extract(rootJsonObject, HKey._01),
                extract(rootJsonObject, HKey._02),
                extract(rootJsonObject, HKey._03),
                extract(rootJsonObject, HKey._04),
                extract(rootJsonObject, HKey._05),
                extract(rootJsonObject, HKey._06)
            )
        }

        fun readG88(rootJsonObject: JSONObject): ExtraInformation? {
            return G88(
                extract(rootJsonObject, HKey._01),
                extract(rootJsonObject, HKey._02),
                extract(rootJsonObject, HKey._03),
                extract(rootJsonObject, HKey._04),
                extract(rootJsonObject, HKey._05)
            )
        }

        fun readG89(rootJsonObject: JSONObject, index: Int?, ucsType: String): G89? {
            return G89(
                extract(rootJsonObject, HKey._01),
                extract(rootJsonObject, HKey._02),
                extract(rootJsonObject, HKey._03),
                extract(rootJsonObject, HKey._04),
                extract(rootJsonObject, HKey._05),
                extract(rootJsonObject, HKey._06),
                extract(rootJsonObject, HKey._07),
                extract(rootJsonObject, HKey._08),
                extract(rootJsonObject, HKey._09),
                extract(rootJsonObject, HKey._10),
                extract(rootJsonObject, HKey._11),
                extract(rootJsonObject, HKey._12)
            )
        }

        fun readInitialization(rootJsonObject: JSONObject): Initialization? {
            val initialization = getIgnoreCase(rootJsonObject, HKey.INITIALIZATION)
            if (!initialization.isNullOrEmpty()) {
                val initializationJSONObject = rootJsonObject.getJSONObject(initialization)
                return Initialization(
                    extract(initializationJSONObject, HKey.COM_METHOD, "BTLE")!!,
                    extract(initializationJSONObject, HKey.EVENT_SOURCE_ID, DexConnectionService.EVENT_SEND)!!,
                    extract(initializationJSONObject, HKey.INI_FILE, "//mnt/sdcard//Android//data//DSD12//config.ini")!!,
                    extract(initializationJSONObject, HKey.INSTANCE_NAME, "DEX")!!,
                    extract(initializationJSONObject, HKey.SYNCH_TYPE, 2)!!
                )
            }
            return null
        }

        fun readConfiguration(rootJsonObject: JSONObject): Configuration? {
            val config = getIgnoreCase(rootJsonObject, HKey.CONFIG)
            if (config != null && rootJsonObject.has(config)) {
                val configJsonObject = rootJsonObject.getJSONObject(config)
                val supplier = getIgnoreCase(configJsonObject, HKey.SUPPLIER)
                val retailer = getIgnoreCase(configJsonObject, HKey.RETAILER)
                var retailerBlock: Retailer? = null
                var supplierBlock: Supplier? = null
                if (!retailer.isNullOrEmpty()) {
                    val retailerJsonObject = configJsonObject.getJSONObject(retailer)
                    retailerBlock = Retailer(
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_COMMUNICATIONS_ID),
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_DUNS_NUMBER)!!,
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_LOCATION),
                        extract(
                            retailerJsonObject,
                            HKey.RETAILER_DEX_VERSION)
                    )
                }
                if (!supplier.isNullOrEmpty()) {
                    val supplierJsonObject = configJsonObject.getJSONObject(supplier)
                    supplierBlock = Supplier(
                        extract(supplierJsonObject, HKey.SUPPLIER_COMMUNICATIONS_ID),
                        extract(supplierJsonObject, HKey.SUPPLIER_DUNS_NUMBER),
                        extract(supplierJsonObject, HKey.SUPPLIER_LOCATION),
                        extract(supplierJsonObject, HKey.SUPPLIER_SIGNATURE_KEY)
                    )
                }
                return Configuration(
                    retailerBlock, supplierBlock,
                    extract(configJsonObject, HKey.TRANSACTION_CONTROL_NUMBER, 1)!!,
                    extract(configJsonObject, HKey.TRANSMISSION_CONTROL_NUMBER, 1)!!,
                    extract(configJsonObject, HKey.TEST_INDICATOR) //test indicator should be on DXS block, Singleton/parameter and then capture from there?
                )
            }
            return null
        }

        fun readTransaction(rootJsonObject: JSONObject): DEXTransaction? {
            return DEXTransaction.readTransaction(rootJsonObject)
        }
    }
}