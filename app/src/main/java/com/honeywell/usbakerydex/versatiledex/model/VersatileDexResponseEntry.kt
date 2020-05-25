package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import java.text.SimpleDateFormat

data class VersatileDexResponseEntry(
    val timestamp: Long,
    val ucsType: Int,
    val code: VersatileResponseCode,
    val invoice: Int,
    val adjustmentType: VersatileResponseAdjustmentType,
    val params: Map<String, Any>
) {

    override fun toString(): String {
        val params = toVersatileString(params)
        val formatter = SimpleDateFormat("YYMMDD:hhmmss")
        val timestamp = formatter.format(timestamp)
        return "$timestamp $ucsType:$code $invoice $adjustmentType $params".replace(
            "\\s+".toRegex(),
            " "
        ) + NEW_LINE
    }

    private fun getValueOrDefault(key: String, map: Map<String, Any>, default: String): Any {
        if (map.containsKey(key))
            if (key == VersatileResponseParams.INVOICE_STATUS && !map[key].toString()
                    .isNullOrBlank()
            )
                return (map[key] as VersatileResponseInvoiceStatus).value
        return map[key] ?: default
        return default

    }

    private fun toVersatileString(map: Map<String, Any>): String {
        return "${getValueOrDefault(VersatileResponseParams.ITEM_INDEX, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.OLD_VALUE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.NEW_VALUE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.RATE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.ADJ_CODE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.REASON, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(
                    VersatileResponseParams.PROD_QUALIFIER,
                    map,
                    ""
                )}$EMPTY_SPACE" +
                "${getValueOrDefault(
                    VersatileResponseParams.PROD_ID_QUALIFIER,
                    map,
                    ""
                )}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.PROD_ID, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.UPC, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.QTY, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.PACKTYPE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.PRICE, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.PACK, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.INNER_PACK, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(
                    VersatileResponseParams.CASE_QUALIFIER,
                    map,
                    ""
                )}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.CASE_ID, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.CASE_UPC, map, "")}$EMPTY_SPACE" +
                "${getValueOrDefault(
                    VersatileResponseParams.UNIT_OF_MEASURE,
                    map,
                    ""
                )}$EMPTY_SPACE" +
                "${getValueOrDefault(
                    VersatileResponseParams.ITEM_LIST_COST,
                    map,
                    ""
                )}$EMPTY_SPACE" +
                "${getValueOrDefault(VersatileResponseParams.INVOICE_STATUS, map, "")}$EMPTY_SPACE"
    }
}


