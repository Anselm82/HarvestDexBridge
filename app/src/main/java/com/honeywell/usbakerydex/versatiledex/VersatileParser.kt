package com.honeywell.usbakerydex.versatiledex

import com.honeywell.usbakerydex.versatiledex.model.VersatileDexResponse
import com.honeywell.usbakerydex.versatiledex.model.VersatileDexResponseEntry
import com.honeywell.usbakerydex.versatiledex.utils.*
import java.text.SimpleDateFormat
import java.util.*


class VersatileConverter {

    companion object {

        fun toVersatileDexResponse(content: String, dexVersion: Int): VersatileDexResponse {
            val lines = content.split(NEW_LINE)
            val entries = mutableListOf<VersatileDexResponseEntry>()
            for (line in lines)
                entries.add(
                    toVersatileDexResponseEntry(
                        line,
                        dexVersion
                    )
                )
            return VersatileDexResponse(entries.toTypedArray())
        }


        private fun toVersatileDexResponseEntry(
            line: String,
            dexVersion: Int
        ): VersatileDexResponseEntry {
            val params = line.split(" ")
            val formatter = SimpleDateFormat("YYMMDD:hhmmss", Locale.US)
            val timestamp = formatter.parse(params[0])!!.time
            val ucsTypeCode = params[1].split(":")
            val ucsType = ucsTypeCode[0].toInt()
            val code = VersatileResponseCode.fromValue(ucsTypeCode[1])
            val invoice = params[2].toInt()
            val adjustmentType = VersatileResponseAdjustmentType.fromValue(params[3])
            val adjustmentTypeParams = params.subList(4, params.size)
            val entryParams =
                toVersatileResponseParamsMap(
                    adjustmentType,
                    adjustmentTypeParams,
                    dexVersion
                )
            return VersatileDexResponseEntry(
                timestamp,
                ucsType,
                code,
                "$invoice",
                adjustmentType,
                entryParams
            )
        }

        private fun toVersatileResponseParamsMap(
            adjustmentType: VersatileResponseAdjustmentType,
            adjustmentTypeParams: List<String>,
            dexVersion: Int
        ): Map<String, Any> {
            val params = mutableMapOf<String, Any>()
            when (adjustmentType) {
                VersatileResponseAdjustmentType.ADJ_PACKTYPE,
                VersatileResponseAdjustmentType.ADJ_PRICE,
                VersatileResponseAdjustmentType.ADJ_QTY,
                VersatileResponseAdjustmentType.ADJ_PACK,
                VersatileResponseAdjustmentType.ADJ_INNERPACK,
                VersatileResponseAdjustmentType.ADJ_PROD_QUALIFIER,
                VersatileResponseAdjustmentType.ADJ_PROD_ID,
                VersatileResponseAdjustmentType.ADJ_UPC,
                VersatileResponseAdjustmentType.ADJ_CASEUPC -> {
                    params[VersatileResponseParams.ITEM_INDEX] = adjustmentTypeParams[0].toInt()
                    params[VersatileResponseParams.OLD_VALUE] = adjustmentTypeParams[1]
                    params[VersatileResponseParams.NEW_VALUE] = adjustmentTypeParams[2]
                }
                VersatileResponseAdjustmentType.ADJ_CHARGE,
                VersatileResponseAdjustmentType.ADJ_ALLOWANCE -> {
                    params[VersatileResponseParams.ITEM_INDEX] = adjustmentTypeParams[0].toInt()
                    params[VersatileResponseParams.RATE] = adjustmentTypeParams[1]
                    params[VersatileResponseParams.ADJ_CODE] = adjustmentTypeParams[2]
                }
                VersatileResponseAdjustmentType.ADJ_CHARGE_REJECTED,
                VersatileResponseAdjustmentType.ADJ_ALLOWANCE_REJECTED -> {
                    params[VersatileResponseParams.ITEM_INDEX] = adjustmentTypeParams[0].toInt()
                    params[VersatileResponseParams.REASON] = adjustmentTypeParams[1]
                }
                VersatileResponseAdjustmentType.ADJ_DEL_ITEM,
                VersatileResponseAdjustmentType.ADJ_KILL_PREVIOUS_ALLOW_CHG ->
                    params[VersatileResponseParams.ITEM_INDEX] = adjustmentTypeParams[0].toInt()
                VersatileResponseAdjustmentType.INVC_STATUS_MANUALLY_CHANGED -> {
                    params[VersatileResponseParams.OLD_VALUE] =
                        VersatileResponseInvoiceStatus.fromValue(adjustmentTypeParams[0].toInt())
                    params[VersatileResponseParams.NEW_VALUE] =
                        VersatileResponseInvoiceStatus.fromValue(adjustmentTypeParams[1].toInt())
                }
                VersatileResponseAdjustmentType.INVC_STATUS ->
                    params[VersatileResponseParams.INVOICE_STATUS] =
                        VersatileResponseInvoiceStatus.fromValue(adjustmentTypeParams[0].toInt())
                VersatileResponseAdjustmentType.ADJ_INVC_ALLOWANCE,
                VersatileResponseAdjustmentType.ADJ_INVC_CHARGE,
                VersatileResponseAdjustmentType.ADJ_INVC_KILL_PREVIOUS_ALLOW_CHG -> {
                    params[VersatileResponseParams.RATE] = adjustmentTypeParams[0]
                    params[VersatileResponseParams.ADJ_CODE] = adjustmentTypeParams[1]
                }
                VersatileResponseAdjustmentType.ADJ_D_R_DATE,
                VersatileResponseAdjustmentType.ADJ_POTD,
                VersatileResponseAdjustmentType.ADJ_PODATE -> {
                    val formatter = SimpleDateFormat("CCYYMMDD", Locale.US)
                    params[VersatileResponseParams.OLD_VALUE] =
                        formatter.parse(adjustmentTypeParams[0])!!.time
                    params[VersatileResponseParams.NEW_VALUE] =
                        formatter.parse(adjustmentTypeParams[1])!!.time
                }
                VersatileResponseAdjustmentType.ADJ_PONUM,
                VersatileResponseAdjustmentType.ADJ_LOCATION -> {
                    params[VersatileResponseParams.OLD_VALUE] = adjustmentTypeParams[0]
                    params[VersatileResponseParams.NEW_VALUE] = adjustmentTypeParams[1]
                }
                VersatileResponseAdjustmentType.ADJ_NEW_ITEM -> {
                    val filteredEmptyValues =
                        adjustmentTypeParams.filter { item -> item.trim().isNotEmpty() }
                    if (dexVersion <= 4010) dex4010params(
                        params,
                        filteredEmptyValues
                    ) else dex5010Params(
                        params,
                        filteredEmptyValues
                    )
                }
                VersatileResponseAdjustmentType.ADJ_NEW_ITEM_REJECTED -> {
                    var index = 0
                    val filteredEmptyValues =
                        adjustmentTypeParams.filter { item -> item.trim().isNotEmpty() }
                    if (dexVersion > 4010) {
                        params[VersatileResponseParams.PROD_ID_QUALIFIER] = filteredEmptyValues[0]
                        index++
                    }
                    dexParams(
                        index,
                        params,
                        filteredEmptyValues
                    )
                }
            }
            return params
        }

        private fun dexParams(
            index: Int,
            params: MutableMap<String, Any>,
            filteredEmptyValues: List<String>
        ) {
            params[VersatileResponseParams.PROD_ID] = filteredEmptyValues[index]
            params[VersatileResponseParams.QTY] = filteredEmptyValues[index + 1]
            params[VersatileResponseParams.UNIT_OF_MEASURE] = filteredEmptyValues[index + 2]
            params[VersatileResponseParams.ITEM_LIST_COST] = filteredEmptyValues[index + 3]
        }

        private fun dex4010params(
            params: MutableMap<String, Any>,
            filteredEmptyValues: List<String>
        ) {
            params[VersatileResponseParams.ITEM_INDEX] = filteredEmptyValues[0].toInt()
            params[VersatileResponseParams.UPC] = filteredEmptyValues[1]
            params[VersatileResponseParams.QTY] = filteredEmptyValues[2]
            params[VersatileResponseParams.PACKTYPE] = filteredEmptyValues[3]
            params[VersatileResponseParams.PRICE] =
                ensureValue(
                    4,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.PACK] =
                ensureValue(
                    5,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.INNER_PACK] =
                ensureValue(
                    6,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.CASE_UPC] =
                ensureValue(
                    7,
                    filteredEmptyValues
                )
        }

        private fun dex5010Params(
            params: MutableMap<String, Any>,
            filteredEmptyValues: List<String>
        ) {
            params[VersatileResponseParams.ITEM_INDEX] = filteredEmptyValues[0].toInt()
            params[VersatileResponseParams.PROD_QUALIFIER] = filteredEmptyValues[1]
            params[VersatileResponseParams.PROD_ID] = filteredEmptyValues[2]
            params[VersatileResponseParams.QTY] = filteredEmptyValues[3]
            params[VersatileResponseParams.PACKTYPE] =
                ensureValue(
                    4,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.PRICE] =
                ensureValue(
                    5,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.PACK] =
                ensureValue(
                    6,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.INNER_PACK] =
                ensureValue(
                    7,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.CASE_QUALIFIER] =
                ensureValue(
                    8,
                    filteredEmptyValues
                )
            params[VersatileResponseParams.CASE_ID] =
                ensureValue(
                    9,
                    filteredEmptyValues
                )
        }

        private fun ensureValue(index: Int, list: List<String>) =
            if (list.size >= index + 1) list[index] else ""

    }
}

